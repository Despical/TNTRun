/**
 * Provides the {@link dev.despical.tntrun.music.Radio} implementations.
 * <p>
 * {@link dev.despical.tntrun.music.impl.NBAPIRadio} plays {@code .nbs} files
 * through NoteBlockAPI, while {@link dev.despical.tntrun.music.impl.EmptyRadio}
 * is a silent stand-in used when NoteBlockAPI is missing. Only the classes in
 * this package reference NoteBlockAPI types, so they must never be loaded
 * unless the plugin is present.
 */
package dev.despical.tntrun.music.impl;
