package com.mobius.software.telco.protocols.diameter.primitives.s9a;
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

import com.mobius.software.telco.protocols.diameter.annotations.DiameterAvpDefinition;
import com.mobius.software.telco.protocols.diameter.primitives.DiameterIdentity;
import com.mobius.software.telco.protocols.diameter.primitives.KnownVendorIDs;

/**
*
* @author yulian oifa
*
*/
/*
 * 	A.7.3.1.1	PCRF-Address
	The PCRF-Address AVP (AVP code 2207) is of type DiameterIdentity and is used by the (V)-PCRF to indicate its own address in the TER command so that the BPCF can address the (V)-PCRF during the S9a session establishment procedure.
	NOTE:	The value in the Origin-Host AVP of the TER command can be replaced by the proxy agent between the (V)-PCRF and the BPCF. 
 */
@DiameterAvpDefinition(code = 2207L, vendorId = KnownVendorIDs.TGPP_ID, name = "PCRF-Address")
public interface PCRFAddress extends DiameterIdentity
{
}