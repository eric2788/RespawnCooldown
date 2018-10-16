package builder.ericlam;

import file.ericlam.Collection;
import file.ericlam.ConfigManager;
import main.ericlam.RC;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;

public class Countdown {
    private Collection col = Collection.getInstance();
    private HashMap<Player,Integer> count = col.getCountdown();
    private HashMap<Player, Integer> timer = col.getTimer();
    private HashMap<Player, GameMode> gm = col.getGm();
    private ConfigManager cf = ConfigManager.getInstance();
    private Inventory gui = Respawngui.getInstance().getGui();
    private FileConfiguration config = cf.getConfig();
    private final int origitime = config.getInt("respawn-delay");
    private Plugin plugin = RC.plugin;
    public void startCountdown(Player player){
        if (count.containsKey(player)) {
            player.sendMessage("count contain player, returned.");
            return;
        }
        gm.put(player,player.getGameMode());
        player.setGameMode(GameMode.SPECTATOR);
        player.sendTitle(cf.msgYamlTranslate("dead-title"),cf.msgYamlTranslate("dead-subtitle"),10,(int)(origitime - origitime/1.2)*20,10);
        if(!timer.containsKey(player)) timer.put(player, origitime);
        int time = timer.get(player);
        int taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, ()->{
            if (time == Math.round(origitime*0.98)){
                player.openInventory(gui);
            }
            if (time <= 0) {
                stopCountDown(player);
                gm.remove(player);
                timer.remove(player);
                col.getLoc().remove(player);
                if(player.getInventory().getName().equals(gui.getName)) player.closeInventory();
                player.sendMessage(cf.getPrefix()+cf.msgYamlTranslate("respawned"));
            }
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(cf.msgYamlTranslate("action-bar").replace("<time>",time+"")));
            time -= 1;
            timer.put(player,time);
        },0L,20L);
        count.put(player,taskID);
        player.sendMessage("added countdown");
        Respawngui.getInstance().givePlayerItem(player);
    }

    public void stopCountDown(Player player){
        if (!count.containsKey(player)) {
            player.sendMessage("count not have player, returned");
            return;
        }
        Bukkit.getScheduler().cancelTask(count.get(player));
        Location location = col.getLoc().get(player);
        Respawngui.getInstance().removePlayerItem(player);
        player.setGameMode(gm.get(player));
        player.teleport(location);
        count.remove(player);
        player.sendMessage("removed countdown/gm/loc");
    }
}
