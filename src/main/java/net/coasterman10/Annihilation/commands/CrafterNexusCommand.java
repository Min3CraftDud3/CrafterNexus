/*******************************************************************************
 * Copyright 2014 stuntguy3000 (Luke Anderson) and coasterman10.
 *  
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 ******************************************************************************/
package org.eodsteven.CrafterNexus.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.eodsteven.CrafterNexus.CrafterNexus;

public class CrafterNexusCommand implements CommandExecutor {
    private CrafterNexus plugin;

    public CrafterNexusCommand(CrafterNexus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String cyan = ChatColor.DARK_AQUA.toString();
        String white = ChatColor.WHITE.toString();
        String gray = ChatColor.GRAY.toString();
        String red = ChatColor.RED.toString();
        String gold = ChatColor.GOLD.toString();
        String yellow = ChatColor.YELLOW.toString();
        String dgray = ChatColor.DARK_GRAY.toString();
        String green = ChatColor.GREEN.toString();
        String prefix = cyan + "[CrafterNexus] " + gray;
        
        if (args.length == 0) {
            sender.sendMessage(prefix + white + "Crafter Nexus" + plugin.getDescription().getVersion() + " by coasterman10 & stuntguy3000.");
            sender.sendMessage(prefix + gold + "Recoded for 1.7.10 by");
            sender.sendMessage(prefix + yellow + "EODCrafter");
            sender.sendMessage(prefix + gray + "Command Help:");
            sender.sendMessage(prefix + gray + "/crafternexus " + dgray + "-" + white + " Show plugin information.");
            sender.sendMessage(prefix + gray + "/cn start " + dgray + "-" + white + " Begin the game.");
        }
        
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("start")) {
                if (sender.hasPermission("crafternexus.command.start")) {
                    if (!plugin.startTimer()) {
                        sender.sendMessage(prefix + red + "The game has already started");
                    } else {
                        sender.sendMessage(prefix + green + "The game has been started.");
                    }
                } else sender.sendMessage(prefix + red + "You cannot use this command!");
            }
        }
        return false;
    }
}
