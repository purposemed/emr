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
package org.oscarehr.ws.rest;

import org.oscarehr.fax.schedulingTasks.InboundFaxSchedulingTask;
import org.oscarehr.managers.SecurityInfoManager;
import org.oscarehr.ws.rest.response.RestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.LocalDateTime;

@Path("/faxInbound")
@Component("FaxInboundWebService")
public class FaxInboundWebService extends AbstractServiceImpl
{
	@Autowired
	private SecurityInfoManager securityInfoManager;

	@Autowired
	private InboundFaxSchedulingTask inboundFaxSchedulingTask;

	@GET
	@Path("/getNextPullTime")
	@Produces(MediaType.APPLICATION_JSON)
	public RestResponse<LocalDateTime> getNextPullTime()
	{
		String loggedInProviderNo = getLoggedInInfo().getLoggedInProviderNo();
		securityInfoManager.requireOnePrivilege(loggedInProviderNo, SecurityInfoManager.READ, null, "_admin", "_admin.fax");

		return RestResponse.successResponse(inboundFaxSchedulingTask.getNextRunTime());
	}

}
