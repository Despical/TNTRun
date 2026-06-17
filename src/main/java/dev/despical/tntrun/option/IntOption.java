package dev.despical.tntrun.option;

/**
 * @author Despical
 * <p>
 * Created at 12.12.2025
 */
public enum IntOption implements ConfigOption<Integer> {

    ARENA_TICK_PERIOD("arena-settings.tick-period", 5),
    LOBBY_WAITING_TIME("time-settings.waiting-time", 300),
    PRE_GAME_WAITING_TIME("time-settings.pre-game-waiting-time", 120),
    GAME_STARTING_TIME("time-settings.game-starting-time", 30),
    FULL_GAME_STARTING_TIME("time-settings.full-game-starting-time", 15),
    ENDING_TIME("time-settings.ending-time", 10);

    private final String path;
    private final int defaultValue;

    IntOption(String path, int defaultValue) {
        this.path = path;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Class<Integer> getType() {
        return Integer.class;
    }

    @Override
    public Integer getDefaultValue() {
        return defaultValue;
    }
}
