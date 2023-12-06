package com.mobius.software.telco.protocols.diameter.impl.commands.slh;

import com.mobius.software.telco.protocols.diameter.annotations.DiameterCommandImplementation;
import com.mobius.software.telco.protocols.diameter.commands.slh.LCSRoutingInfoRequest;
import com.mobius.software.telco.protocols.diameter.impl.primitives.s6a.GMLCNumberImpl;
import com.mobius.software.telco.protocols.diameter.impl.primitives.sh.MSISDNImpl;
import com.mobius.software.telco.protocols.diameter.primitives.common.AuthSessionStateEnum;
import com.mobius.software.telco.protocols.diameter.primitives.s6a.GMLCNumber;
import com.mobius.software.telco.protocols.diameter.primitives.sh.MSISDN;

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
@DiameterCommandImplementation(applicationId = 16777291, commandCode = 8388622, request = true)
public class LCSRoutingInfoRequestImpl extends SlhRequestImpl implements LCSRoutingInfoRequest
{
	private MSISDN msisdn;
	
	private GMLCNumber gmlcNumber;
	
	protected LCSRoutingInfoRequestImpl() 
	{
		super();
	}
	
	public LCSRoutingInfoRequestImpl(String originHost,String originRealm,String destinationHost,String destinationRealm,Boolean isRetransmit, String sessionID,AuthSessionStateEnum authSessionState)
	{
		super(originHost, originRealm, destinationHost, destinationRealm, isRetransmit, sessionID, authSessionState);		
	}
	
	@Override
	public String getMSISDN()
	{
		if(msisdn == null)
			return null;
		
		return msisdn.getAddress();
	}
	
	@Override
	public void setMSISDN(String value)
	{
		if(value == null)
			this.msisdn = null;
		else
			this.msisdn = new MSISDNImpl(value);
	}
	
	@Override
	public String getGMLCNumber()
	{
		if(gmlcNumber == null)
			return null;
		
		return gmlcNumber.getAddress();
	}
	 
	@Override
	public void setGMLCNumber(String value)
	{
		if(value == null)
			this.gmlcNumber = null;
		else
			this.gmlcNumber = new GMLCNumberImpl(value);
	}
}