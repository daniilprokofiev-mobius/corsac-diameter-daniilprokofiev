package com.mobius.software.telco.protocols.diameter.impl.app;
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
import java.util.Collection;

import com.mobius.software.telco.protocols.diameter.AsyncCallback;
import com.mobius.software.telco.protocols.diameter.DiameterProvider;
import com.mobius.software.telco.protocols.diameter.ResultCodes;
import com.mobius.software.telco.protocols.diameter.app.ClientAccListener;
import com.mobius.software.telco.protocols.diameter.app.ClientAccSession;
import com.mobius.software.telco.protocols.diameter.app.SessionStateEnum;
import com.mobius.software.telco.protocols.diameter.commands.DiameterAnswer;
import com.mobius.software.telco.protocols.diameter.commands.DiameterRequest;
import com.mobius.software.telco.protocols.diameter.commands.commons.AccountingAnswer;
import com.mobius.software.telco.protocols.diameter.commands.commons.AccountingRequest;
import com.mobius.software.telco.protocols.diameter.exceptions.DiameterException;
import com.mobius.software.telco.protocols.diameter.impl.DiameterSessionImpl;
import com.mobius.software.telco.protocols.diameter.primitives.common.AccountingRealtimeRequiredEnum;
import com.mobius.software.telco.protocols.diameter.primitives.common.AccountingRecordTypeEnum;
/**
*
* @author yulian oifa
*
*/
public class ClientAccSessionImpl<R1 extends AccountingRequest,A1 extends AccountingAnswer> extends DiameterSessionImpl implements ClientAccSession<R1>
{
	private Boolean isRetry = false;
	
	private DiameterProvider<? extends ClientAccListener<A1>, ?, ?, ?, ?> provider;
	
	public ClientAccSessionImpl(String sessionID, String remoteHost, String remoteRealm, DiameterProvider<? extends ClientAccListener<A1>, ?, ?, ?, ?> provider)
	{
		super(sessionID, remoteHost, remoteRealm, provider);
		this.provider = provider;
	}

	@Override
	public void sendAccountingRequest(R1 request, AsyncCallback callback)
	{
		setSessionState(SessionStateEnum.PENDING);
		if(!isRetry) 
		{
			setLastSentRequest(request);	
			requestSent(request, new CallbackWrapper(callback));
		}
		
		provider.getStack().sendRequestToNetwork(request, callback);			
	}
	
	@Override
	public void requestReceived(DiameterRequest request, AsyncCallback callback)
	{
		callback.onError(new DiameterException("Received unexpected request", null, ResultCodes.DIAMETER_COMMAND_UNSUPPORTED, null));
		return;
	}
	
	@Override
	public void answerReceived(DiameterAnswer answer, AsyncCallback callback, Long idleTime,Boolean stopSendTimer)
	{
		DiameterRequest request = getLastSendRequest();
		Long newTime = null;
		if(request!=null)
		{
			try
			{
				@SuppressWarnings("unchecked")
				A1 castedAnswer = (A1)answer;
				@SuppressWarnings("unchecked")
				Collection<ClientAccListener<A1>> listeners = (Collection<ClientAccListener<A1>>) provider.getClientListeners().values();
				
				if(getSessionState()==SessionStateEnum.PENDING)
				{
					if(((AccountingRequest)request).getAcctInterimInterval()!=null)
						newTime = ((AccountingRequest)request).getAcctInterimInterval()*1000L;
					
					if(((AccountingRequest)request).getAccountingRecordType()!=null && ((AccountingRequest)request).getAccountingRecordType()==AccountingRecordTypeEnum.STOP_RECORD)
					{
						setSessionState(SessionStateEnum.IDLE);
						terminate();
						for(ClientAccListener<A1> listener:listeners)
							listener.onAccountingResponse(castedAnswer, callback);
					}
					else if(castedAnswer.getResultCode()!=null && !castedAnswer.getIsError())
					{
						if(((AccountingRequest)request).getAccountingRecordType()!=null && ((AccountingRequest)request).getAccountingRecordType()==AccountingRecordTypeEnum.EVENT_RECORD)
						{
							setSessionState(SessionStateEnum.IDLE);
							terminate();
							for(ClientAccListener<A1> listener:listeners)
								listener.onAccountingResponse(castedAnswer, callback);
						}
						else
						{
							setSessionState(SessionStateEnum.OPEN);
							for(ClientAccListener<A1> listener:listeners)
								listener.onAccountingResponse(castedAnswer, callback);
						}
					}
					else
					{
						Boolean processed = false;
						if(((AccountingRequest)request).getAccountingRecordType()!=null && (((AccountingRequest)request).getAccountingRecordType()==AccountingRecordTypeEnum.START_RECORD || ((AccountingRequest)request).getAccountingRecordType()==AccountingRecordTypeEnum.INTERIM_RECORD))
						{
							if(((AccountingRequest)request).getAccountingRealtimeRequired()!=null && ((AccountingRequest)request).getAccountingRealtimeRequired()==AccountingRealtimeRequiredEnum.GRANT_AND_LOSE)
							{
								setSessionState(SessionStateEnum.OPEN);
								for(ClientAccListener<A1> listener:listeners)
									listener.onAccountingResponse(castedAnswer, callback);
								
								processed = true;
							}
						}
						
						if(!processed)
						{
							setSessionState(SessionStateEnum.IDLE);
							terminate();
							for(ClientAccListener<A1> listener:listeners)
								listener.onAccountingResponse(castedAnswer, callback);													
						}
					}
				}
			}
			catch(Exception ex)
			{
				callback.onError(new DiameterException("Received unexpected answer", null, ResultCodes.DIAMETER_COMMAND_UNSUPPORTED, null));
				return;
			}
			
			if(getSessionState()!=SessionStateEnum.IDLE)
				super.answerReceived(answer, callback,newTime, !isRetry);			
		}
		else 
			callback.onError(new DiameterException("Received unexpected answer", null, ResultCodes.DIAMETER_COMMAND_UNSUPPORTED, null));		
	}

	@Override
	public boolean isServer()
	{
		return false;
	}
	
	private class CallbackWrapper implements AsyncCallback
	{
		private AsyncCallback realCallback;
		
		public CallbackWrapper(AsyncCallback realCallback)
		{
			this.realCallback = realCallback;
		}
		
		@Override
		public void onSuccess()
		{
			realCallback.onSuccess();
		}

		@Override
		public void onError(DiameterException ex)
		{
			DiameterRequest request = getLastSendRequest();
			if(request!=null)
			{
				Boolean shouldResend=false;
				try
				{
					if(!isRetry)
					{
						if(((AccountingRequest)request).getAccountingRecordType()!=null && (((AccountingRequest)request).getAccountingRecordType()==AccountingRecordTypeEnum.START_RECORD || ((AccountingRequest)request).getAccountingRecordType()==AccountingRecordTypeEnum.INTERIM_RECORD))
						{
							
							if(((AccountingRequest)request).getAccountingRealtimeRequired()!=null && ((AccountingRequest)request).getAccountingRealtimeRequired()!=AccountingRealtimeRequiredEnum.DELIVER_AND_GRANT)
								shouldResend = true;
						}
						else
							shouldResend=true;
					}
				}
				catch(DiameterException ex1)
				{
					
				}
				
				//does not really matters since will be changed right away to PENDING again
				setSessionState(SessionStateEnum.IDLE);
				if(!shouldResend)
				{
					terminate();
					realCallback.onError(ex);	
				}
				else
				{
					@SuppressWarnings("unchecked")
					R1 castedRequest=(R1)request;
					sendAccountingRequest(castedRequest, realCallback);				
				}
			}
			else
				realCallback.onError(ex);
		}		
	}
}