package com.mobius.software.telco.protocols.diameter.impl.app.custom;
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
import com.mobius.software.telco.protocols.diameter.DiameterProvider;
import com.mobius.software.telco.protocols.diameter.app.ServerCCListener;
import com.mobius.software.telco.protocols.diameter.app.custom.CustomServerSession;
import com.mobius.software.telco.protocols.diameter.commands.custom.AbortSessionAnswer;
import com.mobius.software.telco.protocols.diameter.commands.custom.AbortSessionRequest;
import com.mobius.software.telco.protocols.diameter.commands.custom.CreditControlAnswer;
import com.mobius.software.telco.protocols.diameter.commands.custom.CreditControlRequest;
import com.mobius.software.telco.protocols.diameter.commands.custom.ReAuthAnswer;
import com.mobius.software.telco.protocols.diameter.commands.custom.ReAuthRequest;
import com.mobius.software.telco.protocols.diameter.commands.custom.SessionTerminationAnswer;
import com.mobius.software.telco.protocols.diameter.commands.custom.SessionTerminationRequest;
import com.mobius.software.telco.protocols.diameter.impl.app.ServerCCSessionImpl;

public class CustomServerSessionImpl extends ServerCCSessionImpl<CreditControlRequest, CreditControlAnswer,ReAuthRequest,ReAuthAnswer,AbortSessionRequest,AbortSessionAnswer,SessionTerminationRequest,SessionTerminationAnswer> implements CustomServerSession
{
	public CustomServerSessionImpl()
	{
		super(Long.valueOf(99999));
	}
	
	public CustomServerSessionImpl(String sessionID, String remoteHost, String remoteRealm, DiameterProvider<?, ? extends ServerCCListener<CreditControlRequest, CreditControlAnswer,ReAuthRequest,ReAuthAnswer,AbortSessionRequest,AbortSessionAnswer,SessionTerminationRequest,SessionTerminationAnswer>, ?, ?, ?> provider)
	{
		super(sessionID, Long.valueOf(99999), remoteHost, remoteRealm, provider);
	}
}