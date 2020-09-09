package me.schooltests.regenblocks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.schooltests.regenblocks.commands.RegenBlocksCommand;
import me.schooltests.regenblocks.events.ChunkEvents;
import me.schooltests.regenblocks.events.RegenEvents;
import me.schooltests.regenblocks.regions.LocationSerializer;
import me.schooltests.regenblocks.regions.RegenRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class RegenBlocks extends JavaPlugin {
    private final RegenBlocksAPI api = new RegenBlocksAPI(this);
    private final YamlConfiguration config = new YamlConfiguration();
    static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .enableComplexMapKeySerialization()
            .disableHtmlEscaping()
            .registerTypeAdapter(Location.class, new LocationSerializer())
            .create();

    public static File REGIONS_FILE;

    public void onEnable() {
        // Register the plugin to the Bukkit Services Provider
        Bukkit.getServicesManager().register(RegenBlocks.class, this, this, ServicePriority.Normal);

        // Load configuration and data files
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }

        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        REGIONS_FILE = new File(getDataFolder(), "regions.json");
        try {
            if (!REGIONS_FILE.exists()) {
                REGIONS_FILE.getParentFile().mkdirs();
                REGIONS_FILE.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        api.loadRegions(REGIONS_FILE);

        // Register events
        Bukkit.getPluginManager().registerEvents(new ChunkEvents(this), this);
        Bukkit.getPluginManager().registerEvents(new RegenEvents(this), this);
        Bukkit.getPluginManager().registerEvents(new BreakableBlocksProvider(), this);

        // Load commands
        getCommand("regenblocks").setExecutor(new RegenBlocksCommand(this));
    }

    public void onDisable() {
        for (RegenRegion r : api.getRegionMap().values()) {
            api.forceRegen(r, false);
        }

        api.saveRegions(REGIONS_FILE);
    }

    public RegenBlocksAPI getAPI() {
        return api;
    }

    public FileConfiguration getConfig() {
        return config;
    }
}