package com.mobius.software.telco.protocols.diameter;

import java.io.Externalizable;

import org.restcomm.cluster.ClusteredID;

import com.mobius.software.telco.protocols.diameter.app.SessionStateEnum;
import com.mobius.software.telco.protocols.diameter.commands.DiameterAnswer;
import com.mobius.software.telco.protocols.diameter.commands.DiameterRequest;

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
public interface DiameterSession
{
	String getID();
	
	Long getApplicationID();
	
	void setRemoteHost(String remoteHost);
	
	String getRemoteHost();
	
	void setRemoteRealm(String remoteRealm);
	
	String getRemoteRealm();
	
	ClusteredID<?> getIdleTimerID();
	
	ClusteredID<?> getSendTimerID();
	
	void setIdleTimerID(ClusteredID<?> id);
	
	void setSendTimerID(ClusteredID<?> id);
	
	void setSessionState(SessionStateEnum state);
	
	SessionStateEnum getSessionState();
	
	void setUserObject(Externalizable uo);
	
	Externalizable getUserObject();
	
	void requestReceived(DiameterRequest request,String linkID, AsyncCallback callback);
	
	void answerReceived(DiameterAnswer answer, Long idleTime,Boolean stopSendTimer,String linkID, AsyncCallback callback);
	
	void requestSent(Boolean newSession, DiameterRequest request, AsyncCallback callback);
	
	void answerSent(DiameterAnswer answer, Long idleTime, AsyncCallback callback);
	
	void terminate(Long resultCode);
	
	void onTimeout();
	
	void onIdleTimeout();
	
	boolean isServer();
	
	DiameterRequest getLastSendRequest();
	
	void setLastSentRequest(DiameterRequest request);
	
	Boolean isRetry();
	
	void setIsRetry(Boolean isRetry);	
	
	DiameterProvider<?, ?, ?, ?, ?> getProvider();
	
	void setProvider(DiameterProvider<?, ?, ?, ?, ?> provider);
	
	void load(String sessionID, SessionStateEnum sessionSate, byte otherFields);
	
	byte getOtherFieldsByte();
}