package me.despical.tntrun.handlers.language;

import me.despical.commons.file.FileUtils;
import me.despical.commons.util.Collections;
import me.despical.commons.util.LogUtils;
import me.despical.tntrun.Main;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

/**
 * @author Despical
 * <p>
 * Created at 01.11.2018
 */
public class LanguageManager {

	private final Main plugin;

	private Locale locale;

	public LanguageManager(Main plugin) {
		this.plugin = plugin;

		registerLocales();
		setupLocale();
		init();
	}

	private void init() {
		if (Collections.contains(locale.aliases, plugin.getChatManager().message("language"))) return;

		try {
			FileUtils.copyURLToFile(new URL("https://raw.githubusercontent.com/Despical/LocaleStorage/main/Minecraft/TNT%20Run/" + locale.prefix + ".yml"), new File(plugin.getDataFolder() + File.separator + "messages.yml"));
		} catch (IOException e) {
			LogUtils.sendConsoleMessage("&c[TNTRun] Error while connecting to internet!");
		}
	}

	private void registerLocales() {
		Arrays.asList(
			new Locale("English", "en_GB", "english", "en", "default"),
			new Locale("German", "de_DE", "german", "de"),
			new Locale("Turkish", "tr_TR", "türkçe", "turkce", "tr"))
			.forEach(LocaleRegistry::registerLocale);
	}

	private void setupLocale() {
		String localeName = plugin.getConfig().getString("locale", "default").toLowerCase();

		for (Locale locale : LocaleRegistry.getRegisteredLocales()) {
			if (locale.prefix.equalsIgnoreCase(localeName)) {
				this.locale = locale;
				break;
			}

			for (String alias : locale.aliases) {
				if (alias.equals(localeName)) {
					this.locale = locale;
					break;
				}
			}
		}

		if (locale == null) {
			LogUtils.sendConsoleMessage("&c[TNTRun] Plugin locale is invalid! Using default one...");
			locale = LocaleRegistry.getByName("English");
		}

		LogUtils.sendConsoleMessage("[TNTRun] Loaded locale " + locale.name + " (ID: " + locale.prefix + ")");
	}

	public Locale getLocale() {
		return locale;
	}
}