package com.ericdebouwer.zombieapocalypse;

import com.ericdebouwer.zombieapocalypse.apocalypse.ApocalypseCommand;
import com.ericdebouwer.zombieapocalypse.apocalypse.ApocalypseListener;
import com.ericdebouwer.zombieapocalypse.apocalypse.ApocalypseManager;
import com.ericdebouwer.zombieapocalypse.config.ConfigurationManager;
import com.ericdebouwer.zombieapocalypse.integration.silkspawners.SilkZombieItems;
import com.ericdebouwer.zombieapocalypse.integration.silkspawners.SpawnerBreakListener;
import com.ericdebouwer.zombieapocalypse.zombie.ZombieCommand;
import com.ericdebouwer.zombieapocalypse.zombie.ZombieItems;
import com.ericdebouwer.zombieapocalypse.zombie.ZombieListener;
import com.ericdebouwer.zombieapocalypse.zombie.ZombieFactory;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Consumer;

public class ZombieApocalypse extends JavaPlugin {

	private ApocalypseManager apoManager;
	private ConfigurationManager configManager;
	private ZombieFactory zombieFactory;
	private ZombieItems zombieItems;
	
	public boolean isPaperMC = false;
	
	public String logPrefix;

	@Override
	public void onEnable(){
		logPrefix = "[" + this.getName() + "] ";

		zombieFactory = new ZombieFactory(this);
		configManager = new ConfigurationManager(this);

		if (!configManager.isValid()){
			getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.RED + logPrefix + "Invalid config.yml, plugin will disable to prevent crashing!");
 			getServer().getConsoleSender().sendMessage(ChatColor.BOLD + "" + ChatColor.RED + logPrefix + "See the header of the config.yml about fixing the problem.");
			return;
		}
		getLogger().info("Configuration has been successfully loaded!");
		getLogger().info("If you really love this project, you could consider donating to help me keep this project alive! https://paypal.me/3ricL");

		ApocalypseCommand apoCommand = new ApocalypseCommand(this);
		this.getCommand("apocalypse").setExecutor(apoCommand);
		this.getCommand("apocalypse").setTabCompleter(apoCommand);
		
		apoManager = new ApocalypseManager(this);
		zombieItems = new ZombieItems(this);
		
		ZombieCommand zombieCommand = new ZombieCommand(this);
		this.getCommand("zombie").setExecutor(zombieCommand);
		this.getCommand("zombie").setTabCompleter(zombieCommand);
		
		try {
			Class.forName("org.bukkit.World").getDeclaredMethod("spawn", Location.class, Class.class, Consumer.class, CreatureSpawnEvent.SpawnReason.class);
		    isPaperMC = true;
		    getLogger().info("PaperMC detected! Changing spawning algorithm accordingly");
		} catch (ClassNotFoundException | NoSuchMethodException ignore) {
		}

		getServer().getPluginManager().registerEvents(new ZombieListener(this), this);
		getServer().getPluginManager().registerEvents(new ApocalypseListener(this), this);

		if (getServer().getPluginManager().getPlugin("SilkSpawners") != null){
			getLogger().info("SilkSpawners detected! Making our spawners comply with it");
			zombieItems = new SilkZombieItems(this);
			getServer().getPluginManager().registerEvents(new SpawnerBreakListener(this), this);
		}

		if (configManager.checkUpdates) {
			new UpdateChecker(this)
					.onStart(() -> getLogger().info( "Checking for updates..."))
					.onError(() -> getLogger().warning( "Failed to check for updates!"))
					.onOldVersion((oldVersion, newVersion) -> {
						getLogger().info( "Update detected! You are using version " + oldVersion + ", but version " + newVersion + " is available!");
						getLogger().info("You can download the new version here -> https://www.spigotmc.org/resources/" +  UpdateChecker.RESOURCE_ID + "/updates");
					})
					.onNoUpdate(() -> getLogger().info("You are running the latest version."))
					.run();
		}
	}
	
	@Override
	public void onDisable(){
		if (apoManager != null)
			apoManager.onDisable();
	}

	public ZombieItems getZombieItems() {return this.zombieItems; }
	
	public ApocalypseManager getApocalypseManager() {
		return this.apoManager;
	}
	
	public ConfigurationManager getConfigManager(){
		return this.configManager;
	}

	public ZombieFactory getZombieFactory(){return this.zombieFactory;}
}
