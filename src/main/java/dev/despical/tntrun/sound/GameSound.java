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

    DOUBLE_JUMP("double-jump");

    private final String path;
}
