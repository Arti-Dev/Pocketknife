package com.articreep.pocketknife;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SpleefMinionSupplements extends PocketknifeSubcommand implements PocketknifeFeature, Listener {
    @Override
    public String getDescription() {
        return "Supplements for my Spleef Minion idea";
    }

    @Override
    boolean runCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            player.getInventory().addItem(createSpleefball(1), createSpleefBlock(1), createTNTSpleefBlock(1),
                    createSpleefGun(1), createExploBow(1));
            player.sendMessage("Added things to your inventory");
        }
        return true;
    }

    @Override
    List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    @Override
    String getSyntax() {
        return null;
    }

    private final NamespacedKey spleefKey = new NamespacedKey(Pocketknife.getInstance(), "spleefkey");
    private final NamespacedKey exploKey = new NamespacedKey(Pocketknife.getInstance(), "explokey");
    @EventHandler
    public void onSnowballHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Snowball snowball) {
            if (snowball.getPersistentDataContainer().has(spleefKey, PersistentDataType.BOOLEAN)) {
                Block b = event.getHitBlock();
                if (b != null)
                    // b.breakNaturally();
                    // todo make materials go into inventory
                    b.setType(Material.AIR);
            }
        } else if (event.getEntity() instanceof Arrow arrow) {
            if (arrow.getPersistentDataContainer().has(exploKey, PersistentDataType.BOOLEAN)) {
                World w = arrow.getWorld();
                w.spawnEntity(arrow.getLocation(), EntityType.PRIMED_TNT);
                arrow.remove();
            }
        }
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (Objects.equals(Utils.getItemID(item), Utils.getItemID(createSpleefGun(1)))) {
                player.playSound(player.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1, 1);
                Snowball snowball = player.launchProjectile(Snowball.class);
                snowball.getPersistentDataContainer().set(spleefKey, PersistentDataType.BOOLEAN, true);
            }
        } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (Objects.equals(Utils.getItemID(item), Utils.getItemID(createExploBow(1)))) {
                new BukkitRunnable() {
                    int i = 0;
                    @Override
                    public void run() {
                        if (i >= 2) this.cancel();
                        ArrayList<Arrow> arrows = new ArrayList<>();
                        Vector v = player.getLocation().getDirection();
                        v.multiply(4.5);
                        arrows.add(player.launchProjectile(Arrow.class, v));
                        arrows.add(player.launchProjectile(Arrow.class, Utils.rotateVectorAroundY(v, 15)));
                        arrows.add(player.launchProjectile(Arrow.class, Utils.rotateVectorAroundY(v, -15)));
                        for (Arrow arrow : arrows) {
                            arrow.getPersistentDataContainer().set(exploKey, PersistentDataType.BOOLEAN, true);
                        }
                        i++;
                    }
                }.runTaskTimer(Pocketknife.getInstance(), 0, 4);
            }
        }
    }

    public static ItemStack createSpleefball(int amount) {
        ItemStack spleefball = new ItemStack(Material.SNOWBALL, amount);
        ItemMeta meta = spleefball.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + "Spleefball");
        Utils.setItemID(meta, "SPLEEFBALL");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Dropped from the floors of the Spleef Minion's arena.",
                "",
                ChatColor.YELLOW + "Right-click to view recipes!",
                "",
                ChatColor.WHITE + "" + ChatColor.BOLD + "COMMON"));
        spleefball.setItemMeta(meta);
        return spleefball;
    }

    public static ItemStack createSpleefBlock(int amount) {
        ItemStack spleefblock = new ItemStack(Material.SNOW_BLOCK, amount);
        ItemMeta meta = spleefblock.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Spleef Block");
        Utils.setItemID(meta, "SPLEEF_BLOCK");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Perfect for making a spleef arena!",
                ChatColor.GRAY + "Destructible by both island members AND",
                ChatColor.GRAY + "visitors when placed on your island.",
                "",
                ChatColor.DARK_GRAY + "Use /social spleef to regenerate.",
                ChatColor.DARK_GRAY + "Right click on block to remove permanently.",
                ChatColor.DARK_PURPLE + "Block Zapper " + ChatColor.DARK_GRAY + "compatible!",
                "",
                ChatColor.YELLOW + "Right-click to view recipes!",
                "",
                ChatColor.GREEN + "" + ChatColor.BOLD + "UNCOMMON"));
        spleefblock.setItemMeta(meta);
        return spleefblock;
    }

    public static ItemStack createTNTSpleefBlock(int amount) {
        ItemStack spleefblock = new ItemStack(Material.TNT, amount);
        ItemMeta meta = spleefblock.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "TNT Spleef Block");
        Utils.setItemID(meta, "SPLEEF_BLOCK");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Perfect for making a bow spleef arena!",
                ChatColor.GRAY + "Destructible by both island members AND",
                ChatColor.GRAY + "visitors using a bow when placed on your island.",
                "",
                ChatColor.DARK_GRAY + "Use /social spleef to regenerate.",
                ChatColor.DARK_GRAY + "Right click on block with empty hand to remove permanently.",
                ChatColor.DARK_PURPLE + "Block Zapper " + ChatColor.DARK_GRAY + "compatible!",
                "",
                ChatColor.YELLOW + "Right-click to view recipes!",
                "",
                ChatColor.GREEN + "" + ChatColor.BOLD + "UNCOMMON"));
        spleefblock.setItemMeta(meta);
        return spleefblock;
    }

    public static ItemStack createSpleefGun(int amount) {
        ItemStack spleefgun = new ItemStack(Material.DIAMOND_HORSE_ARMOR, amount);
        ItemMeta meta = spleefgun.getItemMeta();
        meta.setDisplayName(ChatColor.BLUE + "Ender Spleef Gun");
        Utils.setItemID(meta, "ENDER_SPLEEF_GUN");
        meta.setLore(Arrays.asList(ChatColor.GOLD + "Ability: Spleef Fire " + ChatColor.YELLOW + ChatColor.BOLD + "RIGHT CLICK",
                ChatColor.GRAY + "Consumes and launches " + ChatColor.YELLOW + "Spleefballs" + ChatColor.GRAY + ".",
                ChatColor.YELLOW + "Spleefballs " + ChatColor.GRAY + "destroy any block it hits and transfers ",
                ChatColor.GRAY + "it to your inventory.",
                "",
                ChatColor.GOLD + "Ability: Ender Storage " + ChatColor.YELLOW + ChatColor.BOLD + "LEFT CLICK",
                ChatColor.GRAY + "Opens this gun's storage. Place" + ChatColor.YELLOW + " Spleefballs " + ChatColor.GRAY + "inside.",
                "",
                ChatColor.BLUE + "" + ChatColor.BOLD + "RARE"));
        spleefgun.setItemMeta(meta);
        return spleefgun;
    }

    public static ItemStack createExploBow(int amount) {
        ItemStack exploBow = new ItemStack(Material.BOW, amount);
        ItemMeta meta = exploBow.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_PURPLE + "Explosive Bow");
        Utils.setItemID(meta, "EXPLOSIVE_BOW");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "no time for lore :(",
                "",
                ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "EPIC"));
        exploBow.setItemMeta(meta);
        return exploBow;
    }
}
