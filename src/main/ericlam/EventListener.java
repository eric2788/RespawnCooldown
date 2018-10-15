package main.ericlam;

import builder.ericlam.Countdown;
import builder.ericlam.Respawngui;
import file.ericlam.Collection;
import file.ericlam.ConfigManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EventListener implements Listener {
    private Collection col = Collection.getInstance();
    private Countdown count = new Countdown();
    private Economy eco;
    private Plugin plugin;

    public EventListener(){
        plugin = RC.plugin;
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        eco = rsp.getProvider();
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e){
        Player player = e.getPlayer();
        Location loc = e.getRespawnLocation();
        col.getLoc().put(player,loc);
        if (col.getCountdown().containsKey(player)) return;
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
            count.startCountdown(player);
        },40L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        if (!col.getRespawning().contains(player.getUniqueId())) return;
        col.getLoc().put(player,player.getWorld().getSpawnLocation());
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                count.startCountdown(player);
                col.getRespawning().remove(player.getUniqueId());
        },20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        Player p = e.getPlayer();
        if (!col.getCountdown().containsKey(p)) return;
        col.getRespawning().add(p.getUniqueId());
        count.stopCountDown(p);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e){
        Player player = (Player) e.getPlayer();
        if (e.getInventory().getName().equals(Respawngui.getInstance().getGui().getName()) && col.getCountdown().containsKey(player)){
            Respawngui.getInstance().givePlayerItem(player);
        }
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent e){
        Player player = (Player) e.getPlayer();
        if (e.getInventory().getName().equals(Respawngui.getInstance().getGui().getName()) && col.getCountdown().containsKey(player)){
            Respawngui.getInstance().removePlayerItem(player);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        Inventory gui = Respawngui.getInstance().getGui();
        ConfigManager cf = ConfigManager.getInstance();
        double price = cf.getConfig().getInt("price");
        FileConfiguration guiyml = cf.getGui();
        Inventory inventory = e.getInventory();
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        Material skipitem = Material.getMaterial(guiyml.getString("skip-item.material"));
        Material waititem = Material.getMaterial(guiyml.getString("wait-item.material"));
        if (inventory == null) return;
        if (e.getSlotType() == InventoryType.SlotType.OUTSIDE) return;
        if (!col.getCountdown().containsKey(player)) return;
        if (inventory.getName().equals(gui.getName())){
            if (item.getType() == skipitem){
                if (eco.withdrawPlayer(player,price).type == EconomyResponse.ResponseType.SUCCESS) {
                    new Countdown().stopCountDown(player);
                    player.sendMessage(cf.getPrefix()+cf.msgYamlTranslate("spent").replace("<money>",price+""));
                    player.closeInventory();
                } else{
                    player.sendMessage(cf.getPrefix()+cf.msgYamlTranslate("not-enough-money"));
                    player.closeInventory();
                }
            } else if (item.getType() == waititem){
                player.closeInventory();
                player.sendMessage(cf.getPrefix()+cf.msgYamlTranslate("wait"));
            }
            e.setCancelled(true);
        }
        if (item.getItemMeta().getDisplayName().equals(Respawngui.getInstance().getSkipbook().getItemMeta().getDisplayName())){
            player.openInventory(gui);
            e.setCancelled(true);
        } else{ player.sendMessage("not match item");}


    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e){
        Player p = e.getPlayer();
        ConfigManager cf = ConfigManager.getInstance();
        if (!col.getCountdown().containsKey(p)) return;
        FileConfiguration config = ConfigManager.getInstance().getConfig();
        String[] command = e.getMessage().split(" ");
        String cmd = command[0];
        if (config.getStringList("allow-command").contains(cmd)){
            e.setCancelled(true);
            p.sendMessage(cf.getPrefix()+cf.msgYamlTranslate("blocked-command"));
        }
    }


}
