package com.mobius.software.telco.protocols.diameter.primitives.rfc5777;
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

import com.mobius.software.telco.protocols.diameter.AvpCodes;
import com.mobius.software.telco.protocols.diameter.annotations.DiameterAvpDefinition;
import com.mobius.software.telco.protocols.diameter.primitives.DiameterOctetString;

/**
*
* @author yulian oifa
*
*/

/*
	4.1.7.10.  MAC-Address-Mask-Pattern AVP

   	The MAC-Address-Mask-Pattern AVP (AVP Code 526) is of type
   	OctetString.  The value is 6 octets specifying the bit positions of a
   	MAC address that are taken for matching.
 */
@DiameterAvpDefinition(code = AvpCodes.MAC_ADDRESS_MASK_PATTERN, vendorId = -1L, name = "MAC-Address-Mask-Pattern")
public interface MACAddressMaskPattern extends DiameterOctetString
{
}