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
        ConfigManager cf = ConfigManager.getInstance();
        boolean custom = cf.getConfig().getBoolean("use-custom-spawn-location");
        boolean customNotNull = cf.getConfig().get("spawn-location") != null;
        if (custom && customNotNull) e.setRespawnLocation((Location)cf.getConfig().get("spawn-location"));
        if (col.getCountdown().containsKey(player) || player.hasPermission("rsc.skip")) return;
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
            if (custom && customNotNull){
                 col.getLoc().put(player.getUniqueId(),(Location)cf.getConfig().get("spawn-location"));

            }else{
                if (custom) {
                    player.sendMessage(cf.getPrefix()+"§4錯誤 -> §c本插件的自定義重生位置為空，請通知管理員。");
                    player.sendMessage(cf.getPrefix()+"§4錯誤 -> §c已把重生點重新指向至默認位置。");
                }
                col.getLoc().put(player.getUniqueId(),player.getLocation());
            }
            count.startCountdown(player);
        },20L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        if (!col.getRespawning().contains(player.getUniqueId())) return;
        if (!col.getLoc().containsKey(player.getUniqueId())) {
            //player.sendMessage("DEBUG: no contain loc");
            col.getLoc().put(player.getUniqueId(),player.getWorld().getSpawnLocation());
        }
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
        if (e.getInventory().getName().equals(Respawngui.getInstance().getGui().getName())){
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
                    col.getGm().remove(player.getUniqueId());
                    col.getTimer().remove(player.getUniqueId());
                    col.getLoc().remove(player.getUniqueId());
                    Respawngui.getInstance().removePlayerItem(player);
                } else{
                    player.sendMessage(cf.getPrefix()+cf.msgYamlTranslate("not-enough-money"));
                    player.closeInventory();
                    Respawngui.getInstance().givePlayerItem(player);
                }
            } else if (item.getType() == waititem){
                player.closeInventory();
                player.sendMessage(cf.getPrefix()+cf.msgYamlTranslate("wait"));
                Respawngui.getInstance().givePlayerItem(player);
            }
            e.setCancelled(true);
        }
        if (item.getType().equals(Material.AIR)) return;
        if (item.getItemMeta().getDisplayName().equals(Respawngui.getInstance().getSkipbook().getItemMeta().getDisplayName())){
            player.openInventory(gui);
            e.setCancelled(true);
        }


    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e){
        Player p = e.getPlayer();
        ConfigManager cf = ConfigManager.getInstance();
        if (!col.getCountdown().containsKey(p)) return;
        FileConfiguration config = ConfigManager.getInstance().getConfig();
        String[] command = e.getMessage().split(" ");
        String cmd = command[0];
        if (!config.getStringList("allow-command").contains(cmd) && !p.hasPermission("rsc.command.bypass")){
            e.setCancelled(true);
            p.sendMessage(cf.getPrefix()+cf.msgYamlTranslate("blocked-command"));
        }
    }


}
