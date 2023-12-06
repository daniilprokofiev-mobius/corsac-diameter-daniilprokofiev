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
 * 	3.2.93  Bearer-Capability AVP

	AVP Name
	Bearer-Capability

	AVP Code
	20313

	AVP Data Type
	OctetString
	Length Range: [0,32)

	Vendor ID
	2011

	Description
	Indicates the service which is beared in circuit switch domain, like video call, fax and data service.
 */
@DiameterAvpDefinition(code = 20313L, vendorId = KnownVendorIDs.HUAWEI_ID, name = "Bearer-Capability")
public interface BearerCapability extends DiameterOctetString
{
}