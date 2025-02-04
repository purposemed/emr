/**
 * Copyright (c) 2012-2018. CloudPractice Inc. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * This software was written for
 * CloudPractice Inc.
 * Victoria, British Columbia
 * Canada
 */

package integration.tests;

import integration.tests.util.SeleniumTestBase;
import integration.tests.util.junoUtil.DatabaseUtil;
import integration.tests.util.junoUtil.Navigation;
import integration.tests.util.seleniumUtil.PageUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.oscarehr.common.dao.utils.SchemaUtils;

import java.sql.SQLException;

import static integration.tests.util.junoUtil.Navigation.Consultation_URL;
import static integration.tests.util.junoUtil.Navigation.ECHART_URL;
import static integration.tests.util.seleniumUtil.ActionUtil.dropdownSelectByVisibleText;

public class AddConsultationsClassicUITests extends SeleniumTestBase
{
	@BeforeClass
	public static void setup()
	{
		loadSpringBeans();
		DatabaseUtil.createTestDemographic();
	}

	@AfterClass
	public static void cleanup()
			throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException
	{
		SchemaUtils.restoreTable(
				"admission", "consultationRequestExt", "consultationRequests", "demographic", "log",
				"measurementType", "program", "validations"
				);
	}

	@Test
	public void addConsultationsClassicUITest()
			throws InterruptedException
	{
		String serviceName = "Cardiology";
		driver.get(Navigation.OSCAR_URL + ECHART_URL);
		Thread.sleep(5000);
		String eChartWindowHandle = driver.getWindowHandle();
		driver.findElement(By.xpath("//div[@id='menuTitleconsultation']//descendant::a[contains(., '+')]")).click();
		Thread.sleep(5000);
		PageUtil.switchToLastWindow(driver);
		Thread.sleep(2000);
		dropdownSelectByVisibleText(driver, By.id("service"), serviceName);
		driver.findElement(By.xpath("//input[@name='submitSaveOnly']")).click();

		//** Verify under Consultations section on eChart page. **
		PageUtil.switchToWindow(eChartWindowHandle, driver);
		driver.navigate().refresh();
		Thread.sleep(2000);
		Assert.assertTrue(serviceName + " is NOT added under Consultations successfully.",
				PageUtil.isExistsBy(By.linkText(serviceName), driver));

		//** Verify from Consultations top menu. **
		driver.get(Navigation.OSCAR_URL + Consultation_URL);
		Thread.sleep(2000);
		PageUtil.switchToLastWindow(driver);
		Assert.assertTrue(serviceName + " is NOT added under Consultations successfully.",
				PageUtil.isExistsBy(By.linkText(serviceName), driver));
	}

}

