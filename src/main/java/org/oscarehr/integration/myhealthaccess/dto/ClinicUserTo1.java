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

package org.oscarehr.integration.myhealthaccess.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClinicUserTo1 implements Serializable
{

	@JsonProperty("id")
	private String myhealthaccessId;

	@JsonProperty("first_name")
	private String firstName;

	@JsonProperty("last_name")
	private String lastName;

	@JsonProperty("remote_id")
	private String remoteId;

	@JsonProperty("email")
	private String email;

	public ClinicUserTo1()
	{
	}

	public ClinicUserTo1(String remoteId, String email, String firstName, String lastName)
	{
		setRemoteId(remoteId);
		setEmail(email);
		setFirstName(firstName);
		setLastName(lastName);
	}

	public String getFirstName()
	{
		return firstName;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public String getLastName()
	{
		return lastName;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	public String getMyhealthaccessId()
	{
		return myhealthaccessId;
	}

	public void setMyhealthaccessId(String myhealthaccessId)
	{
		this.myhealthaccessId = myhealthaccessId;
	}

	public String getRemoteId()
	{
		return remoteId;
	}

	public void setRemoteId(String remoteId)
	{
		this.remoteId = remoteId;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}
}
