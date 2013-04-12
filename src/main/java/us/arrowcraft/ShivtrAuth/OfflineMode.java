package us.arrowcraft.ShivtrAuth;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Turtle9598
 */
public class OfflineMode implements Listener {

    private Plugin plugin;
    private AuthenticationCore authenticationCore;

    private List<String> lockedPlayers = new ArrayList<String>();

    public OfflineMode(ShivtrAuth plugin) {

        this.plugin = plugin;
        this.authenticationCore = plugin.getAuthenticationCore();
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {

        Player player = event.getPlayer();

        if (!plugin.getServer().getOnlineMode()) {

            lockPlayer(player);
            messagePlayer(player);

        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        authenticationCore.getCharacter(player).setAuthToken("");
    }

    private void messagePlayer(Player player) {

        player.sendMessage(ChatColor.YELLOW + "The server is in offline mode.");
        player.sendMessage(ChatColor.YELLOW + "Please login with your " +
                "Shivtr account login details by using /login <email> <password>.");
    }

    public boolean isLocked(Player player) {

        return isLocked(player.getName());
    }

    public boolean isLocked(String playerName) {

        return lockedPlayers.contains(playerName);
    }

    public void lockPlayer(Player player) {

        lockPlayer(player.getName());
    }

    public void lockPlayer(String playerName) {

        lockedPlayers.add(playerName);
    }

    public void unlockPlayer(Player player) {

        unlockPlayer(player.getName());
    }

    public void unlockPlayer(String playerName) {

        lockedPlayers.remove(playerName);
    }
}
