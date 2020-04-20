package com.elaviers.core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedList;

public class ElvCore extends JavaPlugin {
    public static ElvCore INSTANCE = null;

    LinkedList<PlayerConfig> loadedConfigs;

    @Override
    public void onEnable() {
        INSTANCE = this;

        loadedConfigs = new LinkedList<>();

        Bukkit.getScheduler().runTaskTimer(this, this::deleteOldConfigs, 0, 10000);
    }

    public PlayerConfig getPlayerConfig(Player player)
    {
        for (PlayerConfig config : loadedConfigs) {
            if (config.player == player) {
                config.lastAccessTime = System.currentTimeMillis();
                return config;
            }
        }

        PlayerConfig config = new PlayerConfig(player, System.currentTimeMillis());
        loadedConfigs.add(config);
        return config;
    }

    private void deleteOldConfigs()
    {
        long time = System.currentTimeMillis();
        loadedConfigs.removeIf(config -> time - config.lastAccessTime > 60000);
    }
}
