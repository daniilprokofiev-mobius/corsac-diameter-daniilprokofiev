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
import com.mobius.software.telco.protocols.diameter.primitives.DiameterEnumerated;

/**
*
* @author yulian oifa
*
*/
/*
 * 9.9.  MIP-Replay-Mode AVP

   The MIP-Replay-Mode AVP (AVP Code 346) is of type Enumerated and
   contains the replay mode the Home Agent for authenticating the mobile
   node.  The HAAA selects the replay mode.

   The following values are supported (see [MOBILEIP] for more
   information):

         1   None
         2   Timestamps
         3   Nonces
 */
@DiameterAvpDefinition(code = 345L, vendorId = -1L, name = "MIP-Replay-Mode")
public interface MIPReplayMode extends DiameterEnumerated<MIPReplayModeEnum>
{
}