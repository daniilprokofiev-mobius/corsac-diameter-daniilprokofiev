package com.mobius.software.telco.protocols.diameter.impl.commands.s7a;

import com.mobius.software.telco.protocols.diameter.annotations.DiameterCommandImplementation;
import com.mobius.software.telco.protocols.diameter.annotations.DiameterValidate;
import com.mobius.software.telco.protocols.diameter.commands.s7a.CancelVCSGLocationRequest;
import com.mobius.software.telco.protocols.diameter.impl.primitives.s6a.CancellationTypeImpl;
import com.mobius.software.telco.protocols.diameter.primitives.common.AuthSessionStateEnum;
import com.mobius.software.telco.protocols.diameter.primitives.s6a.CancellationType;
import com.mobius.software.telco.protocols.diameter.primitives.s6a.CancellationTypeEnum;

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
@DiameterCommandImplementation(applicationId = 16777308, commandCode = 8388642, request = true)
public class CancelVCSGLocationRequestImpl extends S7aRequestImpl implements CancelVCSGLocationRequest
{
	private CancellationType cancellationType;
	
	protected CancelVCSGLocationRequestImpl() 
	{
		super();
	}
	
	public CancelVCSGLocationRequestImpl(String originHost,String originRealm,String destinationHost,String destinationRealm,Boolean isRetransmit, String sessionID,AuthSessionStateEnum authSessionState,CancellationTypeEnum cancellationType)
	{
		super(originHost, originRealm, destinationHost, destinationRealm, isRetransmit, sessionID, authSessionState);
		
		setCancellationType(cancellationType);
	}
	
	@Override
	public CancellationTypeEnum getCancellationType()
	{
		if(cancellationType == null)
			return null;
		
		return cancellationType.getEnumerated(CancellationTypeEnum.class);
	}
	
	@Override
	public void setCancellationType(CancellationTypeEnum value)
	{
		if(value == null)
			throw new IllegalArgumentException("Cancellation-Type is required");
		
		this.cancellationType = new CancellationTypeImpl(value, null, null);
	}
	
	@DiameterValidate
	public String validate()
	{
		if(cancellationType == null)
			return "Cancellation-Type is required";
		
		return super.validate();
	}	
}