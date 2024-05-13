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

import com.mobius.software.telco.protocols.diameter.HuaweiAvpCodes;
import com.mobius.software.telco.protocols.diameter.VendorIDs;
import com.mobius.software.telco.protocols.diameter.annotations.DiameterAvpDefinition;
import com.mobius.software.telco.protocols.diameter.primitives.DiameterUTF8String;

/**
*
* @author yulian oifa
*
*/
/*
 * 	3.2.95  UE-IMSI AVP
	
	AVP Name
	UE-IMSI

	AVP Code
	20318

	AVP Data Type
	UTF8String
	Length Range: [0,32)

	Vendor ID
	2011

	Description
	Indicates the IMSI number of a user.

	NOTE
	IMSI: International Mobile Subscriber Identity
 */
@DiameterAvpDefinition(code = HuaweiAvpCodes.UE_IMSI, vendorId = VendorIDs.HUAWEI_ID, name = "UE-IMSI")
public interface UEIMSI extends DiameterUTF8String
{
}