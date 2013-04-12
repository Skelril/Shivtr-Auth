package us.arrowcraft.ShivtrAuth;

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
                authenticationCore.updateWhiteList(authenticationCore.getResultArrayFrom("characters.json"));
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
