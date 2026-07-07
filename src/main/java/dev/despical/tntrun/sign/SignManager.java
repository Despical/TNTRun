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

package dev.despical.tntrun.sign;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.commons.serializer.LocationSerializer;
import dev.despical.tntrun.TNTRun;
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.chat.ChatManager;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.GameState;
import dev.despical.tntrun.utils.Var;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;

import java.util.*;

/**
 * @author Despical
 * <p>
 * Created at 12.12.2025
 */
public class SignManager {

    private String fullGameState;
    private FileConfiguration config;
    private List<Component> signLines;
    private List<Component> inactiveSignLines;

    private final TNTRun plugin;
    private final ChatManager chatManager;
    private final Map<BlockKey, ArenaSign> signsByBlock;
    private final Map<Arena, Set<ArenaSign>> signsByArena;
    private final Map<GameState, String> gameStateToString;
    private ArenaSignEvents arenaSignEvents;

    public SignManager(TNTRun plugin) {
        this.plugin = plugin;
        this.chatManager = plugin.getChatManager();
        this.signsByBlock = new HashMap<>();
        this.signsByArena = new HashMap<>();
        this.signLines = List.of();
        this.inactiveSignLines = List.of();
        this.gameStateToString = new EnumMap<>(GameState.class);
        this.loadSigns();
    }

    private void loadSigns() {
        loadConfig();
        signsByBlock.clear();
        signsByArena.clear();

        for (GameState state : GameState.values()) {
            gameStateToString.put(state, getRawString("game-states." + state.getPath()));
        }

        fullGameState = getRawString("game-states.full-game");

        inactiveSignLines = signLines.stream()
            .map(this::formatInactiveArena)
            .toList();

        FileConfiguration config = plugin.getArenaRegistry().getConfig();
        boolean updateConfig = false;

        for (String arenaId : config.getKeys(false)) {
            Arena arena = plugin.getArenaRegistry().getArena(arenaId);
            List<String> locations = config.getStringList(arenaId + ".signs");
            Iterator<String> iterator = locations.iterator();

            while (iterator.hasNext()) {
                Block block = LocationSerializer.fromString(iterator.next()).getBlock();

                if (block.getState() instanceof Sign) {
                    if (arena != null) {
                        ArenaSign arenaSign = new ArenaSign(arena, block);
                        trackArenaSign(arenaSign);
                        updateSign(arenaSign);
                    }

                    continue;
                }

                iterator.remove();
                updateConfig = true;
            }

            if (updateConfig) {
                config.set(arenaId + ".signs", locations);
                updateConfig = false;
            }
        }

        refreshListenerRegistration();
    }

    public void reload() {
        this.loadSigns();
    }

    public void sendMessage(CommandSender recipient, String path, Var... vars) {
        recipient.sendMessage(getMessageComponent(path, vars));
    }

    public void updateSigns(Arena arena) {
        getSigns(arena).forEach(this::updateSign);
    }

    private void updateSign(ArenaSign arenaSign) {
        Sign sign = arenaSign.sign();
        Arena arena = arenaSign.arena();
        Game game = arena.getGame();
        int onlineCount = game == null ? 0 : game.getUsers().size();
        int maxPlayers = arena.getOption(ArenaKeys.MAX_PLAYERS);
        int minPlayers = arena.getOption(ArenaKeys.MIN_PLAYERS);
        String state = game == null
            ? gameStateToString.get(GameState.INACTIVE)
            : onlineCount >= maxPlayers ? fullGameState : gameStateToString.get(game.getState());

        SignSide side = sign.getSide(Side.FRONT);
        boolean changed = false;

        for (int i = 0; i < signLines.size(); i++) {
            Component line = game == null
                ? inactiveSignLines.get(i)
                : formatSign(signLines.get(i), state, onlineCount, maxPlayers, minPlayers);
            if (Objects.equals(side.line(i), line)) {
                continue;
            }

            side.line(i, line);
            changed = true;
        }

        if (!sign.isWaxed()) {
            sign.setWaxed(true);
            changed = true;
        }

        if (changed) {
            sign.update();
        }
    }

    public void addArenaSign(Arena arena, Block block) {
        ArenaSign arenaSign = new ArenaSign(arena, block);
        ArenaSign existing = getArenaSignByBlock(block);
        if (existing != null) {
            untrackArenaSign(existing);
        }

        trackArenaSign(arenaSign);
        refreshListenerRegistration();

        updateSign(arenaSign);
    }

    public void removeArenaSign(ArenaSign arenaSign) {
        if (arenaSign == null) {
            return;
        }

        untrackArenaSign(arenaSign);
        refreshListenerRegistration();
    }

    public void removeArenaSigns(Arena arena) {
        Set<ArenaSign> arenaSigns = signsByArena.remove(arena);
        if (arenaSigns == null || arenaSigns.isEmpty()) {
            refreshListenerRegistration();
            return;
        }

        for (ArenaSign arenaSign : arenaSigns) {
            signsByBlock.remove(BlockKey.of(arenaSign.block()));
        }

        refreshListenerRegistration();
    }

    public List<ArenaSign> getSigns(Arena arena) {
        Set<ArenaSign> arenaSigns = signsByArena.get(arena);
        return arenaSigns == null ? List.of() : List.copyOf(arenaSigns);
    }

    public Var[] getSignVars(Block block) {
        Location location = block.getLocation();
        Directional directional = (Directional) block.getBlockData();

        return new Var[]{
            Var.of("%x%", location.getBlockX()),
            Var.of("%y%", location.getBlockY()),
            Var.of("%z%", location.getBlockZ()),
            Var.of("%direction%", directional.getFacing()),
        };
    }

    public boolean isGameSign(Block block) {
        return getArenaSignByBlock(block) != null;
    }

    private Component formatSign(Component component, String state, int onlineCount, int maxPlayers, int minPlayers) {
        return chatManager.replaceVarsInComponent(component,
            Var.of("%state%", state),
            Var.of("%players%", onlineCount),
            Var.of("%max_players%", maxPlayers),
            Var.of("%min_players%", minPlayers)
        );
    }

    private Component formatInactiveArena(Component component) {
        Var[] vars = {
            Var.of("%min_players%", "0"),
            Var.of("%max_players%", "0"),
            Var.of("%players%", "0"),
            Var.of("%state%", gameStateToString.get(GameState.INACTIVE))
        };

        return chatManager.replaceVarsInComponent(component, vars);
    }

    ArenaSign getArenaSignByBlock(Block block) {
        BlockKey blockKey = BlockKey.of(block);
        if (blockKey == null) {
            return null;
        }

        ArenaSign arenaSign = signsByBlock.get(blockKey);
        if (arenaSign == null) {
            return null;
        }

        if (block.getState() instanceof Sign) {
            return arenaSign;
        }

        untrackArenaSign(arenaSign);
        refreshListenerRegistration();
        return null;
    }

    private void trackArenaSign(ArenaSign arenaSign) {
        signsByBlock.put(BlockKey.of(arenaSign.block()), arenaSign);
        signsByArena.computeIfAbsent(arenaSign.arena(), key -> new HashSet<>()).add(arenaSign);
    }

    private void untrackArenaSign(ArenaSign arenaSign) {
        signsByBlock.remove(BlockKey.of(arenaSign.block()));

        Set<ArenaSign> arenaSigns = signsByArena.get(arenaSign.arena());
        if (arenaSigns == null) {
            return;
        }

        arenaSigns.remove(arenaSign);
        if (arenaSigns.isEmpty()) {
            signsByArena.remove(arenaSign.arena());
        }
    }

    private void refreshListenerRegistration() {
        if (signsByBlock.isEmpty()) {
            unregisterArenaSignEvents();
            return;
        }

        registerArenaSignEvents();
    }

    private void registerArenaSignEvents() {
        if (arenaSignEvents != null) {
            return;
        }

        arenaSignEvents = new ArenaSignEvents(this);
    }

    private void unregisterArenaSignEvents() {
        if (arenaSignEvents == null) {
            return;
        }

        HandlerList.unregisterAll(arenaSignEvents);
        arenaSignEvents = null;
    }

    private void loadConfig() {
        config = ConfigUtils.getConfig(plugin, "signs");
        signLines = config.getStringList("lines")
            .stream()
            .map(chatManager::parseMessage)
            .toList();
    }

    private Component getMessageComponent(String path, Var... vars) {
        return chatManager.parseMessage(getRawString(path), vars);
    }

    private String getRawString(String path) {
        return config.getString(path, "");
    }

    private record BlockKey(UUID worldId, int x, int y, int z) {

        private static BlockKey of(Block block) {
            if (block == null) {
                return null;
            }

            return new BlockKey(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
        }
    }
}
