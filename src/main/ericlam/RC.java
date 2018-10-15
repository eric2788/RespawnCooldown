package main.ericlam;

import builder.ericlam.Respawngui;
import file.ericlam.Collection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
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
    public void onDisable() {
        Collection col = Collection.getInstance();
        for (OfflinePlayer p : Bukkit.getOfflinePlayers()){
            UUID uuid = p.getUniqueId();
            if (p.isOnline() && col.getCountdown().containsKey(p.getPlayer())) {
                p.getPlayer().setGameMode(col.getGm().get(p.getPlayer()));
                p.getPlayer().teleport(col.getLoc().get(p.getPlayer()));
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
