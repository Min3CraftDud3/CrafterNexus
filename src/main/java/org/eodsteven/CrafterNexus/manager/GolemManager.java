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
package org.eodsteven.CrafterNexus.manager;

import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.eodsteven.CrafterNexus.CrafterNexus;
import org.eodsteven.CrafterNexus.Util;
import org.eodsteven.CrafterNexus.object.Golem;

public class GolemManager {
    public HashMap<String, Golem> bosses = new HashMap<String, Golem>();
    public HashMap<String, Golem> bossNames = new HashMap<String, Golem>();

    private CrafterNexus plugin;

    public GolemManager(CrafterNexus instance) {
        this.plugin = instance;
    }

    public void loadBosses(HashMap<String, Golem> b) {
        bosses = b;
    }

    public void spawnBosses() {
        for (Golem b : bosses.values())
            spawn(b);
    }

    @SuppressWarnings("deprecation")
    public void spawn(Golem b) {
        Location spawn = b.getSpawn();

        if (spawn != null && spawn.getWorld() != null) {
            IronGolem boss = (IronGolem) spawn.getWorld().spawnCreature(spawn,
                    EntityType.IRON_GOLEM);
            boss.setMaxHealth(b.getHealth());
            boss.setHealth(b.getHealth());
            boss.setCanPickupItems(false);
            boss.setPlayerCreated(false);
            boss.setRemoveWhenFarAway(false);
            boss.setCustomNameVisible(true);
            boss.setCustomName(ChatColor.translateAlternateColorCodes('&',
                    b.getBossName() + " &8» &a" + (int) b.getHealth() + " HP"));
            bossNames.put(boss.getCustomName(), b);
            Util.spawnFirework(b.getSpawn());
            Util.spawnFirework(b.getSpawn());
            Util.spawnFirework(b.getSpawn());
        }
    }

    public void update(Golem boss, IronGolem g) {
        boss.setHealth((int) g.getHealth());
        g.setCustomName(ChatColor.translateAlternateColorCodes('&',
                boss.getBossName() + " &8» &a" + (int) boss.getHealth() + " HP"));
        bossNames.put(g.getCustomName(), boss);
        bosses.put(boss.getConfigName(), boss);
    }

    public Golem newBoss(Golem b) {
        String boss = b.getConfigName();
        bosses.remove(boss);
        bossNames.remove(boss);

        FileConfiguration config = plugin.getConfigManager().getConfig(
                "maps.yml");
        ConfigurationSection section = config.getConfigurationSection(plugin
                .getMapManager().getCurrentMap().getName());
        ConfigurationSection sec = section.getConfigurationSection("bosses");

        Golem bb = new Golem(boss, sec.getInt(boss + ".hearts") * 2,
                sec.getString(boss + ".name"), Util.parseLocation(plugin
                        .getMapManager().getCurrentMap().getWorld(),
                        sec.getString(boss + ".spawn")), Util.parseLocation(
                        plugin.getMapManager().getCurrentMap().getWorld(),
                        sec.getString(boss + ".chest")));
        bosses.put(boss, bb);

        return bb;
    }
}
