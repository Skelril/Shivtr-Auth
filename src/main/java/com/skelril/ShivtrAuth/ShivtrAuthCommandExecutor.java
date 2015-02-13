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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

/**
 * Author: Turtle9598
 */
public class ShivtrAuthCommandExecutor implements CommandExecutor {

    private ShivtrAuth plugin;
    private Logger log;
    private AuthenticationCore authenticationCore;
    private OfflineMode offlineMode;

    public ShivtrAuthCommandExecutor(ShivtrAuth plugin) {

        this.plugin = plugin;
        this.log = plugin.getLogger();
        this.authenticationCore = plugin.getAuthenticationCore();
        this.offlineMode = plugin.getOfflineMode();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        if (cmd.getName().equalsIgnoreCase("authupdate")) {
            if (args.length != 0) {
                sender.sendMessage(ChatColor.RED + "Too many arguments!");
            } else if (sender.hasPermission("shivtrauth.commands.update")) {
                authenticationCore.updateWhiteList(authenticationCore.getFrom("characters.json"));
                if (sender instanceof Player) {
                    sender.sendMessage(ChatColor.YELLOW + "The Characters List(s) is now being updated.");
                }
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            }
        }
        return false;
    }
}
