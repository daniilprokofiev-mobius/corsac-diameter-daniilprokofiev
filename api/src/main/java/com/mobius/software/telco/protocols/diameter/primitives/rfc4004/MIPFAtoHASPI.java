package com.mobius.software.telco.protocols.diameter.primitives.rfc4004;
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
import com.mobius.software.telco.protocols.diameter.primitives.DiameterUnsigned32;

/**
*
* @author yulian oifa
*
*/

/*
	9.11.  MIP-FA-to-HA-SPI AVP

   	The MIP-FA-to-HA-SPI AVP (AVP Code 318) is of type Unsigned32 and
   	contains the Security Parameter Index the FA and HA use to refer to
   	the FA-HA mobility security association.  The HA allocates the SPI,
   	and it MUST NOT have a value between zero (0) and 255, which is the
   	reserved namespace defined in [MOBILEIP].	   
 */
@DiameterAvpDefinition(code = 318L, vendorId = -1, name = "MIP-FA-to-HA-SPI")
public interface MIPFAtoHASPI extends DiameterUnsigned32
{
}