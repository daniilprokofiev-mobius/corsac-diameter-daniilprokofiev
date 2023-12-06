package com.mobius.software.telco.protocols.diameter.impl.commands.sh;

import java.util.Date;
import java.util.List;

import com.mobius.software.telco.protocols.diameter.annotations.DiameterCommandImplementation;
import com.mobius.software.telco.protocols.diameter.commands.sh.SubscribeNotificationsAnswer;
import com.mobius.software.telco.protocols.diameter.impl.primitives.cxdx.WildcardedIMPUImpl;
import com.mobius.software.telco.protocols.diameter.impl.primitives.cxdx.WildcardedPublicIdentityImpl;
import com.mobius.software.telco.protocols.diameter.impl.primitives.sh.ExpiryTimeImpl;
import com.mobius.software.telco.protocols.diameter.impl.primitives.sh.UserDataImpl;
import com.mobius.software.telco.protocols.diameter.primitives.accounting.OCOLR;
import com.mobius.software.telco.protocols.diameter.primitives.common.AuthSessionStateEnum;
import com.mobius.software.telco.protocols.diameter.primitives.cxdx.WildcardedIMPU;
import com.mobius.software.telco.protocols.diameter.primitives.cxdx.WildcardedPublicIdentity;
import com.mobius.software.telco.protocols.diameter.primitives.rfc7683.OCSupportedFeatures;
import com.mobius.software.telco.protocols.diameter.primitives.rfc8583.Load;
import com.mobius.software.telco.protocols.diameter.primitives.sh.ExpiryTime;
import com.mobius.software.telco.protocols.diameter.primitives.sh.UserData;

import io.netty.buffer.ByteBuf;

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
@DiameterCommandImplementation(applicationId = 16777217, commandCode = 308, request = false)
public class SubscribeNotificationsAnswerImpl extends ShAnswerImpl implements SubscribeNotificationsAnswer
{
	private WildcardedPublicIdentity wildcardedPublicIdentity;
	
	private WildcardedIMPU wildcardedIMPU;
	
	private UserData userData;
	
	private ExpiryTime expiryTime;
	
	private OCSupportedFeatures ocSupportedFeatures;
	
	private OCOLR ocOLR;
	
	private List<Load> load;
	
	protected SubscribeNotificationsAnswerImpl() 
	{
		super();
		setExperimentalResultAllowed(false);
	}
	
	public SubscribeNotificationsAnswerImpl(String originHost,String originRealm,Boolean isRetransmit, Long resultCode, String sessionID,AuthSessionStateEnum authSessionState)
	{
		super(originHost, originRealm, isRetransmit, resultCode, sessionID, authSessionState);
		setExperimentalResultAllowed(false);
	}
	
	public String getWildcardedPublicIdentity()
	{
		if(wildcardedPublicIdentity==null)
			return null;
		
		return wildcardedPublicIdentity.getString();
	}
	
	public void setWildcardedPublicIdentity(String value)
	{
		if(value==null)
			this.wildcardedPublicIdentity = null;
		else
			this.wildcardedPublicIdentity = new WildcardedPublicIdentityImpl(value, null, null);
	}

	public String getWildcardedIMPU()
	{
		if(wildcardedIMPU==null)
			return null;
		
		return wildcardedIMPU.getString();
	}
	
	public void setWildcardedIMPU(String value)
	{
		if(value==null)
			this.wildcardedIMPU = null;
		else
			this.wildcardedIMPU = new WildcardedIMPUImpl(value, null, null);
	}
	
	public ByteBuf getUserData()
	{
		if(userData==null)
			return null;
		
		return userData.getValue();
	}
	
	public void setUserData(ByteBuf value)
	{
		if(value==null)
			this.userData = null;
		else
			this.userData = new UserDataImpl(value, null, null);
	}
	
	public Date getExpiryTime()
	{
		if(expiryTime == null)
			return null;
		
		return expiryTime.getDateTime();
	}
	
	public void setExpiryTime(Date value)
	{
		if(value==null)
			this.expiryTime = null;
		else
			this.expiryTime = new ExpiryTimeImpl(value, null, null);
	}
	
	public OCSupportedFeatures getOCSupportedFeatures()
	{
		return this.ocSupportedFeatures;
	}
	 
	public void setOCSupportedFeatures(OCSupportedFeatures value)
	{
		this.ocSupportedFeatures = value;
	}
	 		
	public OCOLR getOCOLR()
	{
		return this.ocOLR;
	}
	 
	public void setOCOLR(OCOLR value)
	{
		this.ocOLR = value;
	}
	
	public List<Load> getLoad()
	{
		return this.load;
	}
	 
	public void setLoad(List<Load> value)
	{
		this.load = value;
	}
}