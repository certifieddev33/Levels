package com.gmail.certifieddev33.Levels;

import java.io.File;
import java.math.BigDecimal;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.mewin.WGRegionEvents.events.RegionEnterEvent;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.md_5.bungee.api.ChatColor;

public class Levels extends JavaPlugin implements Listener,CommandExecutor {
	//TODO setup level-based areas
	FileConfiguration config = this.getConfig();
	private File configFile = new File("plugins/Levels/config.yml");
	@Override
	public void onEnable() {
		setupConfig();
		getServer().getPluginManager().registerEvents(this, this);
		this.getCommand("setminlevel").setExecutor(this);
	}
	public void onDisable() {
		
	}
	public void setupConfig() {
		try { 
			if(this.configFile.exists()) {
				this.config = YamlConfiguration.loadConfiguration(configFile);
				this.config.load(this.configFile);
				reloadConfig();
				saveDefaultConfig();
			}else {
				saveResource("config.yml", false);
				this.config = YamlConfiguration.loadConfiguration(this.configFile);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(!event.getPlayer().hasPlayedBefore()) {
			config.set("players." + event.getPlayer().getName() + "." + event.getPlayer().getUniqueId() + ".level", 1);
			config.set("players." + event.getPlayer().getName() + "." + event.getPlayer().getUniqueId() + ".rank", "NEWBIE");
			config.set("players." + event.getPlayer().getName() + "." + event.getPlayer().getUniqueId() + ".experience", 0);
			try {
				config.save(configFile);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}else if(config.get("players." + event.getPlayer().getName() + "." + event.getPlayer().getUniqueId() + ".level")==null || config.get("players." + event.getPlayer().getName() + "." + event.getPlayer().getUniqueId() + ".rank")==null
				|| config.get("players." + event.getPlayer().getName() + "." + event.getPlayer().getUniqueId() + ".experience")==null) {
			config.set("players." + event.getPlayer().getName() + "." + event.getPlayer().getUniqueId() + ".level", 1);
			config.set("players." + event.getPlayer().getName() + "." + event.getPlayer().getUniqueId() + ".rank", "NEWBIE");
			config.set("players." + event.getPlayer().getName() + "." + event.getPlayer().getUniqueId() + ".experience", 0);
			try {
				config.save(configFile);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if(config.get("players." + event.getPlayer().getName() + "." + event.getPlayer().getUniqueId() + ".level")==null || config.get("players." + event.getPlayer().getName() + "." + event.getPlayer().getUniqueId() + ".rank")==null
				|| config.get("players." + event.getPlayer().getName() + "." + event.getPlayer().getUniqueId() + ".experience")==null) {
			config.set("players." + event.getPlayer().getName() + "." + event.getPlayer().getUniqueId() + ".level", 1);
			config.set("players." + event.getPlayer().getName() + "." + event.getPlayer().getUniqueId() + ".rank", "NEWBIE");
			config.set("players." + event.getPlayer().getName() + "." + event.getPlayer().getUniqueId() + ".experience", 0);
			try {
				config.save(configFile);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		event.setFormat(ChatColor.YELLOW + "" + config.get("players." + event.getPlayer().getName() + "." + event.getPlayer().getUniqueId() + ".level") +
		" " + ChatColor.RED + "" + ChatColor.BOLD + config.get("players." + event.getPlayer().getName() + "." + event.getPlayer().getUniqueId() + ".rank") + ChatColor.RESET + " " + event.getFormat());
	}
	public WorldGuardPlugin getWorldGuard() {
		Plugin p = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
		if(p instanceof WorldGuardPlugin) return (WorldGuardPlugin) p;
		else return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage("The console cannot send this command.");
			return true;
		}
		Player player = (Player) sender;
		if(args.length>2) {
			player.sendMessage(ChatColor.YELLOW + "/setminlevel <region> <minLevel>");
			return true;
		}
		if(args.length<2) {
			player.sendMessage(ChatColor.YELLOW + "/setminlevel <region> <minLevel>");
			return true;
		}else if(args.length==2) {
			Double testDouble = new Double(Double.parseDouble(args[1]));
			if(testDouble.isNaN()) {
				player.sendMessage(ChatColor.RED + args[1] + " is not a number.");
				return true;
			}
			if(getWorldGuard().getRegionManager(player.getWorld()).getRegion(args[0])==null) {
				player.sendMessage(ChatColor.RED + "Could not find region '" + args[0] + "'");
			}else {
				double minLevel = Double.parseDouble(args[1]);
				config.set("regions." + args[0] + ".minLevel", minLevel);
				try {
					config.save(configFile);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			}
		}
		return true;
	}
	@EventHandler
	public void onRegionEnter(RegionEnterEvent event) {
		Double minLevel = (Double) config.get("regions." + event.getRegion().getId() + ".minLevel");
		Double playerMinLevel = (Double) config.get("players." + event.getPlayer().getName() + "." + event.getPlayer().getUniqueId() + ".level");
		if(playerMinLevel.doubleValue()<minLevel.doubleValue()) {
			event.getPlayer().sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "You are not high enough level to enter this area!");
			event.setCancelled(true);
		}
	}
}
