package com.mobius.software.telco.protocols.diameter.app.nta;

import com.mobius.software.telco.protocols.diameter.commands.nta.EventConfigurationAnswer;
import com.mobius.software.telco.protocols.diameter.commands.nta.EventConfigurationRequest;
import com.mobius.software.telco.protocols.diameter.commands.nta.EventReportingAnswer;
import com.mobius.software.telco.protocols.diameter.commands.nta.EventReportingRequest;
import com.mobius.software.telco.protocols.diameter.exceptions.AvpNotSupportedException;
import com.mobius.software.telco.protocols.diameter.exceptions.MissingAvpException;

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

public interface MessageFactory
{
	public EventConfigurationRequest createEventConfigurationRequest(String originHost,String originRealm,String destinationHost,String destinationRealm,String externalIdentifier) throws MissingAvpException, AvpNotSupportedException;	
	
	public EventConfigurationAnswer createEventConfigurationAnswer(EventConfigurationRequest request,Long hopByHopIdentifier, Long endToEndIdentifier,Long resultCode) throws  MissingAvpException, AvpNotSupportedException;	
	
	public EventConfigurationAnswer createEventConfigurationAnswer(String originHost,String originRealm,Long hopByHopIdentifier, Long endToEndIdentifier,Long resultCode, String sessionID) throws  MissingAvpException, AvpNotSupportedException;
	
	public EventReportingRequest createEventReportingRequest(String originHost,String originRealm,String destinationHost,String destinationRealm) throws  MissingAvpException, AvpNotSupportedException;	
	
	public EventReportingAnswer createEventReportingAnswer(EventReportingRequest request,Long hopByHopIdentifier, Long endToEndIdentifier,Long resultCode) throws  MissingAvpException, AvpNotSupportedException;	
	
	public EventReportingAnswer createEventReportingAnswer(String originHost,String originRealm,Long hopByHopIdentifier, Long endToEndIdentifier,Long resultCode, String sessionID) throws  MissingAvpException, AvpNotSupportedException;	
	
}