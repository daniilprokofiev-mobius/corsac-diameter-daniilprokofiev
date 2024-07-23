package com.mobius.software.telco.protocols.diameter.impl.app.creditcontrol.huawei;
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
import com.mobius.software.telco.protocols.diameter.app.ClientCCListener;
import com.mobius.software.telco.protocols.diameter.app.creditcontrol.huawei.HuaweiCreditControlClientSession;
import com.mobius.software.telco.protocols.diameter.commands.creditcontrol.huawei.AbortSessionAnswer;
import com.mobius.software.telco.protocols.diameter.commands.creditcontrol.huawei.AbortSessionRequest;
import com.mobius.software.telco.protocols.diameter.commands.creditcontrol.huawei.CreditControlAnswer;
import com.mobius.software.telco.protocols.diameter.commands.creditcontrol.huawei.CreditControlRequest;
import com.mobius.software.telco.protocols.diameter.commands.creditcontrol.huawei.ReAuthAnswer;
import com.mobius.software.telco.protocols.diameter.commands.creditcontrol.huawei.ReAuthRequest;
import com.mobius.software.telco.protocols.diameter.commands.creditcontrol.huawei.SessionTerminationAnswer;
import com.mobius.software.telco.protocols.diameter.commands.creditcontrol.huawei.SessionTerminationRequest;
import com.mobius.software.telco.protocols.diameter.impl.app.ClientCCSessionImpl;

public class HuaweiCreditControlClientSessionImpl extends ClientCCSessionImpl<CreditControlRequest, CreditControlAnswer,ReAuthRequest,ReAuthAnswer,AbortSessionRequest,AbortSessionAnswer,SessionTerminationRequest,SessionTerminationAnswer> implements HuaweiCreditControlClientSession
{
	public HuaweiCreditControlClientSessionImpl(String sessionID, String remoteHost, String remoteRealm, DiameterProvider<? extends ClientCCListener<CreditControlRequest,CreditControlAnswer,ReAuthRequest,ReAuthAnswer,AbortSessionRequest,AbortSessionAnswer,SessionTerminationRequest,SessionTerminationAnswer>, ?, ?, ?, ?> provider)
	{
		super(sessionID, remoteHost, remoteRealm, provider);
	}
}