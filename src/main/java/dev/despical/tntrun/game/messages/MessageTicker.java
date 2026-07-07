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

package dev.despical.tntrun.game.messages;

import dev.despical.commons.number.NumberUtils;
import dev.despical.tntrun.TNTRun;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.GameState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Despical
 * <p>
 * Created at 12.12.2025
 */
public class MessageTicker {

    private static final TNTRun PLUGIN = TNTRun.getInstance();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final Map<String, TickerMessage> gameOverMessages;
    private final Map<GameState, Map<Integer, List<TickerAction>>> messages;

    public MessageTicker() {
        this.messages = new EnumMap<>(GameState.class);
        this.gameOverMessages = new HashMap<>();
        this.initializeMessages();
    }

    public void tick(Game game) {
        var states = messages.get(game.getState());

        if (states == null) {
            return;
        }

        var actions = states.get(game.getTimer());

        if (actions == null) {
            return;
        }

        actions.forEach(action ->
            action.type().execute(game, action)
        );
    }

    @NotNull
    public TickerMessage getGameOverMessage(String message) {
        return gameOverMessages.get(message);
    }

    private void initializeMessages() {
        for (GameState state : GameState.values()) {
            ConfigurationSection section = PLUGIN.getChatManager().getConfigSection("ticker." + state.getPath());
            if (section == null) continue;

            Map<Integer, List<TickerAction>> tickMap = new HashMap<>();
            for (String key : section.getKeys(false)) {
                int time = NumberUtils.getInt(key);
                List<String> rawList = section.getStringList(key);

                if (!rawList.isEmpty()) {
                    tickMap.put(time, parseActions(rawList));
                }
            }

            messages.put(state, tickMap);
        }

        this.initializeRankMessages("game-over", gameOverMessages);
    }

    private List<TickerAction> parseActions(List<String> rawList) {
        List<TickerAction> actions = new ArrayList<>();
        Component title = null, subtitle = null;

        for (String line : rawList) {
            if (line.startsWith("title:")) {
                title = MINI_MESSAGE.deserialize(line.substring(6));
            } else if (line.startsWith("subtitle:")) {
                subtitle = MINI_MESSAGE.deserialize(line.substring(9));
            } else if (line.startsWith("sound:")) {
                actions.add(TickerAction.sound(line.substring(6)));
            } else {
                actions.add(TickerAction.chat(MINI_MESSAGE.deserialize(line)));
            }
        }

        if (title != null || subtitle != null) {
            actions.add(TickerAction.title(title, subtitle));
        }

        return actions;
    }

    private void initializeRankMessages(String sectionName, Map<String, TickerMessage> messageMap) {
        ConfigurationSection placement = PLUGIN.getChatManager().getConfigSection(sectionName);

        for (String key : placement.getKeys(false)) {
            String message = placement.getString(key + ".message");
            String title = placement.getString(key + ".title");
            String subtitle = placement.getString(key + ".subtitle");

            Component messageComponent = MINI_MESSAGE.deserialize(message);
            Component titleComponent = MINI_MESSAGE.deserialize(title);
            Component subtitleComponent = MINI_MESSAGE.deserialize(subtitle);

            messageMap.put(key, new TickerMessage(messageComponent, titleComponent, subtitleComponent));
        }
    }
}
