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
	private Locale pluginLocale;

	public LanguageManager(Main plugin) {
		this.plugin = plugin;

		registerLocales();
		setupLocale();
		init();
	}

	private void init() {
		if (Collections.contains(pluginLocale.aliases, plugin.getChatManager().colorMessage("Language"))) {
			return;
		}

		try {
			FileUtils.copyURLToFile(new URL("https://raw.githubusercontent.com/Despical/LocaleStorage/main/Minecraft/TNT%20Run/" + pluginLocale.prefix + ".yml"), new File(plugin.getDataFolder() + File.separator + "messages.yml"));
		} catch (IOException e) {
			LogUtils.sendConsoleMessage("&c[TNTRun] Error while connecting to internet!");
		}
	}

	private void registerLocales() {
		Arrays.asList(
			new Locale("English", "en_GB", "english", "en"),
			new Locale("German", "de_DE", "german", "de"),
			new Locale("Turkish", "tr_TR", "türkçe", "turkce", "tr"))
			.forEach(LocaleRegistry::registerLocale);
	}

	private void setupLocale() {
		String localeName = plugin.getConfig().getString("locale", "default").toLowerCase();

		for (Locale locale : LocaleRegistry.getRegisteredLocales()) {
			if (locale.prefix.equalsIgnoreCase(localeName)) {
				pluginLocale = locale;
				break;
			}

			for (String alias : locale.aliases) {
				if (alias.equals(localeName)) {
					pluginLocale = locale;
					break;
				}
			}
		}

		if (pluginLocale == null) {
			LogUtils.sendConsoleMessage("&c[TNTRun] Plugin locale is invalid! Using default one...");
			pluginLocale = LocaleRegistry.getByName("English");
		}

		LogUtils.sendConsoleMessage("[TNTRun] Loaded locale " + pluginLocale.name + " (ID: " + pluginLocale.prefix + ")");
	}

	public Locale getPluginLocale() {
		return pluginLocale;
	}
}