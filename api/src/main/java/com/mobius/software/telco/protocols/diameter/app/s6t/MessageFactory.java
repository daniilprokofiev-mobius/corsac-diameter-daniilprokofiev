package com.mobius.software.telco.protocols.diameter.app.s6t;
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

import com.mobius.software.telco.protocols.diameter.commands.s6t.ConfigurationInformationAnswer;
import com.mobius.software.telco.protocols.diameter.commands.s6t.ConfigurationInformationRequest;
import com.mobius.software.telco.protocols.diameter.commands.s6t.NIDDInformationAnswer;
import com.mobius.software.telco.protocols.diameter.commands.s6t.NIDDInformationRequest;
import com.mobius.software.telco.protocols.diameter.commands.s6t.ReportingInformationAnswer;
import com.mobius.software.telco.protocols.diameter.commands.s6t.ReportingInformationRequest;
import com.mobius.software.telco.protocols.diameter.exceptions.AvpNotSupportedException;
import com.mobius.software.telco.protocols.diameter.exceptions.AvpOccursTooManyTimesException;
import com.mobius.software.telco.protocols.diameter.exceptions.MissingAvpException;
import com.mobius.software.telco.protocols.diameter.primitives.s6t.UserIdentifier;

public interface MessageFactory
{
	public ConfigurationInformationRequest createConfigurationInformationRequest(String originHost,String originRealm,String destinationHost,String destinationRealm, UserIdentifier userIdentifier) throws MissingAvpException, AvpNotSupportedException;			
	
	public ConfigurationInformationAnswer createConfigurationInformationAnswer(ConfigurationInformationRequest request, Long hopByHopIdentifier, Long endToEndIdentifier,Long resultCode) throws AvpOccursTooManyTimesException, MissingAvpException, AvpNotSupportedException;	
	
	public ConfigurationInformationAnswer createConfigurationInformationAnswer(String originHost,String originRealm, Long hopByHopIdentifier, Long endToEndIdentifier,Long resultCode, String sessionID) throws AvpOccursTooManyTimesException, MissingAvpException, AvpNotSupportedException;	
	
	public NIDDInformationRequest createNIDDInformationRequest(String originHost,String originRealm,String destinationHost,String destinationRealm,UserIdentifier userIdentifier) throws MissingAvpException, AvpNotSupportedException;
	
	public NIDDInformationAnswer createNIDDInformationAnswer(NIDDInformationRequest request, Long hopByHopIdentifier, Long endToEndIdentifier,Long resultCode) throws AvpOccursTooManyTimesException, MissingAvpException, AvpNotSupportedException;	
	
	public NIDDInformationAnswer createNIDDInformationAnswer(String originHost,String originRealm, Long hopByHopIdentifier, Long endToEndIdentifier,Long resultCode, String sessionID) throws AvpOccursTooManyTimesException, MissingAvpException, AvpNotSupportedException;	
	
	public ReportingInformationRequest createReportingInformationRequest(String originHost,String originRealm,String destinationHost,String destinationRealm) throws MissingAvpException, AvpNotSupportedException;
	
	public ReportingInformationAnswer createReportingInformationAnswer(ReportingInformationRequest request, Long hopByHopIdentifier, Long endToEndIdentifier,Long resultCode) throws AvpOccursTooManyTimesException, MissingAvpException, AvpNotSupportedException;	
	
	public ReportingInformationAnswer createReportingInformationAnswer(String originHost,String originRealm, Long hopByHopIdentifier, Long endToEndIdentifier,Long resultCode, String sessionID) throws AvpOccursTooManyTimesException, MissingAvpException, AvpNotSupportedException;	
}