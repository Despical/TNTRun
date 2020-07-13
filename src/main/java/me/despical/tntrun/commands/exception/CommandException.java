package me.despical.tntrun.commands.exception;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class CommandException extends Exception {

	private static final long serialVersionUID = 1L;

	public CommandException(String message) {
		super (message);
	}
}