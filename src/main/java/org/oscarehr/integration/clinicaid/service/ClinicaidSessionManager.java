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

package org.oscarehr.integration.clinicaid.service;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.oscarehr.common.model.Provider;
import org.oscarehr.integration.clinicaid.dto.ClinicaidResultTo1;
import org.oscarehr.integration.clinicaid.dto.ClinicaidUserTo1;
import org.oscarehr.util.MiscUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import oscar.OscarProperties;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.X509Certificate;

@Service
@Scope(value = WebApplicationContext.SCOPE_SESSION)
public class ClinicaidSessionManager
{
	private static OscarProperties oscarProps = OscarProperties.getInstance();
	private final String clinicaidDomain = oscarProps.getProperty("clinicaid_domain");
	private final String instanceName = oscarProps.getProperty("clinicaid_instance_name");
	private final String apiKey = oscarProps.getProperty("clinicaid_api_key");
	private final String loginEndPoint = clinicaidDomain + "/auth/pushed_login/";

	private ClinicaidUserTo1 clinicaidUser;
	private String nonce;

	private ClinicaidSessionManager()
	{
	}

	protected String getClinicaidDomain()
	{
		return clinicaidDomain;
	}

	public ClinicaidResultTo1 post(URL url, String postData) throws IOException
	{
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Authorization", getBasicAuth());
		ClinicaidResultTo1 result = executeRequest(connection, postData);
		connection.disconnect();
		return result;
	}

	public ClinicaidResultTo1 get(URL url) throws IOException
	{
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Authorization", getBasicAuth());
		ClinicaidResultTo1 result = executeRequest(connection, null);
		connection.disconnect();
		return result;
	}

	protected String getLoginToken(Provider loggedInProvider) throws IOException
	{
		if (nonce == null)
		{
			clinicaidUser = new ClinicaidUserTo1();
			clinicaidUser.setIdentifier(loggedInProvider.getProviderNo());
			clinicaidUser.setFirstName(loggedInProvider.getFirstName());
			clinicaidUser.setLastName(loggedInProvider.getLastName());
			pushLogin();
		}
		return nonce;
	}

	private void pushLogin() throws IOException
	{
		if (OscarProperties.getInstance().isPropertyActive("clinicaid_dev_mode"))
		{
			IgnoreSSLVerify();
		}

		ObjectMapper mapper = new ObjectMapper();

		URL nonceUrl = new URL(loginEndPoint);
		ClinicaidResultTo1 result = post(nonceUrl, mapper.writeValueAsString(clinicaidUser));
		nonce = result.getNonce();
	}

	private ClinicaidResultTo1 executeRequest
			(HttpURLConnection connection, String data) throws IOException
	{
		if (data != null)
		{
			connection.setDoOutput(true);
			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
			writer.write(data);
			writer.flush();
			writer.close();
		}

		// Read the result
		String inputLine;
		String response = "";
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		while ((inputLine = reader.readLine()) != null)
		{
			response += inputLine;
		}

		reader.close();
		MiscUtils.getLogger().debug("############################################");
		MiscUtils.getLogger().debug("Clinicaid Response: " + response);
		MiscUtils.getLogger().debug("############################################");

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(MapperFeature.USE_ANNOTATIONS, true);
		ClinicaidResultTo1 result = mapper.readValue(response, ClinicaidResultTo1.class);

		if (result.hasError())
		{
			MiscUtils.getLogger().error("###########################");
			MiscUtils.getLogger().error("Errors: " + result.getErrors().getErrorString());
			MiscUtils.getLogger().error("###########################");
			throw new IllegalArgumentException("Error: " + result.getErrors().getErrorString());
		}
		return result;
	}

	private String getBasicAuth()
	{
		String userPassString = instanceName + ":" + apiKey;
		String userPassBase64String = new String(new Base64().encode(userPassString.getBytes()));
		userPassBase64String = userPassBase64String.replaceAll("\n", "").replaceAll("\r", "");
		String basicAuthString = "Basic " + userPassBase64String;
		return basicAuthString;
	}

	protected String urlEncode(String inValue)
	{
		if (inValue == null)
		{
			return null;
		}

		String outValue;
		try
		{
			outValue = URLEncoder.encode(inValue, "UTF-8");
		}
		catch (UnsupportedEncodingException E)
		{
			outValue = "";
		}

		return outValue.trim();
	}

	TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager()
	{
		public java.security.cert.X509Certificate[] getAcceptedIssuers()
		{
			return null;
		}

		public void checkClientTrusted(X509Certificate[] certs, String authType)
		{
		}

		public void checkServerTrusted(X509Certificate[] certs, String authType)
		{
		}
	}
	};

	private void IgnoreSSLVerify()
	{
		try
		{
			SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
		}
		catch (Exception e)
		{
			MiscUtils.getLogger().debug("+++++++++++++++++++++++++++++++");
			MiscUtils.getLogger().debug("Error Ignoring SSL verify: " + e.getMessage());
			MiscUtils.getLogger().debug("Error: " + org.apache.commons.lang.exception.ExceptionUtils.getFullStackTrace(e));
			MiscUtils.getLogger().debug("+++++++++++++++++++++++++++++++");
		}
	}
}