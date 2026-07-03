package dev.despical.tntrun.sound;

/**
 * @author Despical
 * <p>
 * Created at 03.07.2026
 */
public enum GameSound {

    DOUBLE_JUMP("double-jump");

    private final String path;

    GameSound(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
