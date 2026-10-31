/**
 * Provides the arena music system of TNT Run.
 * <p>
 * Every arena can play a different song during each {@link
 * dev.despical.tntrun.music.MusicPhase}: while players wait in the lobby, while
 * the round is running and while the end-of-round summary is shown. The
 * per-arena choices are exposed through {@link
 * dev.despical.tntrun.music.ArenaMusicSettings}, and playback is delegated to
 * a {@link dev.despical.tntrun.music.Radio}. Game code only depends on the
 * abstractions in this package, so the server keeps working when the optional
 * NoteBlockAPI plugin is not installed.
 */
package dev.despical.tntrun.music;
