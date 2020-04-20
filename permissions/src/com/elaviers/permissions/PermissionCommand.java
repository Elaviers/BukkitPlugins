package com.elaviers.permissions;

import com.elaviers.core.ElvCore;
import com.elaviers.core.PermissionData;
import com.elaviers.core.PlayerConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.*;

public class PermissionCommand implements CommandExecutor, TabCompleter {
    ElvPermissions plugin;

    public PermissionCommand(ElvPermissions plugin)
    {
        this.plugin = plugin;
    }

    enum PermissionOperation
    {
        GRANT,
        DENY,
        REMOVE_ATTACHMENT
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (strings.length >= 1)
        {
            Player player = sender.getServer().getPlayer(strings[0]);
            if (player == null)
            {
                sender.sendMessage("§cPlayer §c§o" + strings[0] + " §cnot found");
                return true;
            }

            if (strings.length == 1)
            {
                LinkedList<String> messages = new LinkedList<String>();
                for (PermissionAttachmentInfo attachmentInfo : player.getEffectivePermissions())
                {
                    messages.add((attachmentInfo.getAttachment() != null ? "§3§o" : "§7") + attachmentInfo.getPermission() + ": " + (attachmentInfo.getValue() ? "§2true" : "§4false"));
                }

                sender.sendMessage(messages.toArray(new String[messages.size()]));
                return true;
            }
            else
            {
                for (int i = 1; i < strings.length; i++)
                {
                    PermissionOperation operation = PermissionOperation.GRANT;
                    int ticks = 0;

                    String perm = strings[i];
                    char first = perm.charAt(0);
                    if (first == '+')
                    {
                        operation = PermissionOperation.GRANT;
                        perm = perm.substring(1);
                    }
                    else if (first == '-') {
                        operation = PermissionOperation.DENY;
                        perm = perm.substring(1);
                    }
                    else if (first == '~')
                    {
                        operation = PermissionOperation.REMOVE_ATTACHMENT;
                        perm = perm.substring(1);
                    }

                    int split = perm.indexOf(':');
                    if (split > 0) {
                        try {
                            ticks = Integer.parseInt(perm.substring(split + 1));
                            perm = perm.substring(0, split);
                        }
                        catch (NumberFormatException e)
                        {
                            sender.sendMessage("§cInvalid tick count for argument \"" + perm);
                            continue;
                        }
                    }

                    if (operation == PermissionOperation.REMOVE_ATTACHMENT)
                    {
                        if (ticks > 0) {
                            sender.sendMessage("§c§oYou cannot specify ticks for reverting a permission! (permission is \"" + perm + "\")");

                            continue;
                        } else if (perm.length() == 0) {
                            for (PermissionAttachmentInfo attachmentInfo : player.getEffectivePermissions())
                            {
                                PermissionAttachment attachment = attachmentInfo.getAttachment();
                                if (attachment != null)
                                    attachment.remove();
                            }

                            PlayerConfig config = ElvCore.INSTANCE.getPlayerConfig(player);
                            config.permissions.clear();
                            config.save();

                            sender.sendMessage("§2Reverted all permissions for " + player.getName());

                            continue;
                        }
                    }

                    if (operation == PermissionOperation.REMOVE_ATTACHMENT)
                    {
                        for (PermissionAttachmentInfo attachmentInfo : player.getEffectivePermissions())
                        {
                            PermissionAttachment attachment = attachmentInfo.getAttachment();
                            if (attachment != null && attachment.getPlugin() == plugin && attachmentInfo.getPermission().equalsIgnoreCase(perm))
                            {
                                sender.sendMessage("§3§oRemoving attachment containing the following permissions from " + player.getName() + ":");

                                StringBuilder permsString = new StringBuilder("§3§o{");

                                Iterator<String> iterator = attachment.getPermissions().keySet().iterator();
                                while (true)
                                {
                                    permsString.append(iterator.next());

                                    if (iterator.hasNext())
                                    {
                                        permsString.append(", ");
                                    }
                                    else break;
                                }

                                sender.sendMessage(permsString + "}");

                                player.removeAttachment(attachment);
                            }
                        }

                        PlayerConfig config = ElvCore.INSTANCE.getPlayerConfig(player);
                        config.permissions.remove(perm);
                        config.save();

                        sender.sendMessage("§2Reverted permission \"" + perm + "\" to default for " + player.getName());
                        continue;
                    }

                    PermissionData data = new PermissionData(operation != PermissionOperation.DENY, ticks > 0 ? (System.currentTimeMillis() + ticks * 50) : -1); //1 tick = 50ms
                    data.applyToPlayer(plugin, player, perm);

                    PlayerConfig config = ElvCore.INSTANCE.getPlayerConfig(player);
                    config.permissions.put(perm, data);
                    config.save();

                    sender.sendMessage((data.state ? "§2Granted " : "§2Denied ") + player.getName() + " permission \"" + perm + (ticks > 0 ? ("\" for " + ticks + " ticks") : "\""));
                }

                return true;
            }
        }

        return false;
    }

    private static final ArrayList<String> empty = new ArrayList<>();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1)
            return null;

        return empty;
    }
}
