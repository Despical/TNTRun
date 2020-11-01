package me.despical.tntrun.handlers.language;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry class for holding locales
 *
 * @author Despical
 * @since 1.0.8
 * <p>
 * Created at 01.11.2020
 */
public class LocaleRegistry {

	private static final List<Locale> registeredLocales = new ArrayList<>();

	/**
	 * Register new locale into registry
	 *
	 * @param locale locale to register
	 * @throws IllegalArgumentException if same locale is registered twice
	 */
	public static void registerLocale(Locale locale) {
		if (registeredLocales.contains(locale)) {
			throw new IllegalArgumentException("Cannot register same locale twice!");
		}

		registeredLocales.add(locale);
	}

	/**
	 * Get all registered locales
	 *
	 * @return all registered locales
	 */
	public static List<Locale> getRegisteredLocales() {
		return registeredLocales;
	}

	/**
	 * Get locale by its name
	 *
	 * @param name name to search
	 * @return locale by name or locale "Undefined" when not found (null is not returned)
	 * @since 1.0.8
	 */
	public static Locale getByName(String name) {
		for (Locale locale : registeredLocales) {
			if (locale.getName().equals(name)) {
				return locale;
			}
		}

		return new Locale("Undefined", "Undefined", "", "System", new ArrayList<>());
	}
}