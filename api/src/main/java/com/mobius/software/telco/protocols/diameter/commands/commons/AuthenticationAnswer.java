package com.mobius.software.telco.protocols.diameter.commands.commons;
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

import java.util.List;

import com.mobius.software.telco.protocols.diameter.commands.DiameterAnswer;
import com.mobius.software.telco.protocols.diameter.exceptions.AvpNotSupportedException;
import com.mobius.software.telco.protocols.diameter.exceptions.InvalidAvpValueException;
import com.mobius.software.telco.protocols.diameter.exceptions.MissingAvpException;
import com.mobius.software.telco.protocols.diameter.primitives.common.RedirectHostUsageEnum;

/**
*
* @author yulian oifa
*
*/

public abstract interface AuthenticationAnswer extends DiameterAnswer
{
	public Long getAuthApplicationId() throws AvpNotSupportedException;
	
	void setAuthApplicationId(Long value) throws AvpNotSupportedException, MissingAvpException;
	
	public List<String> getRedirectHost() throws AvpNotSupportedException;
	
	void setRedirectHost(List<String> value) throws AvpNotSupportedException,InvalidAvpValueException;

	public RedirectHostUsageEnum getRedirectHostUsage()throws AvpNotSupportedException;;
	
	void setRedirectHostUsage(RedirectHostUsageEnum value)throws AvpNotSupportedException;;
	
	public Long getRedirectMaxCacheTime()throws AvpNotSupportedException;;
	
	void setRedirectMaxCacheTime(Long value)throws AvpNotSupportedException;;
}