package com.elaviers.permissions;

import com.elaviers.core.ElvCore;
import com.elaviers.core.PermissionData;
import com.elaviers.core.PlayerConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ElvPermissions extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        getCommand("permission").setExecutor(new PermissionCommand(this));
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerConfig config = ElvCore.INSTANCE.getPlayerConfig(player);
        config.permissions.forEach((String permission, PermissionData data) -> data.applyToPlayer(this, player, permission));
    }
}
