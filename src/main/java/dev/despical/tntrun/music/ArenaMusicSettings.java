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

package dev.despical.tntrun.music;

import dev.despical.tntrun.arena.Arena;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Typed view over the music related options of an {@link Arena}.
 * <p>
 * The view hides the raw option keys, so the editor menu and the radio can
 * read and change songs by {@link MusicPhase} without knowing how they are stored.
 *
 * @author Despical
 * <p>
 * Created at 31.10.2026
 */
@RequiredArgsConstructor(staticName = "of")
public final class ArenaMusicSettings {

    private final Arena arena;

    /**
     * Gets the song configured for a phase.
     *
     * @param phase the phase to look up
     * @return the song name, or an empty optional when the phase is silent
     */
    public Optional<String> getSong(MusicPhase phase) {
        String song = arena.getOption(phase.getSongOption());

        return song == null || song.isBlank() ? Optional.empty() : Optional.of(song);
    }

    /**
     * Sets the song for a phase.
     *
     * @param phase the phase to change
     * @param song  the song name, or {@code null} to make the phase silent
     */
    public void setSong(MusicPhase phase, @Nullable String song) {
        arena.setOption(phase.getSongOption(), song == null || song.isBlank() ? null : song);
    }

    /**
     * Checks whether an identical song should continue into the given phase.
     *
     * @param phase the phase that is about to start
     * @return {@code true} if the song keeps playing, always {@code false} for phases without a predecessor
     */
    public boolean isContinuing(MusicPhase phase) {
        return phase.hasContinueOption() && arena.getOption(phase.getContinueOption());
    }

    /**
     * Flips whether an identical song continues into the given phase.
     *
     * @param phase the phase to change
     * @return the new value, or {@code false} if the phase has no continue option
     */
    public boolean toggleContinuing(MusicPhase phase) {
        if (!phase.hasContinueOption()) {
            return false;
        }

        boolean newValue = !isContinuing(phase);
        arena.setOption(phase.getContinueOption(), newValue);

        return newValue;
    }
}
