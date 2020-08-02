package me.despical.tntrun.utils;

import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class Debugger {

	private static final HashSet<String> listenedPerformance = new HashSet<>();
	private static boolean enabled = false;
	private static boolean deep = false;
	private static final Logger logger = Logger.getLogger("");

	private Debugger() {}

	public static void setEnabled(boolean enabled) {
		Debugger.enabled = enabled;
	}

	public static void deepDebug(boolean deep) {
		Debugger.deep = deep;
	}

	public static void monitorPerformance(String task) {
		listenedPerformance.add(task);
	}

	/**
	 * Prints debug message with selected log level. Messages of level INFO or TASK
	 * won't be posted if debugger is enabled, warnings and errors will be.
	 *
	 * @param level level of debugged message
	 * @param msg   debugged message
	 */
	public static void debug(Level level, String msg) {
		if (!enabled && (level != Level.WARNING || level != Level.SEVERE)) {
			return;
		}
		logger.log(level, "[TRDBG] " + msg);
	}

	/**
	 * Prints debug message with selected log level and replaces parameters.
	 * Messages of level INFO or TASK won't be posted if debugger is enabled,
	 * warnings and errors will be.
	 *
	 * @param level level of debugged message
	 * @param msg   debugged message
	 */
	public static void debug(Level level, String msg, Object... params) {
		if (!enabled && (level != Level.WARNING || level != Level.SEVERE)) {
			return;
		}
		logger.log(level, "[TRDBG] " + msg, params);
	}

	/**
	 * Prints performance debug message with selected log level and replaces
	 * parameters.
	 *
	 * @param msg debugged message
	 */
	public static void performance(String monitorName, String msg, Object... params) {
		if (!deep) {
			return;
		}
		if (!listenedPerformance.contains(monitorName)) {
			return;
		}
		logger.log(Level.INFO, "[TRDBG] " + msg, params);
	}
}