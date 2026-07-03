package dev.despical.tntrun.game.messages;

import dev.despical.commons.number.NumberUtils;
import dev.despical.tntrun.Main;
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

    private static final Main PLUGIN = Main.getInstance();
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
