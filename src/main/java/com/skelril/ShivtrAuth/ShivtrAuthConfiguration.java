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

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;


/**
 * Settings
 */

public class ShivtrAuthConfiguration {

    public ShivtrAuthConfiguration(FileConfiguration cfg, File dataFolder) {

        this.dataFolder = dataFolder;
        globalSettings = new GlobalSettings(cfg);

    }

    private final File dataFolder;
    public final GlobalSettings globalSettings;

    public class GlobalSettings {

        public int updateFrequency;
        public final String websiteURL;
        public final boolean enableOffline;

        private GlobalSettings(FileConfiguration cfg) {
            websiteURL = cfg.getString("website-url", "http://example.shivtr.com/");
            updateFrequency = cfg.getInt("update-frequency", 30);
            enableOffline = cfg.getBoolean("forced-login-during-offline-mode", true);

        }
    }
}
