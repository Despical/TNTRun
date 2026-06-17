package dev.despical.tntrun.sign;

import dev.despical.tntrun.arena.Arena;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

/**
 * @author Despical
 * <p>
 * Created at 12.12.2025
 */
public record ArenaSign(Arena arena, Block block) {

    public Sign sign() {
        return (Sign) block.getState();
    }
}
