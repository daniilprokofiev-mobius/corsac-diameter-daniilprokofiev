package com.mobius.software.telco.protocols.diameter.primitives.gx;

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
 * 	5.3.122 RAN-Rule-Support AVP
	The RAN-Rule-Support AVP (AVP code 2832) is of type Unsigned32, and it is used to indicate the network supports
	the RAN rule indication.
	The following values are defined in this specification:

	0 (RAN rule indication support):
 		This value shall be used to indicate that the network supports the RAN rule indication. 
 */
public enum RANRuleSupportEnum implements IntegerEnum
{
	RAN_RULE_INDICATION_SUPPORT(0);

	private static final Map<Integer, RANRuleSupportEnum> intToTypeMap = new HashMap<Integer, RANRuleSupportEnum>();
	static
	{
	    for (RANRuleSupportEnum type : RANRuleSupportEnum.values()) 
	    {
	    	intToTypeMap.put(type.value, type);
	    }
	}

	public static RANRuleSupportEnum fromInt(Integer value) 
	{
		RANRuleSupportEnum type = intToTypeMap.get(value);
	    return type;
	}
	
	private int value;
	
	private RANRuleSupportEnum(int value)
	{
		this.value=value;
	}
	
	public int getValue()
	{
		return value;
	}
}
