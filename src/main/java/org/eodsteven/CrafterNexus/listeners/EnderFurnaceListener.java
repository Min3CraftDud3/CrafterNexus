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
package org.eodsteven.CrafterNexus.listeners;

import java.util.HashMap;
import net.minecraft.server.v1_8_R1.EntityHuman;
import net.minecraft.server.v1_8_R1.EntityPlayer;
import net.minecraft.server.v1_8_R1.TileEntityFurnace;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftInventoryFurnace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.eodsteven.CrafterNexus.CrafterNexus;
import org.eodsteven.CrafterNexus.object.GameTeam;
import org.eodsteven.CrafterNexus.object.PlayerMeta;

public class EnderFurnaceListener implements Listener {
    private HashMap<GameTeam, Location> locations;
    private HashMap<String, VirtualFurnace> furnaces;

    public EnderFurnaceListener(CrafterNexus plugin) {
        locations = new HashMap<>();
        furnaces = new HashMap<>();
        
        Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                for (VirtualFurnace f : furnaces.values())
                    f.h();
            }
        }, 0L, 1L);
    }

    public void setFurnaceLocation(GameTeam team, Location loc) {
        locations.put(team, loc);
    }

    @EventHandler
    public void onFurnaceOpen(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        
        Block b = e.getClickedBlock();
        if (b.getType() != Material.BURNING_FURNACE)
            return;

        Location loc = b.getLocation();
        Player player = e.getPlayer();
        GameTeam team = PlayerMeta.getMeta(player).getTeam();
        if (team == null || !locations.containsKey(team))
            return;

        if (locations.get(team).equals(loc)) {
            e.setCancelled(true);
            EntityPlayer handle = ((CraftPlayer) player).getHandle();
            handle.openContainer(getFurnace(player));
            player.sendMessage(ChatColor.DARK_AQUA
                    + "This is your team's Ender Furnace. Any items you store or smelt here are safe from all other players.");
        }
    }

    @EventHandler
    public void onFurnaceBreak(BlockBreakEvent e) {
        if (locations.values().contains(e.getBlock().getLocation()))
            e.setCancelled(true);
    }

    private VirtualFurnace getFurnace(Player player) {
        if (!furnaces.containsKey(player.getName())) {
            EntityPlayer handle = ((CraftPlayer) player).getHandle();
            furnaces.put(player.getName(), new VirtualFurnace(handle));
        }
        return furnaces.get(player.getName());
    }

    private class VirtualFurnace extends TileEntityFurnace {
        public VirtualFurnace(EntityHuman entity) {
            world = entity.world;
        }

        @Override
        public boolean a(EntityHuman entity) {
            return true;
        }

        public int p() {
            return 0;
        }

        public net.minecraft.server.v1_8_R1.Block q() {
            return net.minecraft.server.v1_8_R1.Blocks.BURNING_FURNACE;
        }

        @Override
        public void update() {

        }

        @Override
        public InventoryHolder getOwner() {
            return new InventoryHolder() {
                @Override
                public Inventory getInventory() {
                    return new CraftInventoryFurnace(VirtualFurnace.this);
                }
            };
        }
    }
}
