package org.mocraft;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class LetsSign extends JavaPlugin implements Listener {

    public LetsSign instance;
    public FileConfiguration config = getConfig();

    public Map<UUID, Boolean[]> playersSign = new HashMap<>();
    public ItemStack[] gifts = new ItemStack[31];

    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getServer().getPluginManager().registerEvents(this, this);

        playersSign.clear();
        ConfigurationSection playerSection = config.getConfigurationSection("Players");
        for(String key : playerSection.getKeys(false))
            playersSign.put(UUID.fromString(key), config.getBooleanList("Players." + key).toArray(new Boolean[31]));

        ConfigurationSection giftSection = config.getConfigurationSection("Gifts");
        for(int i = 0; i < gifts.length; ++i)
            gifts[i] = giftSection.getItemStack(String.valueOf(i));

        saveConfig();
    }

    @Override
    public void onDisable() {
        ConfigurationSection  playerSection = new MemoryConfiguration();
        Iterator set = playersSign.entrySet().iterator();
        while(set.hasNext()) {
            Map.Entry player = (Map.Entry) set.next();
            playerSection.set(player.getKey().toString(), player.getValue());
        }
        config.set("Players", playerSection);

        ConfigurationSection  giftSection = new MemoryConfiguration();
        for(int i = 0; i < gifts.length; ++i)
            giftSection.set(String.valueOf(i), gifts[i]);
        config.set("Gifts", giftSection);
        saveConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            if(label.equalsIgnoreCase("ls")) {
                if(args[0].equalsIgnoreCase("sign")) {
                    Player player = (Player) sender;
                    if(!playersSign.containsKey(player.getUniqueId())) {
                        Boolean[] signedDay = new Boolean[]{
                                false, false, false, false, false, false, false, false, false, false,
                                false, false, false, false, false, false, false, false, false, false,
                                false, false, false, false, false, false, false, false, false, false,
                                false
                        };
                        playersSign.put(player.getUniqueId(), signedDay);
                    }
                    InventorySign invSign = new InventorySign(instance, playersSign.get(player.getUniqueId()));
                    invSign.open(player);
                } else if(sender.isOp() && args[0].equalsIgnoreCase("reward-day")) {
                    Player player = (Player) sender;
                    ItemStack inHand = player.getItemInHand();
                    gifts[Integer.valueOf(args[1])] = inHand;
                    return true;
                }
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerJoinWorld(PlayerJoinEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                Player player = event.getPlayer();
                if(!playersSign.containsKey(player.getUniqueId())) {
                    Boolean[] signedDay = new Boolean[]{
                            false, false, false, false, false, false, false, false, false, false,
                            false, false, false, false, false, false, false, false, false, false,
                            false, false, false, false, false, false, false, false, false, false,
                            false
                    };
                    playersSign.put(player.getUniqueId(), signedDay);
                }
                InventorySign invSign = new InventorySign(instance, playersSign.get(player.getUniqueId()));
                invSign.open(player);
            }
        }, 5L);

    }
}
