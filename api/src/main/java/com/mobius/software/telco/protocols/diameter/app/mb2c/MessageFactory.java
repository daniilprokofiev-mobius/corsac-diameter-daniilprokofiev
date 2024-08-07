package com.mobius.software.telco.protocols.diameter.app.mb2c;
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

import com.mobius.software.telco.protocols.diameter.commands.mb2c.GCSActionAnswer;
import com.mobius.software.telco.protocols.diameter.commands.mb2c.GCSActionRequest;
import com.mobius.software.telco.protocols.diameter.commands.mb2c.GCSNotificationAnswer;
import com.mobius.software.telco.protocols.diameter.commands.mb2c.GCSNotificationRequest;
import com.mobius.software.telco.protocols.diameter.exceptions.AvpNotSupportedException;
import com.mobius.software.telco.protocols.diameter.exceptions.AvpOccursTooManyTimesException;
import com.mobius.software.telco.protocols.diameter.exceptions.MissingAvpException;

public interface MessageFactory
{
	public GCSNotificationRequest createGCSNotificationRequest(String originHost,String originRealm,String destinationHost, String destinationRealm) throws MissingAvpException, AvpNotSupportedException;	
	
	public GCSNotificationAnswer createGCSNotificationAnswer(GCSNotificationRequest request, Long hopByHopIdentifier, Long endToEndIdentifier,Long resultCode) throws AvpOccursTooManyTimesException, MissingAvpException, AvpNotSupportedException;	
	
	public GCSNotificationAnswer createGCSNotificationAnswer(String originHost,String originRealm, Long hopByHopIdentifier, Long endToEndIdentifier,Long resultCode, String sessionID) throws AvpOccursTooManyTimesException, MissingAvpException, AvpNotSupportedException;	
	
	public GCSActionRequest createGCSActionRequest(String originHost,String originRealm,String destinationHost,String destinationRealm) throws MissingAvpException, AvpNotSupportedException;
	
	public GCSActionAnswer createGCSActionAnswer(GCSActionRequest request, Long hopByHopIdentifier, Long endToEndIdentifier,Long resultCode, Long authApplicationId) throws AvpOccursTooManyTimesException, MissingAvpException, AvpNotSupportedException;	
	
	public GCSActionAnswer createGCSActionAnswer(String originHost,String originRealm, Long hopByHopIdentifier, Long endToEndIdentifier,Long resultCode, String sessionID, Long authApplicationId) throws AvpOccursTooManyTimesException, MissingAvpException, AvpNotSupportedException;	
	
}