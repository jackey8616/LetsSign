package org.mocraft;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;

public class InventorySign implements Listener {

    private LetsSign instance;
    private String name = "Sign it!";
    private String[] iconsName = new String[54];
    private ItemStack[] icons = new ItemStack[54];
    private String day = "";
    private ItemStack dayStack, gift;
    private int signdays = 0;

    public InventorySign(LetsSign instance, Boolean[] signedDays) {
        this.instance = instance;
        LocalDate today = ZonedDateTime.now().toLocalDate();
        for(int i = 1; i <= today.getMonth().maxLength(); ++i) {
            LocalDate time = LocalDate.of(today.getYear(), today.getMonth(), i);
            ItemStack stack;
            String iconsName = "";

            if(signedDays[i - 1] == true) {
                stack = new ItemStack(Material.STAINED_GLASS_PANE, i, (short) 5);
                iconsName = "&6本月" + i + "日  &a【已簽到】";
            } else if(time.isBefore(today)) {
                stack = new ItemStack(Material.STAINED_GLASS_PANE, i, (short) 15);
                iconsName = "&6本月" + i + "日  &4【已錯過】";
            } else if(time.isAfter(today)) {
                stack = new ItemStack(Material.STAINED_GLASS_PANE, i, (short) 0);
                iconsName = "本月" + i + "日  &7【尚未到達】";
            } else {
                stack = new ItemStack(Material.STAINED_GLASS_PANE, i, (short) 1);
                iconsName = "&6本月" + i + "日  &a【可簽到】";
            }
            setOption(i - 1, stack, iconsName);
        }

        for(Boolean signCheck : signedDays)
            signdays += signCheck ? 1 : 0;
        this.dayStack = setItemNameAndLore(new ItemStack(Material.WATCH, signdays), "已簽到了" + signdays + "天", null);

        this.gift = getCloestReward();
        if(gift != null)
            this.gift = setItemNameAndLore(gift.clone(), "今日獎勵: " + (gift.getItemMeta().hasDisplayName() ? gift.getItemMeta().getDisplayName() : gift.getType().name().replace("_", " ").toLowerCase()), null);

        Bukkit.getPluginManager().registerEvents(this, instance);
    }

    public InventorySign setOption(int position, ItemStack icon, String name, String... info) {
        iconsName[position] = name;
        icons[position] = setItemNameAndLore(icon, name, info);
        return this;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(player, 54, name);
        for (int i = 0, j = 0; i < icons.length && j < icons.length; j++) {
            if(j <= 45 && (j % 9 == 0 || (j + 1) % 9 == 0)) { continue; }
            if (icons[i] != null) {
                inv.setItem(j, icons[i]);
                i++;
            }
        }
        inv.setItem(52, dayStack);
        inv.setItem(53, gift);
        player.openInventory(inv);
    }

    private ItemStack getCloestReward() {
        for(int i = signdays; i < instance.gifts.length; ++i)
            if(instance.gifts[i] != null)
                return instance.gifts[i];
        return instance.gifts[30];
    }

    private ItemStack setItemNameAndLore(ItemStack item, String name, String[] lore) {
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        if(lore != null)
            im.setLore(Arrays.asList(lore));
        item.setItemMeta(im);
        return item;
    }

    @EventHandler(priority= EventPriority.MONITOR)
    void onInventoryClick(InventoryClickEvent event) {
        if(!event.getInventory().getName().equals(this.name)) { return; }
        event.setCancelled(true);
        int clickSlot = event.getSlot();
        ItemStack clickStack = event.getInventory().getItem(clickSlot);
        if(clickStack != null && clickStack.getItemMeta().getDisplayName().contains("可簽到")) {

            ItemStack signStack = new ItemStack(Material.STAINED_GLASS_PANE, clickStack.getAmount(), (short) 5);
            setItemNameAndLore(signStack, "&6本月" + clickStack.getAmount() + "日  &a【已簽到】", null);
            event.getInventory().setItem(clickSlot, signStack);

            signdays += 1;

            ItemStack countStack = new ItemStack(Material.WATCH, signdays);
            setItemNameAndLore(countStack, "已簽到了" + signdays + "天",null);
            event.getInventory().setItem(52, countStack);

            Boolean[] signedDay = instance.playersSign.get(event.getWhoClicked().getUniqueId());
            signedDay[ZonedDateTime.now().toLocalDate().getDayOfMonth() - 1] = true;
            instance.playersSign.remove(event.getWhoClicked().getUniqueId());
            instance.playersSign.put(event.getWhoClicked().getUniqueId(), signedDay);

            event.getWhoClicked().getInventory().addItem(gift);
        }
    }
}
