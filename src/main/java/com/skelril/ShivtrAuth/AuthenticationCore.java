package com.skelril.ShivtrAuth;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.skelril.ShivtrAuth.DataTypes.Character;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.ALLOWED;
import static org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST;

/**
 * Author: Turtle9598
 */
public class AuthenticationCore implements Listener, Runnable {

    private final ShivtrAuth plugin;
    private final Logger log;

    private String websiteURL;
    private ConcurrentHashMap<String, Character> characters = new ConcurrentHashMap<String, Character>();

    public AuthenticationCore(ShivtrAuth plugin, String websiteURL) {

        this.plugin = plugin;
        this.log = plugin.getLogger();
        this.websiteURL = websiteURL;
    }

    @Override
    public synchronized void run() {


        JSONArray characters = getFrom("characters.json");

        log.info("Testing the connection to " + websiteURL + "...");
        if (characters != null) {
            log.info("Connection test successful.");
            if (characters.size() > 0) {
                updateWhiteList(characters);
            } else {
                log.warning("No characters could be downloaded!");
                log.info("Your website could be under maintenance or contain no characters.");
            }
        } else {
            log.warning("Connection test failed!");
        }

        if (this.characters.size() == 0) {
            log.info("Attempting to load offline files...");
            loadBackupWhiteList();
        }

        log.info(this.characters.size() + " characters have been loaded.");
    }

    @EventHandler
    public void playerLogin(AsyncPlayerPreLoginEvent event) {

        try {
            if (!canJoin(event.getName()) && event.getLoginResult().equals(ALLOWED)) {
                event.disallow(KICK_WHITELIST, "You must register on your account on " + websiteURL + ".");
            }
        } catch (Exception e) {
            event.disallow(KICK_WHITELIST, "An error has occurred please try again in a few minutes.");
        }
    }

    public synchronized Character getCharacter(String playerName) {

        return characters.get(playerName.trim().toLowerCase());
    }

    public synchronized boolean canJoin(String playerName) {

        return characters.keySet().contains(playerName.trim().toLowerCase());
    }

    public synchronized JSONArray getFrom(String subAddress) {

        JSONArray objective = null;
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {

            List<JSONObject> objects = new ArrayList<JSONObject>();
            JSONParser parser = new JSONParser();
            for (int i = 1; true; i++) {

                try {
                    // Establish the connection
                    URL url = new URL(websiteURL + subAddress + "?page=" + i);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(1500);
                    connection.setReadTimeout(1500);

                    // Check response codes return if invalid
                    if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 300) return null;

                    // Begin to read results
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }

                    // Parse Data
                    JSONObject o = (JSONObject) parser.parse(builder.toString());
                    JSONArray ao = (JSONArray) o.get("characters");
                    if (ao.isEmpty()) break;
                    Collections.addAll(objects, (JSONObject[]) ao.toArray(new JSONObject[ao.size()]));

                } catch (ParseException e) {
                    break;
                }
            }
            objective = new JSONArray();
            objective.addAll(objects);
        } catch (IOException e) {
            return null;
        } finally {
            if (connection != null) connection.disconnect();

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }

        return objective;
    }

    private static final FilenameFilter filenameFilter = new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {

            return name.startsWith("character") && name.endsWith(".json");
        }
    };

    public synchronized void updateWhiteList(JSONArray object) {

        // Load the storage directory
        File charactersDirectory = new File(plugin.getDataFolder().getPath() + "/characters");
        if (!charactersDirectory.exists()) charactersDirectory.mkdir();
        log.info("Updating white list...");

        // Remove outdated JSON backup files
        for (File file : charactersDirectory.listFiles(filenameFilter)) {
            file.delete();
        }

        // Create new JSON backup file
        BufferedWriter out = null;

        File characterList = new File(charactersDirectory, "character-list.json");
        try {
            if (characterList.createNewFile()) {
                out = new BufferedWriter(new FileWriter(characterList));
                out.write(object.toJSONString());
            } else {
                log.warning("Could not create the new character list offline file!");
            }
        } catch (IOException ignored) {
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {
                }
            }
        }

        loadCharacters((JSONObject[]) object.toArray(new JSONObject[object.size()]));

        log.info("The white list has updated successfully.");
    }

    private synchronized void loadBackupWhiteList() {

        File charactersDirectory = new File(plugin.getDataFolder().getPath() + "/characters");
        File characterFile = new File(charactersDirectory, "character-list.json");
        if (!characterFile.exists()) {
            log.warning("No offline file found!");
            return;
        }

        BufferedReader reader = null;
        JSONParser parser = new JSONParser();

        try {
            reader = new BufferedReader(new FileReader(characterFile));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            JSONArray characterArray = ((JSONArray) parser.parse(builder.toString()));
            loadCharacters((JSONObject[]) characterArray.toArray(new JSONObject[characterArray.size()]));
        } catch (IOException e) {
            log.warning("Could not read file: " + characterFile.getName() + ".");
        } catch (ParseException p) {
            log.warning("Could not parse file: " + characterFile.getName() + ".");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }

        log.info("The offline file has been loaded.");
    }

    public synchronized void loadCharacters(JSONObject[] characters) {

        // Remove Old Characters
        this.characters.clear();

        // Add all new Characters
        for (JSONObject aCharacter : characters) {
            this.characters.put(aCharacter.get("name").toString().trim().toLowerCase(),
                    new Character(aCharacter.get("name").toString()));
        }
    }
}
