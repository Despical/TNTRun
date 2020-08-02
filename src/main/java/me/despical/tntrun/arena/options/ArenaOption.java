package me.despical.tntrun.arena.options;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public enum ArenaOption {
	
	/**
	 * Current arena timer, ex. 30 seconds before game starts.
	 */
	TIMER(0),
	/**
	 * Minimum players in arena needed to start.
	 */
	MINIMUM_PLAYERS(2),
	/**
	 * Maximum players arena can hold, users with full games permission can bypass
	 * this!
	 */
	MAXIMUM_PLAYERS(12);

	private final int defaultValue;

	ArenaOption(int defaultValue) {
		this.defaultValue = defaultValue;
	}

	public int getDefaultValue() {
		return defaultValue;
	}
}