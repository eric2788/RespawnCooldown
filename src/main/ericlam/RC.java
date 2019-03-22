package main.ericlam;

import builder.ericlam.Countdown;
import builder.ericlam.Respawngui;
import file.ericlam.Collection;
import file.ericlam.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class RC extends JavaPlugin {
    public static Plugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        addNewfile("config.yml");
        addNewfile("messages.yml");
        addNewfile("respawngui.yml");
        ConsoleCommandSender console = getServer().getConsoleSender();
        console.sendMessage(ChatColor.GREEN+"重生冷卻插件已被啟用。");
        console.sendMessage(ChatColor.GOLD+"作者: EricLam / Hydranapse_");
        console.sendMessage(ChatColor.GOLD+"網站: www.hypernite.com");
        Respawngui.getInstance().createGUI();
        if (getServer().getPluginManager().getPlugin("Vault") != null){
            console.sendMessage(ChatColor.AQUA+"找到 Vault 插件! 正在掛接...");
        }
        getServer().getPluginManager().registerEvents(new EventListener(),this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin = this;
        if(command.getName().equalsIgnoreCase("respawncooldown")) {
            sender.sendMessage("§6RespawnCooldown §7- §c重生冷卻插件 §7- §c資訊§8:");
            sender.sendMessage("§4=================================================");
            sender.sendMessage("§d製作: §aEricLam §7(§eID: §bHydranapse_§7)");
            sender.sendMessage("§d作者連結:");
            sender.sendMessage("    §8Gtihub: §ahttps://github.com/eric2788");
            sender.sendMessage("    §9Facebook: §ahttps://facebook.com/caxcraftblank");
            sender.sendMessage("    §cYoutube: §ahttps://youtube.com/caxcraftblank");
            sender.sendMessage("§d插件詳情: §bhttps://github.com/eric2788/RespawnCooldown");
            sender.sendMessage("§4=================================================");
            return true;
        }
        if (command.getName().equalsIgnoreCase("setspawn")){
            if (!(sender instanceof Player)) return false;
            ConfigManager cf = ConfigManager.getInstance();
            if (!sender.hasPermission("rsc.setspawn")){
                sender.sendMessage(cf.getPrefix()+cf.msgYamlTranslate("no-perm"));
                return false;
            }
            if (!cf.getConfig().getBoolean("use-custom-spawn-location")){
                sender.sendMessage(cf.getPrefix()+"§c你沒有在 config.yml 開啟自定義重生位置。");
                return false;
            }
            Player player = (Player) sender;
            Location ploc = player.getLocation();
            cf.getConfig().set("spawn-location",ploc);
            try {
                cf.getConfig().save(new File(this.getDataFolder(),"config.yml"));
            } catch (IOException e) {
                e.printStackTrace();
                player.sendMessage(cf.getPrefix()+"§c重生點保存失敗。");
            }
            player.sendMessage(cf.getPrefix()+"§a重生點保存成功!");
            return true;
        }
        return false;
    }

    @Override
    public void onDisable() {
        Collection col = Collection.getInstance();
        for (OfflinePlayer p : Bukkit.getOfflinePlayers()){
            UUID uuid = p.getUniqueId();
            if (p.isOnline() && col.getCountdown().containsKey(p.getPlayer())) {
                Countdown.getInstance().stopCountDown(p.getPlayer());
            }
        }
        getLogger().info("重生冷卻插件已被關閉。");
    }

    private void addNewfile(String yaml){
        File file = new File(plugin.getDataFolder(),yaml);
        if(!file.exists()) plugin.saveResource(yaml,true);
        YamlConfiguration.loadConfiguration(file);
    }
}
