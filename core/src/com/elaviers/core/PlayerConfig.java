package com.elaviers.core;

import com.sun.istack.internal.NotNull;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.HashMap;
import java.util.UUID;

public class PlayerConfig {
    private final static String CONFIG_ROOT = "plugins/ElvCore/players/";

    public final Relation defaultRelation;
    private final HashMap<UUID, Relation> playerRelations;

    protected final Player player;
    protected long lastAccessTime;

    public final HashMap<String, PermissionData> permissions;

    //Will load any information from file
    protected PlayerConfig(Player player, long time)
    {
        this.lastAccessTime = time;
        this.player = player;

        this.defaultRelation = new Relation(Relation.GotoMode.NEUTRAL);
        this.playerRelations = new HashMap<>();
        this.permissions = new HashMap<>();

        File file = new File(CONFIG_ROOT, player.getUniqueId().toString() + ".txt");
        if (file.exists())
        {
            if (file.canRead())
            {
                try {
                    FileReader fileReader = new FileReader(file);
                    BufferedReader reader = new BufferedReader(fileReader);

                    try {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] tokens = line.split(" ");

                            if (tokens[0].equals("relation")) {
                                if (tokens.length == 2)
                                    defaultRelation.gotoMode = Relation.GotoMode.fromID(Integer.parseInt(tokens[1]));
                                else
                                    playerRelations.put(UUID.fromString(tokens[1]), new Relation(Relation.GotoMode.fromID(Integer.parseInt(tokens[2]))));
                            }
                            else if (tokens[0].equals("permission")) {
                                if (tokens.length == 3) {
                                    permissions.put(tokens[1], new PermissionData(!tokens[2].equals("0")));
                                }
                                else if (tokens.length >= 4) {
                                    long expiryTime = Long.parseLong(tokens[3]);
                                    if (System.currentTimeMillis() < expiryTime)
                                        permissions.put(tokens[1], new PermissionData(!tokens[2].equals("0"), expiryTime));
                                }
                            }
                        }

                        reader.close();
                        fileReader.close();
                    }
                    catch (IOException e)
                    {
                        ElvCore.INSTANCE.getLogger().warning("An IOException was thrown trying to read from player configuration file \"" + file.getAbsolutePath() + "\"");
                    }

                } catch (FileNotFoundException e) {
                    //This shouldn't ever happen
                    ElvCore.INSTANCE.getLogger().warning("Could not find player configuration file \"" + file.getAbsolutePath() + "\", even though it exists!");
                }
            }
            else
                ElvCore.INSTANCE.getLogger().warning("Could not read player configuration file \"" + file.getAbsolutePath() + "\"!");

        }
    }

    public void save()
    {
        File file = new File(CONFIG_ROOT, player.getUniqueId().toString() + ".txt");

        if (playerRelations.size() == 0 && defaultRelation.gotoMode == Relation.GotoMode.NEUTRAL && permissions.size() == 0)
        {
            if (file.exists()) {
                try {
                    if (!file.delete())
                        ElvCore.INSTANCE.getLogger().warning("Could not delete player configuration file \"" + file.getAbsolutePath() + "\"");
                } catch (SecurityException e) {
                    ElvCore.INSTANCE.getLogger().warning("Security exception thrown while attempting to delete player configuration file \"" + file.getAbsolutePath() + "\"");
                }
            }

            return;
        }

        File dir = new File(CONFIG_ROOT);

        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();

        if (!file.exists())
        {
            try {
                if (!file.createNewFile())
                    ElvCore.INSTANCE.getLogger().warning("Unable to create new player configuration file \"" + file.getAbsolutePath() + "\"!");
            } catch (IOException e) {
                ElvCore.INSTANCE.getLogger().warning("An IOException was thrown trying to create player configuration file \"" + file.getAbsolutePath() + "\"!");
            }
        }

        if (file.exists())
        {
            if (file.canWrite())
            {
                try {
                    FileWriter fileWriter = new FileWriter(file);
                    BufferedWriter writer = new BufferedWriter(fileWriter);
                    writer.write("relation " + defaultRelation.gotoMode.id + '\n');

                    playerRelations.forEach(
                            (UUID key, Relation rel) -> {
                                try {
                                    writer.write("relation " + key + ' ' + rel.gotoMode.id + '\n');
                                } catch (IOException e) {
                                    ElvCore.INSTANCE.getLogger().warning("An IOException was thrown trying to write to player configuration file \"" + file.getAbsolutePath() + "\"!");
                                }
                            });
                    
                    permissions.forEach((String permission, PermissionData data) -> {
                        try {
                            if (data.expiryTimeMs < 0)
                                writer.write("permission " + permission + (data.state ? " 1\n" : " 0\n"));
                            else
                                writer.write("permission " + permission + (data.state ? " 1 " : " 0 ") + data.expiryTimeMs + '\n');
                        } catch (IOException e) {
                            ElvCore.INSTANCE.getLogger().warning("An IOException was thrown trying to write to player configuration file \"" + file.getAbsolutePath() + "\"!");
                        }
                    });

                    writer.flush();
                    fileWriter.close();
                    writer.close();
                } catch (IOException e) {
                    ElvCore.INSTANCE.getLogger().warning("An IOException was thrown trying to write to player configuration file \"" + file.getAbsolutePath() + "\"!");
                }
            }
            else
            {
                ElvCore.INSTANCE.getLogger().warning("Unable to write to player configuration file \"" + file.getAbsolutePath() + "\"!");
            }
        }
    }

    @NotNull
    public final Relation getRelation(UUID uuid)
    {
        this.lastAccessTime = System.currentTimeMillis();
        Relation relation = playerRelations.get(uuid);
        return relation != null ? relation : defaultRelation;
    }

    public final PlayerConfig setRelation(UUID uuid, @NotNull Relation relation)
    {
        this.lastAccessTime = System.currentTimeMillis();
        playerRelations.put(uuid, relation);
        return this;
    }

    public final PlayerConfig removeRelation(UUID uuid)
    {
        this.lastAccessTime = System.currentTimeMillis();
        playerRelations.remove(uuid);
        return this;
    }
}
