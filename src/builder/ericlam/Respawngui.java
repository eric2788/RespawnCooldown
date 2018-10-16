package builder.ericlam;

import file.ericlam.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class Respawngui {
    private Inventory gui;
    private ItemStack skipbook;
    private static Respawngui respawngui;
    private ConfigManager cf;
    private FileConfiguration guiyml;

    private Respawngui(){
        cf = ConfigManager.getInstance();
        guiyml = ConfigManager.getInstance().getGui();
    }

    private String translate(String path){
        return cf.guiYamlTranslate(path);
    }

    public static Respawngui getInstance() {
        if (respawngui == null) respawngui = new Respawngui();
        return respawngui;
    }

    public void createGUI(){
        Inventory respawn = Bukkit.createInventory(null, guiyml.getInt("inventory-slot"), translate("inventory-title"));
        ItemStack skipitem = new ItemStack(Material.getMaterial(guiyml.getString("skip-item.material")));
        ItemMeta skipItemMeta = skipitem.getItemMeta();
        skipItemMeta.setDisplayName(translate("skip-item.name"));
        skipItemMeta.setLore(cf.guiloreTranslate("skip-item.lore"));
        skipitem.setItemMeta(skipItemMeta);
        ItemStack waititem = new ItemStack(Material.getMaterial(guiyml.getString("wait-item.material")));
        ItemMeta waitItemMeta = waititem.getItemMeta();
        waitItemMeta.setDisplayName(translate("wait-item.name"));
        waitItemMeta.setLore(cf.guiloreTranslate("wait-item.lore"));
        waititem.setItemMeta(waitItemMeta);
        respawn.setItem(guiyml.getInt("skip-item.slot")-1,skipitem);
        respawn.setItem(guiyml.getInt("wait-item.slot")-1,waititem);
        gui = respawn;
        ItemStack book = new ItemStack(Material.getMaterial(guiyml.getString("death-item.material")));
        ItemMeta bookmeta = book.getItemMeta();
        bookmeta.setDisplayName(translate("death-item.name"));
        bookmeta.setLore(cf.guiloreTranslate("death-item.lore"));
        book.setItemMeta(bookmeta);
        skipbook = book;
    }

    public void givePlayerItem(Player player){
        //if (player.getInventory().contains(skipbook)) return;
        player.getInventory().setItem(22,skipbook);
    }

    public void removePlayerItem(Player player){
        //if (!player.getInventory().contains(skipbook)) return;
        player.getInventory().remove(skipbook);
    }

    public ItemStack getSkipbook() {
        return skipbook;
    }

    public Inventory getGui() {
        return gui;
    }
}
