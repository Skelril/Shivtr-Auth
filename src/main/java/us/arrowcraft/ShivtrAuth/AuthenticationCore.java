package us.arrowcraft.ShivtrAuth;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import us.arrowcraft.ShivtrAuth.DataTypes.Character;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

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
    public void run() {

        JSONArray[] objects = getResultArrayFrom("characters.json");

        log.info("Testing the connection to " + websiteURL + "...");
        if (objects != null) {
            log.info("Connection test successful.");
            if (objects.length > 0) {
                updateWhiteList(objects);
            } else {
                log.warning("No characters could be found!");
                log.info("Your website could be under maintenance or contain no characters.");
            }
        } else {
            log.warning("Connection test failed!");
            if (characters.size() == 0) {
                log.info("Attempting to load offline files...");
                loadBackupWhiteList();
            }
        }
    }

    @EventHandler
    public void playerLogin(PlayerLoginEvent event) {

        try {
            if (!canJoin(event.getPlayer())) {
                event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "You must register on " +
                        "your account on " + websiteURL + ".");
            }
        } catch (Exception e) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, "An error has occurred " +
                    "please try again in a few minutes.");
        }
    }

    public Character getCharacter(Player player) {

        return getCharacter(player.getName());
    }

    public Character getCharacter(String playerName) {

        return characters.get(playerName.trim().toLowerCase());
    }

    public boolean canJoin(Player player) {

        return canJoin(player.getName());
    }

    public boolean canJoin(String playerName) {

        return characters.keySet().contains(playerName.trim().toLowerCase());
    }

    public JSONArray[] getResultArrayFrom(String subAddress) {

        JSONArray objective[] = null;
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {

            List<JSONArray> objects = new ArrayList<JSONArray>();
            JSONParser parser = new JSONParser();
            for (int i = 1; true; i++) {

                try {
                    // Establish the connection
                    URL url = new URL(websiteURL + subAddress + "?page=" + i);
                    connection = (HttpURLConnection) url.openConnection();

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
                    JSONArray o = (JSONArray) parser.parse(builder.toString());
                    if (o.isEmpty()) break;
                    objects.add(o);

                } catch (ParseException e) {
                    break;
                }
            }
            objective = objects.toArray(new JSONArray[objects.size()]);
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

    public String authenticate(String email, String password) {

        HttpURLConnection connection = null;
        OutputStream out = null;
        Writer writer = null;
        BufferedReader reader = null;

        try {

            // Setup the connection
            URL url = new URL(websiteURL + "/users/sign_in.json");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setAllowUserInteraction(false);
            connection.setRequestProperty("Content-Type", "application/xml");

            // Create properties and send them
            JSONObject object = new JSONObject();
            object.put("email", email);
            object.put("password", password);
            out = connection.getOutputStream();
            writer = new OutputStreamWriter(out, "UTF-8");
            writer.write(object.toJSONString());

            // Check response codes return if invalid
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 300) return "";

            // Begin to read results
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            JSONParser parser = new JSONParser();
            JSONObject responseObject = (JSONObject) parser.parse(builder.toString());

            return responseObject.get("authentication-token").toString();
        } catch (IOException e) {
            return "";
        } catch (ParseException e) {
            return "";
        } finally {
            if (connection != null) connection.disconnect();

            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static final FilenameFilter filenameFilter = new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {

            return name.startsWith("character") && name.endsWith(".json");
        }
    };

    public void updateWhiteList(JSONArray[] object) {

        // Load the storage directory
        File charactersDirectory = new File(plugin.getDataFolder().getPath() + "/characters");
        if (!charactersDirectory.exists()) charactersDirectory.mkdir();
        log.info("Updating white list.");

        // Remove outdated JSON backup files
        for (File file : charactersDirectory.listFiles(filenameFilter)) {
            log.info("Removed file: " + file.getName() + ".");
            file.delete();
        }

        // Create new JSON backup files
        int fileNumber = 1;
        for (JSONArray aJSONArray : object) {

            BufferedWriter out = null;

            File f = new File(charactersDirectory, "character-list-" + fileNumber + ".json");
            try {
                if (f.createNewFile()) {
                    out = new BufferedWriter(new FileWriter(f));
                    out.write(aJSONArray.toJSONString());
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
            fileNumber++;

            addCharacters(aJSONArray);
        }

        log.info("The white list has updated successfully.");
    }

    private void loadBackupWhiteList() {

        File charactersDirectory = new File(plugin.getDataFolder().getPath() + "/characters");
        if (!charactersDirectory.exists()) {
            log.warning("No offline files found!");
            return;
        }

        BufferedReader reader = null;
        JSONParser parser = new JSONParser();

        for (File file : charactersDirectory.listFiles(filenameFilter)) {
            try {
                log.info("Found file: " + file.getName() + ".");
                reader = new BufferedReader(new FileReader(file));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                addCharacters((JSONArray) parser.parse(builder.toString()));
            } catch (IOException e) {
                log.warning("Could not read file: " + file.getName() + ".");
            } catch (ParseException p) {
                log.warning("Could not parse file: " + file.getName() + ".");
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }

        log.info("All found offline files have been loaded.");
    }

    public void addCharacters(JSONArray aJSONArray) {

        // Remove Old Characters
        characters.clear();

        // Add all new Characters
        for (Object aCharacterObject : aJSONArray) {
            JSONObject aJSONCharacterObject = (JSONObject) aCharacterObject;
            characters.put(aJSONCharacterObject.get("name").toString().trim().toLowerCase(),
                    new Character(aJSONCharacterObject.get("name").toString()));
        }
    }
}