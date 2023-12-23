package de.zillolp.groupsystem.infosigns;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class InfoSign {
    private final Player player;
    private final Location location;
    private String[] signLines;
    private boolean isUpdated;

    public InfoSign(Player player, Location location, String[] signLines) {
        this.player = player;
        this.location = location;
        this.signLines = signLines;
        isUpdated = false;
        // When creating the sign, the shield should be updated immediately as well.
        update(signLines, true);
    }

    // This method is for updating all information signs.
    public void update(String[] lines, boolean forceUpdate) {
        World world = location.getWorld();
        if (world == null || (!(world.getName().equals(player.getWorld().getName())))) {
            return;
        }
        /* When the player is too far away from the sign, the chunks are unloaded, causing the sent packet from the sign to be 'lost.'
        It must be updated as soon as the player comes back into proximity.
        In this context, 41 blocks is a value that fits well based on experience. */
        if (player.getLocation().distance(location) >= 41) {
            if (isUpdated) {
                isUpdated = false;
            }
            return;
        }
        // The sign only needs to be updated if it is forced, has not been updated yet, or if the lines on the sign do not match the new ones.
        if (isUpdated && (!(forceUpdate)) && Arrays.equals(lines, signLines)) {
            return;
        }
        isUpdated = true;
        Block block = location.getBlock();
        if (!(block.getType().name().contains("SIGN"))) {
            return;
        }
        signLines = lines;
        player.sendSignChange(location, lines);
    }

    public Location getLocation() {
        return location;
    }
}
