package file.ericlam;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class Collection {
    private HashMap<Player, GameMode> gm = new HashMap<>();
    private HashMap<Player, Integer> countdown = new HashMap<>();
    private HashMap<Player, Location> loc = new HashMap<>();
    private HashSet<UUID> respawning = new HashSet<>();
    private static Collection collect;

    public HashMap<Player, Integer> getCountdown() {
        return countdown;
    }

    public static Collection getInstance() {
        if (collect == null) collect = new Collection();
        return collect;
    }

    public HashMap<Player, Location> getLoc() {
        return loc;
    }

    public HashSet<UUID> getRespawning() {
        return respawning;
    }

    public HashMap<Player, GameMode> getGm() {
        return gm;
    }
}
