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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import oscar.util.Jackson.ZonedDateTimeStringDeserializer;
import oscar.util.Jackson.ZonedDateTimeStringSerializer;

import java.io.Serializable;
import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class AppointmentCacheTo1 implements Serializable
{
	@JsonProperty("remote_id")
	private String id;

	@JsonProperty("is_virtual")
	private Boolean isVirtual;

	@JsonProperty("is_cancelled")
	private Boolean isCancelled;

	@JsonProperty("provider_no")
	private String providerNo;

	@JsonProperty("demographic_no")
	private String demographicNo;

	@JsonProperty("start_datetime")
	@JsonSerialize(using = ZonedDateTimeStringSerializer.class)
	@JsonDeserialize(using = ZonedDateTimeStringDeserializer.class)
	private ZonedDateTime startDateTime;

	@JsonProperty("end_datetime")
	@JsonSerialize(using = ZonedDateTimeStringSerializer.class)
	@JsonDeserialize(using = ZonedDateTimeStringDeserializer.class)
	private ZonedDateTime endDateTime;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public Boolean getVirtual()
	{
		return isVirtual;
	}

	public void setVirtual(Boolean virtual)
	{
		isVirtual = virtual;
	}

	public Boolean getCancelled()
	{
		return isCancelled;
	}

	public void setCancelled(Boolean canceled)
	{
		isCancelled = canceled;
	}

	public ZonedDateTime getStartDateTime()
	{
		return startDateTime;
	}

	public void setStartDateTime(ZonedDateTime startDateTime)
	{
		this.startDateTime = startDateTime;
	}

	public ZonedDateTime getEndDateTime()
	{
		return endDateTime;
	}

	public void setEndDateTime(ZonedDateTime endDateTime)
	{
		this.endDateTime = endDateTime;
	}

	public String getProviderNo()
	{
		return providerNo;
	}

	public void setProviderNo(String providerNo)
	{
		this.providerNo = providerNo;
	}

	public String getDemographicNo()
	{
		return demographicNo;
	}

	public void setDemographicNo(String demographicNo)
	{
		this.demographicNo = demographicNo;
	}

	@Override
	public String toString()
	{
		return new ReflectionToStringBuilder(this).toString();
	}
}
