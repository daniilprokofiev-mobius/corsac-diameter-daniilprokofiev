package com.mobius.software.telco.protocols.diameter.impl.app.s7a;
import com.mobius.software.telco.protocols.diameter.ApplicationIDs;
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
import com.mobius.software.telco.protocols.diameter.app.ServerAuthStatelessListener;
import com.mobius.software.telco.protocols.diameter.app.s7a.S7aServerSession;
import com.mobius.software.telco.protocols.diameter.commands.s7a.S7aAnswer;
import com.mobius.software.telco.protocols.diameter.commands.s7a.S7aRequest;
import com.mobius.software.telco.protocols.diameter.impl.app.ServerAuthSessionStatelessImpl;
/**
*
* @author yulian oifa
*
*/
public class S7aServerSessionImpl extends ServerAuthSessionStatelessImpl<S7aRequest, S7aAnswer> implements S7aServerSession
{
	public S7aServerSessionImpl()
	{
		super(Long.valueOf(ApplicationIDs.S7A));
	}
	
	public S7aServerSessionImpl(String sessionID, String remoteHost, String remoteRealm, DiameterProvider<?, ? extends ServerAuthStatelessListener<S7aRequest, S7aAnswer>, ?, ?, ?> provider)
	{
		super(sessionID, Long.valueOf(ApplicationIDs.S7A), remoteHost, remoteRealm, provider);
	}
}