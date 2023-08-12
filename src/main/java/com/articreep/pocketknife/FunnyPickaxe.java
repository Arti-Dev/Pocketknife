package com.articreep.pocketknife;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import net.craftcitizen.imagemaps.ImageMaps;
import net.craftcitizen.imagemaps.PlacementData;
import net.craftcitizen.imagemaps.clcore.util.Tuple;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class FunnyPickaxe extends PocketknifeSubcommand implements Listener {

    private static int mode = 0;

    /** For mode 0, where blocks are broken by simply clicking twice **/
    private final Set<Location> confirmationsNoCommands = new HashSet<>();

    /** For mode 1, where blocks are broken by confirming in chat **/
    private final Map<UUID, Location> confirmations = new HashMap<>();
    /** For mode 2, where blocks are confirmed in chat, then a giant captcha appears. Limit one captcha at a time. **/
    private final Map<UUID, Location> captcha = new HashMap<>();
    private static boolean isBreaking = false;

    /** These are hard-coded locations **/
    final CuboidRegion region = new CuboidRegion(BlockVector3.at(-14, 82, 13), BlockVector3.at(-14, 95, -8));

    @Override
    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            if (args.length == 0) {
                // todo I want to abstract this in the abstract class
                sendDescriptionMessage(player);
                sendSyntaxMessage(player);
            } else {
                if (args[0].equalsIgnoreCase("confirm") && args.length == 2) {
                    // Player might have clicked a confirm button
                    // Second one must be a UUID
                    UUID uuid;
                    try {
                        uuid = UUID.fromString(args[1]);
                    } catch (IllegalArgumentException e) {
                        player.sendMessage(ChatColor.RED + "Something went wrong. Impressive.");
                        return true;
                    }

                    // Check mode
                    switch (mode) {
                        case 0 -> player.sendMessage("Mode 0 does not require command confirmation");
                        case 1 -> {
                            if (!confirmBreakBlock(player, uuid))
                                player.sendMessage(ChatColor.RED + "This confirmation has expired!");
                        }
                        case 2 -> {
                            // Is this UUID on a captcha?
                            if (!captcha.containsKey(uuid)) {
                                try {
                                    generateCaptcha(uuid, player);
                                } catch (NoSuchMethodException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                breakCaptcha(player, uuid);
                            }
                        }
                    }
                } else if (args[0].equalsIgnoreCase("mode")) {
                    int number;
                    try {
                        number = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "That's not an integer.");
                        return true;
                    }
                    if (number >= 0 && number <= 2) {
                        mode = number;
                        player.sendMessage(ChatColor.GREEN + "Set the mode to " + mode);
                    }
                    else player.sendMessage(ChatColor.RED + "Mode must be between 0 and 2");
                } else {
                    // The first argument might be a number
                    int amount;
                    try {
                        amount = Integer.parseInt(args[0]);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "That's not an integer.");
                        return true;
                    }
                    givePickaxe(amount, player);
                }
            }

        } else {
            sender.sendMessage("You're not a player.");
        }
        return true;
    }

    private void givePickaxe(int amount, Player player) {
        if (amount <= 0) {
            player.sendMessage(ChatColor.YELLOW + "Zero or less?");
            return;
        } else if (amount == 1) {
            player.sendMessage(ChatColor.YELLOW + "Gave you a " + ChatColor.GOLD + "Golden Pickaxe");
        } else {
            player.sendMessage(ChatColor.YELLOW + "Stacking is definitely an intended mechanic.");
        }
        player.getInventory().addItem(goldenPickaxe(amount));
    }

    @EventHandler
    public void onPickaxeBreak(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block != null && block.getType() == Material.OBSIDIAN) {
            // Verify they're holding the golden pickaxe
            ItemStack item = player.getInventory().getItemInMainHand();
            if (Objects.equals(Utils.getItemID(item), "FUNNY_PICKAXE")) {
                event.setCancelled(true);

                // Different things will happen depending on the mode
                switch (mode) {
                    case 0 -> {
                        // Attempt to break the block
                        if (!confirmBreakBlock(player, block)) {
                            confirmationsNoCommands.add(block.getLocation());
                            Bukkit.getScheduler().runTaskLater(Pocketknife.getInstance(),
                                    () -> confirmationsNoCommands.remove(block.getLocation()), 200);
                            player.sendMessage(ChatColor.DARK_PURPLE + String.valueOf(ChatColor.BOLD) + "BLOCK! " +
                                    ChatColor.GRAY + "Are you sure you want to break this block? Click the block again to confirm!");
                        }
                    }
                    case 1, 2 -> {
                        // I'm too worried about dupe UUIDs, aren't I?
                        UUID randomUUID = UUID.randomUUID();
                        while (confirmations.containsKey(randomUUID)) {
                            randomUUID = UUID.randomUUID();
                        }
                        confirmations.put(randomUUID, block.getLocation());
                        UUID finalRandomUUID = randomUUID;
                        Bukkit.getScheduler().runTaskLater(Pocketknife.getInstance(), () -> confirmations.remove(finalRandomUUID), 200);

                        // Send the JSON chat message
                        TextComponent component = new TextComponent(ChatColor.DARK_PURPLE + String.valueOf(ChatColor.BOLD) + "BLOCK! " +
                                ChatColor.GRAY + "Are you sure you want to break this block? ");
                        TextComponent confirmComponent = new TextComponent(ChatColor.GREEN + "" + ChatColor.BOLD + "[YES]");
                        confirmComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pocketknife funnypickaxe confirm " + randomUUID));
                        confirmComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GREEN + "Click to confirm!")));
                        component.addExtra(confirmComponent);
                        player.spigot().sendMessage(component);
                    }
                }
            }
        }
    }
    private void generateCaptcha(UUID uuid, Player player) throws NoSuchMethodException {

        if (Bukkit.getPluginManager().getPlugin("ImageMaps") instanceof ImageMaps imagePlugin) {
            if (captcha.isEmpty()) {

                World world = player.getWorld();
                if (!world.getName().equalsIgnoreCase("Elementals")) {
                    player.sendMessage("Couldn't generate captcha since you are not on the \"Elementals\" world!");
                    return;
                }

                placeWall(region, world);

                // Static coding
                Location topLeft = new Location(world, -14, 95, 13);

                Method method = imagePlugin.getClass().getDeclaredMethod("placeImage", Player.class, Block.class,
                        BlockFace.class, PlacementData.class);
                method.setAccessible(true);

                // The tuple are the dimensions of the picture
                PlacementData data = new PlacementData("taxi.png", false, false, false, new Tuple<>(22, 16));

                Bukkit.getScheduler().runTask(Pocketknife.getInstance(), () -> {
                    try {
                        method.invoke(imagePlugin, player, topLeft.getBlock(), BlockFace.EAST, data);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });

                captcha.put(uuid, confirmations.get(uuid));
                player.sendMessage(ChatColor.YELLOW + "Please solve this captcha by clicking all boxes that make up the taxi.");
                TextComponent confirmComponent = new TextComponent(ChatColor.GREEN + "" + ChatColor.BOLD + "Click here when you're done");
                confirmComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pocketknife funnypickaxe confirm " + uuid));
                confirmComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GREEN + "Click when you're done!!")));
                player.spigot().sendMessage(confirmComponent);
            } else {
                player.sendMessage("Can't break this block right now as someone is already doing a captcha!");
            }
        } else {
            player.sendMessage("Couldn't generate captcha since ImageMaps and/or WorldEdit is not installed on this server!");
        }
    }

    private void placeWall(CuboidRegion region, World world) {
        for (BlockVector3 blockVector3 : region) {
            Location loc = new Location(world, blockVector3.getBlockX(), blockVector3.getBlockY(), blockVector3.getBlockZ());
            loc.getBlock().setType(Material.STONE);
        }
    }

    @EventHandler
    public void onItemFrameRightClick(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof ItemFrame itemFrame) {
            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();
            if (Objects.equals(Utils.getItemID(item), Utils.getItemID(goldenPickaxe(1)))) {
                event.setCancelled(true);
                player.playSound(player, Sound.ITEM_GLOW_INK_SAC_USE, 1, 1);
                boolean glowing = itemFrame.isGlowing();
                itemFrame.setGlowing(!glowing);

            }
        }

    }

    private void breakCaptcha(Player player, UUID uuid) {
        if (isBreaking) {
            player.sendMessage("A captcha is currently being broken down.");
            return;
        }

        World world = player.getWorld();
        if (!world.getName().equalsIgnoreCase("Elementals")) {
            player.sendMessage("Couldn't break captcha since you are not on the \"Elementals\" world!");
            return;
        }
        isBreaking = true;
        new BukkitRunnable() {
            final Iterator<BlockVector3> iterator = region.iterator();
            final List<Block> selectedBlocks = new ArrayList<>();
            @Override
            public void run() {
                if (!iterator.hasNext()) {
                    Bukkit.getScheduler().runTaskLater(Pocketknife.getInstance(), () -> {
                        for (Block block : selectedBlocks) {
                            ItemFrame itemFrame = getItemFrameFromBlock(block);
                            if (itemFrame != null) itemFrame.remove();
                            block.breakNaturally(new ItemStack(Material.DIRT));
                        }
                        breakBlock(captcha.get(uuid).getBlock(), player);
                        isBreaking = false;
                        captcha.remove(uuid);
                    }, 40);
                    this.cancel();
                    return;
                }

                BlockVector3 blockVector3 = iterator.next();
                Location loc = new Location(world, blockVector3.getBlockX(), blockVector3.getBlockY(), blockVector3.getBlockZ());
                Block block = loc.getBlock();
                // Get the item frame attached to this block
                // If the item frame is glowing, add block to a list
                // If not, remove the block
                ItemFrame itemFrame = getItemFrameFromBlock(block);
                if (itemFrame != null) {
                    if (itemFrame.isGlowing()) {
                        selectedBlocks.add(block);
                    } else {
                        itemFrame.remove();
                        block.setType(Material.AIR);
                    }
                }
            }
        }.runTaskTimer(Pocketknife.getInstance(), 20, 1);
    }

    private @Nullable ItemFrame getItemFrameFromBlock(Block block) {
        World world = block.getWorld();

        for (Entity entity : world.getNearbyEntities(block.getLocation(), 2, 2, 2)) {
            if (entity instanceof ItemFrame itemFrame && entity.getLocation().getBlock().getRelative(
                    ((ItemFrame) entity).getAttachedFace()).equals(block)) {
                return itemFrame;
            }
        }
        return null;
    }

    /**
     * Mode 0 block breaking. The block's location must be in confirmationsNoCommands to be broken.
     * @return Whether the block could successfully be broken.
     **/
    private boolean confirmBreakBlock(Player player, Block block) {
        if (confirmationsNoCommands.contains(block.getLocation())) {
            confirmationsNoCommands.remove(block.getLocation());
            return breakBlock(block, player);
        }
        return false;
    }

    /**
     * Mode 1 block breaking.
     * @return Whether the block was successfully broken
     */
    private boolean confirmBreakBlock(Player player, UUID uuid) {
        if (confirmations.containsKey(uuid)) {
            Location loc = confirmations.get(uuid);
            Block block = loc.getBlock();
            // Remove any other confirmations referencing the block, then break the block
            confirmations.keySet().removeIf(uuid1 -> confirmations.get(uuid1).equals(loc));
            return breakBlock(block, player);
        } else {
            return false;
        }
    }

    /** Generic block breaking. **/
    private boolean breakBlock(Block block, Player player) {
        if (block.getType() == Material.OBSIDIAN) {
            block.setType(Material.AIR);
            player.playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 1, 1);
            return true;
        // Block is not obsidian
        } else return false;
    }


    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    @Override
    String getSyntax() {
        return "Usage: /pocketknife FunnyPickaxe <amount>";
    }

    private static ItemStack goldenPickaxe(int quantity) {
        ItemStack item = new ItemStack(Material.GOLDEN_PICKAXE);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.GOLD + "Golden Pickaxe");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Breaks a 5-high pillar of", ChatColor.GRAY + "obsidian when 2-tapping it."));
        Utils.setItemID(meta, "FUNNY_PICKAXE");

        item.setItemMeta(meta);
        item.setAmount(quantity);
        return item;
    }

    @Override
    public String getDescription() {
        return "Meant to be a joke";
    }
}
