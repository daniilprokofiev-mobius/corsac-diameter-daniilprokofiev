package com.mobius.software.telco.protocols.diameter.impl.app.nta;
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

import com.mobius.software.telco.protocols.diameter.ApplicationIDs;
import com.mobius.software.telco.protocols.diameter.DiameterProvider;
import com.mobius.software.telco.protocols.diameter.app.ClientAuthStatelessListener;
import com.mobius.software.telco.protocols.diameter.app.nta.NtaClientSession;
import com.mobius.software.telco.protocols.diameter.commands.nta.NtaAnswer;
import com.mobius.software.telco.protocols.diameter.commands.nta.NtaRequest;
import com.mobius.software.telco.protocols.diameter.impl.app.ClientAuthSessionStatelessImpl;

public class NtaClientSessionImpl extends ClientAuthSessionStatelessImpl<NtaRequest,NtaAnswer> implements NtaClientSession
{
	public NtaClientSessionImpl()
	{
		super(Long.valueOf(ApplicationIDs.NTA));
	}
	
	public NtaClientSessionImpl(String sessionID, String remoteHost, String remoteRealm, DiameterProvider<? extends ClientAuthStatelessListener<NtaRequest,NtaAnswer>, ?, ?, ?, ?> provider)
	{
		super(sessionID, Long.valueOf(ApplicationIDs.NTA), remoteHost, remoteRealm, provider);
	}
}