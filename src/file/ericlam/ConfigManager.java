package file.ericlam;

import main.ericlam.RC;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigManager {
    private FileConfiguration config;
    private FileConfiguration msg;
    private FileConfiguration gui;
    private static ConfigManager cm;

    public static ConfigManager getInstance() {
        if (cm == null) cm = new ConfigManager();
        return cm;
    }

    private ConfigManager(){
        Plugin plugin = RC.plugin;
        config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(),"config.yml"));
        msg = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(),"messages.yml"));
        gui = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(),"respawngui.yml"));
    }

    public String getPrefix(){
        return msgYamlTranslate("prefix");
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public String guiYamlTranslate(String path){
        String msgString = gui.getString(path);
        return ChatColor.translateAlternateColorCodes('&',msgString);
    }

    public String msgYamlTranslate(String path){
        String msgString = msg.getString(path);
        return ChatColor.translateAlternateColorCodes('&',msgString);
    }

    public List<String> guiloreTranslate(String path){
        List<String> list = gui.getStringList(path);
        return list.stream().map( s -> ChatColor.translateAlternateColorCodes('&',s).replace("<money>",config.getInt("price")+"")).collect(Collectors.toList());
    }

    public FileConfiguration getGui() {
        return gui;
    }
}
