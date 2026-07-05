/*
 * TNT Run - A fast-paced parkour minigame for Minecraft.
 * Copyright (C) 2026  Berke Akçen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dev.despical.tntrun.chat;

import dev.despical.commandframework.CommandArguments;
import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.tntrun.Main;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.utils.StringUtils;
import dev.despical.tntrun.utils.Utils;
import dev.despical.tntrun.utils.Var;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
public class ChatManager {

    private static final String NO_CENTER_PREFIX = "%no_center%";

    private final Main plugin;
    private final MiniMessage miniMessage;
    private final Map<String, Component> messages;

    private FileConfiguration messagesFile;

    public ChatManager(Main plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.messages = new HashMap<>();
        this.loadFile();
    }

    public void loadFile() {
        this.messages.clear();
        this.messagesFile = ConfigUtils.getConfig(plugin, "messages");
    }

    public void sendRawComponent(Player player, Component component, Var... vars) {
        if (component == Component.empty()) {
            return;
        }

        player.sendMessage(replaceVarsInComponent(component, vars));
    }

    public void sendRawTitleComponent(Player player, Component title, Component subtitle, int fadeIn, int stay, int fadeOut, Var... vars) {
        if (title == null) title = Component.empty();
        if (subtitle == null) subtitle = Component.empty();

        Title titleObj = Title.title(replaceVarsInComponent(title, vars), replaceVarsInComponent(subtitle, vars), fadeIn, stay, fadeOut);
        player.showTitle(titleObj);
    }

    public void sendMessage(CommandArguments arguments, String messageKey, Var... vars) {
        Component component = getMessageComponent(messageKey, vars);
        if (component == Component.empty()) {
            return;
        }

        arguments.sendMessage(component);
    }

    public void sendMessage(CommandSender recipient, String messageKey, Var... vars) {
        Component component = getMessageComponent(messageKey, vars);
        if (component == Component.empty()) {
            return;
        }

        recipient.sendMessage(component);
    }

    public void sendCenteredMessage(CommandSender recipient, String path, Var... vars) {
        List<String> configuredMessages = messagesFile.getStringList(path);
        if (!configuredMessages.isEmpty()) {
            configuredMessages.stream()
                .map(line -> Utils.format(line, vars))
                .forEach(message -> sendPossiblyCenteredMessage(recipient, message, vars));
            return;
        }

        String singleMessage = messagesFile.getString(path);
        if (singleMessage != null && !singleMessage.isEmpty()) {
            sendPossiblyCenteredMessage(recipient, Utils.format(singleMessage, vars), vars);
        }
    }

    public void sendCenteredMessage(CommandSender recipient, List<String> messages, Var... vars) {
        messages.forEach(message -> sendPossiblyCenteredMessage(recipient, message, vars));
    }

    private void sendPossiblyCenteredMessage(CommandSender recipient, String message, Var... vars) {
        if (message.startsWith(NO_CENTER_PREFIX)) {
            recipient.sendMessage(parseMessage(message.substring(NO_CENTER_PREFIX.length()), vars));
            return;
        }

        StringUtils.sendCenteredMessage(recipient, parseMessage(message, vars));
    }

    public void sendRawMessage(CommandSender recipient, String message, Var... vars) {
        recipient.sendMessage(parseMessage(message, vars));
    }

    public void sendActionBar(User user, String messageKey, Var... vars) {
        sendActionBar(user.getPlayer(), messageKey, vars);
    }

    public void sendActionBar(CommandSender recipient, String messageKey, Var... vars) {
        recipient.sendActionBar(getMessageComponent(messageKey, vars));
    }

    public void sendRawActionBar(CommandSender recipient, String message, Var... vars) {
        recipient.sendActionBar(parseMessage(message, vars));
    }

    public void sendTitle(User user, String titlePath, String subtitlePath, int stay, Var... vars) {
        sendTitle(user.getPlayer(), titlePath, subtitlePath, 20, stay, 20, vars);
    }

    public void sendTitle(CommandSender recipient, String titlePath, String subtitlePath, int fadeIn, int stay, int fadeOut, Var... vars) {
        Component title = getMessageComponent(titlePath, vars);
        Component subtitle = getMessageComponent(subtitlePath, vars);

        Title titleObj = Title.title(title, subtitle, fadeIn, stay, fadeOut);
        recipient.showTitle(titleObj);
    }

    public void sendRawTitle(CommandSender player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        Component titleComponent = parseMessage(title);
        Component subtitleComponent = parseMessage(subtitle);

        Title titleObj = Title.title(titleComponent, subtitleComponent, fadeIn, stay, fadeOut);
        player.showTitle(titleObj);
    }

    public Component getMessageComponent(String messageKey, Var... vars) {
        String configMessage = Utils.getString(messagesFile, messageKey);
        Component component = messages.computeIfAbsent(messageKey, (k) -> parseMessage(configMessage));

        return replaceVarsInComponent(component, vars);
    }

    public String getRawString(String path, Var... variables) {
        String string = messagesFile.getString(path, "");
        for (Var var : variables) {
            string = string.replace(var.name, String.valueOf(var.value));
        }

        return string;
    }

    public String message(String path, Var... variables) {
        return getRawString(path, variables);
    }

    public String message(String path, User user) {
        return getRawString(path, Var.ofPlayer(user));
    }

    public Component parseMessage(String message, Var... vars) {
        if (message == null) {
            return Component.empty();
        }

        return replaceVarsInComponent(miniMessage.deserialize(message), vars);
    }

    public List<Component> parseList(List<String> list, Var... vars) {
        return list.stream().map(message -> parseMessage(message, vars)).toList();
    }

    public Component replaceVarsInComponent(Component component, Var... vars) {
        for (Var var : vars) {
            Component replacementComponent = miniMessage.deserialize(var.value.toString());
            component = component.replaceText(builder ->
                builder
                    .matchLiteral(var.name)
                    .replacement(replacementComponent)
            );
        }

        return component;
    }

    public ConfigurationSection getConfigSection(String path) {
        return messagesFile.getConfigurationSection(path);
    }

    public List<String> getStringList(String path) {
        return messagesFile.getStringList(path);
    }

    public List<Component> getComponentList(String path) {
        return messagesFile.getStringList(path)
            .stream()
            .map(this::parseMessage)
            .toList();
    }
}
