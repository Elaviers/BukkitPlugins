package com.elaviers.commands;

import org.bukkit.entity.Player;

public class GotoRequest {
    public final Player from;
    public long requestTime;

    public GotoRequest(Player from, long time)
    {
        this.from = from;
        this.requestTime = time;
    }
}
