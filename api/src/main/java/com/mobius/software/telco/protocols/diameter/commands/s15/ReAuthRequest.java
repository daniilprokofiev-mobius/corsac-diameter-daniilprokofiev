package com.mobius.software.telco.protocols.diameter.commands.s15;

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

import com.mobius.software.telco.protocols.diameter.ApplicationIDs;
import com.mobius.software.telco.protocols.diameter.CommandCodes;
import com.mobius.software.telco.protocols.diameter.annotations.DiameterCommandDefinition;
import com.mobius.software.telco.protocols.diameter.primitives.rfc7683.OCSupportedFeatures;
import com.mobius.software.telco.protocols.diameter.primitives.rfc7944.DRMPEnum;
import com.mobius.software.telco.protocols.diameter.primitives.s15.CSServiceResourceReport;

/* E.6.6.4	Re-Auth-Request (RAR) Command
   The RAR command, indicated by the Command-Code field set to 258 and the 'R' bit set in the Command Flags field,
   is sent by the PCRF to the HNB GW in order to report the resource reservation result in the Fixed Broadband Access network.
   Message Format:
   <RA-Request> ::= < Diameter Header: 258, REQ, PXY >
				 < Session-Id >
				 [ DRMP ]
				 { Auth-Application-Id }
				 { Origin-Host }
				 { Origin-Realm }
				 { Destination-Realm }
				 { Destination-Host }
				 { Re-Auth-Request-Type }
				 [ Origin-State-Id ]
				 [ OC-Supported-Features ]
				*[ CS-Service-Resource-Report ]
				*[ Proxy-Info ]
				*[ Route-Record ]
				*[ AVP ]
*/

@DiameterCommandDefinition(applicationId = ApplicationIDs.S15, commandCode = CommandCodes.REAUTH, request = true, proxyable = true, name="Re-Auth-Request")
public interface ReAuthRequest extends com.mobius.software.telco.protocols.diameter.commands.commons.ReAuthRequest
{

	DRMPEnum getDRMP();
	
	void setDRMP(DRMPEnum value);

	OCSupportedFeatures getOCSupportedFeatures();
	
	void setOCSupportedFeatures(OCSupportedFeatures value);
	
	CSServiceResourceReport getCSServiceResourceReport();
	
	void setCSServiceResourceReport(CSServiceResourceReport value);
}