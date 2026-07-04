package dev.despical.tntrun.sound;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Despical
 * <p>
 * Created at 03.07.2026
 */
@Getter
@AllArgsConstructor
public enum GameSound {

    DOUBLE_JUMP("double-jump"),
    GAME_START("game-start");

    private final String path;
}
