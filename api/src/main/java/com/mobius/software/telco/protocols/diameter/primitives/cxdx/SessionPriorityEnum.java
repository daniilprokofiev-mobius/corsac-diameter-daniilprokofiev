package com.mobius.software.telco.protocols.diameter.primitives.cxdx;

import java.util.HashMap;
import java.util.Map;

import com.mobius.software.telco.protocols.diameter.primitives.IntegerEnum;

/*
 * Mobius Software LTD, Open Source Cloud Communications
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
/**
*
* @author yulian oifa
*
*/

/*
 * 	6.3.56	Session-Priority AVP
	The Session-Priority AVP is of type Enumerated and indicates to the HSS the session's priority. 
	The following values are defined:
	
	PRIORITY-0 (0)

	PRIORITY-1 (1)

	PRIORITY-2 (2)

	PRIORITY-3 (3)

	PRIORITY-4 (4)

	PRIORITY-0 is the highest priority.

	The value of the AVP when sent to the HSS is mapped from the value received by the CSCF as described in 3GPP TS 24.229 table A.162. The mapping is operator specific.
	This AVP may be placed as close to the Diameter header as possible in order to potentially allow optimized processing at the receiver.
 */
public enum SessionPriorityEnum implements IntegerEnum
{
	PRIORITY0(0),PRIORITY1(1),PRIORITY2(2),PRIORITY3(3),PRIORITY4(4);

	private static final Map<Integer, SessionPriorityEnum> intToTypeMap = new HashMap<Integer, SessionPriorityEnum>();
	static
	{
	    for (SessionPriorityEnum type : SessionPriorityEnum.values()) 
	    {
	    	intToTypeMap.put(type.value, type);
	    }
	}

	public static SessionPriorityEnum fromInt(Integer value) 
	{
		SessionPriorityEnum type = intToTypeMap.get(value);
	    return type;
	}
	
	private int value;
	
	private SessionPriorityEnum(int value)
	{
		this.value=value;
	}
	
	public int getValue()
	{
		return value;
	}
}
