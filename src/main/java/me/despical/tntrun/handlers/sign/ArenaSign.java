package me.despical.tntrun.handlers.sign;

import me.despical.commons.ReflectionUtils;
import me.despical.tntrun.arena.Arena;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Despical
 * <p>
 * Created at 26.06.2023
 */
public class ArenaSign {

	private Block behind;
	private final Sign sign;
	private final Arena arena;

	public ArenaSign(Sign sign, Arena arena) {
		this.sign = sign;
		this.arena = arena;

		setBehindBlock();
	}

	private void setBehindBlock() {
		if (sign.getBlock().getType().name().equals("WALL_SIGN")) {
			this.behind = ReflectionUtils.supports(14) ? getBlockBehind() : getBlockBehindLegacy();
		}
	}

	private Block getBlockBehind() {
		try {
			Object blockData = sign.getBlock().getState().getClass().getMethod("getBlockData").invoke(sign.getBlock().getState());
			BlockFace face = (BlockFace) blockData.getClass().getMethod("getFacing").invoke(blockData);
			Location loc = sign.getLocation(), location = new Location(sign.getWorld(), loc.getBlockX() - face.getModX(), loc.getBlockY() - face.getModY(), loc.getBlockZ() - face.getModZ());
			return location.getBlock();
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}

	private Block getBlockBehindLegacy() {
		return sign.getBlock().getRelative(((org.bukkit.material.Sign) sign.getData()).getAttachedFace());
	}

	public Sign getSign() {
		return sign;
	}

	public Block getBehind() {
		return behind;
	}

	public Arena getArena() {
		return arena;
	}
}