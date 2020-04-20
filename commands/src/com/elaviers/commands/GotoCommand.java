package com.elaviers.commands;

import com.elaviers.core.ElvCore;
import com.elaviers.core.PlayerConfig;
import com.elaviers.core.Relation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GotoCommand implements CommandExecutor, TabCompleter {
    ElvCommands plugin;

    public GotoCommand(ElvCommands plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player)
        {
            if (args.length >= 1) {
                Player to = sender.getServer().getPlayer(args[0]);
                if (to == null)
                {
                    sender.sendMessage("§cCould not find player §o" + args[0]);
                    return true;
                }

                Player from = (Player)sender;

                PlayerConfig toConfig = ElvCore.INSTANCE.getPlayerConfig(to);
                Relation.GotoMode mode = toConfig.getRelation(from.getUniqueId()).gotoMode;

                if (mode == Relation.GotoMode.ALWAYS) {
                    from.teleport(to);
                    to.sendMessage("§2§o" + from.getName() + " teleported to you");
                    return true;
                }
                else if (mode != Relation.GotoMode.NEVER)
                {
                    LinkedList<GotoRequest> requests = plugin.gotoRequests.computeIfAbsent(to, k -> new LinkedList<>());

                    for (GotoRequest request : requests)
                    {
                        if (request.from == from)
                        {
                            long time = System.currentTimeMillis();
                            if (time - request.requestTime > 5000) {
                                to.sendMessage("§5§o" + from.getName() + " would like to teleport to you");
                                from.sendMessage("§5§oTeleport request sent to " + to.getName());
                            }

                            request.requestTime = time;
                            return true;
                        }
                    }

                    GotoRequest request = new GotoRequest(from, System.currentTimeMillis());
                    requests.add(request);

                    to.sendMessage(new String[]{"§5§o" + from.getName() + " would like to teleport to you", "§7Use §o§l/a " + from.getName() + " §7to accept!"});
                    from.sendMessage("§5§oTeleport request sent to " + to.getName());
                }
                else
                {
                    from.sendMessage("§c§o" + to.getName() + " is ignoring requests from you");
                    return true;
                }
            }
            else return false;
        }

        return true;
    }

    private final static List<String> empty = new ArrayList<>();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player && args.length == 1)
        {
            return null; //player name
        }

        return empty;
    }
}
