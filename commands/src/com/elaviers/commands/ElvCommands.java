package com.elaviers.commands;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.LinkedList;

public class ElvCommands extends JavaPlugin {
    public HashMap<Player, LinkedList<GotoRequest>> gotoRequests;

    @Override
    public void onEnable() {
        gotoRequests = new HashMap<>();

        getCommand("accept").setExecutor(new AcceptCommand(this));
        getCommand("goto").setExecutor(new GotoCommand(this));
    }
}
