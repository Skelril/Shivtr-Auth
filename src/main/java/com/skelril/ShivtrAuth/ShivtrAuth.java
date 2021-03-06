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

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

public class ShivtrAuth extends JavaPlugin {

    private ShivtrAuthConfiguration config;
    private ShivtrAuthCommandExecutor executor;
    private AuthenticationCore authenticationCore;
    private OfflineMode offlineMode = null;
    private static final Logger log = Logger.getLogger("Minecraft.ShivtrAuth");

    @Override
    public void onEnable() {

        log.info(getDescription().getName() + " "
                + getDescription().getVersion() + " enabled.");

        // Config
        getDataFolder().mkdirs();
        createDefaultConfiguration("config.yml");
        config = new ShivtrAuthConfiguration(getConfig(), getDataFolder());

        authenticationCore = new AuthenticationCore(this, config.globalSettings.websiteURL);
        getServer().getPluginManager().registerEvents(authenticationCore, this);

        if (config.globalSettings.enableOffline) {
            offlineMode = new OfflineMode(this);
            getServer().getPluginManager().registerEvents(offlineMode, this);
        }

        int updateFrequency = getLocalConfiguration().globalSettings.updateFrequency;
        // Check the config
        if (updateFrequency < 30) {
            log.warning("The update frequency was set at: " + updateFrequency + " minutes and must be at " +
                    "least 30 minutes.");
            updateFrequency = 30;
        }

        // Start the AuthenticationCore
        getServer().getScheduler().runTaskTimerAsynchronously(this, authenticationCore, 0, 20 * 60 * updateFrequency);

        // Start the command executor
        executor = new ShivtrAuthCommandExecutor(this);
        getCommand("authupdate").setExecutor(executor);
    }

    @Override
    public void onDisable() {

        log.info(getDescription().getName() + " disabled.");
    }

    /**
     * Creates configuration
     *
     * @param name
     */
    private void createDefaultConfiguration(String name) {

        File actual = new File(getDataFolder(), name);
        if (!actual.exists()) {

            InputStream input = this.getClass().getResourceAsStream("/defaults/" + name);
            if (input != null) {
                FileOutputStream output = null;

                try {
                    output = new FileOutputStream(actual);
                    byte[] buf = new byte[8192];
                    int length;
                    while ((length = input.read(buf)) > 0) {
                        output.write(buf, 0, length);
                    }

                    log.info(getDescription().getName() + ": Default configuration file written: " + name);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        input.close();
                    } catch (IOException ignored) {
                    }

                    try {
                        if (output != null) output.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    protected ShivtrAuthConfiguration getLocalConfiguration() {

        return config;
    }

    public AuthenticationCore getAuthenticationCore() {

        return authenticationCore;
    }

    public OfflineMode getOfflineMode() {

        return offlineMode;
    }
}
