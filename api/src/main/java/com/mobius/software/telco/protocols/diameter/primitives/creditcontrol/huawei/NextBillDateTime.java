package com.mobius.software.telco.protocols.diameter.primitives.creditcontrol.huawei;
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
import com.mobius.software.telco.protocols.diameter.primitives.DiameterOctetString;
import com.mobius.software.telco.protocols.diameter.primitives.KnownVendorIDs;

/**
*
* @author yulian oifa
*
*/
/*
 * 	3.2.291  Next-Bill-DateTime AVP

	AVP Name
	Next-Bill-DateTime

	AVP Code
	21109

	AVP Data Type
	OctetString
	Length Range: [0,15)

	Vendor ID
	2011

	Description
	Next monthly settlement date. The value is accurate to second, for example, 20121019102030.
 */
@DiameterAvpDefinition(code = 21109L, vendorId = KnownVendorIDs.HUAWEI_ID, name = "Next-Bill-DateTime")
public interface NextBillDateTime extends DiameterOctetString
{
}