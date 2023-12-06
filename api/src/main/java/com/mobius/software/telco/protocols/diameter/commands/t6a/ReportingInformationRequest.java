package com.mobius.software.telco.protocols.diameter.commands.t6a;
/*
 * Mobius Software LTD
 * Copyright 2023, Mobius Software LTD and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

import java.util.List;

import com.mobius.software.telco.protocols.diameter.annotations.DiameterCommandDefinition;
import com.mobius.software.telco.protocols.diameter.primitives.rfc7683.OCSupportedFeatures;
import com.mobius.software.telco.protocols.diameter.primitives.s6m.UserIdentifier;
import com.mobius.software.telco.protocols.diameter.primitives.s6t.MonitoringEventReport;

/**
*
* @author yulian oifa
*
*/

/*
 * 	6.2.5	Reporting-Information-Request (RIR) Command
	The Reporting-Information-Request (RIR) command, indicated by the Command-Code field set to 8388719 and the "R" bit set in the Command Flags field, is sent from:
		-	the MME/SGSN to the SCEF;
		-	the MME/SGSN to the IWK-SCEF and
		-	the IWK-SCEF to the SCEF.

	This command is originally defined in 3GPP TS 29.336 [5].
	For the T6a/T6b interface, the Reporting-Information-Request command format is specified as following:
	Message Format:

	< Reporting-Information-Request > ::=	< Diameter Header: 8388719, REQ, PXY, 16777346 >
			 < Session-Id >
			 [ DRMP ]
			 { Auth-Session-State }
			 { Origin-Host }
			 { Origin-Realm }
			 [ Destination-Host ]
			 { Destination-Realm }
			 [ OC-Supported-Features ]
			*[ Supported-Features ]
			 [ User-Identifier ]
			*[ Monitoring-Event-Report ]
			*[ Proxy-Info ]
			*[ Route-Record ]
			*[AVP]
 */
@DiameterCommandDefinition(applicationId = 16777346, commandCode = 8388719, request = true, proxyable = true, name="Reporting-Information-Request")
public interface ReportingInformationRequest extends T6aRequest
{
	OCSupportedFeatures getOCSupportedFeatures();
	 
	void setOCSupportedFeatures(OCSupportedFeatures value);	 
	
	UserIdentifier getUserIdentifier();
	
	void setUserIdentifier(UserIdentifier value);
	
	List<MonitoringEventReport> getMonitoringEventReport();
	
	void setMonitoringEventReport(List<MonitoringEventReport> value);
}