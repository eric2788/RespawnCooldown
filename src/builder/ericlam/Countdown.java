package builder.ericlam;

import file.ericlam.Collection;
import file.ericlam.ConfigManager;
import main.ericlam.RC;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import java.rmi.server.UID;
import java.util.HashMap;
import java.util.UUID;

public class Countdown {
    private Collection col = Collection.getInstance();
    private HashMap<Player,Integer> count = col.getCountdown();
    private HashMap<UUID, GameMode> gm = col.getGm();
    private HashMap<UUID, Integer> timer = col.getTimer();
    private ConfigManager cf = ConfigManager.getInstance();
    private Inventory gui = Respawngui.getInstance().getGui();
    private FileConfiguration config = cf.getConfig();
    private final int origitime = config.getInt("respawn-delay");
    private Plugin plugin = RC.plugin;
    private static Countdown countdown;

    public static Countdown getInstance() {
        if (countdown == null) countdown = new Countdown();
        return countdown;
    }
    private Countdown(){}
    public void startCountdown(Player player){
        if (count.containsKey(player)) {
            //player.sendMessage("DEBUG: count contain player, returned.");
            return;
        }
        gm.put(player.getUniqueId(),player.getGameMode());
        ItemStack replace = player.getInventory().getItem(22);
        if (replace == null) replace = new ItemStack(Material.AIR);
        player.setGameMode(GameMode.SPECTATOR);
        player.sendTitle(cf.msgYamlTranslate("dead-title"),cf.msgYamlTranslate("dead-subtitle"),10,(int)(origitime - origitime/1.2)*20,10);
        if(!timer.containsKey(player.getUniqueId())) {
            //player.sendMessage("DEBUG: no contain key on timer");
            timer.put(player.getUniqueId(), origitime);
        }
        //else player.sendMessage("DEBUG: have contain key on timer");
        int taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, ()->{
            int time = timer.get(player.getUniqueId());
            //player.sendMessage("DEBUG: time is now"+time);
            //player.sendMessage("DEBUG: timer is now "+timer.get(player.getUniqueId()));
            if (time == Math.round(origitime*0.98)){
                player.openInventory(gui);
            }
            if (time <= 0) {
                stopCountDown(player);
                gm.remove(player.getUniqueId());
                timer.remove(player.getUniqueId());
                col.getLoc().remove(player.getUniqueId());
                if(player.getInventory().getName().equals(gui.getName())) player.closeInventory();
                player.sendMessage(cf.getPrefix()+cf.msgYamlTranslate("respawned"));
                return;
            }
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(cf.msgYamlTranslate("action-bar").replace("<time>",time+"")));
            time -= 1;
            timer.put(player.getUniqueId(),time);
        },0L,20L);
        count.put(player,taskID);
        //player.sendMessage("DEBUG: added countdown");
        Respawngui.getInstance().givePlayerItem(player);
    }

    public void stopCountDown(Player player){
        if (!count.containsKey(player)) {
            //player.sendMessage("DEBUG: count not have player, returned");
            return;
        }
        Location location = col.getLoc().get(player.getUniqueId());
        Respawngui.getInstance().removePlayerItem(player);
        player.setGameMode(gm.get(player.getUniqueId()));
        player.teleport(location);
        Bukkit.getScheduler().cancelTask(count.get(player));
        count.remove(player);
        //player.sendMessage("DEBUG: removed countdown/gm/loc");
    }
}
