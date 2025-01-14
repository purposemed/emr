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
package org.oscarehr.integration.myhealthaccess.service;

import org.oscarehr.common.IsPropertiesOn;
import org.oscarehr.common.dao.OscarAppointmentDao;
import org.oscarehr.common.model.Appointment;
import org.oscarehr.integration.model.Integration;
import org.oscarehr.integration.model.IntegrationData;
import org.oscarehr.integration.myhealthaccess.client.RestClientBase;
import org.oscarehr.integration.myhealthaccess.client.RestClientFactory;
import org.oscarehr.integration.myhealthaccess.conversion.SessionInfoInboundDtoMHATelehealthSessionInfoConverter;
import org.oscarehr.integration.myhealthaccess.dto.AppointmentAqsLinkTo1;
import org.oscarehr.integration.myhealthaccess.dto.AppointmentBookResponseTo1;
import org.oscarehr.integration.myhealthaccess.dto.AppointmentBookTo1;
import org.oscarehr.integration.myhealthaccess.dto.AppointmentCacheTo1;
import org.oscarehr.integration.myhealthaccess.dto.AppointmentSearchTo1;
import org.oscarehr.integration.myhealthaccess.dto.NotificationTo1;
import org.oscarehr.integration.myhealthaccess.dto.SessionInfoInboundDto;
import org.oscarehr.integration.myhealthaccess.exception.BookingException;
import org.oscarehr.integration.myhealthaccess.exception.InvalidIntegrationException;
import org.oscarehr.integration.myhealthaccess.exception.RecordNotFoundException;
import org.oscarehr.integration.myhealthaccess.exception.RecordNotUniqueException;
import org.oscarehr.integration.myhealthaccess.model.MHAAppointment;
import org.oscarehr.integration.myhealthaccess.model.MHATelehealthSessionInfo;
import org.oscarehr.util.LoggedInInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service("myHealthAppointmentService")
public class AppointmentService extends BaseService
{
	@Autowired
	ClinicService clinicService;

	@Autowired
	OscarAppointmentDao oscarAppointmentDao;

	@Autowired
	SessionInfoInboundDtoMHATelehealthSessionInfoConverter sessionInfoConverter;

	public void updateAppointmentCache(IntegrationData integrationData, AppointmentCacheTo1 appointmentTransfer)
	{
		RestClientBase restClient = RestClientFactory.getRestClient(integrationData.getIntegration());
		String endpoint = "/clinic/%s/appointment/%s/cache";

		String clinicId = integrationData.getIntegration().getRemoteId();
		String appointmentId = appointmentTransfer.getId();

		endpoint = restClient.formatEndpoint(endpoint, clinicId, appointmentId);
		Boolean response = restClient.doPut(endpoint, appointmentTransfer, Boolean.class);
		if(!response)
		{
			throw new RuntimeException("Got bad response status: " + response);
		}
	}

	/**
	 * book a telehealth appointment in MHA.
	 * @param loggedInInfo - logged in info.
	 * @param appointment - the appointment to book.
	 * @param sendNotification - if true the patient is sent a notification of the appointment booking.
	 * @throws InvalidIntegrationException - if MHA integration invalid
	 */
	public void bookTelehealthAppointment(LoggedInInfo loggedInInfo, Appointment appointment, boolean sendNotification) throws InvalidIntegrationException
	{
		bookTelehealthAppointment(loggedInInfo, appointment, sendNotification, null);
	}

	/**
	 * book a telehealth appointment in MHA.
	 * @param loggedInInfo - logged in info.
	 * @param appointment - the appointment to book.
	 * @param sendNotification - if true the patient is sent a notification of the appointment booking.
	 * @param remoteId - if provided (can be null) this overrides demographic_no and the appointment will be booked directly for that remote patient id.
	 * @throws InvalidIntegrationException - if MHA integration invalid
	 */
	public void bookTelehealthAppointment(LoggedInInfo loggedInInfo, Appointment appointment, boolean sendNotification, UUID remoteId) throws InvalidIntegrationException
	{
		String appointmentSite = null;
		if (IsPropertiesOn.isMultisitesEnable())
		{
			appointmentSite = appointment.getLocation();
		}

		RestClientBase restClient = RestClientFactory.getRestClient(integrationOrException(integrationService.findMhaIntegration(appointmentSite)));
		String loginToken = clinicService.loginOrCreateClinicUser(loggedInInfo, appointmentSite).getToken();
		AppointmentBookResponseTo1 appointmentBookResponseTo1 = restClient.doPostWithToken(
				restClient.formatEndpoint("/clinic_user/appointment/book"),
				loginToken,
				new AppointmentBookTo1(appointment, false, sendNotification, remoteId),
				AppointmentBookResponseTo1.class);

		if (!appointmentBookResponseTo1.isSuccess())
		{
			throw new BookingException(appointmentBookResponseTo1.getMessage());
		}
	}

	/**
	 * book a one time telehealth appointment in MHA
	 * @param loggedInInfo - logged in info
	 * @param appointment - the appointment to book.
	 * @param sendNotification - If true MHA will send a notification to the user. This notification will include the one time link.
	 * @throws InvalidIntegrationException
	 */
	public void bookOneTimeTelehealthAppointment(LoggedInInfo loggedInInfo, Appointment appointment, Boolean sendNotification) throws InvalidIntegrationException
	{
		String appointmentSite = null;
		if (IsPropertiesOn.isMultisitesEnable())
		{
			appointmentSite = appointment.getLocation();
		}

		String loginToken = clinicService.loginOrCreateClinicUser(loggedInInfo, appointmentSite).getToken();
		RestClientBase restClient = RestClientFactory.getRestClient(integrationOrException(integrationService.findMhaIntegration(appointmentSite)));
		AppointmentBookResponseTo1 appointmentBookResponseTo1 = restClient.doPostWithToken(
				restClient.formatEndpoint("/clinic_user/appointment/book"),
				loginToken,
				new AppointmentBookTo1(appointment, true, sendNotification, null),
				AppointmentBookResponseTo1.class);

		if (!appointmentBookResponseTo1.isSuccess())
		{
			throw new BookingException(appointmentBookResponseTo1.getMessage());
		}
	}

	/**
	 * send a telehealth appointment notification for the specified appointment, to the patient.
	 * @param integration - the integration under which to perform the action
	 * @param loginToken - the login token of the user performing the action
	 * @param remoteId - the appointments remote_id (mha id)
	 */
	public void sendTelehealthAppointmentNotification(Integration integration, String loginToken, String remoteId)
	{
		RestClientBase restClient = RestClientFactory.getRestClient(integration);
		restClient.doPostWithToken(
				restClient.formatEndpoint("/clinic_user/self/clinic/appointment/%s/send_telehealth_notification", remoteId),
				loginToken,
				null,
				Boolean.class);
	}

	/**
	 * send a general appointment notification for the specified appointment, to the patient.
	 * @param integration - the integration under which to perform the action
	 * @param loginToken - the login token of the user performing the action
	 * @param appointmentNo - the appointment no
	 */
	public void sendGeneralAppointmentNotification(Integration integration, String loginToken, Integer appointmentNo)
	{
		Appointment appointment = oscarAppointmentDao.find(appointmentNo);
		RestClientBase restClient = RestClientFactory.getRestClient(integration);

		if (appointment != null)
		{
			NotificationTo1 notificationTo1 = new NotificationTo1(appointment);
			restClient.doPostWithToken(
					restClient.formatEndpoint("/clinic_user/self/juno/appointment/%s/send_general_notification", appointmentNo.toString()),
					loginToken,
					notificationTo1,
					Boolean.class);
		}
		else
		{
			throw new IllegalArgumentException("Appointment with appointment_no: " + appointmentNo + " not found!");
		}
	}

	/**
	 * fetch remote appointment from MHA by appointmentNo
	 * @param integration - the integration to search for the appointment in
	 * @param appointmentNo - the appointment number to fetch
	 * @return - a new MHAAppointment object
	 */
	public MHAAppointment getAppointment(Integration integration, Integer appointmentNo)
	{
		RestClientBase restClient = RestClientFactory.getRestClient(integration);

		String url = restClient.formatEndpoint(
				"/clinic/%s/appointments?search_by=remote_id&remote_id=%s",
				integration.getRemoteId(),
				appointmentNo);

		AppointmentSearchTo1 result = restClient.doGet(url, AppointmentSearchTo1.class);

		if (result.isSuccess())
		{
			if (result.getAppointments().size() == 1)
			{
				return new MHAAppointment(result.getAppointments().get(0));
			}
			else
			{
				throw new RecordNotUniqueException("Multiple results returned when looking up MHA appointment by appointmentNo" +
						" for integration [" + integration.getId() + "] appointmentNo [" + appointmentNo + "]");
			}
		}
		else if (result.isNotFound())
		{
			throw new RecordNotFoundException("Could not find MHA appointment for integration [" + integration.getId() +
					"] With appointment No [" + appointmentNo + "]");
		}
		else
		{
			throw new RuntimeException("Unexpected status type when looking up MHA appointment for integration [" + integration.getId() +
					"] appointmentNo [" + appointmentNo + "]");
		}
	}

	/**
	 * get information about the MHA telehealth session
	 * @param integration - integration to use when fetching the information
	 * @param mhaAppointmentId - the appointment to get session information for.
	 * @return telehealth session info
	 */
	public MHATelehealthSessionInfo getAppointmentSessionInformation(Integration integration, UUID mhaAppointmentId) throws IllegalAccessException, InstantiationException
	{
		RestClientBase restClient = RestClientFactory.getRestClient(integration);

		String url = restClient.formatEndpoint("/clinic/%s/appointment/%s/session", integration.getRemoteId(), mhaAppointmentId);
		return sessionInfoConverter.convert(restClient.doGet(url, SessionInfoInboundDto.class));
	}

	/**
	 * get information about the MHA telehealth session
	 * @param integration - integration to use when fetching the information
	 * @param appointmentNo - the appointment no to get session information for.
	 * @return telehealth session info
	 */
	public MHATelehealthSessionInfo getAppointmentSessionInformation(Integration integration, Integer appointmentNo) throws InstantiationException, IllegalAccessException
	{
		MHAAppointment mhaAppointment = getAppointment(integration, appointmentNo);
		return getAppointmentSessionInformation(integration, UUID.fromString(mhaAppointment.getId()));
	}


	/**
	 * link an MHA appointment with an AQS telehealth session
	 * @param integration - integration on which to perform the link
	 * @param loggedInInfo - logged in info
	 * @param mhaAppointment - the MHA appointment to link
	 * @param queuedAppointmentId - the queued appointment to link
	 * @throws InvalidIntegrationException - if the integration is not setup correctly
	 */
	public void linkAppointmentToAqsTelehealth(Integration integration, LoggedInInfo loggedInInfo, MHAAppointment mhaAppointment, UUID queuedAppointmentId) throws InvalidIntegrationException
	{
		RestClientBase restClient = RestClientFactory.getRestClient(integration);
		String loginToken = clinicService.loginOrCreateClinicUser(integration, loggedInInfo.getLoggedInSecurity().getSecurityNo()).getToken();
		restClient.doPostWithToken(
				restClient.formatEndpoint("/clinic_user/self/appointment/" + mhaAppointment.getId() + "/aqs_link"),
				loginToken,
				new AppointmentAqsLinkTo1(queuedAppointmentId),
				null);
	}

}
