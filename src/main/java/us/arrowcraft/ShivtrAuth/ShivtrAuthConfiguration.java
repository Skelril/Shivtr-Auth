package us.arrowcraft.ShivtrAuth;

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
