package com.articreep.pocketknife.features;

import com.articreep.pocketknife.Pocketknife;
import com.articreep.pocketknife.PocketknifeFeature;
import com.articreep.pocketknife.PocketknifeSubcommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class BlockList extends PocketknifeSubcommand implements PocketknifeFeature, Listener {

    private final static List<Material> allBlocks = new ArrayList<>();
    private final static HashMap<Player, BlockListData> sessions = new HashMap<>();

    static {
        for (Material material : Material.values()) {
            if (material.isBlock()) allBlocks.add(material);
        }
    }

    static class BlockListData {
        List<Material> finalBlockList;
        List<Material> initialBlockList;
        boolean start;

        public BlockListData() {
            finalBlockList = new ArrayList<>();
            initialBlockList = new ArrayList<>(allBlocks);
            start = true;
        }
    }

    @Override
    public String getDescription() {
        return "For creating a list of every holdable block item in Minecraft for a challenge";
    }

    @Override
    public boolean runCommand(CommandSender sender, Command command, String label, String[] args) {

        Player player = (Player) sender;
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("confirm")) {
                if (sessions.containsKey(player)) {
                    cycle(player, sessions.get(player));
                } else {
                    player.sendMessage(ChatColor.RED + "you're not in a session");
                }
            } else if (args[0].equalsIgnoreCase("fulllist")) {
                StringBuilder message = new StringBuilder();
                for (Material block : allBlocks) {
                    message.append(block.toString()).append(", ");
                }
                player.sendMessage(message.toString());
                player.sendMessage(ChatColor.GREEN + "I count " + allBlocks.size() + " items in this list.");

            } else {
                sendDescriptionMessage(sender);
                sendSyntaxMessage(sender);
            }
            return true;
        }
        player.sendMessage(ChatColor.RED + "We're building a list of blocks! Drop items from your inventory to exclude them.");
        player.sendMessage(ChatColor.GREEN + "When you're done, run /pocketknife BlockList confirm! Then the next set will come by.");

        Bukkit.getScheduler().runTaskLater(Pocketknife.getInstance(), () -> {
            sessions.put(player, new BlockListData());
            cycle(player, sessions.get(player));
        }, 200);

        return true;
    }

    private void cycle(Player player, BlockListData data) {
        Inventory inventory = player.getInventory();

        if (!data.start) {
            // after player input
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null && item.getType() != Material.AIR) {
                    data.finalBlockList.add(item.getType());
                }
            }
        } else data.start = false;

        inventory.clear();


        // if we've cycled through everything now
        if (data.initialBlockList.isEmpty()) {
            StringBuilder message = new StringBuilder();
            for (Material block : data.finalBlockList) {
                message.append(block.toString()).append(", ");
            }
            player.sendMessage(ChatColor.GREEN + "INCLUDED:");
            player.sendMessage(ChatColor.GREEN + message.toString());

            ArrayList<Material> excludeList = diff(data.finalBlockList);
            StringBuilder excludeMessage = new StringBuilder();
            for (Material block : excludeList) {
                excludeMessage.append(block.toString()).append(", ");
            }
            player.sendMessage(ChatColor.RED + "EXCLUDED:");
            player.sendMessage(ChatColor.RED + excludeMessage.toString());

            player.sendMessage(ChatColor.GREEN + "I count " + data.finalBlockList.size() + " items in this list.");
            player.sendMessage(ChatColor.RED + "I count " + excludeList.size() + " items in the exclude list.");
            player.sendMessage(ChatColor.DARK_GRAY + "There SHOULD be " + allBlocks.size() + " blocks in total.");
            sessions.remove(player);
            player.sendMessage("All done!");
            return;
        }

        // beginning of cycle
        player.sendMessage(ChatColor.YELLOW + "Here's the next set!");
        Iterator<Material> it = data.initialBlockList.listIterator();
        while (it.hasNext()) {
            if (inventory.firstEmpty() == -1) break;
            inventory.addItem(new ItemStack(it.next()));
            it.remove();
        }
        // wait for player input
    }

    private static ArrayList<Material> diff(List<Material> customList) {
        ArrayList<Material> allBlocksClone = new ArrayList<>(allBlocks);
        allBlocksClone.removeAll(customList);
        return allBlocksClone;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            ArrayList<String> strings = new ArrayList<>();
            strings.add("confirm");
            strings.add("fulllist");
            StringUtil.copyPartialMatches(args[0], strings, completions);
        }
        return completions;
    }

    @EventHandler
    public void onDC(PlayerQuitEvent event) {
        sessions.remove(event.getPlayer());
    }

    @Override
    public String getSyntax() {
        return "Usage: /pocketknife BlockList";
    }
}
