package com.mobius.software.telco.protocols.diameter.impl;
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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restcomm.protocols.api.Association;
import org.restcomm.protocols.api.AssociationListener;
import org.restcomm.protocols.api.AssociationType;
import org.restcomm.protocols.api.IpChannelType;
import org.restcomm.protocols.api.Management;
import org.restcomm.protocols.api.PayloadData;
import org.restcomm.protocols.api.Server;

import com.mobius.software.telco.protocols.diameter.AsyncCallback;
import com.mobius.software.telco.protocols.diameter.DiameterLink;
import com.mobius.software.telco.protocols.diameter.DiameterStack;
import com.mobius.software.telco.protocols.diameter.NetworkListener;
import com.mobius.software.telco.protocols.diameter.PeerStateEnum;
import com.mobius.software.telco.protocols.diameter.ResultCodes;
import com.mobius.software.telco.protocols.diameter.annotations.DiameterCommandDefinition;
import com.mobius.software.telco.protocols.diameter.commands.DiameterAnswer;
import com.mobius.software.telco.protocols.diameter.commands.DiameterErrorAnswer;
import com.mobius.software.telco.protocols.diameter.commands.DiameterMessage;
import com.mobius.software.telco.protocols.diameter.commands.DiameterRequest;
import com.mobius.software.telco.protocols.diameter.commands.commons.AccountingAnswer;
import com.mobius.software.telco.protocols.diameter.commands.commons.AccountingRequest;
import com.mobius.software.telco.protocols.diameter.commands.commons.CapabilitiesExchangeAnswer;
import com.mobius.software.telco.protocols.diameter.commands.commons.CapabilitiesExchangeRequest;
import com.mobius.software.telco.protocols.diameter.commands.commons.DeviceWatchdogAnswer;
import com.mobius.software.telco.protocols.diameter.commands.commons.DeviceWatchdogRequest;
import com.mobius.software.telco.protocols.diameter.commands.commons.DisconnectPeerAnswer;
import com.mobius.software.telco.protocols.diameter.commands.commons.DisconnectPeerRequest;
import com.mobius.software.telco.protocols.diameter.commands.commons.VendorSpecificAnswer;
import com.mobius.software.telco.protocols.diameter.commands.commons.VendorSpecificRequest;
import com.mobius.software.telco.protocols.diameter.exceptions.AvpNotSupportedException;
import com.mobius.software.telco.protocols.diameter.exceptions.DiameterException;
import com.mobius.software.telco.protocols.diameter.exceptions.MissingAvpException;
import com.mobius.software.telco.protocols.diameter.impl.commands.DiameterErrorAnswerImpl;
import com.mobius.software.telco.protocols.diameter.impl.commands.DiameterErrorAnswerWithSessionImpl;
import com.mobius.software.telco.protocols.diameter.impl.commands.common.CapabilitiesExchangeAnswerImpl;
import com.mobius.software.telco.protocols.diameter.impl.commands.common.CapabilitiesExchangeRequestImpl;
import com.mobius.software.telco.protocols.diameter.impl.commands.common.DeviceWatchdogAnswerImpl;
import com.mobius.software.telco.protocols.diameter.impl.commands.common.DeviceWatchdogRequestImpl;
import com.mobius.software.telco.protocols.diameter.impl.commands.common.DisconnectPeerAnswerImpl;
import com.mobius.software.telco.protocols.diameter.impl.commands.common.DisconnectPeerRequestImpl;
import com.mobius.software.telco.protocols.diameter.impl.primitives.common.AcctApplicationIdImpl;
import com.mobius.software.telco.protocols.diameter.parser.DiameterParser;
import com.mobius.software.telco.protocols.diameter.primitives.common.DisconnectCauseEnum;
import com.mobius.software.telco.protocols.diameter.primitives.common.VendorSpecificApplicationId;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;

/**
 *
 * @author yulian oifa
 *
 */
public class DiameterLinkImpl implements DiameterLink, AssociationListener
{
	public static Logger logger = LogManager.getLogger(DiameterLinkImpl.class);
	public static final Integer DIAMETER_SCTP_PROTOCOL_IDENTIFIER = 46;
	private DiameterParser parser;
	private Management management;
	private Association association;
	private Server server;
	private AtomicReference<PeerStateEnum> peerState = new AtomicReference<PeerStateEnum>(PeerStateEnum.IDLE);
	// data buffer for incoming TCP data
	private CompositeByteBuf tcpBuffer = Unpooled.buffer().alloc().compositeBuffer();
	private String linkId;
	private String localHost;
	private String localRealm;
	private String destinationHost;
	private String destinationRealm;
	private InetAddress remoteAddress;
	private InetAddress localAddress;
	private AtomicInteger wheel = new AtomicInteger(0);
	private Integer maxStreams = 1;
	private List<VendorSpecificApplicationId> applicationIds = new ArrayList<VendorSpecificApplicationId>();
	private List<Long> authApplicationIds = new ArrayList<Long>();
	private List<Long> acctApplicationIds = new ArrayList<Long>();
	private AtomicReference<List<VendorSpecificApplicationId>> remoteApplicationIds = new AtomicReference<List<VendorSpecificApplicationId>>();
	private AtomicReference<List<Long>> remoteAuthApplicationIds = new AtomicReference<List<Long>>();
	private AtomicReference<List<Long>> remoteAcctApplicationIds = new AtomicReference<List<Long>>();
	private ConcurrentHashMap<Long, Package> authApplicationPackages = new ConcurrentHashMap<Long, Package>();
	private ConcurrentHashMap<Long, Package> acctApplicationPackages = new ConcurrentHashMap<Long, Package>();
	private DiameterStack stack;
	private Boolean rejectUnmandatoryAvps;
	private Long inactivityTimeout;
	private Long reconnectTimeout;
	private InactivityTimer inactivityTimer;
	private DisconnectTimer disconnectTimer;
	private ReconnectTimer reconnectTimer;
	private AtomicLong lastActivity = new AtomicLong(System.currentTimeMillis());
	private AtomicBoolean waitingForDWA = new AtomicBoolean(false);
	private ConcurrentHashMap<String, NetworkListener> genericListeners;

	public DiameterLinkImpl(DiameterStack stack, Management management, ConcurrentHashMap<String, NetworkListener> genericListeners, String linkId, InetAddress remoteAddress, Integer remotePort, InetAddress localAddress, Integer localPort, Boolean isServer, Boolean isSctp, String localHost, String localRealm, String destinationHost, String destinationRealm, Boolean rejectUnmandatoryAvps, Long inactivityTimeout, Long responseTimeout, Long reconnectTimeout) throws DiameterException
	{
		this.parser = new DiameterParser(stack.getClassLoader(), Arrays.asList(new Class<?>[] { DiameterErrorAnswerImpl.class, DiameterErrorAnswerWithSessionImpl.class }), Package.getPackage("com.mobius.software.telco.protocols.diameter.impl.primitives"));
		this.genericListeners = genericListeners;
		this.linkId = linkId;
		if (remoteAddress == null)
			throw new DiameterException("The remote address can not be null", null, ResultCodes.DIAMETER_UNKNOWN_PEER, null);
		if (localAddress == null)
			throw new DiameterException("The local address can not be null", null, ResultCodes.DIAMETER_UNKNOWN_PEER, null);
		IpChannelType channelType = IpChannelType.SCTP;
		if (isSctp == null || !isSctp)
			channelType = IpChannelType.TCP;
		this.management = management;
		this.localHost = localHost;
		this.localRealm = localRealm;
		this.destinationHost = destinationHost;
		this.destinationRealm = destinationRealm;
		this.localAddress = localAddress;
		this.remoteAddress = remoteAddress;
		if (isServer != null && isServer)
		{
			if (localPort == null || localPort < 1)
				throw new DiameterException("The local port can not be null and should be positive", null, ResultCodes.DIAMETER_UNKNOWN_PEER, null);
			if (remotePort != null && remotePort < 0)
				throw new DiameterException("The remote port should be positive or null", null, ResultCodes.DIAMETER_UNKNOWN_PEER, null);
			for (Server server : management.getServers())
			{
				if (server.getIpChannelType().equals(channelType) && server.getHostAddress().equals(localAddress.getHostAddress()) && localPort.equals(server.getHostport()))
				{
					this.server = server;
					break;
				}
			}
			StringBuilder serverNameBuilder = new StringBuilder();
			serverNameBuilder.append(channelType.name()).append("://").append(localAddress.getHostAddress()).append(":").append(localPort);
			String serverName = serverNameBuilder.toString();
			if (server == null)
			{
				try
				{
					server = management.addServer(serverName, localAddress.getHostAddress(), localPort, channelType, null);
					management.startServer(serverName);
				}
				catch (Exception ex)
				{
					throw new DiameterException("An error occured while establishing a peer", null, ResultCodes.DIAMETER_UNKNOWN_PEER, null);
				}
			}
			try
			{
				this.association = management.addServerAssociation(remoteAddress.getHostAddress(), remotePort, serverName, linkId, channelType);
			}
			catch (Exception ex)
			{
				logger.warn("An error occured while adding server association," + ex.getMessage(), ex);
				throw new DiameterException("An error occured while establishing a peer", null, ResultCodes.DIAMETER_UNKNOWN_PEER, null);
			}
		}
		else
		{
			if (localPort == null || localPort < 0)
				throw new DiameterException("The local port can not be null and should be zero or positive", null, ResultCodes.DIAMETER_UNKNOWN_PEER, null);
			if (remotePort == null || remotePort < 1)
				throw new DiameterException("The remote port can not be null and should be positive", null, ResultCodes.DIAMETER_UNKNOWN_PEER, null);
			try
			{
				association = management.addAssociation(localAddress.getHostAddress(), localPort, remoteAddress.getHostAddress(), remotePort, linkId, channelType, null);
			}
			catch (Exception ex)
			{
				throw new DiameterException("An error occured while establishing a peer", null, ResultCodes.DIAMETER_UNKNOWN_PEER, null);
			}
		}
		association.setAssociationListener(this);
		// registering common
		@SuppressWarnings("unused")
		Class<?> clazz = CapabilitiesExchangeRequestImpl.class;
		@SuppressWarnings("unused")
		Class<?> avpClass = AcctApplicationIdImpl.class;
		parser.registerApplication(stack.getClassLoader(), Package.getPackage("com.mobius.software.telco.protocols.diameter.impl.commands.common"));
		this.stack = stack;
		this.rejectUnmandatoryAvps = rejectUnmandatoryAvps;
		this.inactivityTimeout = inactivityTimeout;
		this.reconnectTimeout = reconnectTimeout;
		this.inactivityTimer = new InactivityTimer(this, lastActivity, waitingForDWA, inactivityTimeout, responseTimeout);
		this.disconnectTimer = new DisconnectTimer(this);
		this.reconnectTimer = new ReconnectTimer(this);
	}

	@Override
	public void stop(Boolean remove) throws DiameterException
	{
		try
		{
			if (isStarted())
				this.management.stopAssociation(association.getName());
			if (remove)
				this.management.removeAssociation(association.getName());
		}
		catch (Exception ex)
		{
			throw new DiameterException(ex.getMessage(), null, ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);
		}
	}

	@Override
	public void start() throws DiameterException
	{
		try
		{
			this.management.startAssociation(association.getName());
		}
		catch (Exception ex)
		{
			throw new DiameterException(ex.getMessage(), null, ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);
		}
	}

	@Override
	public boolean isStarted()
	{
		return association.isStarted();
	}

	@Override
	public boolean isConnected()
	{
		return association.isConnected() && getPeerState().equals(PeerStateEnum.OPEN);
	}

	@Override
	public boolean isUp()
	{
		return association.isUp();
	}

	@Override
	public InetAddress getRemoteAddress()
	{
		return remoteAddress;
	}

	@Override
	public Integer getRemotePort()
	{
		return association.getPeerPort();
	}

	@Override
	public InetAddress getLocalAddress()
	{
		return localAddress;
	}

	@Override
	public Integer getLocalPort()
	{
		return association.getHostPort();
	}

	@Override
	public Boolean isServer()
	{
		return association.getAssociationType() != AssociationType.CLIENT;
	}

	@Override
	public Boolean isSctp()
	{
		return association.getIpChannelType() == IpChannelType.SCTP;
	}

	@Override
	public String getLocalHost()
	{
		return localHost;
	}

	@Override
	public String getLocalRealm()
	{
		return localRealm;
	}

	@Override
	public String getDestinationHost()
	{
		return destinationHost;
	}

	@Override
	public String getDestinationRealm()
	{
		return destinationRealm;
	}

	@Override
	public void onCommunicationUp(Association association, int maxInboundStreams, int maxOutboundStreams)
	{
		this.maxStreams = maxOutboundStreams;
		if (!isServer())
			sendCER();
	}

	@Override
	public void onCommunicationShutdown(Association association)
	{
		logger.info("Association ," + association + " shutdown");
		setPeerState(PeerStateEnum.IDLE);
		inactivityTimer.stop();
	}

	@Override
	public void onCommunicationLost(Association association)
	{
		logger.info("Association ," + association + " communication lost");
		setPeerState(PeerStateEnum.IDLE);
		inactivityTimer.stop();
	}

	@Override
	public void onCommunicationRestart(Association association)
	{
		logger.info("Association ," + association + " communication restart");
		setPeerState(PeerStateEnum.IDLE);
		inactivityTimer.stop();
	}

	@Override
	public void inValidStreamId(PayloadData payloadData)
	{
		logger.info("Association ," + association + " invalid stream id");
	}

	@Override
	public Boolean canSendMessage(DiameterMessage message)
	{
		if (logger.isDebugEnabled())
			logger.debug("Checking if link " + getID() + "  can send message " + message.getClass().getName());
		if (!getPeerState().equals(PeerStateEnum.OPEN))
			return false;
		DiameterCommandDefinition commandDefintion = DiameterParser.getCommandDefinition(message.getClass());
		if (commandDefintion == null)
			return false;
		if (message instanceof AccountingRequest || message instanceof AccountingAnswer)
		{
			if (logger.isDebugEnabled())
				logger.debug("It is accounting message " + message.getClass().getName());
			Boolean found = false;
			List<Long> remoteIds = remoteAcctApplicationIds.get();
			if (remoteIds != null)
			{
				for (Long currApplicationId : remoteIds)
				{
					if (currApplicationId.equals(commandDefintion.applicationId()))
					{
						if (logger.isDebugEnabled())
							logger.debug("Acct application id matching, application id " + currApplicationId);
						found = true;
					}
					else
					{
						if (logger.isDebugEnabled())
							logger.debug("Acct application id not matching, remote id " + currApplicationId + ",message acct id " + commandDefintion.applicationId());
					}
				}
			}
			else
			{
				if (logger.isDebugEnabled())
					logger.debug("Remote acct application ids are empty for link " + getID());
			}
			if (!found)
				return false;
		}
		else if (message instanceof VendorSpecificRequest || message instanceof VendorSpecificAnswer)
		{
			if (logger.isDebugEnabled())
				logger.debug("It is vendor specific message " + message.getClass().getName());
			VendorSpecificApplicationId appId = null;
			if (message instanceof VendorSpecificRequest)
				appId = ((VendorSpecificRequest) message).getVendorSpecificApplicationId();
			else
				appId = ((VendorSpecificAnswer) message).getVendorSpecificApplicationId();
			Boolean found = false;
			List<VendorSpecificApplicationId> remoteVendorIds = remoteApplicationIds.get();
			if (remoteVendorIds != null && appId != null)
			{
				for (VendorSpecificApplicationId currApplicationId : remoteVendorIds)
				{
					if (sameVendorSpecificApplicationId(appId, currApplicationId))
					{
						found = true;
						break;
					}
					else
					{
						if (logger.isDebugEnabled())
							logger.debug("Vendor id not matching Ids remote vendor id[" + currApplicationId.getVendorId() + "," + currApplicationId.getAuthApplicationId() + "," + currApplicationId.getAcctApplicationId() + "],message vendor id[" + appId.getVendorId() + "," + appId.getAuthApplicationId() + "," + appId.getAcctApplicationId() + "]");
					}
				}
			}
			else
			{
				if (logger.isDebugEnabled())
					logger.debug("No vendor specific application Ids for link " + getID());
			}
			if (!found && appId != null)
			{
				if (logger.isDebugEnabled())
					logger.debug("No vendor application id has been found trying to check based on auth/acct application id message " + message.getClass().getName());
				if (appId.getAcctApplicationId() != null)
				{
					if (logger.isDebugEnabled())
						logger.debug("Checking based on acct application id message " + message.getClass().getName());
					List<Long> remoteIds = remoteAcctApplicationIds.get();
					if (remoteIds != null)
					{
						for (Long currApplicationId : remoteIds)
						{
							if (currApplicationId.equals(commandDefintion.applicationId()))
							{
								if (logger.isDebugEnabled())
									logger.debug("Acct application id matching, application id " + currApplicationId);
								found = true;
							}
							else
							{
								if (logger.isDebugEnabled())
									logger.debug("Acct application id not matching, remote id " + currApplicationId + ",message acct id " + commandDefintion.applicationId());
							}
						}
					}
					else
					{
						if (logger.isDebugEnabled())
							logger.debug("Remote acct application ids are empty for link " + getID());
					}
				}
				else if (appId.getAuthApplicationId() != null)
				{
					if (logger.isDebugEnabled())
						logger.debug("Checking based on auth application id message " + message.getClass().getName());
					List<Long> remoteIds = remoteAuthApplicationIds.get();
					if (remoteIds != null)
					{
						for (Long currApplicationId : remoteIds)
						{
							if (currApplicationId.equals(commandDefintion.applicationId()))
							{
								if (logger.isDebugEnabled())
									logger.debug("Auth application id matching, application id " + currApplicationId);
								found = true;
							}
							else
							{
								if (logger.isDebugEnabled())
									logger.debug("Auth application id not matching, remote id " + currApplicationId + ",message acct id " + commandDefintion.applicationId());
							}
						}
					}
					else
					{
						if (logger.isDebugEnabled())
							logger.debug("Remote auth application ids are empty for link " + getID());
					}
				}
			}
			if (!found)
				return false;
			return true;
		}
		else
		{
			if (logger.isDebugEnabled())
				logger.debug("It is authentication message " + message.getClass().getName());
			Boolean found = false;
			List<Long> remoteIds = remoteAuthApplicationIds.get();
			if (remoteIds != null && remoteIds.size() > 0)
			{
				for (Long currApplicationId : remoteIds)
				{
					if (currApplicationId.equals(commandDefintion.applicationId()))
					{
						if (logger.isDebugEnabled())
							logger.debug("Auth application id matching, application id " + currApplicationId);
						found = true;
					}
					else
					{
						if (logger.isDebugEnabled())
							logger.debug("Auth application id not matching, remote id " + currApplicationId + ",message acct id " + commandDefintion.applicationId());
					}
				}
			}
			else
			{
				if (logger.isDebugEnabled())
					logger.debug("Remote auth application ids are empty for link " + getID());
			}
			if (!found)
				return false;
		}
		return true;
	}

	public ByteBuf sendMessage(DiameterMessage message, AsyncCallback callback)
	{
		if (!getPeerState().equals(PeerStateEnum.OPEN))
		{
			callback.onError(new DiameterException("Invalid state for peer while sending message , current state " + getPeerState(), null, ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null));
			return null;
		}
		DiameterCommandDefinition commandDefintion = DiameterParser.getCommandDefinition(message.getClass());
		if (commandDefintion == null)
		{
			callback.onError(new DiameterException("Command not registered in parser for class " + message.getClass().getCanonicalName(), null, ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null));
			return null;
		}
		if (!canSendMessage(message))
		{
			callback.onError(new DiameterException("Application id is not supported by peer, " + commandDefintion.applicationId(), null, ResultCodes.DIAMETER_APPLICATION_UNSUPPORTED, null));
			return null;
		}
		return sendMessageInternally(message, callback);
	}

	private ByteBuf sendMessageInternally(DiameterMessage message, AsyncCallback callback)
	{		
		if (message.getOriginHost() == null)
		{
			try
			{
				message.setOriginHost(localHost);
			}
			catch (MissingAvpException ex)
			{
				// will not happen value is set
			}
		}
		if (message.getOriginRealm() == null)
		{
			try
			{
				message.setOriginRealm(localRealm);
			}
			catch (MissingAvpException ex)
			{
				// will not happen value is set
			}
		}
		if (message instanceof DiameterRequest)
		{
			DiameterRequest request = (DiameterRequest) message;
			try
			{
				if (request.isDestinationHostRequred() && request.getDestinationHost() == null)
					request.setDestinationHost(destinationHost);
			}
			catch (DiameterException ex)
			{
				// may be not supported
			}
			try
			{
				if (request.getDestinationRealm() == null)
					request.setDestinationRealm(destinationRealm);
			}
			catch (DiameterException ex)
			{
				// may be not supported
			}
		}
		stack.messageSent(message, linkId);
		PayloadData payloadData = null;
		ByteBuf buffer = null;
		ByteBuf copiedBuffer = null;
		try
		{
			buffer = parser.encode(message);
			copiedBuffer = Unpooled.copiedBuffer(buffer);
			payloadData = new PayloadData(buffer.readableBytes(), buffer, true, false, DIAMETER_SCTP_PROTOCOL_IDENTIFIER, wheel.incrementAndGet() % maxStreams);
		}
		catch (DiameterException ex)
		{
			callback.onError(ex);
			return null;
		}
		try
		{
			association.send(payloadData);
			lastActivity.set(System.currentTimeMillis());
			callback.onSuccess();
		}
		catch (Exception ex)
		{
			callback.onError(new DiameterException(ex.getMessage(), null, ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null));
		}
		return copiedBuffer;
	}

	public void sendEncodedMessage(ByteBuf buffer, AsyncCallback callback)
	{
		try
		{			
			PayloadData payloadData = new PayloadData(buffer.readableBytes(), buffer, true, false, DIAMETER_SCTP_PROTOCOL_IDENTIFIER, wheel.incrementAndGet() % maxStreams);
			association.send(payloadData);
			lastActivity.set(System.currentTimeMillis());
			callback.onSuccess();
		}
		catch (Exception ex)
		{
			callback.onError(new DiameterException(ex.getMessage(), null, ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null));
		}
	}

	@Override
	public void onPayload(Association association, PayloadData payloadData)
	{		
		if (logger.isDebugEnabled())
			logger.debug(String.format("Diameter Message received on link=%s %s", this.getID(), payloadData));

		ByteBuf buffer = payloadData.getByteBuf();
		if (association.getIpChannelType() == IpChannelType.TCP)
		{
			tcpBuffer.addComponent(true,buffer);
			buffer = tcpBuffer;
		}

		if (buffer.readableBytes() == 0)
			return;

		DiameterMessage message;
		do
		{
			message = null;
			try
			{
				message = parser.decode(buffer, rejectUnmandatoryAvps);			
				if (message != null)
				{
					String sessionID = null;
					try
					{
						sessionID = message.getSessionId();
					}
					catch(DiameterException ex)
					{
						// sessionless messages are for the link itself
						sessionID = linkId;
					}
					
					Boolean discardBytes = (association.getIpChannelType() == IpChannelType.TCP); 
					stack.getWorkerPool().addTaskLast(new MessageProcessingTask(stack, this, genericListeners, lastActivity, waitingForDWA, association, buffer, sessionID, message, remoteApplicationIds, remoteAuthApplicationIds, remoteAcctApplicationIds, discardBytes));
				}
			}
			catch (DiameterException ex)
			{
				logger.warn("An error occured while parsing incoming message " + ex.getMessage() + " from " + association, ex);
				if (ex.getPartialMessage() != null && ex.getPartialMessage() instanceof DiameterRequest)
				{
					try
					{
						sendError(ex);
					}
					catch (DiameterException ex2)
					{
						logger.warn("An error occured while sending error for incoming message " + ex2.getMessage() + " from " + association, ex2);
					}
				}
			}
		}
		while (message != null && buffer.readableBytes() > 0);
	}

	public void registerApplication(List<VendorSpecificApplicationId> vendorApplicationIds, List<Long> authApplicationIds, List<Long> acctApplicationIds, Package providerPackageName, Package packageName) throws DiameterException
	{
		for (VendorSpecificApplicationId vendorApplicationId : vendorApplicationIds)
		{
			addApplication(vendorApplicationId);
			if (vendorApplicationId.getAcctApplicationId() != null)
				acctApplicationPackages.put(vendorApplicationId.getAcctApplicationId(), providerPackageName);
			if (vendorApplicationId.getAuthApplicationId() != null)
				authApplicationPackages.put(vendorApplicationId.getAuthApplicationId(), providerPackageName);
		}
		for (Long applicationId : authApplicationIds)
		{
			Boolean found = false;
			for (VendorSpecificApplicationId vendorApplicationId : vendorApplicationIds)
			{
				if (vendorApplicationId.getAuthApplicationId() != null && vendorApplicationId.getAuthApplicationId().equals(applicationId))
				{
					found = true;
					break;
				}
			}
			if (!found)
			{
				addAuthApplication(applicationId);
				authApplicationPackages.put(applicationId, providerPackageName);
			}
		}
		for (Long applicationId : acctApplicationIds)
		{
			Boolean found = false;
			for (VendorSpecificApplicationId vendorApplicationId : vendorApplicationIds)
			{
				if (vendorApplicationId.getAcctApplicationId() != null && vendorApplicationId.getAcctApplicationId().equals(applicationId))
				{
					found = true;
					break;
				}
			}
			if (!found)
			{
				addAcctApplication(applicationId);
				acctApplicationPackages.put(applicationId, providerPackageName);
			}
		}
		parser.registerApplication(stack.getClassLoader(), packageName);
		stack.registerGlobalApplication(providerPackageName, packageName);
	}

	public static Boolean sameVendorSpecificApplicationId(VendorSpecificApplicationId oldId, VendorSpecificApplicationId applicationId)
	{
		Boolean sameInterface = true;
		if (oldId.getVendorId() == null)
		{
			if (applicationId.getVendorId() != null)
				sameInterface = false;
		}
		else if (!oldId.getVendorId().equals(applicationId.getVendorId()))
			sameInterface = false;
		if (oldId.getAuthApplicationId() == null)
		{
			if (applicationId.getAuthApplicationId() != null)
				sameInterface = false;
		}
		else if (!oldId.getAuthApplicationId().equals(applicationId.getAuthApplicationId()))
			sameInterface = false;
		if (oldId.getAcctApplicationId() == null)
		{
			if (applicationId.getAcctApplicationId() != null)
				sameInterface = false;
		}
		else if (!oldId.getAcctApplicationId().equals(applicationId.getAcctApplicationId()))
			sameInterface = false;
		return sameInterface;
	}

	private void addApplication(VendorSpecificApplicationId applicationId) throws DiameterException
	{
		for (VendorSpecificApplicationId oldId : applicationIds)
		{
			Boolean sameInterface = sameVendorSpecificApplicationId(oldId, applicationId);
			if (sameInterface)
				throw new DiameterException("Application is already registred for this peer", null, ResultCodes.DIAMETER_APPLICATION_UNSUPPORTED, null);
		}
		if (applicationId.getAuthApplicationId() != null)
		{
			for (Long oldId : authApplicationIds)
			{
				if (oldId.equals(applicationId.getAuthApplicationId()))
					throw new DiameterException("Application is already registred for this peer", null, ResultCodes.DIAMETER_APPLICATION_UNSUPPORTED, null);
			}
		}
		if (applicationId.getAcctApplicationId() != null)
		{
			for (Long oldId : acctApplicationIds)
			{
				if (oldId.equals(applicationId.getAuthApplicationId()))
					throw new DiameterException("Application is already registred for this peer", null, ResultCodes.DIAMETER_APPLICATION_UNSUPPORTED, null);
			}
		}
		applicationIds.add(applicationId);
		if (applicationId.getAuthApplicationId() != null)
			authApplicationIds.add(applicationId.getAuthApplicationId());
		if (applicationId.getAcctApplicationId() != null)
			acctApplicationIds.add(applicationId.getAcctApplicationId());
	}

	private void addAuthApplication(Long applicationId) throws DiameterException
	{
		for (Long oldId : authApplicationIds)
		{
			if (oldId.equals(applicationId))
				throw new DiameterException("Application is already registred for this peer", null, ResultCodes.DIAMETER_APPLICATION_UNSUPPORTED, null);
		}
		authApplicationIds.add(applicationId);
	}

	private void addAcctApplication(Long applicationId) throws DiameterException
	{
		for (Long oldId : acctApplicationIds)
		{
			if (oldId.equals(applicationId))
				throw new DiameterException("Application is already registred for this peer", null, ResultCodes.DIAMETER_APPLICATION_UNSUPPORTED, null);
		}
		acctApplicationIds.add(applicationId);
	}

	public void sendCER()
	{
		List<InetAddress> addresses = Arrays.asList(new InetAddress[] { localAddress });
		CapabilitiesExchangeRequest cer = null;
		try
		{
			cer = new CapabilitiesExchangeRequestImpl(localHost, localRealm, false, addresses, stack.getVendorID(), stack.getProductName());
			Long hopIdentifier = stack.getNextHopByHopIdentifier();
			cer.setHopByHopIdentifier(hopIdentifier);
			cer.setEndToEndIdentifier(hopIdentifier);
			cer.setOriginStateId(stack.getOriginalStateId());
			cer.setFirmwareRevision(stack.getFirmwareRevision());
			List<VendorSpecificApplicationId> vaiToSend = new ArrayList<VendorSpecificApplicationId>();
			List<Long> acctToSend = new ArrayList<Long>();
			List<Long> authToSend = new ArrayList<Long>();
			List<Long> vendorsToSend = new ArrayList<Long>();
			ConcurrentHashMap<Long, Boolean> acctUsed = new ConcurrentHashMap<Long, Boolean>();
			ConcurrentHashMap<Long, Boolean> authUsed = new ConcurrentHashMap<Long, Boolean>();
			for (VendorSpecificApplicationId vai : applicationIds)
			{
				if (vai.getVendorId() == null)
					vaiToSend.add(vai);
				else
				{
					Boolean found = false;
					for (Long currVendor : vendorsToSend)
						if (currVendor.equals(vai.getVendorId()))
						{
							found = true;
							break;
						}
					if (!found)
						vendorsToSend.add(vai.getVendorId());
					if (vai.getAuthApplicationId() != null)
					{
						authToSend.add(vai.getAuthApplicationId());
						authUsed.put(vai.getAuthApplicationId(), true);
					}
					if (vai.getAcctApplicationId() != null)
					{
						acctToSend.add(vai.getAcctApplicationId());
						acctUsed.put(vai.getAcctApplicationId(), true);
					}
				}
			}
			for (Long acct : acctApplicationIds)
			{
				if (acctUsed.putIfAbsent(acct, true) == null)
					acctToSend.add(acct);
			}
			for (Long auth : authApplicationIds)
				if (authUsed.putIfAbsent(auth, true) == null)
					authToSend.add(auth);
			if (vaiToSend.size() > 0)
				cer.setVendorSpecificApplicationIds(vaiToSend);
			if (authToSend.size() > 0)
				cer.setAuthApplicationIds(authToSend);
			if (acctToSend.size() > 0)
				cer.setAcctApplicationIds(acctToSend);
			if (vendorsToSend.size() > 0)
				cer.setSupportedVendorIds(vendorsToSend);
			setPeerState(PeerStateEnum.CER_SENT);
			sendMessageInternally(cer, new AsyncCallback()
			{
				@Override
				public void onSuccess()
				{
				}

				@Override
				public void onError(DiameterException ex)
				{
					logger.warn("An error occured while sending CER to " + association + " " + ex.getMessage(), ex);
					setPeerState(PeerStateEnum.IDLE);
					resetReconnectTimer();
				}
			});
		}
		catch (Exception ex)
		{
			logger.warn("An error occured while sending CER to " + association + " " + ex.getMessage(), ex);
		}
	}

	public void sendCEA(CapabilitiesExchangeRequest request)
	{
		List<InetAddress> addresses = Arrays.asList(new InetAddress[] { localAddress });
		CapabilitiesExchangeAnswer cea = null;
		try
		{
			cea = new CapabilitiesExchangeAnswerImpl(localHost, localRealm, false, ResultCodes.DIAMETER_SUCCESS, addresses, stack.getVendorID(), stack.getProductName());
			cea.setHopByHopIdentifier(request.getHopByHopIdentifier());
			cea.setEndToEndIdentifier(request.getEndToEndIdentifier());
			cea.setOriginStateId(stack.getOriginalStateId());
			cea.setFirmwareRevision(stack.getFirmwareRevision());
			List<VendorSpecificApplicationId> vaiToSend = new ArrayList<VendorSpecificApplicationId>();
			List<Long> acctToSend = new ArrayList<Long>();
			List<Long> authToSend = new ArrayList<Long>();
			List<Long> vendorsToSend = new ArrayList<Long>();
			for (VendorSpecificApplicationId vai : applicationIds)
			{
				if (vai.getVendorId() == null)
					vaiToSend.add(vai);
				else
				{
					Boolean found = false;
					for (Long currVendor : vendorsToSend)
						if (currVendor.equals(vai.getVendorId()))
						{
							found = true;
							break;
						}
					if (!found)
						vendorsToSend.add(vai.getVendorId());
					if (vai.getAuthApplicationId() != null)
						authToSend.add(vai.getAuthApplicationId());
					if (vai.getAcctApplicationId() != null)
						acctToSend.add(vai.getAcctApplicationId());
				}
			}
			for (Long acct : acctApplicationIds)
			{
				Boolean isNonVendor = true;
				for (VendorSpecificApplicationId vai : applicationIds)
					if (vai.getAcctApplicationId().equals(acct))
					{
						isNonVendor = false;
						break;
					}
				if (isNonVendor)
					acctToSend.add(acct);
			}
			for (Long auth : authApplicationIds)
			{
				Boolean isNonVendor = true;
				for (VendorSpecificApplicationId vai : applicationIds)
					if (vai.getAuthApplicationId().equals(auth))
					{
						isNonVendor = false;
						break;
					}
				if (isNonVendor)
					authToSend.add(auth);
			}
			if (vaiToSend.size() > 0)
				cea.setVendorSpecificApplicationIds(vaiToSend);
			if (authToSend.size() > 0)
				cea.setAuthApplicationIds(authToSend);
			if (acctToSend.size() > 0)
				cea.setAcctApplicationIds(acctToSend);
			if (vendorsToSend.size() > 0)
				cea.setSupportedVendorIds(vendorsToSend);
			setPeerState(PeerStateEnum.OPEN);
			sendMessageInternally(cea, new AsyncCallback()
			{
				@Override
				public void onSuccess()
				{
				}

				@Override
				public void onError(DiameterException ex)
				{
					logger.warn("An error occured while sending CEA to " + association + " " + ex.getMessage(), ex);
					setPeerState(PeerStateEnum.IDLE);
					resetReconnectTimer();
				}
			});
		}
		catch (Exception ex)
		{
			logger.warn("An error occured while sending CER to " + association + " " + ex.getMessage(), ex);
		}
	}

	public void sendDWR()
	{		
		DeviceWatchdogRequest dwr = null;
		try
		{
			dwr = new DeviceWatchdogRequestImpl(localHost, localRealm, false);
			Long hopIdentifier = stack.getNextHopByHopIdentifier();
			dwr.setHopByHopIdentifier(hopIdentifier);
			dwr.setEndToEndIdentifier(hopIdentifier);
			dwr.setOriginStateId(stack.getOriginalStateId());
			sendMessageInternally(dwr, new AsyncCallback()
			{
				@Override
				public void onSuccess()
				{
					// waiting for DWA
				}

				@Override
				public void onError(DiameterException ex)
				{
					logger.warn("An error occured while sending DWR to " + association + " " + ex.getMessage(), ex);
					setPeerState(PeerStateEnum.IDLE);
					resetReconnectTimer();
				}
			});
		}
		catch (Exception ex)
		{
			logger.warn("An error occured while sending DWR to " + association + " " + ex.getMessage(), ex);
		}
	}

	public void sendDWA(DeviceWatchdogRequest request, long resultCode)
	{
		DeviceWatchdogAnswer dwa = null;
		try
		{
			dwa = new DeviceWatchdogAnswerImpl(localHost, localRealm, false, resultCode);
			dwa.setHopByHopIdentifier(request.getHopByHopIdentifier());
			dwa.setEndToEndIdentifier(request.getEndToEndIdentifier());
			dwa.setOriginStateId(stack.getOriginalStateId());
			sendMessageInternally(dwa, new AsyncCallback()
			{
				@Override
				public void onSuccess()
				{
					// waiting for DWA
				}

				@Override
				public void onError(DiameterException ex)
				{
					logger.warn("An error occured while sending DWA to " + association + " " + ex.getMessage(), ex);
					setPeerState(PeerStateEnum.IDLE);
					resetReconnectTimer();
				}
			});
		}
		catch (Exception ex)
		{
			logger.warn("An error occured while sending DWA to " + association + " " + ex.getMessage(), ex);
		}
	}

	public void sendDPA(DisconnectPeerRequest request, long resultCode)
	{
		DisconnectPeerAnswer dpa = null;
		try
		{
			dpa = new DisconnectPeerAnswerImpl(localHost, localRealm, false, resultCode);
			dpa.setHopByHopIdentifier(request.getHopByHopIdentifier());
			dpa.setEndToEndIdentifier(request.getEndToEndIdentifier());
			sendMessageInternally(dpa, new AsyncCallback()
			{
				@Override
				public void onSuccess()
				{
					// waiting for DWA
				}

				@Override
				public void onError(DiameterException ex)
				{
					logger.warn("An error occured while sending DPA to " + association + " " + ex.getMessage(), ex);
					setPeerState(PeerStateEnum.IDLE);
					resetReconnectTimer();
				}
			});
		}
		catch (Exception ex)
		{
			logger.warn("An error occured while sending DPA to " + association + " " + ex.getMessage(), ex);
		}
	}

	public void sendDPR(DisconnectCauseEnum cause, AsyncCallback callback)
	{
		DisconnectPeerRequest dpr = null;
		try
		{
			dpr = new DisconnectPeerRequestImpl(localHost, localRealm, false, cause);
			Long hopIdentifier = stack.getNextHopByHopIdentifier();
			dpr.setEndToEndIdentifier(hopIdentifier);
			dpr.setHopByHopIdentifier(hopIdentifier);
			setPeerState(PeerStateEnum.DPR_SENT);
			sendMessageInternally(dpr, new AsyncCallback()
			{
				@Override
				public void onSuccess()
				{
					disconnectTimer.startTimer(inactivityTimeout, callback);
					stack.getWorkerPool().getPeriodicQueue().store(disconnectTimer.getRealTimestamp(), disconnectTimer);
				}

				@Override
				public void onError(DiameterException ex)
				{
					logger.warn("An error occured while sending DPR to " + association + " " + ex.getMessage(), ex);
					setPeerState(PeerStateEnum.IDLE);
					inactivityTimer.stop();
					if (callback != null)
						callback.onError(ex);
				}
			});
		}
		catch (Exception ex)
		{
			logger.warn("An error occured while sending CER to " + association + " " + ex.getMessage(), ex);
		}
	}

	public void sendError(DiameterException ex) throws MissingAvpException, AvpNotSupportedException
	{
		if (ex.getPartialMessage() == null || ex.getPartialMessage() instanceof DiameterAnswer)
			return;
		DiameterErrorAnswer answer;
		String sessionId = null;
		try
		{
			sessionId = ex.getPartialMessage().getSessionId();
		}
		catch (DiameterException ex2)
		{
			// may be without session
		}
		String originHost = null;
		String originRealm = null;
		Integer commandCode = ex.getCommandCode();
		Long applicationID = ex.getApplicationID();
		if (commandCode == null || applicationID == null)
		{
			DiameterCommandDefinition commandDef = DiameterParser.getCommandDefinition(ex.getPartialMessage().getClass());
			if (commandDef != null)
			{
				commandCode = commandDef.commandCode();
				applicationID = commandDef.applicationId();
			}
		}
		if (ex.getPartialMessage() instanceof DiameterRequest)
		{
			originHost = ((DiameterRequest) ex.getPartialMessage()).getDestinationHost();
			originRealm = ((DiameterRequest) ex.getPartialMessage()).getDestinationRealm();
		}
		if (originHost == null)
			originHost = this.localHost;
		if (originRealm == null)
			originRealm = this.localRealm;
		if (sessionId == null)
			answer = new DiameterErrorAnswerImpl(applicationID, commandCode, originHost, originRealm, false, ex.getErrorCode());
		else
			answer = new DiameterErrorAnswerWithSessionImpl(applicationID, commandCode, originHost, originRealm, false, ex.getErrorCode(), sessionId);
		answer.setErrorMessage(ex.getMessage());
		answer.setErrorReportingHost(localHost);
		answer.setEndToEndIdentifier(ex.getPartialMessage().getEndToEndIdentifier());
		answer.setHopByHopIdentifier(ex.getPartialMessage().getHopByHopIdentifier());
		sendMessageInternally(answer, new AsyncCallback()
		{
			@Override
			public void onSuccess()
			{
			}

			@Override
			public void onError(DiameterException ex)
			{
				logger.warn("An error occured while sending error answer " + ex.getMessage() + " from " + association, ex);
			}
		});
	}

	@Override
	public PeerStateEnum getPeerState()
	{
		return peerState.get();
	}

	@Override
	public void setPeerState(PeerStateEnum peerState)
	{
		this.peerState.set(peerState);
	}

	@Override
	public Package getPackage(Long applicationID, Boolean isAuth)
	{
		if (isAuth != null && isAuth)
			return authApplicationPackages.get(applicationID);
		return acctApplicationPackages.get(applicationID);
	}

	@Override
	public List<Long> getAuthApplicationIds()
	{
		return authApplicationIds;
	}

	@Override
	public List<Long> getAcctApplicationIds()
	{
		return acctApplicationIds;
	}

	@Override
	public List<VendorSpecificApplicationId> getVendorSpecificApplicationIds()
	{
		return applicationIds;
	}

	@Override
	public void resetInactivityTimer()
	{		
		inactivityTimer.resetTimer();
		stack.getWorkerPool().getPeriodicQueue().store(inactivityTimer.getRealTimestamp(), inactivityTimer);
	}

	@Override
	public void resetReconnectTimer()
	{
		reconnectTimer.startTimer(reconnectTimeout, null);
		stack.getWorkerPool().getPeriodicQueue().store(reconnectTimer.getRealTimestamp(), reconnectTimer);
	}

	@Override
	public void disconnectOperationCompleted()
	{
		disconnectTimer.execute();
	}

	@Override
	public String getID()
	{
		return linkId;
	}
}