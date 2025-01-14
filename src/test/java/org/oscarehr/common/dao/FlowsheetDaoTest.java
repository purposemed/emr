/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
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
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */
package org.oscarehr.common.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.oscarehr.common.dao.utils.EntityDataGenerator;
import org.oscarehr.common.dao.utils.SchemaUtils;
import org.oscarehr.common.model.Flowsheet;
import org.oscarehr.util.SpringUtils;

public class FlowsheetDaoTest extends DaoTestFixtures
{

	protected FlowsheetDao dao = SpringUtils.getBean(FlowsheetDao.class);

	@Before
	public void before() throws Exception {
		SchemaUtils.restoreTable("Flowsheet");
	}

        @Test
        public void testCreate() throws Exception {
                Flowsheet entity = new Flowsheet();
                EntityDataGenerator.generateTestDataForModelClass(entity);
                dao.persist(entity);
                assertNotNull(entity.getId());
        }

	@Test
	public void testFindAll() throws Exception {
		
		Flowsheet flowSheet1 = new Flowsheet();
		EntityDataGenerator.generateTestDataForModelClass(flowSheet1);
		dao.persist(flowSheet1);
		
		Flowsheet flowSheet2 = new Flowsheet();
		EntityDataGenerator.generateTestDataForModelClass(flowSheet2);
		dao.persist(flowSheet2);
		
		Flowsheet flowSheet3 = new Flowsheet();
		EntityDataGenerator.generateTestDataForModelClass(flowSheet3);
		dao.persist(flowSheet3);
		
		Flowsheet flowSheet4 = new Flowsheet();
		EntityDataGenerator.generateTestDataForModelClass(flowSheet4);
		dao.persist(flowSheet4);
		
		List<Flowsheet> result = dao.findAll();

		assertTrue(result.contains(flowSheet1));
		assertTrue(result.contains(flowSheet2));
		assertTrue(result.contains(flowSheet3));
		assertTrue(result.contains(flowSheet4));
	}
	
	@Test
	public void testFindByName() throws Exception {
		
		String name1 = "alpha";
		String name2 = "bravo";
		String name3 = "charlie";
		
		Flowsheet flowSheet1 = new Flowsheet();
		EntityDataGenerator.generateTestDataForModelClass(flowSheet1);
		flowSheet1.setName(name1);
		dao.persist(flowSheet1);
		
		Flowsheet flowSheet2 = new Flowsheet();
		EntityDataGenerator.generateTestDataForModelClass(flowSheet2);
		flowSheet2.setName(name2);
		dao.persist(flowSheet2);
		
		Flowsheet flowSheet3 = new Flowsheet();
		EntityDataGenerator.generateTestDataForModelClass(flowSheet3);
		flowSheet3.setName(name3);
		dao.persist(flowSheet3);
		
		Flowsheet expectedResult = flowSheet2;
		Flowsheet result = dao.findByName(name2);
		
		assertEquals(expectedResult, result);
	}
}
