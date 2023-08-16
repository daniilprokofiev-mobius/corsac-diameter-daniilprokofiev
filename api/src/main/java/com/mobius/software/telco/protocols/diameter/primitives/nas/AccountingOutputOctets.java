package com.mobius.software.telco.protocols.diameter.primitives.nas;
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
import com.mobius.software.telco.protocols.diameter.primitives.DiameterUnsigned64;

/**
*
* @author yulian oifa
*
*/
/*
 * 4.6.2.  Accounting-Output-Octets AVP

   The Accounting-Output-Octets AVP (AVP Code 364) is of type Unsigned64
   and contains the number of octets sent to the user.

   For NAS usage, this AVP indicates how many octets have been sent to
   the port in the course of this session.  It can only be present in
   ACR messages with an Accounting-Record-Type of INTERIM_RECORD or
   STOP_RECORD.
 */
@DiameterAvpDefinition(code = 364L, vendorId = -1L, name = "Accounting-Output-Octets")
public interface AccountingOutputOctets extends DiameterUnsigned64
{
}