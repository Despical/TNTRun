/*
 * TNT Run - A Minecraft parkour minigame
 * Copyright (C) 2024  Berke Akçen
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

package dev.despical.tntrun.command;

import dev.despical.commandframework.CommandArguments;
import dev.despical.commandframework.Message;
import dev.despical.commandframework.annotations.Command;
import dev.despical.commons.util.Strings;
import dev.despical.tntrun.Main;
import dev.despical.tntrun.handlers.ChatManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.BiFunction;
import java.util.stream.Stream;

public abstract class AbstractCommand {

    protected static final Main plugin = JavaPlugin.getPlugin(Main.class);
    protected static final ChatManager chatManager = plugin.getChatManager();

    static {
        var commandFramework = plugin.getCommandFramework();
        commandFramework.addCustomParameter("User", args -> plugin.getUserManager().getUser(args.getSender()));

        Message.setColorFormatter(Strings::format);

        BiFunction<Command, CommandArguments, Boolean> sendUsage = (command, arguments) -> {
            arguments.sendMessage(chatManager.message("admin-commands.correct-usage").replace("%usage%", command.usage()));
            return true;
        };

        Stream.of(Message.SHORT_ARG_SIZE, Message.LONG_ARG_SIZE).forEach(message -> message.setMessage(sendUsage));
    }

    public AbstractCommand() {
        plugin.getCommandFramework().registerCommands(this);
    }
}
