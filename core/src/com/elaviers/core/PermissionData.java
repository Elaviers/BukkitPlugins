package com.elaviers.core;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PermissionData {
    public final boolean state;
    public final long expiryTimeMs;

    public PermissionData(boolean state, long expiryTimeMs)
    {
        this.state = state;
        this.expiryTimeMs = expiryTimeMs;
    }

    public PermissionData(boolean state)
    {
        this(state, -1);
    }

    public boolean isNotExpired()
    {
        return this.expiryTimeMs < 0 || System.currentTimeMillis() < this.expiryTimeMs;
    }

    public boolean applyToPlayer(Plugin plugin, Player player, String name)
    {
        if (this.expiryTimeMs < 0) {
            player.addAttachment(plugin, name, state);
            return true;
        }

        if (this.expiryTimeMs >= 50)
        {
            player.addAttachment(plugin, name, state, (int)((this.expiryTimeMs - System.currentTimeMillis()) / 50)); //1 tick = 50ms
            return true;
        }

        return false;
    }
}
