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
package org.oscarehr.integration.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.cxf.jaxrs.utils.ExceptionUtils;
import org.hibernate.annotations.Where;
import org.oscarehr.common.model.AbstractModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name="integration_push_update")
@Where(clause="deleted_at IS NULL")
public class IntegrationPushUpdate extends AbstractModel<Integer>
{
	public enum PUSH_STATUS
	{
		QUEUED,
		SENT,
		ERROR,
	}

	public enum UPDATE_TYPE
	{
		APPOINTMENT_CACHE,
		PATIENT_CONNECTION,
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@Column(name = "update_type")
	@Enumerated(EnumType.STRING)
	private UPDATE_TYPE updateType;

	@Column(name = "integration_type")
	private String integrationType;

	@Column(name = "integration_id")
	private Integer integrationId;

	@Column(name = "security_no")
	private Integer securityNo;

	@Column(name = "created_at")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;

	@Column(name = "updated_at")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;

	@Column(name = "deleted_at")
	@Temporal(TemporalType.TIMESTAMP)
	private Date deletedAt;

	@Column(name = "sent_at")
	@Temporal(TemporalType.TIMESTAMP)
	private Date sentAt;

	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	private PUSH_STATUS status = PUSH_STATUS.QUEUED;

	@Column(name = "send_count")
	private Integer sendCount = 0;

	@Column(name = "json_data")
	private String jsonData;

	@Getter @Setter
	@Column(name = "error_message")
	private String errorMessage;

	// The identifier of the target of the update.
	// Can be NULL. This identifier, if not NULL
	// is used to insure in-order delivery of updates
	@Getter
	@Setter
	@Column(name = "target_id")
	private String targetId;

	/**
	 * set this record to an error state
	 * @param errorMessage - error message describing why the error occurred
	 */
	public void recordError(String errorMessage)
	{
		this.setStatus(PUSH_STATUS.ERROR);
		this.setErrorMessage(errorMessage);
	}

	/**
	 * set this record to an error state
	 * @param e - the exception that caused the error status
	 */
	public void recordError(Exception e)
	{
		this.setStatus(PUSH_STATUS.ERROR);
		this.setErrorMessage(ExceptionUtils.getStackTrace(e));
	}

	@Override
	public Integer getId()
	{
		return id;
	}

	public UPDATE_TYPE getUpdateType()
	{
		return updateType;
	}

	public void setUpdateType(UPDATE_TYPE updateType)
	{
		this.updateType = updateType;
	}

	public String getIntegrationType()
	{
		return integrationType;
	}

	public void setIntegrationType(String integrationType)
	{
		this.integrationType = integrationType;
	}

	public Integer getIntegrationId()
	{
		return integrationId;
	}

	public void setIntegrationId(Integer integrationId)
	{
		this.integrationId = integrationId;
	}

	public Integer getSecurityNo()
	{
		return securityNo;
	}

	public void setSecurityNo(Integer securityNo)
	{
		this.securityNo = securityNo;
	}

	public Date getCreatedAt()
	{
		return createdAt;
	}

	public void setCreatedAt(Date createdAt)
	{
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt()
	{
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt)
	{
		this.updatedAt = updatedAt;
	}

	public Date getDeletedAt()
	{
		return deletedAt;
	}

	public void setDeletedAt(Date deletedAt)
	{
		this.deletedAt = deletedAt;
	}

	public Date getSentAt()
	{
		return sentAt;
	}

	public void setSentAt(Date sentAt)
	{
		this.sentAt = sentAt;
	}

	public PUSH_STATUS getStatus()
	{
		return status;
	}

	public void setStatus(PUSH_STATUS status)
	{
		this.status = status;
	}

	public Integer getSendCount()
	{
		return sendCount;
	}

	public void setSendCount(Integer sendCount)
	{
		this.sendCount = sendCount;
	}

	public void incrementSendCount()
	{
		this.setSendCount(this.getSendCount() + 1);
	}

	public String getJsonData()
	{
		return jsonData;
	}

	public void setJsonData(String jsonData)
	{
		this.jsonData = jsonData;
	}

	public Boolean hasTarget()
	{
		return this.targetId != null;
	}

	@PrePersist
	protected void jpaSetCreatedAtTime()
	{
		Date now = new Date();
		this.setCreatedAt(now);
		this.setUpdatedAt(now);
	}

	@PreUpdate
	protected void jpaSetUpdateAtTime()
	{
		this.setUpdatedAt(new Date());
	}
}
