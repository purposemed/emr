
var myhealthaccess = myhealthaccess || {};

myhealthaccess = {
	ERROR_NO_INTEGRATION: "error_no_integration",


	SESSION_STATUS_UNKNOWN: "UNKNOWN",
	SESSION_STATUS_PENDING: "PENDING",
	SESSION_STATUS_INBOUND: "INBOUND",
	SESSION_STATUS_OUTBOUND: "OUTBOUND",
	SESSION_STATUS_IN_CALL: "IN_CALL",
	SESSION_STATUS_CALL_ENDED: "CALL_ENDED",

	// ask MHA if the demographic is confirmed with this clinic
	checkDemographicConfirmed: function checkDemographicConfirmed(contextPath, demographicNo, site)
	{
		return myhealthaccess.getIntegrationWrapper(contextPath, site, (resolve, reject, integration) =>
		{
			jQuery.ajax(
					{
						url: contextPath + "/ws/rs/myhealthaccess/integration/" + integration.id +
								"/demographic/" + demographicNo + "/confirmed",
						type: "GET",
						success: (result) =>
						{
							resolve(result);
						},
						error: (error) =>
						{
							reject(error);
						}
					});
		});
	},


	// get the mha integration for the specified site
	getIntegration: function getIntegration(contextPath, site)
	{
		return new Promise((resolve, reject) =>
		{
			var siteParam = "";
			if (site)
			{
				var siteParam = "?site=" + site;
			}
			jQuery.ajax(
					{
						url: contextPath + "/ws/rs/myhealthaccess/integrations/" + siteParam,
						type: "GET",
						success: (result) =>
						{
							resolve(result);
						},
						error: (error) =>
						{
							reject(error);
						}
					});
		});
	},

	// get the mha integration for the specified site
	getAppointment: function getAppointment(contextPath, site, apptNo)
	{
		return myhealthaccess.getIntegrationWrapper(contextPath, site, (resolve, reject, integration) =>
		{
			var queryParams = "?appointmentNo=" + apptNo;
			jQuery.ajax(
					{
						url: contextPath + "/ws/rs/myhealthaccess/integration/" + integration.id + "/appointments/" + queryParams,
						type: "GET",
						success: (result) =>
						{
							resolve(result);
						},
						error: (error) =>
						{
							reject(error);
						}
					});
		});
	},

	// send a telehealth appointment notification to the the patient of the appointment
	sendTelehealthAppointmentNotification: function (contextPath, site, mhaAppointment)
	{
		return myhealthaccess.getIntegrationWrapper(contextPath, site, (resolve, reject, integration) =>
		{
			jQuery.ajax(
				{
					url: contextPath + "/ws/rs/myhealthaccess/integration/" + integration.id + "/appointment/" + mhaAppointment.id + "/send_telehealth_appt_notification",
					type: "POST",
					success: (result) =>
					{
						resolve(result);
					},
					error: (error) =>
					{
						reject(error);
					}
				});
		});
	},

	// send a general appointment notification to the the patient of the appointment
	sendGeneralAppointmentNotification: function (contextPath, site, junoAppointmentNo)
	{
		return myhealthaccess.getIntegrationWrapper(contextPath, site, (resolve, reject, integration) =>
		{
			jQuery.ajax(
					{
						url: contextPath + "/ws/rs/myhealthaccess/integration/" + integration.id + "/appointment/non_mha/" +
								junoAppointmentNo + "/send_general_appt_notification",
						type: "POST",
						success: (result) =>
						{
							resolve(result);
						},
						error: (error) =>
						{
							reject(error);
						}
					});
		});
	},

	// get telehealth session information
	getAppointmentSessionInformation: function (contextPath, site, junoAppointmentNo)
	{
		return myhealthaccess.getIntegrationWrapper(contextPath, site, (resolve, reject, integration) =>
		{
			jQuery.ajax(
				{
					url: contextPath + "/ws/rs/myhealthaccess/integration/" + integration.id + "/appointment/non_mha/" +
							junoAppointmentNo + "/session",
					type: "GET",
					success: (result) =>
					{
						resolve(result);
					},
					error: (error) =>
					{
						reject(error);
					}
				});
		});
	},

	getIntegrationWrapper: function getIntegrationWrapper(contextPath, site, callback)
	{
		return new Promise((resolve, reject) =>
		{
			myhealthaccess.getIntegration(contextPath, site).then((integration) =>
			{
				integrations = JSON.parse(integration).body;
				if (integrations.length > 0)
				{
					callback(resolve, reject, integrations[0])
				}
				else
				{
					reject(myhealthaccess.ERROR_NO_INTEGRATION)
				}
			});
		});
	},

	telehealthStatusToDisplayName: function (status)
	{
		switch(status)
		{
			case this.SESSION_STATUS_CALL_ENDED:
				return "Call Over";
			case this.SESSION_STATUS_IN_CALL:
				return "In Session";
			case this.SESSION_STATUS_INBOUND:
			case this.SESSION_STATUS_OUTBOUND:
			case this.SESSION_STATUS_PENDING:
				return "Patient Waiting";
			case null:
				return "No Patient";
			default:
				return "Status Unknown";
		}
	}
};