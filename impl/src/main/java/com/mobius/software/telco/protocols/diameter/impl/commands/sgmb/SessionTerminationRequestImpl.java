package com.mobius.software.telco.protocols.diameter.impl.commands.sgmb;

import com.mobius.software.telco.protocols.diameter.annotations.DiameterCommandImplementation;
import com.mobius.software.telco.protocols.diameter.commands.sgmb.SessionTerminationRequest;
import com.mobius.software.telco.protocols.diameter.impl.primitives.sgmb.DiagnosticInfoImpl;
import com.mobius.software.telco.protocols.diameter.impl.primitives.sgmb.RestartCounterImpl;
import com.mobius.software.telco.protocols.diameter.primitives.common.TerminationCauseEnum;
import com.mobius.software.telco.protocols.diameter.primitives.sgmb.DiagnosticInfo;
import com.mobius.software.telco.protocols.diameter.primitives.sgmb.RestartCounter;

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
@DiameterCommandImplementation(applicationId = 16777292, commandCode = 275, request = true)
public class SessionTerminationRequestImpl extends com.mobius.software.telco.protocols.diameter.impl.commands.common.SessionTerminationRequestImpl implements SessionTerminationRequest
{
	private DiagnosticInfo diagnosticInfo;
	   
	private RestartCounter restartCounter;
	
	protected SessionTerminationRequestImpl() 
	{
		super();
	}
		
	public SessionTerminationRequestImpl(String originHost,String originRealm,String destinationRealm,Boolean isRetransmit, String sessionID, Long authApplicationID, TerminationCauseEnum terminationCause)
	{		
		super(originHost, originRealm, destinationRealm, isRetransmit, sessionID, authApplicationID, terminationCause);
	}
	
	@Override
	public Long getDiagnosticInfo() 
	{
		if(diagnosticInfo == null)
			return null;
		
		return diagnosticInfo.getUnsigned();
	}

	@Override
	public void setDiagnosticInfo(Long value) 
	{
		if(value == null)
			this.diagnosticInfo = null;
		else
			this.diagnosticInfo = new DiagnosticInfoImpl(value, null, null);
	}
	
	@Override
	public Long getRestartCounter()
	{
		if(restartCounter==null)
			return null;
		
		return restartCounter.getUnsigned();
	}
	
	@Override
	public void setRestartCounter(Long value)
	{
		if(value==null)
			this.restartCounter = null;
		else
			this.restartCounter = new RestartCounterImpl(value, null, null);
	}
}