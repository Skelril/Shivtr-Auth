/*
 * Copyright (c) 2015 Wyatt Childers.
 *
 * This file is part of Shivtr Auth.
 *
 * Shivtr Auth is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Shivtr Auth is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Shivtr Auth.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.skelril.ShivtrAuth;

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

        authenticationCore.getCharacter(player.getName()).setAuthToken("");
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
