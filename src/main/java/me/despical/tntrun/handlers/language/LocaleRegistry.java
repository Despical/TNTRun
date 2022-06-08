package me.despical.tntrun.handlers.language;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 01.11.2020
 */
public class LocaleRegistry {

	private static final Set<Locale> registeredLocales = new HashSet<>();

	public static void registerLocale(Locale locale) {
		registeredLocales.add(locale);
	}

	public static Set<Locale> getRegisteredLocales() {
		return registeredLocales;
	}

	public static Locale getByName(String name) {
		for (Locale locale : registeredLocales) {
			if (locale.name.equals(name)) {
				return locale;
			}
		}

		return null;
	}
}