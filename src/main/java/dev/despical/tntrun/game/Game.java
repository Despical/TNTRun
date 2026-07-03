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

package dev.despical.tntrun.game;

import dev.despical.commons.serializer.InventorySerializer;
import dev.despical.commons.XPotion;
import dev.despical.commons.XSound;
import dev.despical.tntrun.Main;
import dev.despical.tntrun.api.event.game.GameStateChangeEvent;
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.bossbar.BossBarManager;
import dev.despical.tntrun.game.messages.MessageTicker;
import dev.despical.tntrun.game.messages.PlacementMessenger;
import dev.despical.tntrun.game.scores.ScoreRegistry;
import dev.despical.tntrun.game.states.*;
import dev.despical.tntrun.game.visibility.VisibilityManager;
import dev.despical.tntrun.option.IntOption;
import dev.despical.tntrun.scoreboard.ScoreboardManager;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.utils.ItemUtils;
import dev.despical.tntrun.utils.Var;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.NumberConversions;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
public class Game extends BukkitRunnable {

    private static final Main plugin = Main.getInstance();

    @Getter
    private int timer;
    private int tick;

    private boolean tickImmediately;

    private GameState gameState = GameState.INACTIVE;
    private GameStateHandler stateHandler;

    private final int period;

    private boolean stopped;

    private final @Getter Arena arena;
    private final @Getter VisibilityManager visibilityManager;
    private final @Getter ScoreboardManager scoreboardManager;
    private final @Getter ScoreRegistry scores;
    private final @Getter MessageTicker messageTicker;
    private final @Getter PlacementMessenger placementMessenger;
    private final @Getter BossBarManager bossBarManager;

    private final @Getter List<User> users;
    private final Map<GameState, GameStateHandler> states;

    public Game(Arena arena, int tickPeriod) {
        this.arena = arena;
        this.period = tickPeriod;
        this.visibilityManager = new VisibilityManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.messageTicker = plugin.getGameManager().getMessageTicker();
        this.placementMessenger = new PlacementMessenger(this);
        this.bossBarManager = new BossBarManager(this);
        this.users = new ArrayList<>();
        this.scores = new ScoreRegistry(this);
        this.states = Map.of(
            GameState.WAITING, new WaitingState(this),
            GameState.STARTING, new StartingState(this),
            GameState.IN_GAME, new InGameState(this),
            GameState.ENDING, new EndingState(this),
            GameState.RESTARTING, new RestartingState(this)
        );
    }

    @Override
    public void run() {
        boolean shouldRun = (tick += period) >= 20 || shouldTickImmediately();

        if (shouldRun) {
            tick = 0;

            stateHandler.tick();
            messageTicker.tick(this);
        }
    }

    private boolean shouldTickImmediately() {
        if (tickImmediately) {
            tickImmediately = false;
            return true;
        }

        return false;
    }

    public void tickImmediately() {
        tickImmediately = true;
    }

    public void broadcastMessage(String msgPath) {
        users.forEach(user -> user.sendMessage(msgPath));
    }

    public void broadcastMessage(String msgPath, Var... variables) {
        users.forEach(user -> user.sendMessage(msgPath, variables));
    }

    public void broadcastRawMessage(String message, Var... variables) {
        users.forEach(user -> plugin.getChatManager().sendRawMessage(user.getPlayer(), message, variables));
    }

    public void broadcastRawComponent(Component message, Var... variables) {
        users.forEach(user -> plugin.getChatManager().sendRawComponent(user.getPlayer(), message, variables));
    }

    public final void setTimer(int timer) {
        this.timer = timer;

        scoreboardManager.updateAllScoreboards();
    }

    public final void setTimer(IntOption option) {
        setTimer(option.value());
    }

    public void joinAsPlayer(User user) {
        join(user, users);
    }

    private void join(User user, List<User> list) {
        if (!isState(GameState.WAITING, GameState.STARTING)) {
            user.sendMessage("cannot-join-now");
            return;
        }

        user.resetTemporaryStats();

        list.add(user);
        states.get(gameState).join(user);

        scoreboardManager.updateAllScoreboards();

        plugin.getSignManager().updateSigns(arena);
    }

    public void leaveUser(User user) {
        this.leaveUser(user, player -> InventorySerializer.loadInventory(plugin, player));
    }

    public void quitUser(User user) {
        this.leaveUser(user, player -> {});
    }

    private void leaveUser(User user, Consumer<Player> playerConsumer) {
        users.remove(user);

        states.get(gameState).leave(user);
        scoreboardManager.updateAllScoreboards();

        plugin.getSignManager().updateSigns(arena);
        plugin.getDatabase().saveData(user);

        Player player = user.getPlayer();
        if (player != null && player.isOnline()) {
            player.teleport(arena.getOption(ArenaKeys.END_LOCATION));
            player.clearTitle();
            player.sendActionBar(Component.empty());
            player.setItemOnCursor(null);

            PlayerInventory inventory = player.getInventory();
            inventory.clear();
            inventory.setArmorContents(ItemUtils.EMPTY_ARMORS);

            playerConsumer.accept(player);

            visibilityManager.hidePlayerFromGame(player);
            visibilityManager.showPlayerOutsideTheGame(player);

            scoreboardManager.removeScoreboard(player);
            bossBarManager.removePlayer(player);
        }
    }

    public boolean isPlaying(User user) {
        return users.contains(user);
    }

    public boolean isPlaying(Player player) {
        UUID uuid = player.getUniqueId();
        return users.stream().anyMatch(user -> user.getUUID().equals(uuid));
    }

    public void setGameState(GameState newState) {
        if (this.gameState == newState) return;

        GameStateChangeEvent event = plugin.getEventManager().gameStateChange(this, this.gameState, newState);
        if (event.isCancelled()) return;

        this.gameState = newState;
        this.bossBarManager.update();

        Optional.ofNullable(plugin.getSignManager())
            .ifPresent(manager -> manager.updateSigns(arena));

        stateHandler = states.get(newState);
        stateHandler.firstTick();
    }

    public GameState getState() {
        return gameState;
    }

    public List<Player> getPlayers() {
        return users.stream().map(User::getPlayer).filter(Objects::nonNull).toList();
    }

    public Location getStartLocation() {
        return arena.getOption(ArenaKeys.START_LOCATION).clone();
    }

    public void startBlockRemoving() {
        int startBlockRemoving = plugin.getConfig().getInt("Time-Settings.Start-Block-Removing", 5);
        int blockRemoveDelay = plugin.getConfig().getInt("Time-Settings.Block-Remove-Delay", 12);
        List<String> removableBlocks = plugin.getConfig().getStringList("Whitelisted-Blocks");

        new BukkitRunnable() {

            @Override
            public void run() {
                if (!isState(GameState.IN_GAME)) {
                    cancel();
                    return;
                }

                if (getTimer() <= startBlockRemoving) {
                    return;
                }

                for (User user : getPlayersLeft()) {
                    for (Block block : getRemovableBlocks(user)) {
                        if (!removableBlocks.contains(block.getType().name())) {
                            continue;
                        }

                        arena.addDestroyedBlock(block.getState());
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> block.setType(Material.AIR), blockRemoveDelay);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public Set<User> getPlayersLeft() {
        return users.stream().filter(user -> !user.isSpectator()).collect(java.util.stream.Collectors.toSet());
    }

    public void addSpectator(User user) {
        Player player = user.getPlayer();
        if (player == null) {
            return;
        }

        int nightVision = user.getStatistic(dev.despical.tntrun.stats.Statistics.SPECTATOR_NIGHT_VISION_LEVEL);
        if (nightVision == 1) {
            player.addPotionEffect(XPotion.NIGHT_VISION.buildPotionEffect(Integer.MAX_VALUE, 1));
        }

        int level = user.getStatistic(dev.despical.tntrun.stats.Statistics.SPECTATOR_SPEED) + 1;
        player.setFlySpeed(.1F + level * .05F);
        player.addPotionEffect(XPotion.SPEED.buildPotionEffect(Integer.MAX_VALUE, level));
    }

    public void hideSpectator(User user) {
        if (!user.isSpectator()) {
            return;
        }

        Player player = user.getPlayer();
        if (player == null) {
            return;
        }

        for (User otherUser : users) {
            Player otherPlayer = otherUser.getPlayer();
            if (otherPlayer == null) {
                continue;
            }

            otherPlayer.showPlayer(plugin, player);

            if (otherUser.isSpectator()) {
                player.showPlayer(plugin, otherPlayer);
            } else {
                otherPlayer.hidePlayer(plugin, player);
            }
        }
    }

    public void playSound(XSound sound) {
        getPlayers().forEach(sound::play);
    }

    private List<Block> getRemovableBlocks(User user) {
        List<Block> removableBlocks = new ArrayList<>();

        Player player = user.getPlayer();
        if (player == null) {
            return removableBlocks;
        }

        Location location = player.getLocation();
        int scanDepth = plugin.getConfig().getInt(player.isOnGround() ? "Scanning-Depth.On-Ground" : "Scanning-Depth.In-Air", player.isOnGround() ? 2 : 6);
        int y = location.getBlockY();

        for (int i = 0; i <= scanDepth; i++) {
            Block block = getBlockUnderPlayer(y--, location);

            if (block != null) {
                removableBlocks.add(block);
            }
        }

        return removableBlocks;
    }

    private Block getBlockUnderPlayer(int y, Location location) {
        Position loc = new Position(location.getX(), y, location.getZ());
        Block b1 = loc.getBlock(location.getWorld(), 0.3, -0.3);

        if (b1.getType() != Material.AIR) {
            return b1;
        }

        Block b2 = loc.getBlock(location.getWorld(), -0.3, 0.3);

        if (b2.getType() != Material.AIR) {
            return b2;
        }

        Block b3 = loc.getBlock(location.getWorld(), 0.3, 0.3);

        if (b3.getType() != Material.AIR) {
            return b3;
        }

        Block b4 = loc.getBlock(location.getWorld(), -0.3, -0.3);

        if (b4.getType() != Material.AIR) {
            return b4;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends GameStateHandler> T getHandler(GameState gameState) {
        return (T) states.get(gameState);
    }

    public boolean isState(GameState gameState, GameState... states) {
        if (this.gameState == gameState) return true;

        for (GameState state : states) {
            if (this.gameState == state) return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return "Game[state=%s, players=%d]".formatted(gameState, users.size());
    }

    private record Position(double x, int y, double z) {

        public Block getBlock(World world, double addx, double addz) {
            return world.getBlockAt(NumberConversions.floor(x + addx), y, NumberConversions.floor(z + addz));
        }
    }
}

