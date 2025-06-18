package com.mobius.software.telco.protocols.diameter.impl.app.sh;
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

import com.mobius.software.telco.protocols.diameter.ApplicationIDs;
import com.mobius.software.telco.protocols.diameter.DiameterStack;
import com.mobius.software.telco.protocols.diameter.VendorIDs;
import com.mobius.software.telco.protocols.diameter.app.sh.MessageFactory;
import com.mobius.software.telco.protocols.diameter.commands.sh.ProfileUpdateAnswer;
import com.mobius.software.telco.protocols.diameter.commands.sh.ProfileUpdateRequest;
import com.mobius.software.telco.protocols.diameter.commands.sh.PushNotificationAnswer;
import com.mobius.software.telco.protocols.diameter.commands.sh.PushNotificationRequest;
import com.mobius.software.telco.protocols.diameter.commands.sh.SubscribeNotificationsAnswer;
import com.mobius.software.telco.protocols.diameter.commands.sh.SubscribeNotificationsRequest;
import com.mobius.software.telco.protocols.diameter.commands.sh.UserDataAnswer;
import com.mobius.software.telco.protocols.diameter.commands.sh.UserDataRequest;
import com.mobius.software.telco.protocols.diameter.exceptions.AvpNotSupportedException;
import com.mobius.software.telco.protocols.diameter.exceptions.AvpOccursTooManyTimesException;
import com.mobius.software.telco.protocols.diameter.exceptions.MissingAvpException;
import com.mobius.software.telco.protocols.diameter.impl.commands.sh.ProfileUpdateAnswerImpl;
import com.mobius.software.telco.protocols.diameter.impl.commands.sh.ProfileUpdateRequestImpl;
import com.mobius.software.telco.protocols.diameter.impl.commands.sh.PushNotificationsAnswerImpl;
import com.mobius.software.telco.protocols.diameter.impl.commands.sh.PushNotificationsRequestImpl;
import com.mobius.software.telco.protocols.diameter.impl.commands.sh.SubscribeNotificationsAnswerImpl;
import com.mobius.software.telco.protocols.diameter.impl.commands.sh.SubscribeNotificationsRequestImpl;
import com.mobius.software.telco.protocols.diameter.impl.commands.sh.UserDataAnswerImpl;
import com.mobius.software.telco.protocols.diameter.impl.commands.sh.UserDataRequestImpl;
import com.mobius.software.telco.protocols.diameter.impl.primitives.common.VendorSpecificApplicationIdImpl;
import com.mobius.software.telco.protocols.diameter.primitives.common.AuthSessionStateEnum;
import com.mobius.software.telco.protocols.diameter.primitives.common.VendorSpecificApplicationId;
import com.mobius.software.telco.protocols.diameter.primitives.sh.DataReferenceEnum;
import com.mobius.software.telco.protocols.diameter.primitives.sh.SubsReqTypeEnum;
import com.mobius.software.telco.protocols.diameter.primitives.sh.UserIdentity;

import io.netty.buffer.ByteBuf;
/**
*
* @author yulian oifa
*
*/
public class MessageFactoryImpl implements MessageFactory
{
	public static final long APPLICATION_ID=ApplicationIDs.SH;
	
	private  DiameterStack stack;
	
	private Long applicationId = APPLICATION_ID;
	
	public MessageFactoryImpl(DiameterStack stack)
	{
		this.stack = stack;
	}
	
	public MessageFactoryImpl(DiameterStack stack, long applicationId)
	{
		this.stack = stack;
		this.applicationId = applicationId;
	}
	
	public ProfileUpdateRequest createProfileUpdateRequest(String originHost,String originRealm,String destinationHost,String destinationRealm, UserIdentity userIdentity, ByteBuf userData,List<DataReferenceEnum> dataReference) throws MissingAvpException, AvpNotSupportedException, AvpOccursTooManyTimesException
	{
		VendorSpecificApplicationId appId = new VendorSpecificApplicationIdImpl(VendorIDs.TGPP_ID, applicationId, null);
		ProfileUpdateRequest request = new ProfileUpdateRequestImpl(originHost, originRealm, destinationHost, destinationRealm, false, stack.generateNewSessionID(), AuthSessionStateEnum.NO_STATE_MAINTAINED, userIdentity, userData, dataReference); 
		request.setVendorSpecificApplicationId(appId);
		return request;
	}
	
	@Override
	public ProfileUpdateAnswer createProfileUpdateAnswer(ProfileUpdateRequest request, Long hopByHopIdentifier, Long endToEndIdentifier, Long resultCode) throws AvpOccursTooManyTimesException, MissingAvpException, AvpNotSupportedException
	{
		ProfileUpdateAnswerImpl result = new  ProfileUpdateAnswerImpl(request.getDestinationHost(), request.getDestinationRealm(), false, resultCode, request.getSessionId(), AuthSessionStateEnum.NO_STATE_MAINTAINED);
		result.setHopByHopIdentifier(hopByHopIdentifier);
		result.setEndToEndIdentifier(endToEndIdentifier);
		result.setVendorSpecificApplicationId(request.getVendorSpecificApplicationId());
		return result;
	}

	@Override
	public ProfileUpdateAnswer createProfileUpdateAnswer(String originHost, String originRealm, Long hopByHopIdentifier, Long endToEndIdentifier, Long resultCode, String sessionID) throws AvpOccursTooManyTimesException, MissingAvpException, AvpNotSupportedException
	{
		VendorSpecificApplicationId appId = new VendorSpecificApplicationIdImpl(VendorIDs.TGPP_ID, applicationId, null);
		ProfileUpdateAnswerImpl result = new  ProfileUpdateAnswerImpl(originHost, originRealm, false, resultCode,  sessionID, AuthSessionStateEnum.NO_STATE_MAINTAINED);
		result.setHopByHopIdentifier(hopByHopIdentifier);
		result.setEndToEndIdentifier(endToEndIdentifier);
		result.setVendorSpecificApplicationId(appId);
		return result;
	}
	
	public PushNotificationRequest createPushNotificationRequest(String originHost,String originRealm,String destinationHost,String destinationRealm, UserIdentity userIdentity, ByteBuf userData) throws MissingAvpException, AvpNotSupportedException, AvpOccursTooManyTimesException
	{
		VendorSpecificApplicationId appId = new VendorSpecificApplicationIdImpl(VendorIDs.TGPP_ID, applicationId, null);
		PushNotificationRequest request = new PushNotificationsRequestImpl(originHost, originRealm, destinationHost, destinationRealm, false, stack.generateNewSessionID(), AuthSessionStateEnum.NO_STATE_MAINTAINED, userIdentity, userData); 
		request.setVendorSpecificApplicationId(appId);
		return request;
	}
	
	@Override
	public PushNotificationAnswer createPushNotificationAnswer(PushNotificationRequest request, Long hopByHopIdentifier, Long endToEndIdentifier, Long resultCode) throws AvpOccursTooManyTimesException, MissingAvpException, AvpNotSupportedException
	{
		PushNotificationsAnswerImpl result = new  PushNotificationsAnswerImpl(request.getDestinationHost(), request.getDestinationRealm(), false, resultCode, request.getSessionId(), AuthSessionStateEnum.NO_STATE_MAINTAINED);
		result.setHopByHopIdentifier(hopByHopIdentifier);
		result.setEndToEndIdentifier(endToEndIdentifier);
		result.setVendorSpecificApplicationId(request.getVendorSpecificApplicationId());
		return result;
	}

	@Override
	public PushNotificationAnswer createPushNotificationAnswer(String originHost, String originRealm, Long hopByHopIdentifier, Long endToEndIdentifier, Long resultCode, String sessionID) throws AvpOccursTooManyTimesException, MissingAvpException, AvpNotSupportedException
	{
		VendorSpecificApplicationId appId = new VendorSpecificApplicationIdImpl(VendorIDs.TGPP_ID, applicationId, null);
		PushNotificationsAnswerImpl result = new  PushNotificationsAnswerImpl(originHost, originRealm, false, resultCode,  sessionID, AuthSessionStateEnum.NO_STATE_MAINTAINED);
		result.setHopByHopIdentifier(hopByHopIdentifier);
		result.setEndToEndIdentifier(endToEndIdentifier);
		result.setVendorSpecificApplicationId(appId);
		return result;
	}
	
	public SubscribeNotificationsRequest createSubscribeNotificationsRequest(String originHost,String originRealm,String destinationHost,String destinationRealm, UserIdentity userIdentity, SubsReqTypeEnum subsReqType, List<DataReferenceEnum> dataReference) throws MissingAvpException, AvpNotSupportedException, AvpOccursTooManyTimesException
	{
		VendorSpecificApplicationId appId = new VendorSpecificApplicationIdImpl(VendorIDs.TGPP_ID, applicationId, null);
		SubscribeNotificationsRequest request = new SubscribeNotificationsRequestImpl(originHost, originRealm, destinationHost, destinationRealm, false, stack.generateNewSessionID(), AuthSessionStateEnum.NO_STATE_MAINTAINED, userIdentity, subsReqType, dataReference); 
		request.setVendorSpecificApplicationId(appId);
		return request;
	}
	
	@Override
	public SubscribeNotificationsAnswer createSubscribeNotificationsAnswer(SubscribeNotificationsRequest request, Long hopByHopIdentifier, Long endToEndIdentifier, Long resultCode) throws AvpOccursTooManyTimesException, MissingAvpException, AvpNotSupportedException
	{
		SubscribeNotificationsAnswerImpl result = new  SubscribeNotificationsAnswerImpl(request.getDestinationHost(), request.getDestinationRealm(), false, resultCode, request.getSessionId(), AuthSessionStateEnum.NO_STATE_MAINTAINED);
		result.setHopByHopIdentifier(hopByHopIdentifier);
		result.setEndToEndIdentifier(endToEndIdentifier);
		result.setVendorSpecificApplicationId(request.getVendorSpecificApplicationId());
		return result;
	}

	@Override
	public SubscribeNotificationsAnswer createSubscribeNotificationsAnswer(String originHost, String originRealm, Long hopByHopIdentifier, Long endToEndIdentifier, Long resultCode, String sessionID) throws AvpOccursTooManyTimesException, MissingAvpException, AvpNotSupportedException
	{
		VendorSpecificApplicationId appId = new VendorSpecificApplicationIdImpl(VendorIDs.TGPP_ID, applicationId, null);
		SubscribeNotificationsAnswerImpl result = new  SubscribeNotificationsAnswerImpl(originHost, originRealm, false, resultCode,  sessionID, AuthSessionStateEnum.NO_STATE_MAINTAINED);
		result.setHopByHopIdentifier(hopByHopIdentifier);
		result.setEndToEndIdentifier(endToEndIdentifier);
		result.setVendorSpecificApplicationId(appId);
		return result;
	}
	
	public UserDataRequest createUserDataRequest(String originHost,String originRealm,String destinationHost,String destinationRealm, UserIdentity userIdentity, List<DataReferenceEnum> dataReference) throws MissingAvpException, AvpNotSupportedException, AvpOccursTooManyTimesException
	{
		VendorSpecificApplicationId appId = new VendorSpecificApplicationIdImpl(VendorIDs.TGPP_ID, applicationId, null);
		UserDataRequest request = new UserDataRequestImpl(originHost, originRealm, destinationHost, destinationRealm, false, stack.generateNewSessionID(), AuthSessionStateEnum.NO_STATE_MAINTAINED, userIdentity, dataReference);
		request.setVendorSpecificApplicationId(appId);
		return request;
	}
	
	@Override
	public UserDataAnswer createUserDataAnswer(UserDataRequest request, Long hopByHopIdentifier, Long endToEndIdentifier, Long resultCode) throws AvpOccursTooManyTimesException, MissingAvpException, AvpNotSupportedException
	{
		UserDataAnswerImpl result = new  UserDataAnswerImpl(request.getDestinationHost(), request.getDestinationRealm(), false, resultCode, request.getSessionId(), AuthSessionStateEnum.NO_STATE_MAINTAINED);
		result.setHopByHopIdentifier(hopByHopIdentifier);
		result.setEndToEndIdentifier(endToEndIdentifier);
		result.setVendorSpecificApplicationId(request.getVendorSpecificApplicationId());
		return result;
	}

	@Override
	public UserDataAnswer createUserDataAnswer(String originHost, String originRealm, Long hopByHopIdentifier, Long endToEndIdentifier, Long resultCode, String sessionID) throws AvpOccursTooManyTimesException, MissingAvpException, AvpNotSupportedException
	{
		VendorSpecificApplicationId appId = new VendorSpecificApplicationIdImpl(VendorIDs.TGPP_ID, applicationId, null);
		UserDataAnswerImpl result = new  UserDataAnswerImpl(originHost, originRealm, false, resultCode,  sessionID, AuthSessionStateEnum.NO_STATE_MAINTAINED);
		result.setHopByHopIdentifier(hopByHopIdentifier);
		result.setEndToEndIdentifier(endToEndIdentifier);
		result.setVendorSpecificApplicationId(appId);
		return result;
	}
}