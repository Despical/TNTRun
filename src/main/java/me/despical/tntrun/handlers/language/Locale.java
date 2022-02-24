package me.despical.tntrun.handlers.language;

/**
 * @author Despical
 * <p>
 * Created at 01.11.2020
 */
public class Locale {

	public final String name, prefix, aliases[];

	public Locale(String name, String prefix, String... aliases) {
		this.prefix = prefix;
		this.name = name;
		this.aliases = aliases;
	}
}