package me.despical.tntrun.utils;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class UtilsTest {

	@Test
	public void serializeInt() {
		assertEquals(9, Utils.serializeInt(3));
		assertEquals(45, Utils.serializeInt(37));
		assertEquals(45, Utils.serializeInt(43));
	}
}