package me.despical.tntrun.utils;

import org.junit.Test;

import junit.framework.Assert;
import me.despical.tntrun.utils.Utils;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class UtilsTest {

	@Test
	public void serializeInt() {
		Assert.assertEquals(9, Utils.serializeInt(3));
		Assert.assertEquals(45, Utils.serializeInt(37));
		Assert.assertEquals(45, Utils.serializeInt(43));
	}
}