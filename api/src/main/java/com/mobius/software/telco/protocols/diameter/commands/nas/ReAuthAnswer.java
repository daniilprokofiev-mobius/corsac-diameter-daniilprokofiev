package com.mobius.software.telco.protocols.diameter.commands.nas;
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

import java.util.List;

import com.mobius.software.telco.protocols.diameter.annotations.DiameterCommandDefinition;
import com.mobius.software.telco.protocols.diameter.primitives.common.ReAuthRequestTypeEnum;
import com.mobius.software.telco.protocols.diameter.primitives.nas.OriginAAAProtocolEnum;
import com.mobius.software.telco.protocols.diameter.primitives.nas.PromptEnum;
import com.mobius.software.telco.protocols.diameter.primitives.nas.ServiceTypeEnum;

import io.netty.buffer.ByteBuf;

/**
*
* @author yulian oifa
*
*/

/*
 * <RA-Answer>  ::= < Diameter Header: 258, PXY >
                          < Session-Id >
                          { Result-Code }
                          { Origin-Host }
                          { Origin-Realm }
                          [ User-Name ]
                          [ Origin-AAA-Protocol ]
                          [ Origin-State-Id ]
                          [ Error-Message ]
                          [ Error-Reporting-Host ]
                        * [ Failed-AVP ]
                        * [ Redirected-Host ]
                          [ Redirected-Host-Usage ]
                          [ Redirected-Host-Cache-Time ]
                          [ Service-Type ]
                        * [ Configuration-Token ]
                          [ Idle-Timeout ]
                          [ Authorization-Lifetime ]
                          [ Auth-Grace-Period ]
                          [ Re-Auth-Request-Type ]
                          [ State ]
                        * [ Class ]
                        * [ Reply-Message ]
                          [ Prompt ]
                        * [ Proxy-Info ]
                        * [ AVP ]
 */
@DiameterCommandDefinition(applicationId = 1, commandCode = 258, request = false, proxyable = true, name="Re-Auth-Answer")
public interface ReAuthAnswer extends com.mobius.software.telco.protocols.diameter.commands.commons.ReAuthAnswer
{
	OriginAAAProtocolEnum getOriginAAAProtocol();
	
	void setOriginAAAProtocol(OriginAAAProtocolEnum value);	
	
	ServiceTypeEnum getServiceType();
	
	void setServiceType(ServiceTypeEnum value);	
	
	List<ByteBuf> getConfigurationToken();

	void setConfigurationToken(List<ByteBuf> value);	
	
	Long getIdleTimeout();
	
	void setIdleTimeout(Long value);	
	
	Long getAuthorizationLifetime();
	
	void setAuthorizationLifetime(Long value);	
	
	Long getAuthGracePeriod();
	
	void setAuthGracePeriod(Long value);	
	
	ReAuthRequestTypeEnum getReAuthRequestType();
	
	void setReAuthRequestType(ReAuthRequestTypeEnum value);	
	
	ByteBuf getState();
	
	void setState(ByteBuf value);	
	
	List<ByteBuf> getDiameterClass();

	void setDiameterClass(List<ByteBuf> value);	
	
	List<String> getReplyMessage();

	void setReplyMessage(List<String> value);
	
	PromptEnum getPrompt();
	
	void setPrompt(PromptEnum value);		
}