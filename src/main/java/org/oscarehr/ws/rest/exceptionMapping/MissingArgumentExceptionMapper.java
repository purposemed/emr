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
package org.oscarehr.ws.rest.exceptionMapping;

import org.oscarehr.ws.rest.exception.MissingArgumentException;
import org.oscarehr.ws.rest.response.RestResponse;
import org.oscarehr.ws.rest.response.RestResponseMapError;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


@Provider
public class MissingArgumentExceptionMapper implements ExceptionMapper<MissingArgumentException>
{
	public MissingArgumentExceptionMapper()
	{
	}

	@Override
	public Response toResponse(MissingArgumentException exception)
	{
		RestResponseMapError responseError = new RestResponseMapError(exception.getMessage());
		responseError.setInfoMap(exception.getMissingArgumentMap());
		RestResponse<String> response = RestResponse.errorResponse(responseError);

		return Response.status(Response.Status.PRECONDITION_FAILED).entity(response)
				.type(MediaType.APPLICATION_JSON).build();
	}
}