package com.elaviers.commands;

import com.elaviers.core.ElvCore;
import com.elaviers.core.PlayerConfig;
import com.elaviers.core.Relation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class AcceptCommand implements CommandExecutor, TabCompleter {
    ElvCommands plugin;

    public AcceptCommand(ElvCommands plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player)
        {
            Player to = (Player)sender;

            if (args.length >= 1) {

                boolean all = args[0].equals("*");
                Player from = sender.getServer().getPlayer(args[0]);
                if (!all && from == null)
                {
                    sender.sendMessage("§cCould not find player §o" + args[0]);
                    return true;
                }

                if (args.length >= 2)
                {
                    if (args[1].equalsIgnoreCase("always"))
                    {
                        PlayerConfig config = ElvCore.INSTANCE.getPlayerConfig(to);
                        if (all) {
                            config.defaultRelation.gotoMode = Relation.GotoMode.ALWAYS;
                            sender.sendMessage("§5Default request behaviour changed to §3§lACCEPT");
                        }
                        else {
                            config.setRelation(from.getUniqueId(), new Relation(Relation.GotoMode.ALWAYS));
                            sender.sendMessage("§5Requests from " + from.getName() + " will always be accepted");
                        }
                        config.save();
                    }
                    else if (args[1].equalsIgnoreCase("ask"))
                    {
                        PlayerConfig config = ElvCore.INSTANCE.getPlayerConfig(to);
                        if (all) {
                            config.defaultRelation.gotoMode = Relation.GotoMode.NEUTRAL;
                            sender.sendMessage("§5Default request behaviour changed to §3§lASK");
                        }
                        else {
                            config.setRelation(from.getUniqueId(), new Relation(Relation.GotoMode.NEUTRAL));
                            sender.sendMessage("§5Requests from " + from.getName() + " will need to be accepted");
                        }
                        config.save();

                        return true;
                    }
                    else if (args[1].equalsIgnoreCase("never"))
                    {
                        PlayerConfig config = ElvCore.INSTANCE.getPlayerConfig(to);
                        if (all) {
                            config.defaultRelation.gotoMode = Relation.GotoMode.NEVER;
                            sender.sendMessage("§5Default request behaviour changed to §3§lDENY");
                        }
                        else {
                            config.setRelation(from.getUniqueId(), new Relation(Relation.GotoMode.NEVER));
                            sender.sendMessage("§5Requests from " + from.getName() + " will be ignored");
                        }

                        config.save();

                        LinkedList<GotoRequest> requests = plugin.gotoRequests.get(to);
                        if (requests != null)
                            requests.removeIf(request -> request.from == from);
                    }
                    else if (args[1].equalsIgnoreCase("default"))
                    {
                        PlayerConfig config = ElvCore.INSTANCE.getPlayerConfig(to);
                        if (all) {
                            config.defaultRelation.gotoMode = Relation.GotoMode.NEUTRAL;
                            sender.sendMessage("§5Default request behaviour changed to §3§lASK");
                        }
                        else {
                            config.removeRelation(from.getUniqueId());
                            sender.sendMessage("§5Requests from " + from.getName() + " will follow default behaviour");
                        }

                        config.save();
                    }
                    else
                    {
                        return false;
                    }
                }

                LinkedList<GotoRequest> requests = plugin.gotoRequests.get(to);
                if (requests != null) {
                    if (all)
                    {
                        requests.forEach(request -> {
                            request.from.teleport(to);
                            sender.sendMessage("§2§o" + request.from.getName() + " has been teleported to you");
                        });

                        requests.clear();
                    }
                    else {
                        for (GotoRequest request : requests) {
                            if (request.from == from) {
                                requests.remove(request);
                                from.teleport(to);
                                sender.sendMessage("§2§o" + from.getName() + " has been teleported to you");
                                return true;
                            }
                        }

                        sender.sendMessage("§c§o" + from.getName() + " does not have a pending teleport request");
                    }
                }
            }
            else
            {
                LinkedList<GotoRequest> requests = plugin.gotoRequests.get(to);

                if (requests != null &&requests.size() > 0)
                {
                    sender.sendMessage("§3§n§oPending Teleport Requests");

                    requests.forEach(request -> sender.sendMessage("§5  " + request.from.getName()));
                }
                else
                    sender.sendMessage("§3§oYou have no pending teleport requests");
            }
        }

        return true;
    }

    private final static ArrayList<String> empty = new ArrayList<>();
    private final static List<String> modes = Arrays.asList("always", "ask", "never");
    private final static List<String> modes2 = Arrays.asList("always", "ask", "default", "never");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player)
        {
            if (args.length == 1)
            {
                Collection<? extends Player> players = sender.getServer().getOnlinePlayers();
                ArrayList<String> choices = new ArrayList<>(players.size() + 1);

                choices.add("*");
                players.forEach(player -> choices.add(player.getName()));
                return choices.stream().filter(string -> string.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
            }
            else if (args.length == 2)
            {
                return (args[0].equals("*") ? modes : modes2).stream().filter(string -> string.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
            }
        }

        return empty;
    }
}
