package me.despical.tntrun.handlers.rewards;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class Reward {

	private RewardType type;
	private RewardExecutor executor;
	private String executableCode;
	private double chance;

	public Reward(RewardType type, String rawCode) {
		this.type = type;
		String processedCode = rawCode;

		if (rawCode.contains("p:")) {
			this.executor = RewardExecutor.PLAYER;
			processedCode = StringUtils.replace(processedCode, "p:", "");
		} else if (rawCode.contains("script:")) {
			this.executor = RewardExecutor.SCRIPT;
			processedCode = StringUtils.replace(processedCode, "script:", "");
		} else {
			this.executor = RewardExecutor.CONSOLE;
		}
		if (processedCode.contains("chance(")) {
			int loc = processedCode.indexOf(")");
			if (loc == -1) {
				Bukkit.getLogger().warning("rewards.yml configuration is broken! Make sure you don't forget using ')' character in chance condition! Command: " + rawCode);
				this.chance = 0.0;
				return;
			}
			String chanceStr = processedCode;
			chanceStr = chanceStr.substring(0, loc).replaceAll("[^0-9]+", "");
			double chance = Double.parseDouble(chanceStr);
			processedCode = StringUtils.replace(processedCode, "chance(" + chanceStr + "):", "");
			this.chance = chance;
		} else {
			this.chance = 100.0;
		}
		this.executableCode = processedCode;
	}

	public RewardExecutor getExecutor() {
		return executor;
	}

	public String getExecutableCode() {
		return executableCode;
	}

	public double getChance() {
		return chance;
	}

	public RewardType getType() {
		return type;
	}

	public enum RewardType {
		END_GAME("endgame"), LOSE("lose"), WIN("win");

		private String path;

		RewardType(String path) {
			this.path = path;
		}

		public String getPath() {
			return path;
		}
	}

	public enum RewardExecutor {
		CONSOLE, PLAYER, SCRIPT
	}
}