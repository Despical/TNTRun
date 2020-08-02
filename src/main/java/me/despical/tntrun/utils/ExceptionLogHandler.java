package me.despical.tntrun.utils;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.bukkit.Bukkit;

import me.despical.tntrun.Main;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class ExceptionLogHandler extends Handler {

	private final List<String> blacklistedClasses = Arrays.asList("me.despical.tntrun.user.data.MysqlManager", "me.despical.tntrun.commonsbox.database.MysqlDatabase");
	private final Main plugin;
	
	public ExceptionLogHandler(Main plugin) {
		this.plugin = plugin;
		Bukkit.getLogger().addHandler(this);
	}

	@Override
	public void close() throws SecurityException {}

	@Override
	public void flush() {}

	@Override
	public void publish(LogRecord record) {
		Throwable throwable = record.getThrown();
		if (!(throwable instanceof Exception) || !throwable.getClass().getSimpleName().contains("Exception")) {
			return;
		}
		if (throwable.getStackTrace().length <= 0) {
			return;
		}
		if (throwable.getCause() != null && throwable.getCause().getStackTrace() != null) {
			if (!throwable.getCause().getStackTrace()[0].getClassName().contains("me.despical.tntrun")) {
				return;
			}
		}
		if (!throwable.getStackTrace()[0].getClassName().contains("me.despical.tntrun")) {
			return;
		}
		if (containsBlacklistedClass(throwable)) {
			return;
		}
		record.setThrown(null);
	
		Exception exception = throwable.getCause() != null ? (Exception) throwable.getCause() : (Exception) throwable;
		StringBuilder stacktrace = new StringBuilder(exception.getClass().getSimpleName());
		if (exception.getMessage() != null) {
			stacktrace.append(" (").append(exception.getMessage()).append(")");
		}
		stacktrace.append("\n");
		for (StackTraceElement str : exception.getStackTrace()) {
			stacktrace.append(str.toString()).append("\n");
		}
		plugin.getLogger().log(Level.WARNING, "[Reporter service] <<-----------------------------[START]----------------------------->>");
		plugin.getLogger().log(Level.WARNING, stacktrace.toString());
		plugin.getLogger().log(Level.WARNING, "[Reporter service] <<------------------------------[END]------------------------------>>");
		record.setMessage("[TNT Run] We have found a bug in the code. Contact us at our official discord server (Invite link: https://discordapp.com/invite/Vhyy4HA) with the following error given above!");
	}

	private boolean containsBlacklistedClass(Throwable throwable) {
		for (StackTraceElement element : throwable.getStackTrace()) {
			for (String blacklist : blacklistedClasses) {
				if (element.getClassName().contains(blacklist)) {
					return true;
				}
			}
		}
		return false;
	}
}