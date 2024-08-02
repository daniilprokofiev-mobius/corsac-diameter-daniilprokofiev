package com.mobius.software.telco.protocols.diameter.impl.app.rfc5778i;
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
import com.mobius.software.telco.protocols.diameter.DiameterSession;
import com.mobius.software.telco.protocols.diameter.DiameterStack;
import com.mobius.software.telco.protocols.diameter.app.rfc5778i.AvpFactory;
import com.mobius.software.telco.protocols.diameter.app.rfc5778i.ClientListener;
import com.mobius.software.telco.protocols.diameter.app.rfc5778i.MessageFactory;
import com.mobius.software.telco.protocols.diameter.app.rfc5778i.ServerListener;
import com.mobius.software.telco.protocols.diameter.app.rfc5778i.SessionFactory;
import com.mobius.software.telco.protocols.diameter.commands.DiameterRequest;
import com.mobius.software.telco.protocols.diameter.commands.rfc5778i.EAPRequest;
import com.mobius.software.telco.protocols.diameter.commands.rfc5778i.AccountingRequest;
import com.mobius.software.telco.protocols.diameter.exceptions.DiameterException;
import com.mobius.software.telco.protocols.diameter.impl.DiameterProviderImpl;

public class Rfc5778iProviderImpl extends DiameterProviderImpl<ClientListener, ServerListener, AvpFactory, MessageFactory, SessionFactory>
{
	public Rfc5778iProviderImpl(DiameterStack stack)
	{
		super(stack, new AvpFactoryImpl(), new MessageFactoryImpl(stack.getIDGenerator()));
		setSessionFactory(new SessionFactoryImpl(this));
	}

	@Override
	public DiameterSession getNewSession(DiameterRequest message)
	{		
		try
		{
			if(message instanceof EAPRequest)
				return new Rfc5778iClientSessionImpl(true,message.getSessionId(), message.getOriginHost(), message.getOriginRealm(), this);
			else if(message instanceof AccountingRequest)
				return new Rfc5778iClientSessionImpl(false,message.getSessionId(), message.getOriginHost(), message.getOriginRealm(), this);
			
		}
		catch(DiameterException ex)
		{			
		}
		
		return null;
	}
}