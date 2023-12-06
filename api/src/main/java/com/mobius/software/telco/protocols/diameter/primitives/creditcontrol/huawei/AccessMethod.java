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
import com.mobius.software.telco.protocols.diameter.primitives.DiameterUnsigned32;
import com.mobius.software.telco.protocols.diameter.primitives.KnownVendorIDs;

/**
*
* @author yulian oifa
*
*/
/*
 * 	3.2.106  Access-Method AVP

	AVP Name
	Access-Method

	AVP Code
	20340

	AVP Data Type
	Unsigned32

	Vendor ID
	2011

	Description
	This AVP is valid to the recharge and balance query processes. The options are as follows:
    	- 1: IVR
    	- 2: USSD
    	- 3: SMS

	For the UVC-controlled recharge process, the options are as follows:
    	- 0: IVR
    	- 1: USSD
    	- 2: SMS
 */
@DiameterAvpDefinition(code = 20340L, vendorId = KnownVendorIDs.HUAWEI_ID, name = "Access-Method")
public interface AccessMethod extends DiameterUnsigned32
{
}