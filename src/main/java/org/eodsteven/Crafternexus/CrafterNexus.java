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
package org.eodsteven.CrafterNexus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import net.gravitydevelopment.updater.Updater;
import net.gravitydevelopment.updater.Updater.UpdateResult;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Team;
import static org.eodsteven.CrafterNexus.Translation._;
import org.eodsteven.CrafterNexus.api.GameStartEvent;
import org.eodsteven.CrafterNexus.api.PhaseChangeEvent;
import org.eodsteven.CrafterNexus.bar.BarUtil;
import org.eodsteven.CrafterNexus.chat.ChatListener;
import org.eodsteven.CrafterNexus.chat.ChatUtil;
import org.eodsteven.CrafterNexus.commands.ClassCommand;
import org.eodsteven.CrafterNexus.commands.CrafterNexusCommand;
import org.eodsteven.CrafterNexus.commands.DistanceCommand;
import org.eodsteven.CrafterNexus.commands.MapCommand;
import org.eodsteven.CrafterNexus.commands.StatsCommand;
import org.eodsteven.CrafterNexus.commands.TeamCommand;
import org.eodsteven.CrafterNexus.commands.TeamShortcutCommand;
import org.eodsteven.CrafterNexus.commands.VoteCommand;
import org.eodsteven.CrafterNexus.listeners.ClassAbilityListener;
import org.eodsteven.CrafterNexus.listeners.CraftingListener;
import org.eodsteven.CrafterNexus.listeners.EnderBrewingStandListener;
import org.eodsteven.CrafterNexus.listeners.EnderChestListener;
import org.eodsteven.CrafterNexus.listeners.EnderFurnaceListener;
import org.eodsteven.CrafterNexus.listeners.GolemListener;
import org.eodsteven.CrafterNexus.listeners.PlayerListener;
import org.eodsteven.CrafterNexus.listeners.ResourceListener;
import org.eodsteven.CrafterNexus.listeners.SoulboundListener;
import org.eodsteven.CrafterNexus.listeners.WandListener;
import org.eodsteven.CrafterNexus.listeners.WorldListener;
import org.eodsteven.CrafterNexus.manager.ConfigManager;
import org.eodsteven.CrafterNexus.manager.DatabaseManager;
import org.eodsteven.CrafterNexus.manager.GolemManager;
import org.eodsteven.CrafterNexus.manager.MapManager;
import org.eodsteven.CrafterNexus.manager.PhaseManager;
import org.eodsteven.CrafterNexus.manager.RestartHandler;
import org.eodsteven.CrafterNexus.manager.ScoreboardManager;
import org.eodsteven.CrafterNexus.manager.SignManager;
import org.eodsteven.CrafterNexus.manager.VotingManager;
import org.eodsteven.CrafterNexus.maps.MapLoader;
import org.eodsteven.CrafterNexus.object.GameTeam;
import org.eodsteven.CrafterNexus.object.Golem;
import org.eodsteven.CrafterNexus.object.Kit;
import org.eodsteven.CrafterNexus.object.PlayerMeta;
import org.eodsteven.CrafterNexus.object.Shop;
import org.eodsteven.CrafterNexus.stats.StatType;
import org.eodsteven.CrafterNexus.stats.StatsManager;
import org.mcstats.Metrics;

public final class CrafterNexus extends JavaPlugin {
    private ConfigManager configManager;
    private VotingManager voting;
    private MapManager maps;
    private PhaseManager timer;
    private ResourceListener resources;
    private EnderFurnaceListener enderFurnaces;
    private EnderBrewingStandListener enderBrewingStands;
    private EnderChestListener enderChests;
    private StatsManager stats;
    private SignManager sign;
    private ScoreboardManager sb;
    private DatabaseManager db;
    private GolemManager boss;

    public static HashMap<String, String> messages = new HashMap<String, String>();

    public boolean useMysql = false;
    public boolean updateAvailable = false;
    public boolean motd = true;
    public String newVersion;

    public int build = 1;
    public int lastJoinPhase = 2;
    public int respawn = 10;

    public boolean runCommand = false;
    public List<String> commands = new ArrayList<String>();

    public String mysqlName = "annihilation";

    @Override
    public void onEnable() {
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {

        }

        UpdateResult updateResult = null;
        Updater u = null;

        if (this.getConfig().getBoolean("allowUpdater"))
            u = new Updater(this, 72127, this.getFile(),
                    Updater.UpdateType.DEFAULT, true);

        if (u != null)
            updateResult = u.getResult();

        if (updateResult != null) {
            if (updateResult == UpdateResult.SUCCESS) {
                updateAvailable = true;
                newVersion = u.getLatestName();
            }
        }

        configManager = new ConfigManager(this);
        configManager.loadConfigFiles("config.yml", "maps.yml", "shops.yml",
                "stats.yml", "messages.yml");

        MapLoader mapLoader = new MapLoader(getLogger(), getDataFolder());

        runCommand = getConfig().contains("commandsToRunAtEndGame");

        if (runCommand) {
            commands = getConfig().getStringList("commandsToRunAtEndGame");
        } else commands = null;

        maps = new MapManager(this, mapLoader,
                configManager.getConfig("maps.yml"));

        Configuration shops = configManager.getConfig("shops.yml");
        new Shop(this, "Weapon", shops);
        new Shop(this, "Brewing", shops);

        stats = new StatsManager(this, configManager);
        resources = new ResourceListener(this);
        enderFurnaces = new EnderFurnaceListener(this);
        enderBrewingStands = new EnderBrewingStandListener(this);
        enderChests = new EnderChestListener();
        sign = new SignManager(this);
        Configuration config = configManager.getConfig("config.yml");
        timer = new PhaseManager(this, config.getInt("start-delay"),
                config.getInt("phase-period"));
        voting = new VotingManager(this);
        sb = new ScoreboardManager();
        boss = new GolemManager(this);

        PluginManager pm = getServer().getPluginManager();

        File messagesFile = new File("plugins/" + getDescription().getName() + "/messages.yml");
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(messagesFile);

        for (String id : yml.getKeys(false))
            messages.put(id, yml.getString(id));

        sign.loadSigns();

        sb.resetScoreboard(ChatColor.DARK_AQUA + "Voting" + ChatColor.WHITE
                + " | " + ChatColor.GOLD + "/vote <name>");

        build = this.getConfig().getInt("build", 5);
        lastJoinPhase = this.getConfig().getInt("lastJoinPhase", 2);
        respawn = this.getConfig().getInt("bossRespawnDelay", 10);

        pm.registerEvents(resources, this);
        pm.registerEvents(enderFurnaces, this);
        pm.registerEvents(enderBrewingStands, this);
        pm.registerEvents(enderChests, this);
        pm.registerEvents(new ChatListener(this), this);
        pm.registerEvents(new PlayerListener(this), this);
        pm.registerEvents(new WorldListener(), this);
        pm.registerEvents(new SoulboundListener(), this);
        pm.registerEvents(new WandListener(this), this);
        pm.registerEvents(new CraftingListener(), this);
        pm.registerEvents(new ClassAbilityListener(this), this);
        pm.registerEvents(new GolemListener(this), this);

        getCommand("crafternexus").setExecutor(new CrafterNexusCommand(this));
        getCommand("class").setExecutor(new ClassCommand());
        getCommand("stats").setExecutor(new StatsCommand(stats));
        getCommand("team").setExecutor(new TeamCommand(this));
        getCommand("vote").setExecutor(new VoteCommand(voting));
        getCommand("red").setExecutor(new TeamShortcutCommand());
        getCommand("green").setExecutor(new TeamShortcutCommand());
        getCommand("yellow").setExecutor(new TeamShortcutCommand());
        getCommand("blue").setExecutor(new TeamShortcutCommand());
        getCommand("distance").setExecutor(new DistanceCommand(this));
        getCommand("map").setExecutor(new MapCommand(this, mapLoader));

        BarUtil.init(this);

        if (config.getString("stats").equalsIgnoreCase("sql"))
            useMysql = true;

        motd = config.getBoolean("enableMotd", true);

        if (useMysql) {
            String host = config.getString("MySQL.host");
            Integer port = config.getInt("MySQL.port");
            String name = config.getString("MySQL.name");
            String user = config.getString("MySQL.user");
            String pass = config.getString("MySQL.pass");
            db = new DatabaseManager(host, port, name, user, pass, this);

            db.query("CREATE TABLE IF NOT EXISTS `" + mysqlName + "` ( `username` varchar(16) NOT NULL, "
                    + "`kills` int(16) NOT NULL, `deaths` int(16) NOT NULL, `wins` int(16) NOT NULL, "
                    + "`losses` int(16) NOT NULL, `nexus_damage` int(16) NOT NULL, "
                    + "UNIQUE KEY `username` (`username`) ) ENGINE=InnoDB DEFAULT CHARSET=latin1;");
        } else
            db = new DatabaseManager(this);

        if (getServer().getPluginManager().isPluginEnabled("Vault")) {
            VaultHooks.vault = true;
            if (!VaultHooks.instance().setupPermissions()) {
                VaultHooks.vault = false;
                getLogger().warning("Unable to load Vault: No permission plugin found.");
            } else {
                if (!VaultHooks.instance().setupChat()) {
                    VaultHooks.vault = false;
                    getLogger().warning("Unable to load Vault: No chat plugin found.");
                } else {
                    getLogger().info("Vault hook initalized!");
                }
            }
        } else {
            getLogger().warning("Vault not found! Permissions features disabled.");
        }

        reset();

        ChatUtil.setRoman(getConfig().getBoolean("roman", false));
    }

    public boolean startTimer() {
        if (timer.isRunning())
            return false;

        timer.start();

        return true;
    }

    public void loadMap(final String map) {
        FileConfiguration config = configManager.getConfig("maps.yml");
        ConfigurationSection section = config.getConfigurationSection(map);

        World w = getServer().getWorld(map);

        for (GameTeam team : GameTeam.teams()) {
            String name = team.name().toLowerCase();
            if (section.contains("spawns." + name)) {
                for (String s : section.getStringList("spawns." + name))
                    team.addSpawn(Util.parseLocation(getServer().getWorld(map),
                            s));
            }
            if (section.contains("nexuses." + name)) {
                Location loc = Util.parseLocation(w,
                        section.getString("nexuses." + name));
                team.loadNexus(loc, 75);
            }
            if (section.contains("furnaces." + name)) {
                Location loc = Util.parseLocation(w,
                        section.getString("furnaces." + name));
                enderFurnaces.setFurnaceLocation(team, loc);
                loc.getBlock().setType(Material.BURNING_FURNACE);
            }
            if (section.contains("brewingstands." + name)) {
                Location loc = Util.parseLocation(w,
                        section.getString("brewingstands." + name));
                enderBrewingStands.setBrewingStandLocation(team, loc);
                loc.getBlock().setType(Material.BREWING_STAND);
            }
            if (section.contains("enderchests." + name)) {
                Location loc = Util.parseLocation(w,
                        section.getString("enderchests." + name));
                enderChests.setEnderChestLocation(team, loc);
                loc.getBlock().setType(Material.ENDER_CHEST);
            }
        }

        if (section.contains("bosses")) {
            HashMap<String, Golem> bosses = new HashMap<String, Golem>();
            ConfigurationSection sec = section
                    .getConfigurationSection("bosses");
            for (String boss : sec.getKeys(false))
                bosses.put(boss,
                        new Golem(boss, sec.getInt(boss + ".hearts") * 2, sec
                                .getString(boss + ".name"), Util.parseLocation(
                                w, sec.getString(boss + ".spawn")), Util
                                .parseLocation(w,
                                        sec.getString(boss + ".chest"))));
            boss.loadBosses(bosses);
        }

        if (section.contains("diamonds")) {
            Set<Location> diamonds = new HashSet<Location>();
            for (String s : section.getStringList("diamonds"))
                diamonds.add(Util.parseLocation(w, s));
            resources.loadDiamonds(diamonds);
        }
    }

    public void startGame() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            for (Player pp : Bukkit.getOnlinePlayers()) {
                p.showPlayer(pp);
                pp.showPlayer(p);
            }
        }

        Bukkit.getPluginManager().callEvent(
                new GameStartEvent(maps.getCurrentMap()));
        sb.scores.clear();

        for (OfflinePlayer score : sb.sb.getPlayers())
            sb.sb.resetScores(score);

        sb.obj.setDisplayName(ChatColor.DARK_AQUA + "Map: "
                + WordUtils.capitalize(voting.getWinner()));

        for (GameTeam t : GameTeam.teams()) {
            sb.scores.put(t.name(), sb.obj.getScore(Bukkit
                    .getOfflinePlayer(WordUtils.capitalize(t.name()
                            .toLowerCase() + " Nexus"))));
            sb.scores.get(t.name()).setScore(t.getNexus().getHealth());

            Team sbt = sb.sb.registerNewTeam(t.name() + "SB");
            sbt.addPlayer(Bukkit.getOfflinePlayer(WordUtils
                    .capitalize(WordUtils.capitalize(t.name().toLowerCase()
                            + " Nexus"))));
            sbt.setPrefix(t.color().toString());
        }

        sb.obj.setDisplayName(ChatColor.DARK_AQUA + "Map: "
                + WordUtils.capitalize(voting.getWinner()));

        for (Player p : getServer().getOnlinePlayers())
            if (PlayerMeta.getMeta(p).getTeam() != GameTeam.NONE)
                Util.sendPlayerToGame(p, this);

        sb.update();

        getServer().getScheduler().runTaskTimer(this, new Runnable() {
            public void run() {
                for (Player p : getServer().getOnlinePlayers()) {
                    if (PlayerMeta.getMeta(p).getKit() == Kit.SCOUT) {
                        PlayerMeta.getMeta(p).getKit().addScoutParticles(p);
                    }
                }
            }
        }, 0L, 1200L);

        getServer().getScheduler().runTaskTimer(this, new Runnable() {
            public void run() {
                for (GameTeam t : GameTeam.values()) {
                    if (t != GameTeam.NONE && t.getNexus().isAlive()) {
                        Location nexus = t.getNexus().getLocation().clone();
                        nexus.add(0.5, 0, 0.5);
                        Util.ParticleEffects.sendToLocation(Util.ParticleEffects.ENDER, nexus, 1F, 1F, 1F, 0, 20);
                        Util.ParticleEffects.sendToLocation(Util.ParticleEffects.ENCHANTMENT_TABLE, nexus, 1F, 1F, 1F, 0, 20);
                    }
                }
            }
        }, 100L, 5L);
    }

    public void advancePhase() {
        ChatUtil.phaseMessage(timer.getPhase());

        if (timer.getPhase() == 2)
            boss.spawnBosses();

        if (timer.getPhase() == 3)
            resources.spawnDiamonds();

        Bukkit.getPluginManager().callEvent(
                new PhaseChangeEvent(timer.getPhase()));

        getSignHandler().updateSigns(GameTeam.RED);
        getSignHandler().updateSigns(GameTeam.BLUE);
        getSignHandler().updateSigns(GameTeam.GREEN);
        getSignHandler().updateSigns(GameTeam.YELLOW);
    }

    public void onSecond() {
        long time = timer.getTime();

        if (time == -5L) {

            String winner = voting.getWinner();
            voting.end();
            getServer().broadcastMessage(ChatColor.GOLD + "Voting is now closed!");
            maps.selectMap(winner);
            getServer().broadcastMessage(
                    ChatColor.GREEN + WordUtils.capitalize(winner)
                            + " was chosen!");
            loadMap(winner);

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.ENDERDRAGON_WINGS, 10, 1);
                p.playSound(p.getLocation(), Sound.ENDERDRAGON_GROWL, 10, 1);
            }
        }

        if (time <= -5L) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                //p.playSound(p.getLocation(), Sound.ENDERDRAGON_WINGS, 10, 1);
                //p.playSound(p.getLocation(), Sound.ENDERDRAGON_GROWL, 10, 1);
                p.playSound(p.getLocation(), Sound.NOTE_PLING, 10, 2F);
                p.playSound(p.getLocation(), Sound.NOTE_BASS_GUITAR, 10, 2F);
                p.playSound(p.getLocation(), Sound.NOTE_PIANO, 10, 2F);
            }
        }

        if (time == 0L)
            startGame();
    }

    public int getPhase() {
        return timer.getPhase();
    }

    public MapManager getMapManager() {
        return maps;
    }

    public StatsManager getStatsManager() {
        return stats;
    }

    public DatabaseManager getDatabaseHandler() {
        return db;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public int getPhaseDelay() {
        return configManager.getConfig("config.yml").getInt("phase-period");
    }

    public void log(String m, Level l) {
        getLogger().log(l, m);
    }

    public VotingManager getVotingManager() {
        return voting;
    }

    public ScoreboardManager getScoreboardHandler() {
        return sb;
    }

    public void endGame(GameTeam winner) {
        if (winner == null)
            return;

        ChatUtil.winMessage(winner);
        timer.stop();

        for (Player p : getServer().getOnlinePlayers()) {
            if (PlayerMeta.getMeta(p).getTeam() == winner)
                stats.incrementStat(StatType.WINS, p);
        }

        long restartDelay = configManager.getConfig("config.yml").getLong(
                "restart-delay");
        RestartHandler rs = new RestartHandler(this, restartDelay);
        rs.start(timer.getTime(), winner.getColor(winner));
    }

    public void reset() {
        sb.resetScoreboard(ChatColor.DARK_AQUA + "Voting" + ChatColor.WHITE
                + " | " + ChatColor.GOLD + "/vote <name>");
        maps.reset();
        timer.reset();
        PlayerMeta.reset();
        for (Player p : getServer().getOnlinePlayers()) {
            PlayerMeta.getMeta(p).setTeam(GameTeam.NONE);
            p.teleport(maps.getLobbySpawnPoint());
            BarUtil.setMessageAndPercent(p, ChatColor.DARK_AQUA
                    + "Welcome to Crafter's Nexus!", 0.01F);
            p.setMaxHealth(20D);
            p.setHealth(20D);
            p.setFoodLevel(20);
            p.setSaturation(20F);
        }

        voting.start();
        sb.update();

        for (Player p : Bukkit.getOnlinePlayers()) {
            for (Player pp : Bukkit.getOnlinePlayers()) {
                p.showPlayer(pp);
                pp.showPlayer(p);
            }
        }

        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            @SuppressWarnings("deprecation")
            @Override
            public void run() {
                for (Player p : getServer().getOnlinePlayers()) {
                    PlayerInventory inv = p.getInventory();
                    inv.setHelmet(null);
                    inv.setChestplate(null);
                    inv.setLeggings(null);
                    inv.setBoots(null);

                    p.getInventory().clear();

                    for (PotionEffect effect : p.getActivePotionEffects())
                        p.removePotionEffect(effect.getType());

                    p.setLevel(0);
                    p.setExp(0);
                    p.setSaturation(20F);

                    ItemStack selector = new ItemStack(Material.FEATHER);
                    ItemMeta itemMeta = selector.getItemMeta();
                    itemMeta.setDisplayName(ChatColor.AQUA
                            + "Right click to select class");
                    selector.setItemMeta(itemMeta);

                    p.getInventory().setItem(0, selector);

                    p.updateInventory();
                }

                for (GameTeam t : GameTeam.values())
                    if (t != GameTeam.NONE)
                        sign.updateSigns(t);

                checkStarting();
            }
        }, 2L);
    }

    public void checkWin() {
        int alive = 0;
        GameTeam aliveTeam = null;
        for (GameTeam t : GameTeam.teams()) {
            if (t.getNexus().isAlive()) {
                alive++;
                aliveTeam = t;
            }
        }
        if (alive == 1) {
            endGame(aliveTeam);
        }
    }

    public SignManager getSignHandler() {
        return sign;
    }

    public void setSignHandler(SignManager sign) {
        this.sign = sign;
    }

    public void checkStarting() {
        if (!timer.isRunning()) {
            if (Bukkit.getOnlinePlayers().length >= getConfig().getInt(
                    "requiredToStart"))
                timer.start();
        }
    }

    public GolemManager getBossManager() {
        return boss;
    }

    public PhaseManager getPhaseManager() {
        return timer;
    }

    public void listTeams(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "============[ "
                + ChatColor.DARK_AQUA + "Teams" + ChatColor.GRAY
                + " ]============");
        for (GameTeam t : GameTeam.teams()) {
            int size = 0;

            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerMeta meta = PlayerMeta.getMeta(p);
                if (meta.getTeam() == t)
                    size++;
            }

            if (size != 1) {
                sender.sendMessage(t.coloredName() + " - " + size + " " + _("INFO_TEAM_LIST_PLAYERS") + _("DYNAMIC_S"));
            } else {
                sender.sendMessage(t.coloredName() + " - " + size + " " + _("INFO_TEAM_LIST_PLAYERS"));
            }
        }
        sender.sendMessage(ChatColor.GRAY + "===============================");
    }

    public void joinTeam(Player player, String team) {
        PlayerMeta meta = PlayerMeta.getMeta(player);
        if (meta.getTeam() != GameTeam.NONE && !player.hasPermission("annihilation.bypass.teamlimitor")) {
            player.sendMessage(ChatColor.GOLD + _("CRAFTERNEXUS_PREFIX") + ChatColor.DARK_AQUA + _("ERROR_PLAYER_NOSWITCHTEAM"));
            return;
        }

        GameTeam target;
        try {
            target = GameTeam.valueOf(team.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.GOLD + _("CRAFTERNEXUS_PREFIX") + ChatColor.RED + _("ERROR_GAME_INVALIDTEAM"));
            listTeams(player);
            return;
        }

        if (Util.isTeamTooBig(target)
                && !player.hasPermission("annihilation.bypass.teamlimit")) {
            player.sendMessage(ChatColor.GOLD + _("CRAFTERNEXUS_PREFIX") + ChatColor.RED + _("ERROR_GAME_TEAMFULL"));
            return;
        }

        if (target.getNexus() != null) {
            if (target.getNexus().getHealth() == 0 && getPhase() > 1) {
                player.sendMessage(ChatColor.GOLD + _("CRAFTERNEXUS_PREFIX") + ChatColor.RED + _("ERROR_GAME_TEAMNONEXUS"));
                return;
            }
        }

        if (getPhase() > lastJoinPhase
                && !player.hasPermission("annhilation.bypass.phaselimiter")) {
            player.kickPlayer(ChatColor.GOLD + _("CRAFTERNEXUS_PREFIX") + ChatColor.RED
                    + "You cannot join the game in this phase!");
            return;
        }

        player.sendMessage(ChatColor.GOLD + _("CRAFTERNEXUS_PREFIX") + ChatColor.DARK_AQUA + "You joined "
                + target.coloredName());
        meta.setTeam(target);

        getScoreboardHandler().teams.get(team.toUpperCase()).addPlayer(
                player);

        if (getPhase() > 0) {
            Util.sendPlayerToGame(player, this);
        }

        getSignHandler().updateSigns(GameTeam.RED);
        getSignHandler().updateSigns(GameTeam.BLUE);
        getSignHandler().updateSigns(GameTeam.GREEN);
        getSignHandler().updateSigns(GameTeam.YELLOW);
    }
}
