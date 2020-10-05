package me.despical.tntrun.utils;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class MessageUtils {

	private MessageUtils() {}
	
	public static void thisVersionIsNotSupported() {
		Debugger.sendConsoleMessage("&c  _   _           _                                                    _                _ ");
		Debugger.sendConsoleMessage("&c | \\ | |   ___   | |_     ___   _   _   _ __    _ __     ___    _ __  | |_    ___    __| |");
		Debugger.sendConsoleMessage("&c |  \\| |  / _ \\  | __|   / __| | | | | | '_ \\  | '_ \\   / _ \\  | '__| | __|  / _ \\  / _` |");
		Debugger.sendConsoleMessage("&c | |\\  | | (_) | | |_    \\__ \\ | |_| | | |_) | | |_) | | (_) | | |    | |_  |  __/ | (_| |");
		Debugger.sendConsoleMessage("&c |_| \\_|  \\___/   \\__|   |___/  \\__,_| | .__/  | .__/   \\___/  |_|     \\__|  \\___|  \\__,_|");
		Debugger.sendConsoleMessage("&c                                       |_|     |_|                                        ");
	}

	public static void errorOccurred() {
		Debugger.sendConsoleMessage("&c  _____                                                                                  _   _ ");
		Debugger.sendConsoleMessage("&c | ____|  _ __   _ __    ___    _ __      ___     ___    ___   _   _   _ __    ___    __| | | |");
		Debugger.sendConsoleMessage("&c |  _|   | '__| | '__|  / _ \\  | '__|    / _ \\   / __|  / __| | | | | | '__|  / _ \\  / _` | | |");
		Debugger.sendConsoleMessage("&c | |___  | |    | |    | (_) | | |      | (_) | | (__  | (__  | |_| | | |    |  __/ | (_| | |_|");
		Debugger.sendConsoleMessage("&c |_____| |_|    |_|     \\___/  |_|       \\___/   \\___|  \\___|  \\__,_| |_|     \\___|  \\__,_| (_)");
		Debugger.sendConsoleMessage("&c                                                                                               ");
	}

	public static void updateIsHere() {
		Debugger.sendConsoleMessage("&a  _   _               _           _          ");
		Debugger.sendConsoleMessage("&a | | | |  _ __     __| |   __ _  | |_    ___ ");
		Debugger.sendConsoleMessage("&a | | | | | '_ \\   / _` |  / _` | | __|  / _ \\");
		Debugger.sendConsoleMessage("&a | |_| | | |_) | | (_| | | (_| | | |_  |  __/");
		Debugger.sendConsoleMessage("&a  \\___/  | .__/   \\__,_|  \\__,_|  \\__|  \\___|");
		Debugger.sendConsoleMessage("&a         |_|                                 ");
	}
}