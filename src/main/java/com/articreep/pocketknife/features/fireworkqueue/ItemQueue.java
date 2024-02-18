package com.articreep.pocketknife.features.fireworkqueue;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class ItemQueue {
    private final int visibleCount = 7;
    private final ArrayList<ItemStack> visibleQueue = new ArrayList<>();
    private final LinkedList<ItemStack> extendedQueue = new LinkedList<>();
    private ItemStack activeItem;
    private ItemStack holdItem;
    private ItemStack lastResortItem;
    private boolean lastResortActive;

    public ItemQueue(ItemStack... items) {
        lastResortItem = createLastResort();
        activeItem = lastResortItem;
        lastResortActive = true;

        for (ItemStack item : items) {
            add(item);
        }
    }

    public void add(ItemStack item) {
        if (activeItem == null || lastResortActive) {
            activeItem = item;
            lastResortActive = false;
        } else if (visibleQueue.size() < visibleCount) {
            visibleQueue.add(item);
        } else {
            extendedQueue.add(item);
        }
    }

    public void pop() {
        // if there is nothing left in queue
        if (visibleQueue.isEmpty()) {
            // Use hold item if available
            if (holdItem != null) {
                activeItem = holdItem;
                holdItem = null;
            } else {
                // Last resort enabled
                activeItem = lastResortItem;
                lastResortActive = true;
            }
        }
        // if there is something in queue
        else activeItem = visibleQueue.remove(0);

        // add new element to visible queue
        if (visibleQueue.size() < visibleCount && !extendedQueue.isEmpty()) {
            visibleQueue.add(extendedQueue.pop());
        }
    }

    public void hold() {
        if (activeItem == null || lastResortActive) return;
        ItemStack toHold = activeItem;
        activeItem = holdItem;
        holdItem = toHold;
        if (activeItem == null) pop();
    }

    public ArrayList<ItemStack> getVisibleQueue() {
        return new ArrayList<>(visibleQueue);
    }

    public ItemStack getActiveItem() {
        return activeItem;
    }

    public ItemStack getHoldItem() {
        return holdItem;
    }

    public ItemStack getLastResortItem() {
        return lastResortItem;
    }

    public void setLastResortItem(ItemStack item) {
        lastResortItem = item;
    }

    public static ItemStack createLastResort() {
        ItemStack item = new ItemStack(Material.STONE_AXE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Last Resort Axe");
        meta.setLore(Arrays.asList("Your final item!", "If this breaks, it's game over!"));
        item.setItemMeta(meta);
        return item;
    }

    public boolean isLastResortActive() {
        return lastResortActive;
    }
}
