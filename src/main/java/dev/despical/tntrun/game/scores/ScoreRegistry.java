package dev.despical.tntrun.game.scores;

import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.user.User;

import java.util.*;

/**
 * @author Despical
 * <p>
 * Created at 3.01.2026
 */
public class ScoreRegistry {

    private UUID winner;
    private boolean dirtyCache = true;
    private Map<UUID, Integer> top3Cache;

    private final Game game;
    private final Map<UUID, Integer> scores;
    private final Map<UUID, String> scorerNames;

    public ScoreRegistry(Game game) {
        this.game = game;
        this.scores = new HashMap<>();
        this.scorerNames = new HashMap<>();
        this.top3Cache = new LinkedHashMap<>();
    }

    public void resetScores() {
        scores.clear();
        scorerNames.clear();
        top3Cache.clear();
        winner = null;

        game.getUsers().forEach(user -> {
            scores.put(user.getUUID(), 0);
            scorerNames.put(user.getUUID(), user.getName());
        });

        refreshTop3Cache();
    }

    public void calculateWinner() {
        this.winner = scores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    public void setWinner(User winner) {
        this.winner = winner == null ? null : winner.getUUID();
        this.dirtyCache = true;
    }

    public User getWinner() {
        return game.getUsers().stream()
            .filter(user -> user.getUUID().equals(winner))
            .findFirst()
            .orElse(null);
    }

    public void addScore(UUID uuid, int score) {
        this.scores.put(uuid, score);
        this.dirtyCache = true;
    }

    public void addScore(User user, int score) {
        this.scorerNames.put(user.getUUID(), user.getName());
        addScore(user.getUUID(), score);
    }

    public void removePlayer(UUID uuid) {
        scores.remove(uuid);
        dirtyCache = true;
    }

    public void increaseEveryone(Set<UUID> players) {
        players.forEach(uuid -> increaseFor(uuid, 1));
        dirtyCache = true;
    }

    public void rewardTopPlayers(List<UUID> players) {
        int score = 3;

        for (UUID uuid : players) {
            increaseFor(uuid, score--);
        }
    }

    private void increaseFor(UUID uuid, int score) {
        scores.merge(uuid, score, Integer::sum);

        dirtyCache = true;
    }

    private void refreshTop3Cache() {
        List<Map.Entry<UUID, Integer>> topEntries = new ArrayList<>(3);

        for (Map.Entry<UUID, Integer> entry : scores.entrySet()) {
            if (entry.getKey().equals(winner)) {
                continue;
            }

            int insertIndex = topEntries.size();
            for (int i = 0; i < topEntries.size(); i++) {
                if (entry.getValue() > topEntries.get(i).getValue()) {
                    insertIndex = i;
                    break;
                }
            }

            if (insertIndex == 3) {
                continue;
            }

            topEntries.add(insertIndex, Map.entry(entry.getKey(), entry.getValue()));
            if (topEntries.size() > 3) {
                topEntries.remove(3);
            }
        }

        LinkedHashMap<UUID, Integer> refreshedCache = new LinkedHashMap<>(topEntries.size());
        if (winner != null && scores.containsKey(winner)) {
            refreshedCache.put(winner, scores.get(winner));
        }

        for (Map.Entry<UUID, Integer> entry : topEntries) {
            if (refreshedCache.size() >= 3) {
                break;
            }

            refreshedCache.put(entry.getKey(), entry.getValue());
        }

        top3Cache = refreshedCache;
        dirtyCache = false;
    }

    public int getScore(UUID uuid) {
        return scores.getOrDefault(uuid, 0);
    }

    public Map<UUID, Integer> getTop3() {
        if (dirtyCache) {
            refreshTop3Cache();
        }

        return top3Cache;
    }

    public boolean isInTop3(UUID uuid) {
        if (dirtyCache) {
            refreshTop3Cache();
        }

        return top3Cache.containsKey(uuid);
    }

    public Optional<RecordScore> getHighestScore() {
        return scores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(entry -> new RecordScore(
                entry.getKey(),
                scorerNames.getOrDefault(entry.getKey(), "None"),
                entry.getValue()
            ));
    }

    public record RecordScore(UUID uuid, String playerName, int score) {
    }
}
