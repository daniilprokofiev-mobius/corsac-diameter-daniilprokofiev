package com.mobius.software.telco.protocols.diameter.primitives.rfc8583;

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
 * 7.2.  Load-Type AVP

   The Load-Type AVP (AVP code 651) is of type Enumerated.  It is used
   to convey the type of Diameter node that sent the load information.
   The following values are defined:

   HOST 0  The load report is for a host.

   PEER 1  The load report is for a peer.
 */
public enum LoadTypeEnum implements IntegerEnum
{
	HOST(0),PEER(1);

	private static final Map<Integer, LoadTypeEnum> intToTypeMap = new HashMap<Integer, LoadTypeEnum>();
	static
	{
	    for (LoadTypeEnum type : LoadTypeEnum.values()) 
	    {
	    	intToTypeMap.put(type.value, type);
	    }
	}

	public static LoadTypeEnum fromInt(Integer value) 
	{
		LoadTypeEnum type = intToTypeMap.get(value);
	    return type;
	}
	
	private int value;
	
	private LoadTypeEnum(int value)
	{
		this.value=value;
	}
	
	public int getValue()
	{
		return value;
	}
}
