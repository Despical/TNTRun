package me.despical.tntrun.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class MessageUtils {

	private MessageUtils() {}
	
	public static void thisVersionIsNotSupported() {
		Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "  _   _           _                                                    _                _ ");
		Bukkit.getConsoleSender().sendMessage(ChatColor.RED + " | \\ | |   ___   | |_     ___   _   _   _ __    _ __     ___    _ __  | |_    ___    __| |");
		Bukkit.getConsoleSender().sendMessage(ChatColor.RED + " |  \\| |  / _ \\  | __|   / __| | | | | | '_ \\  | '_ \\   / _ \\  | '__| | __|  / _ \\  / _` |");
		Bukkit.getConsoleSender().sendMessage(ChatColor.RED + " | |\\  | | (_) | | |_    \\__ \\ | |_| | | |_) | | |_) | | (_) | | |    | |_  |  __/ | (_| |");
		Bukkit.getConsoleSender().sendMessage(ChatColor.RED + " |_| \\_|  \\___/   \\__|   |___/  \\__,_| | .__/  | .__/   \\___/  |_|     \\__|  \\___|  \\__,_|");
		Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "                                       |_|     |_|                                        ");
	}

	public static void errorOccurred() {
		Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "  _____                                                                                  _   _ ");
		Bukkit.getConsoleSender().sendMessage(ChatColor.RED + " | ____|  _ __   _ __    ___    _ __      ___     ___    ___   _   _   _ __    ___    __| | | |");
		Bukkit.getConsoleSender().sendMessage(ChatColor.RED + " |  _|   | '__| | '__|  / _ \\  | '__|    / _ \\   / __|  / __| | | | | | '__|  / _ \\  / _` | | |");
		Bukkit.getConsoleSender().sendMessage(ChatColor.RED + " | |___  | |    | |    | (_) | | |      | (_) | | (__  | (__  | |_| | | |    |  __/ | (_| | |_|");
		Bukkit.getConsoleSender().sendMessage(ChatColor.RED + " |_____| |_|    |_|     \\___/  |_|       \\___/   \\___|  \\___|  \\__,_| |_|     \\___|  \\__,_| (_)");
		Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "                                                                                               ");
	}

	public static void updateIsHere() {
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "  _   _               _           _          ");
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + " | | | |  _ __     __| |   __ _  | |_    ___ ");
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + " | | | | | '_ \\   / _` |  / _` | | __|  / _ \\");
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + " | |_| | | |_) | | (_| | | (_| | | |_  |  __/");
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "  \\___/  | .__/   \\__,_|  \\__,_|  \\__|  \\___|");
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "         |_|                                 ");
	}
}