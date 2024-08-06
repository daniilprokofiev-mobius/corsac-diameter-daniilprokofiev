package com.mobius.software.telco.protocols.diameter;
/*
 * Mobius Software LTD, Open Source Cloud Communications
 * Copyright 2023, Mobius Software LTD and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation), either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY), without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

import java.util.HashMap;
import java.util.Map;

/**
*
* @author yulian oifa
*
*/

public enum VendorID
{
	HP(11),
	SUN_ID(42),
	MERIT_ID(61),
	ERICSSON_ID(193),
	US_ROBOTICS_ID(429),
	TGPP2_ID(5535),
	TGPP_ID(10415),
	VODAFONE_ID(12645),
	ETSI_ID(13019),
	CABLE_LABS_ID(4491),
	HUAWEI_ID(2011),
	ONEM2M_ID(45687);	
	
	private static final Map<Integer, VendorID> intToTypeMap = new HashMap<Integer, VendorID>();
	static 
	{
	    for (VendorID type : VendorID.values()) 
	    {
	        intToTypeMap.put(type.value, type);
	    }
	}
	
	public static VendorID fromInt(int i) 
	{
		return intToTypeMap.get(Integer.valueOf(i));	    
	}
	
	private int value;
	
	private VendorID(int value)
	{
		this.value=value;
	}
	
	public int getValue()
	{
		return value;
	}
}