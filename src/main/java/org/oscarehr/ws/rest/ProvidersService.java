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

package org.oscarehr.ws.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.log4j.Logger;
import org.oscarehr.PMmodule.dao.ProviderDao;
import org.oscarehr.common.dao.SecRoleDao;
import org.oscarehr.common.model.Provider;
import org.oscarehr.common.model.SecRole;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.ws.rest.conversion.ProviderConverter;
import org.oscarehr.ws.rest.response.RestResponse;
import org.oscarehr.ws.rest.response.RestSearchResponse;
import org.oscarehr.ws.rest.to.model.ProviderTo1;
import org.oscarehr.ws.rest.to.model.UserRoleTo1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;


@Path("/providers")
@Component("providersService")
@Tag(name = "providersService")
public class ProvidersService extends AbstractServiceImpl
{
	private static Logger logger = MiscUtils.getLogger();

	@Autowired
	private ProviderDao providerDao;

	@Autowired
	private SecRoleDao secRoleDao;

	private ProviderConverter providerConverter = new ProviderConverter();

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public RestResponse<ProviderTo1> search(@QueryParam("searchText") String searchText,
	                                                @QueryParam("searchMode") @DefaultValue("NAME") String searchMode,
	                                                @QueryParam("page") @DefaultValue("1") Integer page,
	                                                @QueryParam("perPage") @DefaultValue("10") Integer perPage)
	{
		try
		{
			//TODO - standardized provider search
			return RestResponse.errorResponse("Error - not yet implemented");
		}
		catch (Exception e)
		{
			logger.error("Error", e);
		}
		return RestResponse.errorResponse("Error");
	}

	@GET
	@Path("/bySecurityRole")
	@Produces(MediaType.APPLICATION_JSON)
	public RestResponse<List<ProviderTo1>> getBySecurityRole(@QueryParam("role") String role)
	{
		try
		{
			List<Provider> providers = providerDao.getActiveProvidersByRole(role);
			List<ProviderTo1> providersTo1 = providerConverter.getAllAsTransferObjects(getLoggedInInfo(), providers);
			return RestResponse.successResponse(providersTo1);
		}
		catch (Exception e)
		{
			logger.error("Error", e);
		}
		return RestResponse.errorResponse("Error");
	}

	@GET
	@Path("/byType")
	@Produces(MediaType.APPLICATION_JSON)
	public RestResponse<List<ProviderTo1>> getByType(@QueryParam("type") String type)
	{
		try
		{
			List<Provider> providers = providerDao.getActiveProvidersByType(type);
			List<ProviderTo1> providersTo1 = providerConverter.getAllAsTransferObjects(getLoggedInInfo(), providers);
			return RestResponse.successResponse(providersTo1);
		}
		catch (Exception e)
		{
			logger.error("Error", e);
		}
		return RestResponse.errorResponse("Error");
	}

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public RestSearchResponse<ProviderTo1> getAll()
	{
		List<Provider> providers = providerDao.getProviders();
		List<ProviderTo1> providersTo1 = providerConverter.getAllAsTransferObjects(getLoggedInInfo(), providers);
		return RestSearchResponse.successResponseOnePage(providersTo1);
	}

	@GET
	@Path("/providerRoles")
	@Produces(MediaType.APPLICATION_JSON)
	public RestResponse<List<UserRoleTo1>> getProviderRoles()
	{
		ArrayList<UserRoleTo1> userRoles = new ArrayList<>();
		List<SecRole> secRoles = secRoleDao.findAll();
		for (SecRole secRole : secRoles)
		{
			userRoles.add(new UserRoleTo1(secRole.getName(), secRole.getId()));
		}

		return RestResponse.successResponse(userRoles);
	}

	@GET
	@Path("/active")
	@Produces(MediaType.APPLICATION_JSON)
	public RestSearchResponse<ProviderTo1> getActive()
	{
		List<Provider> providers = providerDao.getActiveProviders();
		List<ProviderTo1> providersTo1 = providerConverter.getAllAsTransferObjects(getLoggedInInfo(), providers);
		return RestSearchResponse.successResponseOnePage(providersTo1);
	}
}
