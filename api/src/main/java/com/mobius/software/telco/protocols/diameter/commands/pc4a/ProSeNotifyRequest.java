package com.mobius.software.telco.protocols.diameter.commands.pc4a;
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

import com.mobius.software.telco.protocols.diameter.annotations.DiameterCommandDefinition;
import com.mobius.software.telco.protocols.diameter.primitives.rfc7683.OCSupportedFeatures;

import io.netty.buffer.ByteBuf;

/**
*
* @author yulian oifa
*
*/

/*
 * 	6.2.7	ProSe-Notify-Request (PNR) Command
	The ProSe-Notify-Request (PNR) command, indicated by the Command-Code field set to 8388666 and the "R" bit set in the Command Flags field, is sent from the ProSe Function to the HSS. 
	Message Format

	< ProSe-Notify-Request > ::=	< Diameter Header: 8388666, REQ, PXY, 16777336 >
			 < Session-Id >
			 [ DRMP ] 
			 [ Vendor-Specific-Application-Id ]
			 { Auth-Session-State }
			 { Origin-Host }
			 { Origin-Realm }
			 [ Destination-Host ]
			 { Destination-Realm }
			 [ User-Name ]
			 [ ProSe-Permission ]
			 [ Visited-PLMN-Id ]
			 [ PNR-Flags ]
			*[ Supported-Features ]
			 [ OC-Supported-Features ]
			*[ AVP ]
			*[ Proxy-Info ]
			*[ Route-Record ]
 */
@DiameterCommandDefinition(applicationId = 16777336, commandCode = 8388666, request = true, proxyable = true, name="ProSe-Notify-Request")
public interface ProSeNotifyRequest extends Pc4aRequest
{
	Long getProSePermission();
	
	void setProSePermission(Long value);
			 
	ByteBuf getVisitedPLMNId();
	
	void setVisitedPLMNId(ByteBuf value);
		
	Long getPNRFlags();
	
	void setPNRFlags(Long value);
	
	public OCSupportedFeatures getOCSupportedFeatures();
	 
	void setOCSupportedFeatures(OCSupportedFeatures value);
}