package com.elaviers.deathdroptime;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class DeathDropTime extends JavaPlugin implements Listener {
    private final String TICKSREMAINING = "ticksRemaining";
    private int deathDropTickCount;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        deathDropTickCount = getConfig().getInt("deathDropLifetime");
        if (deathDropTickCount == 0)
        {
            getLogger().warning("Config variable deathDropLifetime not found, using default value of 24000 ticks");
            deathDropTickCount = 24000;
        }

        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    void death(PlayerDeathEvent event)
    {
        if (!event.getKeepInventory())
        {
            World world = event.getEntity().getWorld();
            Location location = event.getEntity().getLocation();
            event.getDrops().forEach(stack -> {
                if (stack != null)
                    world.dropItemNaturally(location, stack).setMetadata(TICKSREMAINING, new FixedMetadataValue(this, deathDropTickCount - 1));
            });
            event.getDrops().clear();
        }
    }

    @EventHandler
    void itemDespawn(ItemDespawnEvent event)
    {
        List<MetadataValue> md = event.getEntity().getMetadata(TICKSREMAINING);
        if (md.size() > 0)
        {
            for (MetadataValue v : md)
            {
                if (v.getOwningPlugin() == this)
                {
                    int remainingLife = v.asInt() - event.getEntity().getTicksLived();
                    if (remainingLife > 0)
                    {
                        event.setCancelled(true);
                    }

                    break;
                }
            }
        }
    }
}
