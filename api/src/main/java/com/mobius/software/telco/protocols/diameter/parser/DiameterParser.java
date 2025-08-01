package com.mobius.software.telco.protocols.diameter.parser;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mobius.software.telco.protocols.diameter.ResultCodes;
import com.mobius.software.telco.protocols.diameter.annotations.DiameterAvpDefinition;
import com.mobius.software.telco.protocols.diameter.annotations.DiameterCommandDefinition;
import com.mobius.software.telco.protocols.diameter.annotations.DiameterDecode;
import com.mobius.software.telco.protocols.diameter.annotations.DiameterEncode;
import com.mobius.software.telco.protocols.diameter.annotations.DiameterLength;
import com.mobius.software.telco.protocols.diameter.annotations.DiameterOrder;
import com.mobius.software.telco.protocols.diameter.annotations.DiameterPrint;
import com.mobius.software.telco.protocols.diameter.annotations.DiameterValidate;
import com.mobius.software.telco.protocols.diameter.commands.DiameterAnswer;
import com.mobius.software.telco.protocols.diameter.commands.DiameterErrorAnswer;
import com.mobius.software.telco.protocols.diameter.commands.DiameterMessage;
import com.mobius.software.telco.protocols.diameter.exceptions.DiameterException;
import com.mobius.software.telco.protocols.diameter.primitives.DiameterAvp;
import com.mobius.software.telco.protocols.diameter.primitives.DiameterAvpKey;
import com.mobius.software.telco.protocols.diameter.primitives.DiameterGroupedAvp;
import com.mobius.software.telco.protocols.diameter.primitives.DiameterUnknownAvp;
import com.mobius.software.telco.protocols.diameter.primitives.common.FailedAvp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/*
 * Mobius Software LTD, Open Source Cloud Communications
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

/**
*
* @author yulian oifa
*
*/
public class DiameterParser
{
	public static Logger logger=LogManager.getLogger(DiameterParser.class);
	
	private static ConcurrentHashMap<Class<?>,DiameterAvpDefinition> avpDefsMap = new ConcurrentHashMap<Class<?>, DiameterAvpDefinition>();
	private static ConcurrentHashMap<Class<?>,DiameterCommandDefinition> commandDefsMap = new ConcurrentHashMap<Class<?>, DiameterCommandDefinition>();
	
	public static FolderFilter folderFilter = new FolderFilter();
	
	private ConcurrentHashMap<String,ConcurrentHashMap<CommandIdentifier,CommandData>> globalInterfacesMapping = new ConcurrentHashMap<String,ConcurrentHashMap<CommandIdentifier,CommandData>>();
	private ConcurrentHashMap<Long,ConcurrentHashMap<CommandIdentifier,CommandData>> interfacesMapping = new ConcurrentHashMap<Long,ConcurrentHashMap<CommandIdentifier,CommandData>>();
	private ConcurrentHashMap<Class<?>,Class<?>> avpImplementationsMap = new ConcurrentHashMap<Class<?>, Class<?>>();
	private ConcurrentHashMap<Class<?>,AvpData> avpsMap = new ConcurrentHashMap<Class<?>, AvpData>();
	private ConcurrentHashMap<Class<?>,CommandData> commandsMap = new ConcurrentHashMap<Class<?>, CommandData>();
	
	public DiameterParser(ClassLoader classLoader, List<Class<?>> errorClasses,Package avpPackage) throws DiameterException
	{
		registerAvps(classLoader, avpPackage);
		for(Class<?> clazz: errorClasses)
		{
			Method[] methods=clazz.getMethods();
			Method validateMethod = null, orderMethod = null;
			for(Method method:methods) 
			{
				DiameterValidate validateMethodAnnotation=method.getAnnotation(DiameterValidate.class);
				if(validateMethodAnnotation!=null && validateMethod==null)
					validateMethod = method;
					
				DiameterOrder orderMethodAnnotation=method.getAnnotation(DiameterOrder.class);
				if(orderMethodAnnotation!=null && orderMethod==null)
					orderMethod = method;
			}
			
			CommandData newCommand = new CommandData(clazz, "ERROR-MESSAGE", 0, 0L, validateMethod, orderMethod, false, true);
			commandsMap.put(clazz, newCommand);
			buildAvpList(newCommand, 0L, false, 0, clazz);	
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ByteBuf encode(DiameterMessage message) throws DiameterException
	{
		CommandData commandData = commandsMap.get(message.getClass());
		if(commandData==null)
			throw new DiameterException("Can not encode class " + message.getClass().getCanonicalName() + " since it has not been registered yet with parser", null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);
		
		Boolean isError = false;
		if((message instanceof DiameterAnswer))
			isError = ((DiameterAnswer)message).getIsError();
		
		if(!isError && commandData.getValidateMethod()!=null)
		{
			try
			{
				commandData.getValidateMethod().invoke(message);
			}
			catch(IllegalAccessException ex)
			{
				throw new DiameterException("Can not validate object of type " + message.getClass().getCanonicalName(), null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
			}
			catch(InvocationTargetException ex)
			{
				if(ex.getTargetException()!=null && ex.getTargetException() instanceof DiameterException)
					throw (DiameterException)ex.getTargetException();
				
				throw new DiameterException("Can not validate object of type " + message.getClass().getCanonicalName(), null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
			}
		}
		
		Integer commandLength = getLength(message, commandData.getAvpOrderedData()) + 20;
		ByteBuf result = Unpooled.buffer(commandLength);
		
		//version
		result.writeByte(1);
		
		//length
		result.writeByte((commandLength >> 16) & 0x0FF);
		result.writeByte((commandLength >> 8) & 0x0FF);
		result.writeByte(commandLength & 0x0FF);
		
		//command flags
		int flags=0;
		if(commandData.getIsRequest()!=null && commandData.getIsRequest())
			flags|=0x080;
		
		if(message.getIsProxyable()!=null)
		{
			if(message.getIsProxyable())
				flags|=0x040;
		}
		else if(commandData.getIsProxyable()!=null)
		{
			if(commandData.getIsProxyable())
				flags|=0x040;
		}
		
		if(isError!=null && isError)
			flags|=0x020;
		
		if(message.getIsRetransmit()!=null && message.getIsRetransmit())
			flags|=0x010;
		
		result.writeByte(flags);
		
		if(message instanceof DiameterErrorAnswer)
		{
			//command code
			result.writeByte((((DiameterErrorAnswer)message).getCommandCode() >> 16) & 0x0FF);
			result.writeByte((((DiameterErrorAnswer)message).getCommandCode() >> 8) & 0x0FF);
			result.writeByte(((DiameterErrorAnswer)message).getCommandCode() & 0x0FF);
			
			//application ID
			result.writeInt(((DiameterErrorAnswer)message).getApplicationId().intValue());
		}
		else
		{
			//command code
			result.writeByte((commandData.getCommandCode() >> 16) & 0x0FF);
			result.writeByte((commandData.getCommandCode() >> 8) & 0x0FF);
			result.writeByte(commandData.getCommandCode() & 0x0FF);
			
			//application ID
			result.writeInt(commandData.getApplicationID().intValue());
		}
		
		//hop by hop identifier
		if(message.getHopByHopIdentifier()!=null)
			result.writeInt(message.getHopByHopIdentifier().intValue());
		else
			result.writeInt(0);
		
		//end by end identifier
		if(message.getEndToEndIdentifier()!=null)
			result.writeInt(message.getEndToEndIdentifier().intValue());
		else
			result.writeInt(0);
		
		
		//now avps
		List<DiameterAvp> childAvps = null;
		if(commandData.getOrderMethod()!=null)
		{
			try
			{
				childAvps = (List<DiameterAvp>)commandData.getOrderMethod().invoke(message);
			}
			catch(IllegalAccessException ex)
			{
				throw new DiameterException("Can not get child avps for class " + message.getClass().getCanonicalName(), null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
			}
			catch(InvocationTargetException ex)
			{
				if(ex.getTargetException()!=null && ex.getTargetException() instanceof DiameterException)
					throw (DiameterException)ex.getTargetException();
				
				throw new DiameterException("Can not get child avps for class " + message.getClass().getCanonicalName(), null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
			}
		}
		else
		{
			childAvps = new ArrayList<DiameterAvp>();
			if(commandData.getAvpOrderedData()!=null)
			{
				for(AvpData curr:commandData.getAvpOrderedData())
				{
					curr.getField().setAccessible(true);
					if(curr.getField().getType().isAssignableFrom(List.class))
					{
						List currList = null;
						try
						{
							currList = (List)curr.getField().get(message);
						}
						catch(IllegalAccessException ex)
						{
							throw new DiameterException("Can not get child avp for class " + message.getClass().getCanonicalName(), null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
						}
						
						if(currList!=null)
						{
							for(Object currAvp:currList)
								childAvps.add((DiameterAvp)currAvp);
						}
					}
					else
					{
						DiameterAvp currAvp = null;
						try
						{
							currAvp = (DiameterAvp)curr.getField().get(message);
						}
						catch(IllegalAccessException ex)
						{
							throw new DiameterException("Can not get child avp for class " + message.getClass().getCanonicalName(), null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
						}
						
						if(currAvp!=null)
							childAvps.add(currAvp);
					}
				}
			}			
		}
		
		for(DiameterAvp curr:childAvps)
		{
			if(curr!=null)
				encode(result, curr, isError);
		}
		
		if(commandData.getOrderMethod()==null)
		{
			Map<DiameterAvpKey,List<DiameterUnknownAvp>> optionalAvps = message.getOptionalAvps();
			if(optionalAvps!=null)
			{
				Iterator<Entry<DiameterAvpKey,List<DiameterUnknownAvp>>> iterator = optionalAvps.entrySet().iterator();
				while(iterator.hasNext())
				{
					Entry<DiameterAvpKey,List<DiameterUnknownAvp>> currEntry = iterator.next();
					if(currEntry.getValue()!=null)
					{
						for(DiameterUnknownAvp unknownAvp : currEntry.getValue())
							encode(result, unknownAvp, isError);
					}
				}
			}
		}
		
		return result;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void encode(ByteBuf result,DiameterAvp avp, Boolean isError) throws DiameterException
	{
		if(avp instanceof DiameterUnknownAvp)
		{
			DiameterUnknownAvp unknownAvp = (DiameterUnknownAvp)avp;
			
			Integer avpLength = unknownAvp.getLength() + 8;
			if(unknownAvp.getVendorID()!=null && unknownAvp.getVendorID()>0)
				avpLength += 4;
			
			//avp code
			result.writeInt(unknownAvp.getAvpCode().intValue());
			
			//avp flags
			int flags=0;
			if(unknownAvp.getVendorID()!=null && unknownAvp.getVendorID()>0)
				flags|=0x080;
				
			if(unknownAvp.getIsProtected()!=null && unknownAvp.getIsProtected())
				flags|=0x020;
			
			result.writeByte(flags);
			
			//length
			result.writeByte((avpLength >> 16) & 0x0FF);
			result.writeByte((avpLength >> 8) & 0x0FF);
			result.writeByte(avpLength & 0x0FF);
			
			//vendor ID
			if(unknownAvp.getVendorID()!=null && unknownAvp.getVendorID()>0)
				result.writeInt(unknownAvp.getVendorID().intValue());
			
			//now data
			unknownAvp.encode(result);
		}
		else
		{
			AvpData avpData = avpsMap.get(avp.getClass());
			if(avpData==null)
				throw new DiameterException("Can not encode class " + avp.getClass().getCanonicalName() + " since it has not been registered yet with parser", null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);
			
			if(isError!=null && avpData.getValidateMethod()!=null)
			{
				try
				{
					avpData.getValidateMethod().invoke(avp);
				}
				catch(IllegalAccessException ex)
				{
					throw new DiameterException("Can not validate object of type " + avp.getClass().getCanonicalName(), null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
				}
				catch(InvocationTargetException ex)
				{
					if(ex.getTargetException()!=null && ex.getTargetException() instanceof DiameterException)
						throw (DiameterException)ex.getTargetException();
					
					throw new DiameterException("Can not validate object of type " + avp.getClass().getCanonicalName(), null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
				}
			}
			
			Integer avpLength = 0;
			List<DiameterAvp> childAvps = null;
			if(avpData.getOrderMethod()!=null)
			{
				try
				{
					childAvps = (List<DiameterAvp>)avpData.getOrderMethod().invoke(avp);
				}
				catch(IllegalAccessException ex)
				{
					throw new DiameterException("Can not get child avps for class " + avp.getClass().getCanonicalName(), null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
				}
				catch(InvocationTargetException ex)
				{
					if(ex.getTargetException()!=null && ex.getTargetException() instanceof DiameterException)
						throw (DiameterException)ex.getTargetException();
					
					throw new DiameterException("Can not get child avps for class " + avp.getClass().getCanonicalName(), null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
				}
				
				//lets calculate length of all child AVPs
				if(childAvps!=null)
				{
					for(DiameterAvp curr:childAvps)
					{
						if(curr instanceof DiameterUnknownAvp)
						{
							avpLength += ((DiameterUnknownAvp)curr).getLength() + 8;
							if(((DiameterUnknownAvp)curr).getVendorID()!=null && ((DiameterUnknownAvp)curr).getVendorID()>0)
								avpLength += 4;
							
						}
						else
						{
							avpLength += getLength(curr) + 8;
							AvpData childData = avpsMap.get(curr.getClass());
							if(childData != null && childData.getVendorID()!=null && childData.getVendorID()>0)
								avpLength += 4;															
						}
						
						if((avpLength%4) != 0)
							avpLength += (4 - (avpLength%4));						
					}
				}
				
				//group header itslef
				avpLength = avpLength + 8;
			}
			else if(avp instanceof DiameterUnknownAvp)
				avpLength = ((DiameterUnknownAvp)avp).getLength() + 8;
			else
				avpLength = getLength(avp) + 8;
			
			if(avpData.getVendorID()!=null && avpData.getVendorID()>0)
				avpLength += 4;
			
			//avp code
			result.writeInt(avpData.getAvpID().intValue());
			
			//avp flags
			int flags=0;
			if(avpData.getVendorID()!=null && avpData.getVendorID()>0)
				flags|=0x080;
				
			if(avp.getIsMust()!=null)
			{
				if(avp.getIsMust())
					flags|=0x040;
			}
			else if(avpData.getIsMust()!=null && avpData.getIsMust())
				flags|=0x040;
			
			if(avp.getIsProtected()!=null && avp.getIsProtected())
				flags|=0x020;
			
			result.writeByte(flags);
			
			//length
			result.writeByte((avpLength >> 16) & 0x0FF);
			result.writeByte((avpLength >> 8) & 0x0FF);
			result.writeByte(avpLength & 0x0FF);
			
			//vendor ID
			if(avpData.getVendorID()!=null && avpData.getVendorID()>0)
				result.writeInt(avpData.getVendorID().intValue());
			
			//now data
			if(avpData.getEncodeMethod()!=null)
			{
				try
				{
					avpData.getEncodeMethod().invoke(avp, result);
				}
				catch(IllegalAccessException ex)
				{
					throw new DiameterException("Can not execute encode for class " + avp.getClass().getCanonicalName(), null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
				}
				catch(InvocationTargetException ex)
				{
					logger.warn("Can not execute encode for class, " + avp.getClass().getCanonicalName() + ", error " + ex.getMessage(), ex);
					if(ex.getTargetException()!=null && ex.getTargetException() instanceof DiameterException)
						throw (DiameterException)ex.getTargetException();
					
					throw new DiameterException("Can not execute encode for class " + avp.getClass().getCanonicalName(), null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
				}
			}
			else
			{
				//or child avps either taken from order method or through iteration of properties
				if(childAvps==null && avpData.getAvpOrderedData()!=null)
				{
					childAvps = new ArrayList<DiameterAvp>();
					for(AvpData curr:avpData.getAvpOrderedData())
					{
						curr.getField().setAccessible(true);
						if(curr.getField().getType().isAssignableFrom(List.class))
						{
							List currList = null;
							try
							{
								currList = (List)curr.getField().get(avp);
							}
							catch(IllegalAccessException ex)
							{
								throw new DiameterException("Can not get child avp for class " + avp.getClass().getCanonicalName(), null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
							}
							
							if(currList!=null)
							{
								for(Object currAvp:currList)
									childAvps.add((DiameterAvp)currAvp);
							}
						}
						else
						{
							DiameterAvp currAvp = null;
							try
							{
								currAvp = (DiameterAvp)curr.getField().get(avp);
							}
							catch(IllegalAccessException ex)
							{
								throw new DiameterException("Can not get child avp for class " + avp.getClass().getCanonicalName(), null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
							}
							
							if(currAvp!=null)
								childAvps.add(currAvp);
						}
					}																		
				}
				
				if(childAvps!=null)
				{
					for(DiameterAvp curr:childAvps)
					{
						if(curr!=null)
							encode(result, curr, isError);
					}
				}
				
				//optional AVPs
				if(avp instanceof DiameterGroupedAvp)
				{
					DiameterGroupedAvp groupedAvp = (DiameterGroupedAvp)avp;
					Map<DiameterAvpKey,List<DiameterUnknownAvp>> optionalAvps = groupedAvp.getOptionalAvps();
					if(optionalAvps!=null)
					{
						Iterator<Entry<DiameterAvpKey,List<DiameterUnknownAvp>>> iterator = optionalAvps.entrySet().iterator();
						while(iterator.hasNext())
						{
							Entry<DiameterAvpKey,List<DiameterUnknownAvp>> currEntry = iterator.next();
							if(currEntry.getValue()!=null)
							{
								for(DiameterUnknownAvp unknownAvp : currEntry.getValue())
									encode(result, unknownAvp, isError);
							}
						}
					}
				}
				
				if(avp instanceof FailedAvp)
				{
					FailedAvp failedAvp = (FailedAvp)avp;
					Map<DiameterAvpKey,List<DiameterAvp>> knownAvps = failedAvp.getKnownAvps();
					if(knownAvps!=null)
					{
						Iterator<Entry<DiameterAvpKey,List<DiameterAvp>>> iterator = knownAvps.entrySet().iterator();
						while(iterator.hasNext())
						{
							Entry<DiameterAvpKey,List<DiameterAvp>> currEntry = iterator.next();
							if(currEntry.getValue()!=null)
							{
								for(DiameterAvp unknownAvp : currEntry.getValue())
									encode(result, unknownAvp, isError);
							}
						}
					}
				}
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String printMessage(DiameterMessage message) throws DiameterException
	{
		CommandData commandData = commandsMap.get(message.getClass());
		if(commandData==null)
			throw new DiameterException("Can not encode class " + message.getClass().getCanonicalName() + " since it has not been registered yet with parser", null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);
		
		Boolean isError = false;
		if((message instanceof DiameterAnswer))
			isError = ((DiameterAnswer)message).getIsError();
		
		Integer commandLength = getLength(message, commandData.getAvpOrderedData()) + 20;
		StringBuilder result = new StringBuilder();
		
		//in case of logger,etc, should not having anything in first line
		result.append(System.getProperty("line.separator"));
		
		printSeparatorLines(result, 1);
		printMesage(result, commandData.getName().toUpperCase(), ' ');
		printSeparatorLines(result, 1);
		
		printLineStart(result);
		//version
		printMesage(result, String.valueOf(1), 8);
		
		printItemSeparator(result);
		
		//length
		printMesage(result, String.valueOf(commandLength), 24);
		
		printLineEnd(result);
		printSeparatorLines(result, 1);
		printLineStart(result);
		
		//command flags
		if(commandData.getIsRequest()!=null && commandData.getIsRequest())
			result.append("R");
		else
			result.append(".");
		
		if(message.getIsProxyable()!=null)
		{
			if(message.getIsProxyable())
				result.append("P");
			else
				result.append(".");
		}
		else if(commandData.getIsProxyable()!=null)
		{
			if(commandData.getIsProxyable())
				result.append("P");
			else
				result.append(".");
		}
		
		if(isError!=null && isError)
			result.append("E");
		else
			result.append(".");
		
		if(message.getIsRetransmit()!=null && message.getIsRetransmit())
			result.append("T");
		else
			result.append(".");
		
		printMesage(result, "", 4);
		
		printItemSeparator(result);
		
		if(message instanceof DiameterErrorAnswer)
			printMesage(result, ((DiameterErrorAnswer)message).getCommandCode().toString(), 24);
			//command code
		else
			//command code
			printMesage(result, commandData.getCommandCode().toString(), 24);
		
		printLineEnd(result);
		printSeparatorLines(result, 1);
		
		if(message instanceof DiameterErrorAnswer)	
			//application ID
			printMesage(result, ((DiameterErrorAnswer)message).getApplicationId().toString(),35);
		else
			//application ID
			printMesage(result, commandData.getApplicationID().toString(),35);
		
		printSeparatorLines(result, 1);
		
		//hop by hop identifier
		if(message.getHopByHopIdentifier()!=null)
			printMesage(result, message.getHopByHopIdentifier().toString(),35);
		else
			printMesage(result, "0",35);
		
		printSeparatorLines(result, 1);
		
		//end by end identifier
		if(message.getEndToEndIdentifier()!=null)
			printMesage(result, message.getEndToEndIdentifier().toString(),35);
		else
			printMesage(result, "0",35);
		
		printSeparatorLines(result, 1);
		
		//now avps
		List<DiameterAvp> childAvps = null;
		if(commandData.getOrderMethod()!=null)
		{
			try
			{
				childAvps = (List<DiameterAvp>)commandData.getOrderMethod().invoke(message);
			}
			catch(IllegalAccessException ex)
			{
				throw new DiameterException("Can not get child avps for class " + message.getClass().getCanonicalName(), null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
			}
			catch(InvocationTargetException ex)
			{
				if(ex.getTargetException()!=null && ex.getTargetException() instanceof DiameterException)
					throw (DiameterException)ex.getTargetException();
				
				throw new DiameterException("Can not get child avps for class " + message.getClass().getCanonicalName(), null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
			}
		}
		else
		{
			childAvps = new ArrayList<DiameterAvp>();
			if(commandData.getAvpOrderedData()!=null)
			{
				for(AvpData curr:commandData.getAvpOrderedData())
				{
					curr.getField().setAccessible(true);
					if(curr.getField().getType().isAssignableFrom(List.class))
					{
						List currList = null;
						try
						{
							currList = (List)curr.getField().get(message);
						}
						catch(IllegalAccessException ex)
						{
							throw new DiameterException("Can not get child avp for class " + message.getClass().getCanonicalName(), null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
						}
						
						if(currList!=null)
						{
							for(Object currAvp:currList)
								childAvps.add((DiameterAvp)currAvp);
						}
					}
					else
					{
						DiameterAvp currAvp = null;
						try
						{
							currAvp = (DiameterAvp)curr.getField().get(message);
						}
						catch(IllegalAccessException ex)
						{
							throw new DiameterException("Can not get child avp for class " + message.getClass().getCanonicalName(), null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
						}
						
						if(currAvp!=null)
							childAvps.add(currAvp);
					}
				}
			}
		}
		
		for(DiameterAvp curr:childAvps)
		{
			if(curr!=null)
				printAvp(result, curr, isError, 1);
		}
		

		if(commandData.getOrderMethod()==null)
		{
			Map<DiameterAvpKey,List<DiameterUnknownAvp>> optionalAvps = message.getOptionalAvps();
			if(optionalAvps!=null)
			{
				Iterator<Entry<DiameterAvpKey,List<DiameterUnknownAvp>>> iterator = optionalAvps.entrySet().iterator();
				while(iterator.hasNext())
				{
					Entry<DiameterAvpKey,List<DiameterUnknownAvp>> currEntry = iterator.next();
					if(currEntry.getValue()!=null)
					{
						for(DiameterUnknownAvp unknownAvp : currEntry.getValue())
							printAvp(result, unknownAvp, isError , 1);
					}
				}
			}
		}
		
		return result.toString();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void printAvp(StringBuilder result,DiameterAvp avp, Boolean isError,int separatorLines) throws DiameterException
	{
		if(separatorLines>1)
			printSeparatorLines(result, separatorLines-1);
		
		if(avp instanceof DiameterUnknownAvp)
		{
			DiameterUnknownAvp unknownAvp = (DiameterUnknownAvp)avp;
			
			printMesage(result, "UNKNOWN-AVP", ' ');
			printSeparatorLines(result, separatorLines);
			
			Integer avpLength = unknownAvp.getLength() + 8;
			if(unknownAvp.getVendorID()!=null && unknownAvp.getVendorID()>0)
				avpLength += 4;
			
			//avp code
			printMesage(result, unknownAvp.getAvpCode().toString(), 35);
			
			printSeparatorLines(result, 1);
			printLineStart(result);
			
			//avp flags
			if(unknownAvp.getVendorID()!=null && unknownAvp.getVendorID()>0)
				result.append("V");
			else
				result.append(".");
			
			if(unknownAvp.getIsMust()!=null && unknownAvp.getIsMust())
				result.append("M");
			else
				result.append(".");
			
			if(unknownAvp.getIsProtected()!=null && unknownAvp.getIsProtected())
				result.append("P");
			else
				result.append(".");
			
			printMesage(result, "", 5);
			printItemSeparator(result);
			
			//length
			printMesage(result, avpLength.toString(), 24);
			
			printLineEnd(result);
			printSeparatorLines(result, 1);
			
			//vendor ID
			if(unknownAvp.getVendorID()!=null && unknownAvp.getVendorID()>0)
				printMesage(result, unknownAvp.getVendorID().toString(), 35);
			else
				printMesage(result, "", 35);
			
			printSeparatorLines(result, 1);
			
			//now data
			printMesage(result, toHex(unknownAvp.getValue()), 35);	
			printSeparatorLines(result, 1);
		}
		else
		{
			AvpData avpData = avpsMap.get(avp.getClass());
			if(avpData==null)
				throw new DiameterException("Can not encode class " + avp.getClass().getCanonicalName() + " since it has not been registered yet with parser", null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);
			
			printMesage(result, avpData.getName().toUpperCase(), ' ');
			printSeparatorLines(result, separatorLines);
			
			Integer avpLength = 0;
			List<DiameterAvp> childAvps = null;
			if(avpData.getOrderMethod()!=null)
			{
				try
				{
					childAvps = (List<DiameterAvp>)avpData.getOrderMethod().invoke(avp);
				}
				catch(IllegalAccessException ex)
				{
					throw new DiameterException("Can not get child avps for class " + avp.getClass().getCanonicalName(), null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
				}
				catch(InvocationTargetException ex)
				{
					if(ex.getTargetException()!=null && ex.getTargetException() instanceof DiameterException)
						throw (DiameterException)ex.getTargetException();
					
					throw new DiameterException("Can not get child avps for class " + avp.getClass().getCanonicalName(), null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
				}
				
				//lets calculate length of all child AVPs
				if(childAvps!=null)
				{
					for(DiameterAvp curr:childAvps)
					{
						if(curr instanceof DiameterUnknownAvp)
						{
							avpLength += ((DiameterUnknownAvp)curr).getLength() + 8;
							if(((DiameterUnknownAvp)curr).getVendorID()!=null && ((DiameterUnknownAvp)curr).getVendorID()>0)
								avpLength += 4;
							
						}
						else
						{
							avpLength += getLength(curr) + 8;
							AvpData childData = avpsMap.get(curr.getClass());
							if(childData != null && childData.getVendorID()!=null && childData.getVendorID()>0)
								avpLength += 4;															
						}
						
						if((avpLength%4) != 0)
							avpLength += (4 - (avpLength%4));						
					}
				}
				
				//group header itslef
				avpLength = avpLength + 8;
			}
			else if(avp instanceof DiameterUnknownAvp)
				avpLength = ((DiameterUnknownAvp)avp).getLength() + 8;
			else
				avpLength = getLength(avp) + 8;
			
			if(avpData.getVendorID()!=null && avpData.getVendorID()>0)
				avpLength += 4;
			
			printMesage(result, avpData.getAvpID().toString(), 35);
			
			printSeparatorLines(result, 1);
			printLineStart(result);
			
			//avp flags
			if(avpData.getVendorID()!=null && avpData.getVendorID()>0)
				result.append("V");
			else
				result.append(".");
			
			if(avp.getIsMust()!=null)
			{
				if(avp.getIsMust())
					result.append("M");
				else
					result.append(".");
			}
			else if(avpData.getIsMust()!=null && avpData.getIsMust())
				result.append("M");
			else
				result.append(".");
			
			if(avp.getIsProtected()!=null && avp.getIsProtected())
				result.append("P");
			else
				result.append(".");
			
			printMesage(result, "", 5);
			printItemSeparator(result);
			
			//length
			printMesage(result, avpLength.toString(), 24);
			
			printLineEnd(result);
			printSeparatorLines(result, 1);
			
			//vendor ID
			if(avpData.getVendorID()!=null && avpData.getVendorID()>0)
				printMesage(result, avpData.getVendorID().toString(), 35);
			else
				printMesage(result, "", 35);
			
			printSeparatorLines(result, 1);
			
			//now data
			if(avpData.getPrintMethod()!=null)
			{
				try
				{
					avpData.getPrintMethod().invoke(avp, result);
				}
				catch(IllegalAccessException ex)
				{
					throw new DiameterException("Can not execute encode for class " + avp.getClass().getCanonicalName(), null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
				}
				catch(InvocationTargetException ex)
				{
					logger.warn("Can not execute encode for class, " + avp.getClass().getCanonicalName() + ", error " + ex.getMessage(), ex);
					if(ex.getTargetException()!=null && ex.getTargetException() instanceof DiameterException)
						throw (DiameterException)ex.getTargetException();
					
					throw new DiameterException("Can not execute encode for class " + avp.getClass().getCanonicalName(), null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
				}	
				
				printSeparatorLines(result, 1);
			}
			else
			{
				//or child avps either taken from order method or through iteration of properties
				if(childAvps==null && avpData.getAvpOrderedData()!=null)
				{
					childAvps = new ArrayList<DiameterAvp>();
					for(AvpData curr:avpData.getAvpOrderedData())
					{
						curr.getField().setAccessible(true);
						if(curr.getField().getType().isAssignableFrom(List.class))
						{
							List currList = null;
							try
							{
								currList = (List)curr.getField().get(avp);
							}
							catch(IllegalAccessException ex)
							{
								throw new DiameterException("Can not get child avp for class " + avp.getClass().getCanonicalName(), null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
							}
							
							if(currList!=null)
							{
								for(Object currAvp:currList)
									childAvps.add((DiameterAvp)currAvp);
							}
						}
						else
						{
							DiameterAvp currAvp = null;
							try
							{
								currAvp = (DiameterAvp)curr.getField().get(avp);
							}
							catch(IllegalAccessException ex)
							{
								throw new DiameterException("Can not get child avp for class " + avp.getClass().getCanonicalName(), null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
							}
							
							if(currAvp!=null)
								childAvps.add(currAvp);
						}
					}																		
				}
				
				if(childAvps!=null)
				{
					for(DiameterAvp curr:childAvps)
					{
						if(curr!=null)
							printAvp(result, curr, isError, separatorLines+1);
					}
				}
				
				//optional AVPs
				if(avp instanceof DiameterGroupedAvp)
				{
					DiameterGroupedAvp groupedAvp = (DiameterGroupedAvp)avp;
					Map<DiameterAvpKey,List<DiameterUnknownAvp>> optionalAvps = groupedAvp.getOptionalAvps();
					if(optionalAvps!=null)
					{
						Iterator<Entry<DiameterAvpKey,List<DiameterUnknownAvp>>> iterator = optionalAvps.entrySet().iterator();
						while(iterator.hasNext())
						{
							Entry<DiameterAvpKey,List<DiameterUnknownAvp>> currEntry = iterator.next();
							if(currEntry.getValue()!=null)
							{
								for(DiameterUnknownAvp unknownAvp : currEntry.getValue())
									printAvp(result, unknownAvp, isError, separatorLines+1);
							}
						}
					}
				}
				
				if(avp instanceof FailedAvp)
				{
					FailedAvp failedAvp = (FailedAvp)avp;
					Map<DiameterAvpKey,List<DiameterAvp>> knownAvps = failedAvp.getKnownAvps();
					if(knownAvps!=null)
					{
						Iterator<Entry<DiameterAvpKey,List<DiameterAvp>>> iterator = knownAvps.entrySet().iterator();
						while(iterator.hasNext())
						{
							Entry<DiameterAvpKey,List<DiameterAvp>> currEntry = iterator.next();
							if(currEntry.getValue()!=null)
							{
								for(DiameterAvp unknownAvp : currEntry.getValue())
									printAvp(result, unknownAvp, isError, separatorLines+1);
							}
						}
					}
				}
			}
		}
	}
	
	private static void printLineStart(StringBuilder sb)
	{
		sb.append("|");
	}
	
	private static void printItemSeparator(StringBuilder sb)
	{
		sb.append("|");
	}
	
	private static void printLineEnd(StringBuilder sb)
	{
		sb.append("|");
		sb.append(System.getProperty("line.separator"));
	}
	
	public static void printMesage(StringBuilder sb,String message)
	{
		printMesage(sb, message, 35);
	}
	
	public static void printMesage(StringBuilder sb,String message, char spacer)
	{
		printMesage(sb, message, spacer, 35);
	}
	
	private static void printMesage(StringBuilder sb,String message,int length)
	{
		printMesage(sb,message,'.',length);
	}
	
	private static void printMesage(StringBuilder sb,String message,char spacer, int length)
	{
		while(message.length()>33)
		{
			printLineStart(sb);
			sb.append(message.substring(0,33));
			printLineEnd(sb);
			message=message.substring(33);
		}
		
		int usedChars=0;
		if(length==35)
		{
			printLineStart(sb);
			usedChars=2;
		}
		
		sb.append(message);
		
		for(int i=0;i<length-message.length()-usedChars;i++)
			sb.append(spacer);
				
		if(length==35)
			printLineEnd(sb);				
	}
	
	private static void printSeparatorLines(StringBuilder sb,Integer lines)
	{
		for(int i=0;i<lines;i++)
		{	
			sb.append("+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+");
			sb.append(System.getProperty("line.separator"));
		}
	}
	
	@SuppressWarnings("rawtypes")
	public Integer getLength(DiameterAvp parent,List<AvpData> childAvps) throws DiameterException
	{
		Integer result = 0;
		if(childAvps!=null && childAvps.size()>0)
		{
			for(AvpData curr:childAvps)
			{
				Field f = curr.getField();
				f.setAccessible(true);
				if(f.getType().isAssignableFrom(List.class))
				{
					List childAvpsList = null;
					try
					{
						childAvpsList = (List)f.get(parent);
					}
					catch(Exception ex)//IllegalAccessException ex)
					{
						throw new DiameterException("Can not read field " + f.getName() + " for class " + parent.getClass().getCanonicalName(), null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
					}
					
					if(childAvpsList!=null)
					{
						for(Object childAvp:childAvpsList)
						{
							Integer currentLength = getLength((DiameterAvp)childAvp);
							if((currentLength%4)!=0)
								currentLength += (4 - (currentLength%4));
							
							currentLength+=8;
							if(curr.getVendorID()!=null && curr.getVendorID()>0)
								currentLength+=4;
							
							result+=currentLength;
						}
					}
				}
				else
				{
					DiameterAvp childAvp = null;
					try
					{
						childAvp = (DiameterAvp)f.get(parent);
					}
					catch(Exception ex)//IllegalAccessException ex)
					{
						throw new DiameterException("Can not read field " + f.getName() + " for class " + parent.getClass().getCanonicalName(), null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
					}
					
					if(childAvp!=null)
					{
						Integer currentLength = getLength(childAvp);
						if((currentLength%4)!=0)
							currentLength += (4 - (currentLength%4));
						
						currentLength+=8;
						if(curr.getVendorID()!=null && curr.getVendorID()>0)
							currentLength+=4;
						
						result+=currentLength;
					}
				}
			}
		}
		
		//add length of all optional AVPs
		if(parent instanceof DiameterGroupedAvp)
		{
			DiameterGroupedAvp grouped = (DiameterGroupedAvp)parent;
			Map<DiameterAvpKey,List<DiameterUnknownAvp>> optionalAvps = grouped.getOptionalAvps();
			if(optionalAvps!=null)
			{
				Iterator<Entry<DiameterAvpKey,List<DiameterUnknownAvp>>> iterator = optionalAvps.entrySet().iterator();
				while(iterator.hasNext())
				{
					Entry<DiameterAvpKey,List<DiameterUnknownAvp>> currEntry = iterator.next();
					if(currEntry.getValue()!=null)
					{
						for(DiameterUnknownAvp currOptionalAvp:currEntry.getValue())
						{
							Integer currentLength = currOptionalAvp.getLength();
							if((currentLength%4)!=0)
								currentLength += (4 - (currentLength%4));
							
							currentLength+=8;
							if(currEntry.getKey().getVendorID()!=null && currEntry.getKey().getVendorID()>0)
								currentLength+=4;
							
							result+=currentLength;
						}
					}
				}
			}
		}
		
		//add length of all known failed
		if(parent instanceof FailedAvp)
		{
			FailedAvp failedAvp = (FailedAvp)parent;
			Map<DiameterAvpKey,List<DiameterAvp>> failedKnownAvps = failedAvp.getKnownAvps();
			if(failedKnownAvps!=null)
			{
				Iterator<Entry<DiameterAvpKey,List<DiameterAvp>>> iterator = failedKnownAvps.entrySet().iterator();
				while(iterator.hasNext())
				{
					Entry<DiameterAvpKey,List<DiameterAvp>> currEntry = iterator.next();
					if(currEntry.getValue()!=null)
					{
						for(DiameterAvp currOptionalAvp:currEntry.getValue())
						{
							Integer currentLength = getLength(currOptionalAvp);
							if((currentLength%4)!=0)
								currentLength += (4 - (currentLength%4));
							
							currentLength+=8;
							if(currEntry.getKey().getVendorID()!=null && currEntry.getKey().getVendorID()>0)
								currentLength+=4;
							
							result+=currentLength;
						}
					}
				}
			}
		}
		
		return result;
	}


	
	//return inner length without header
	public Integer getLength(DiameterAvp parent) throws DiameterException
	{
		AvpData avpData = avpsMap.get(parent.getClass());
		if(avpData == null)
			throw new DiameterException("Can not encode class " + parent.getClass().getCanonicalName() + " since it has not been registered yet with parser", null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);
		
		if(avpData.getLengthMethod()!=null)
		{
			try
			{
				return (Integer)avpData.getLengthMethod().invoke(parent);					
			}
			catch(IllegalAccessException ex)
			{
				throw new DiameterException("Can not read length for class " + parent.getClass().getCanonicalName(), null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
			}
			catch(InvocationTargetException ex)
			{
				if(ex.getTargetException()!=null && ex.getTargetException() instanceof DiameterException)
					throw (DiameterException)ex.getTargetException();
				
				throw new DiameterException("Can not read length for class " + parent.getClass().getCanonicalName(), null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
			}
		}
		else
			return getLength(parent, avpData.getAvpOrderedData());	
	}
	
	public DiameterMessage decode(ByteBuf message, Boolean rejectUnmandatoryAvps) throws DiameterException
	{
		return decode(message, rejectUnmandatoryAvps, false, null);
	}
	
	public DiameterMessage decode(ByteBuf message, Boolean rejectUnmandatoryAvps, Boolean isGlobal, String packageName) throws DiameterException
	{
		if(message.readableBytes()<4)
			return null;
		
		message.markReaderIndex();
		//reading version;
		int version = message.readUnsignedByte();
		
		//reading length with padding
		Integer messageLength=0;
		int currByte = message.readUnsignedByte();
		if(currByte>0)
			messageLength += (currByte << 16);
				
		currByte = message.readUnsignedByte();
		if(currByte>0)
			messageLength += (currByte << 8);
		
		currByte = message.readUnsignedByte();
		if(currByte>0)
			messageLength += currByte;
			
		//check if has all message in buffer
		if(message.readableBytes()<(messageLength-4))
		{
			message.resetReaderIndex();
			return null;
		}
		
		if(version!=1)
		{			
			message.skipBytes(messageLength-4);
			throw new DiameterException("Only version 1 is supported", null, ResultCodes.DIAMETER_UNSUPPORTED_VERSION, null);
		}
		
		if(messageLength<20)
		{
			message.skipBytes(messageLength-4);
			throw new DiameterException("Message length should be at least 20 bytes", null , ResultCodes.DIAMETER_INVALID_MESSAGE_LENGTH, null);
		}
		
		currByte = message.readUnsignedByte();
		Boolean isRequest = (currByte & 0x080)!=0;
		Boolean isProxyable = (currByte & 0x040)!=0;
		Boolean isError = (currByte & 0x020)!=0;
		Boolean isRetransmit = (currByte & 0x010)!=0;
		
		Integer commandCode=0;
		currByte = message.readUnsignedByte();
		if(currByte>0)
			commandCode += (currByte << 16);
		
		currByte = message.readUnsignedByte();
		if(currByte>0)
			commandCode += (currByte << 8);
		
		currByte = message.readUnsignedByte();
		if(currByte>0)
			commandCode += currByte;
		
		Long applicationID = message.readUnsignedInt();
		Long hopByHopIdentifier = message.readUnsignedInt();
		Long endToEndIdentifier = message.readUnsignedInt();
		
		ConcurrentHashMap<CommandIdentifier,CommandData> interfaceMessages = null;
		if(isGlobal && packageName!=null)
			interfaceMessages = globalInterfacesMapping.get(packageName);
		else
			interfaceMessages = interfacesMapping.get(applicationID);
			
		if(interfaceMessages == null)
		{
			if(messageLength>20)
				message.skipBytes(messageLength-20);
			
			DiameterException exception = new DiameterException("Application ID " + applicationID + " not registered for selected parser", null , ResultCodes.DIAMETER_APPLICATION_UNSUPPORTED, null);
			//for request lets provide the details required for error
			if(isRequest)
			{
				exception.setApplicationID(applicationID);
				exception.setCommandCode(commandCode);			
			}
			
			throw exception;
		}
		
		CommandIdentifier commandIdentifier = new CommandIdentifier(commandCode, isRequest);
		CommandData commandData = interfaceMessages.get(commandIdentifier);	
		
		if(commandData == null)
		{
			if(messageLength>20)
				message.skipBytes(messageLength-20);
						
			DiameterException exception;
			if(isRequest)
				exception = new DiameterException("Application ID " + applicationID + " does not have request with command code " + commandCode  + " registered for selected parser", null , ResultCodes.DIAMETER_COMMAND_UNSUPPORTED, null);
			else
				exception = new DiameterException("Application ID " + applicationID + " does not have answer with command code " + commandCode  + " registered for selected parser", null , ResultCodes.DIAMETER_COMMAND_UNSUPPORTED, null);
			
			//for request lets provide the details required for error
			if(isRequest)
			{
				exception.setApplicationID(applicationID);
				exception.setCommandCode(commandCode);			
			}
			
			throw exception;
		}
		
		DiameterMessage diameterMessage = null;
		try
		{
			Constructor<?> ctor = commandData.getClazz().getDeclaredConstructor();
			ctor.setAccessible(true);
			diameterMessage = (DiameterMessage) ctor.newInstance(new Object[] {  });
		}
		catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException ex)
		{
			if(messageLength>20)
				message.skipBytes(messageLength-20);
			
			DiameterException exception;
			if(isRequest)
				exception = new DiameterException("Application ID " + applicationID + " have request with command code " + commandCode  + " that can not be initialized for selected parser", null , ResultCodes.DIAMETER_COMMAND_UNSUPPORTED, null);
			else
				exception = new DiameterException("Application ID " + applicationID + " have answer with command code " + commandCode  + " that can not be initialized for selected parser", null , ResultCodes.DIAMETER_COMMAND_UNSUPPORTED, null);
			
			//for request lets provide the details required for error
			if(isRequest)
			{
				exception.setApplicationID(applicationID);
				exception.setCommandCode(commandCode);			
			}
			
			exception.setPartialMessage(diameterMessage);
			throw exception;
		}
		
		diameterMessage.setEndToEndIdentifier(endToEndIdentifier);
		diameterMessage.setHopByHopIdentifier(hopByHopIdentifier);
		diameterMessage.setIsRetransmit(isRetransmit);
		diameterMessage.setIsProxyable(isProxyable);
		
		if(isError)
		{
			//if error flag is set but result code is either not set or set to 1XXX or 2XXX 
			//should set the code to something that will be properly handled by APP
			if(diameterMessage instanceof DiameterAnswer)
			{
				DiameterAnswer answer = (DiameterAnswer) diameterMessage;
				if(answer.getIsError()==null || !answer.getIsError())
					((DiameterAnswer)diameterMessage).setResultCode(ResultCodes.DIAMETER_CONTRADICTING_AVPS);				
			}
			else
			{
				if(messageLength>20)
					message.skipBytes(messageLength-20);
				
				DiameterException exception = new DiameterException("The message has error bit flag together with request flag", null , ResultCodes.DIAMETER_INVALID_BIT_IN_HEADER, null);
				if(isRequest)
				{
					exception.setApplicationID(applicationID);
					exception.setCommandCode(commandCode);			
				}
				
				exception.setPartialMessage(diameterMessage);
				throw exception;
			}
		}
		
		message.markReaderIndex();
		
		try
		{
			decode(message, diameterMessage, isError, commandData.getAvpData(), messageLength-20, rejectUnmandatoryAvps);
		}
		catch(DiameterException ex)
		{
			//lets skip remaining bytes for message if error occured
			message.resetReaderIndex();
			message.skipBytes(messageLength - 20);
			if(isRequest)
			{
				ex.setApplicationID(applicationID);
				ex.setCommandCode(commandCode);			
			}
			
			ex.setPartialMessage(diameterMessage);
			throw ex;
		}
		
		//lets reset reader index anyway so will not interfer outside of parser
		message.resetReaderIndex();
		message.skipBytes(messageLength - 20);
		
		//lets validate if has validate method
		if(diameterMessage!=null)
		{
			if(!isError && commandData.getValidateMethod()!=null)
			{
				try
				{
					commandData.getValidateMethod().invoke(diameterMessage);
				}
				catch(IllegalAccessException ex)
				{
					DiameterException exception = new DiameterException("The message can not be vallidated", null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);
					exception.setPartialMessage(diameterMessage);
					if(isRequest)
					{
						exception.setApplicationID(applicationID);
						exception.setCommandCode(commandCode);			
					}
					
					throw exception;
				}
				catch(InvocationTargetException ex)
				{
					DiameterException exception;
					if(ex.getTargetException()!=null && ex.getTargetException() instanceof DiameterException)
						exception = (DiameterException)ex.getTargetException();
					else
						exception = new DiameterException("The message can not be validated", null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);
					
					exception.setPartialMessage(diameterMessage);
					if(isRequest)
					{
						exception.setApplicationID(applicationID);
						exception.setCommandCode(commandCode);			
					}
					
					throw exception;
				}
			}
		}
		
		return diameterMessage;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void decode(ByteBuf message,DiameterAvp parent,Boolean isError,ConcurrentHashMap<DiameterAvpKey,AvpData> childAvps,Integer length, Boolean rejectUnmandatoryAvps) throws DiameterException
	{
		while(length>0 && message.readableBytes()>=length)
		{
			Long avpCode = message.readUnsignedInt();
			int currByte = message.readUnsignedByte();
			
			Boolean hasVendorID = (currByte & 0x080)!=0;
			Boolean isMandatory = (currByte & 0x040)!=0;
			Boolean isProtected = (currByte & 0x020)!=0;
			
			//reading length
			Integer avpLength=0;
			currByte = message.readUnsignedByte();
			if(currByte>0)
				avpLength += (currByte << 16);
					
			currByte = message.readUnsignedByte();
			if(currByte>0)
				avpLength += (currByte << 8);
			
			currByte = message.readUnsignedByte();
			if(currByte>0)
				avpLength += currByte;
			
			Long vendorID = null;
			if(hasVendorID)
			{
				if(message.readableBytes()>=4)
					vendorID = message.readUnsignedInt();
				else
					throw new DiameterException("The message has vendor bit set for " + avpCode + ",however not enough bytes for vendor ID", null , ResultCodes.DIAMETER_INVALID_AVP_BITS, null);									
			}
			
			Integer remainingLength = avpLength - 8;
			if(hasVendorID)
				remainingLength -= 4;
			
			DiameterAvpKey key=new DiameterAvpKey(vendorID, avpCode);
			AvpData currentAvp = childAvps.get(key);
			
			if(currentAvp == null)
			{
				//set optional if possible as octet string , ignore mandatory for FailedAvp
				if(isMandatory && !(parent instanceof FailedAvp))
					throw new DiameterException("The message has mandory bit set for " + avpCode + ",however avp is not know for local parser", null , ResultCodes.DIAMETER_AVP_UNSUPPORTED, null);
				else 
				{
					if(rejectUnmandatoryAvps!=null && rejectUnmandatoryAvps)
						throw new DiameterException("The message has " + avpCode + ",however avp is not know for local parser", null , ResultCodes.DIAMETER_AVP_UNSUPPORTED, null);
					else if(!(parent instanceof DiameterGroupedAvp))
						throw new DiameterException("The message has " + avpCode + ",however parent element is not grouped and therefore does not have optional AVPs", null , ResultCodes.DIAMETER_AVP_UNSUPPORTED, null);
										
					((DiameterGroupedAvp)parent).addOptionalAvp(key, message.readSlice(remainingLength.intValue()), isProtected);
				}
			}
			else
			{
				Class<?> implementationClazz = currentAvp.getClazz();
				if(implementationClazz==null)
					throw new DiameterException("The message has " + avpCode + ",which implementation can not be found", null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);
				
				//create the object
				DiameterAvp avp = null;
				try
				{
					Constructor<?> ctor = implementationClazz.getDeclaredConstructor();
					ctor.setAccessible(true);
					avp = (DiameterAvp) ctor.newInstance(new Object[] {  });
					avp.setProtected(isProtected);
					avp.setIsMust(isMandatory);
				}
				catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException ex)
				{
					throw new DiameterException("The message has " + avpCode + ",which can not be initiated", null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);
				}
				
				//either decode by its method or parse children if no decode method
				if(currentAvp.getDecodeMethod()!=null)
				{
					try
					{
						currentAvp.getDecodeMethod().invoke(avp, message, remainingLength);
					}
					catch(IllegalAccessException ex)
					{
						throw new DiameterException("The message has " + avpCode + ",which can not be initiated", null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
					}
					catch(InvocationTargetException ex)
					{
						if(ex.getTargetException()!=null && ex.getTargetException() instanceof DiameterException)
							throw (DiameterException)ex.getTargetException();
						
						throw new DiameterException("The message has " + avpCode + ",which can not be initiated", null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
					}
				}
				else
					decode(message, avp, isError, currentAvp.getAvpData(), remainingLength, rejectUnmandatoryAvps);
				
				//lets validate the structure if has validation
				if(!isError && currentAvp.getValidateMethod()!=null)
				{
					try
					{
						currentAvp.getValidateMethod().invoke(avp);
					}
					catch(IllegalAccessException ex)
					{
						throw new DiameterException("The message has " + avpCode + ",which can not be validated", null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
					}
					catch(InvocationTargetException ex)
					{
						if(ex.getTargetException()!=null && ex.getTargetException() instanceof DiameterException)
							throw (DiameterException)ex.getTargetException();
						
						throw new DiameterException("The message has " + avpCode + ",which can not be validated", null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
					}
				}
				
				//set the field in parent
				currentAvp.getField().setAccessible(true);
				
				try
				{
					if(currentAvp.getField().getType().isAssignableFrom(List.class))
					{
						List avpsList = (List)currentAvp.getField().get(parent);
						if(avpsList ==null)
						{
							avpsList=new ArrayList();
							currentAvp.getField().set(parent, avpsList);
						}
						
						avpsList.add(avp);
					}
					else
						currentAvp.getField().set(parent, avp);
				}
				catch(IllegalAccessException ex)
				{
					throw new DiameterException("The message has " + avpCode + ",which can not be set", null , ResultCodes.DIAMETER_UNABLE_TO_COMPLY, null);						
				}
			}
			
			length -= avpLength;
			//skipping padding
			if((remainingLength%4)!=0)
			{
				message.skipBytes(4 - (remainingLength%4));
				length -= (4 - (remainingLength%4));
			}
		}
	}
	
	public void registerGlobalApplication(ClassLoader classLoader, Package mappingName, Package packageName) throws DiameterException
	{
		//registering only once
		ConcurrentHashMap<CommandIdentifier,CommandData> commandsMapping = globalInterfacesMapping.get(mappingName.getName());
		if(commandsMapping==null)
		{
			commandsMapping = new ConcurrentHashMap<CommandIdentifier,CommandData>();
			ConcurrentHashMap<CommandIdentifier,CommandData> oldMapping = globalInterfacesMapping.putIfAbsent(mappingName.getName(), commandsMapping);
			if(oldMapping!=null)
				return;
		}
		else
			return;
		
		List<Class<?>> jarClasses=loadClassesWithJar(classLoader, packageName, false);
		List<Class<?>> compilerClasses=loadClassesWithCompiler(classLoader, packageName, false);
		ConcurrentHashMap<String,Boolean> usedClasses=new ConcurrentHashMap<String, Boolean>();
		
		for(Class<?> clazz:jarClasses)
		{
			registerGlobalCommand(commandsMapping,clazz);
			usedClasses.put(clazz.getCanonicalName(), true);
		}
		
		for(Class<?> clazz:compilerClasses)
		{
			if(usedClasses.putIfAbsent(clazz.getCanonicalName(), true) == null)
				registerGlobalCommand(commandsMapping,clazz);		
		}
	}
	
	private void registerGlobalCommand(ConcurrentHashMap<CommandIdentifier,CommandData> commandsMapping, Class<?> clazz) throws DiameterException
	{
		DiameterCommandDefinition commandDefition = getCommandDefinition(clazz);						
		if(commandDefition!=null)
		{
			Long applicationID = commandDefition.applicationId();
			String name = commandDefition.name();
			Integer commandCode = commandDefition.commandCode();
			Boolean isRequest = commandDefition.request();
			Boolean isProxyable = commandDefition.proxyable();
			
			CommandIdentifier identifier = new CommandIdentifier(commandCode, isRequest);
			CommandData oldCommand = commandsMapping.get(identifier);
			if(oldCommand!=null)
			{
				if(isRequest!=null && isRequest)
					throw new DiameterException("The request " + commandCode +  " is already registered for application " + applicationID, null, null, null);
				else
					throw new DiameterException("The answer " + commandCode +  " is already registered for application " + applicationID, null, null, null);
			}
				
			Method[] methods=clazz.getMethods();
			Method validateMethod = null, orderMethod = null;
			for(Method method:methods) 
			{
				DiameterValidate validateMethodAnnotation=method.getAnnotation(DiameterValidate.class);
				if(validateMethodAnnotation!=null && validateMethod==null)
					validateMethod = method;
					
				DiameterOrder orderMethodAnnotation=method.getAnnotation(DiameterOrder.class);
				if(orderMethodAnnotation!=null && orderMethod==null)
					orderMethod = method;
			}
				
			CommandData newCommand = new CommandData(clazz, name, commandCode, applicationID, validateMethod, orderMethod, isRequest, isProxyable);
			oldCommand = commandsMapping.putIfAbsent(identifier, newCommand);
			if(oldCommand!=null)
				throw new DiameterException("The command " + commandCode +  " is already registered for application " + applicationID, null, null, null);
				
			commandsMap.put(clazz, newCommand);
			buildAvpList(newCommand, applicationID, isRequest, commandCode, clazz);				
		}
	}
	
	public void registerApplication(ClassLoader classLoader, Package packageName) throws DiameterException
	{
		List<Class<?>> jarClasses=loadClassesWithJar(classLoader, packageName, false);
		List<Class<?>> compilerClasses=loadClassesWithCompiler(classLoader, packageName, false);
		ConcurrentHashMap<String,Boolean> usedClasses=new ConcurrentHashMap<String, Boolean>();
		
		for(Class<?> clazz:jarClasses)
		{
			registerCommand(clazz);
			usedClasses.put(clazz.getCanonicalName(), true);
		}
		
		for(Class<?> clazz:compilerClasses)
		{
			if(usedClasses.putIfAbsent(clazz.getCanonicalName(), true) == null)
				registerCommand(clazz);			
		}
	}
	
	private void registerCommand(Class<?> clazz) throws DiameterException
	{
		DiameterCommandDefinition commandDefition = getCommandDefinition(clazz);						
		if(commandDefition!=null)
		{
			Long applicationID = commandDefition.applicationId();
			Integer commandCode = commandDefition.commandCode();
			String name = commandDefition.name();
			Boolean isRequest = commandDefition.request();
			Boolean isProxyable = commandDefition.proxyable();
			ConcurrentHashMap<CommandIdentifier,CommandData> commandsMapping = interfacesMapping.get(applicationID);
			if(commandsMapping==null)
			{
				commandsMapping = new ConcurrentHashMap<CommandIdentifier,CommandData>();
				ConcurrentHashMap<CommandIdentifier,CommandData> oldMapping = interfacesMapping.putIfAbsent(applicationID, commandsMapping);
				if(oldMapping!=null)
					commandsMapping = oldMapping;
			}
			
			CommandIdentifier identifier = new CommandIdentifier(commandCode, isRequest);
			CommandData oldCommand = commandsMapping.get(identifier);
			if(oldCommand!=null)
			{
				if(isRequest!=null && isRequest)
					throw new DiameterException("The request " + commandCode +  " is already registered for application " + applicationID, null, null, null);
				else
					throw new DiameterException("The answer " + commandCode +  " is already registered for application " + applicationID, null, null, null);
			}
				
			Method[] methods=clazz.getMethods();
			Method validateMethod = null, orderMethod = null;
			for(Method method:methods) 
			{
				DiameterValidate validateMethodAnnotation=method.getAnnotation(DiameterValidate.class);
				if(validateMethodAnnotation!=null && validateMethod==null)
					validateMethod = method;
					
				DiameterOrder orderMethodAnnotation=method.getAnnotation(DiameterOrder.class);
				if(orderMethodAnnotation!=null && orderMethod==null)
					orderMethod = method;
			}
				
			CommandData newCommand = new CommandData(clazz, name, commandCode, applicationID, validateMethod, orderMethod, isRequest, isProxyable);
			oldCommand = commandsMapping.putIfAbsent(identifier, newCommand);
			if(oldCommand!=null)
				throw new DiameterException("The command " + commandCode +  " is already registered for application " + applicationID, null, null, null);
				
			commandsMap.put(clazz, newCommand);
			buildAvpList(newCommand, applicationID, isRequest, commandCode, clazz);				
		}
	}
	
	public void registerAvps(ClassLoader classLoader, Package parentPackageName) throws DiameterException
	{
		List<Class<?>> jarClasses=loadClassesWithJar(classLoader, parentPackageName, true);
		List<Class<?>> compilerClasses=loadClassesWithCompiler(classLoader, parentPackageName, true);
		ConcurrentHashMap<String,Boolean> usedClasses=new ConcurrentHashMap<String, Boolean>();
		
		for(Class<?> clazz:jarClasses)
		{
			Class<?> avpDefinition = getAvpDefinitionClass(clazz);						
			if(avpDefinition!=null)
			{
				Class<?> oldClass = avpImplementationsMap.putIfAbsent(avpDefinition, clazz);
				if(oldClass!=null)
					throw new DiameterException("The class " + avpDefinition.getCanonicalName() +  " already has registered implementation", null, null, null);						
			}
			
			usedClasses.put(clazz.getCanonicalName(), true);
		}
		
		for(Class<?> clazz:compilerClasses)
		{
			Class<?> avpDefinition = getAvpDefinitionClass(clazz);						
			if(avpDefinition!=null)
			{
				if(usedClasses.putIfAbsent(clazz.getCanonicalName(), true) == null)
				{
					Class<?> oldClass = avpImplementationsMap.putIfAbsent(avpDefinition, clazz);
					if(oldClass!=null)
						throw new DiameterException("The class " + avpDefinition.getCanonicalName() +  " already has registered implementation", null, null, null);
				}
			}			
		}
	}
	
	public void buildAvpList(ChildData parentStorage, Long applicationID, Boolean isRequest, Integer commandCode, Class<?> parentType) throws DiameterException
	{
		ConcurrentHashMap<DiameterAvpKey, AvpData> avpMapping = new ConcurrentHashMap<DiameterAvpKey, AvpData>();
		List<AvpData> orderedAvpData = new ArrayList<AvpData>();
		List<Class<?>> allClasses = getAllClasses(parentType);
		for(Class<?> current: allClasses)
		{
			Field[] fields=current.getDeclaredFields();
			if(fields!=null)
			{
				for(Field field:fields)
				{
					Class<?> interfaceType=field.getType();
					if(field.getType().isAssignableFrom(List.class))
					{
						Type[] innerTypes = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
						if(innerTypes.length==1)
							interfaceType=(Class<?>)innerTypes[0];												
					}
					
					DiameterAvpDefinition avpDefition = getAvpDefinition(interfaceType);						
					if(avpDefition!=null)
					{
						Class<?> implementationType = avpImplementationsMap.get(interfaceType);
						if(implementationType==null)
						{
							if(isRequest)
								throw new DiameterException("The AVP " + avpDefition.vendorId() + ":" + avpDefition.code() +  " does not have implementation registered yet for application request " + applicationID + ":" + commandCode + " in class " + parentType.getCanonicalName(), null, null, null);
							else
								throw new DiameterException("The AVP " + avpDefition.vendorId() + ":" + avpDefition.code() +  " does not have implementation registered yet for application answer " + applicationID + ":" + commandCode + " in class " + parentType.getCanonicalName(), null, null, null);
						}
						
						DiameterAvpKey key = new DiameterAvpKey(avpDefition.vendorId(), avpDefition.code());
						AvpData oldAvp = avpMapping.get(key);
						if(oldAvp!=null)
						{
							if(isRequest)
								throw new DiameterException("The AVP " + avpDefition.vendorId() + ":" + avpDefition.code() +  " is already registered for application request " + applicationID + ":" + commandCode + " in class " + parentType.getCanonicalName(), null, null, null);
							else
								throw new DiameterException("The AVP " + avpDefition.vendorId() + ":" + avpDefition.code() +  " is already registered for application answer " + applicationID + ":" + commandCode + " in class " + parentType.getCanonicalName(), null, null, null);
						}
						
						Method[] methods=implementationType.getMethods();
						Method orderMethod = null, validateMethod = null, lengthMethod = null, encodeMethod = null, decodeMethod = null, printMethod = null;
						for(Method method:methods) 
						{
							DiameterOrder orderMethodAnnotation=method.getAnnotation(DiameterOrder.class);
							if(orderMethodAnnotation!=null && orderMethod==null)
								orderMethod = method;
							
							DiameterValidate validateMethodAnnotation=method.getAnnotation(DiameterValidate.class);
							if(validateMethodAnnotation!=null && validateMethod==null)
								validateMethod = method;
							
							DiameterLength lengthMethodAnnotation=method.getAnnotation(DiameterLength.class);
							if(lengthMethodAnnotation!=null && lengthMethod==null)
								lengthMethod = method;
							
							DiameterEncode encodeMethodAnnotation=method.getAnnotation(DiameterEncode.class);
							if(encodeMethodAnnotation!=null && encodeMethod==null)
								encodeMethod = method;
							
							DiameterDecode decodeMethodAnnotation=method.getAnnotation(DiameterDecode.class);
							if(decodeMethodAnnotation!=null && decodeMethod==null)
								decodeMethod = method;
							
							DiameterPrint printMethodAnnotation=method.getAnnotation(DiameterPrint.class);
							if(printMethodAnnotation!=null && printMethod==null)
								printMethod = method;
						}
						
						AvpData avpData = new AvpData(implementationType, avpDefition.name(), field, avpDefition.vendorId(), avpDefition.code(), avpDefition.must(), validateMethod, orderMethod, encodeMethod, decodeMethod, lengthMethod, printMethod);
						oldAvp = avpMapping.putIfAbsent(key, avpData);
						if(oldAvp!=null)
						{
							if(isRequest)
								throw new DiameterException("The AVP " + avpDefition.vendorId() + ":" + avpDefition.code() +  " is already registered for application request " + applicationID + ":" + commandCode + " in class " + parentType.getCanonicalName(), null, null, null);
							else
								throw new DiameterException("The AVP " + avpDefition.vendorId() + ":" + avpDefition.code() +  " is already registered for application answer " + applicationID + ":" + commandCode + " in class " + parentType.getCanonicalName(), null, null, null);
						}
						
						orderedAvpData.add(avpData);
						avpsMap.put(implementationType, avpData);
						buildAvpList(avpData, applicationID, isRequest, commandCode, implementationType);							
					}
				}
			}
		}
		
		parentStorage.setAvpData(avpMapping);
		parentStorage.setOrderedAvpData(orderedAvpData);
	}
 
	public static List<Class<?>> loadClassesWithJar(ClassLoader classLoader, Package packageName, boolean recurse) throws DiameterException
	{
		List<Class<?>> classes = new ArrayList<>();
		try
		{
			String path = packageName.getName().replace('.', '/');
	 
			Enumeration<URL> resources = classLoader.getResources(path);
	 
			while (resources.hasMoreElements())
			{
				URL resource = resources.nextElement();
				String protocol = resource.getProtocol();
	 
				if ("jar".equals(protocol))
				{
					String jarPath = resource.getPath().substring(5, resource.getPath().indexOf("!"));
					try (JarFile jarFile = new JarFile(URLDecoder.decode(jarPath, "UTF-8")))
					{
						Enumeration<JarEntry> entries = jarFile.entries();
						while (entries.hasMoreElements())
						{
							JarEntry entry = entries.nextElement();
							String entryName = entry.getName();
							if (entryName.startsWith(path) && entryName.endsWith(".class") && !entryName.contains("$"))
							{
								String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
								Class<?> clazz = Class.forName(className);
								if(recurse || clazz.getPackage().equals(packageName))
									classes.add(clazz);								
							}
						}
					}
				}
			}
		}
		catch(IOException | ClassNotFoundException ex)
		{
			throw new DiameterException("Package " + packageName.getName() + " can not be loaded", null, null, null);
		}

		return classes;
	}  
	
	public static List<Class<?>> loadClassesWithCompiler(ClassLoader classLoader, Package packageName, boolean recurse) throws DiameterException
	{		
		List<Class<?>> result = new ArrayList<Class<?>>();

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

		StandardLocation location = StandardLocation.CLASS_PATH;
		Set<JavaFileObject.Kind> kinds = new HashSet<>();
		kinds.add(JavaFileObject.Kind.CLASS);
		
		Iterable<JavaFileObject> list = null;
		try
		{
			list = fileManager.list(location, packageName.getName(), kinds, recurse);
		}
		catch(IOException ex)
		{
			throw new DiameterException("Package " + packageName.getName() + " can not be loaded", null, null, null);
		}
		
		String pkgName = packageName.getName();
		String pkgPath = pkgName.replace('.', '/');
	 	
		for (JavaFileObject classFile : list) {
			String name = classFile.getName();
			int startIndex = name.indexOf(pkgPath);
			int endIndex = name.indexOf(".class");
			String className = name.substring(startIndex,endIndex).replace("/", ".");
			
			Class<?> clazz;
			try
			{
				clazz = classLoader.loadClass(className);
				result.add(clazz);
			} 
			catch (ClassNotFoundException e)
			{
				throw new DiameterException("Can not load class " + className, null, null, null);
			}				
		}	
		
		return result;
	}		
	
	public static DiameterAvpDefinition getAvpDefinition(Class<?> clazz)
	{
		DiameterAvpDefinition avpDefinition = avpDefsMap.get(clazz);
		if(avpDefinition!=null)
			return avpDefinition;
		
		List<Class<?>> allInterfaces=getAllInterfaces(clazz);
		for(Class<?> currInterface:allInterfaces)
		{
			if(currInterface.isAnnotationPresent(DiameterAvpDefinition.class))
			{
				avpDefinition = currInterface.getAnnotation(DiameterAvpDefinition.class);
				break;
			}
		}
		
		if(avpDefinition!=null)
			avpDefsMap.put(clazz, avpDefinition);
		
		return avpDefinition;
	}
	
	public static Class<?> getAvpDefinitionClass(Class<?> clazz)
	{
		Class<?> avpDefitionCass = null;
		List<Class<?>> allInterfaces=getAllInterfaces(clazz);
		for(Class<?> currInterface:allInterfaces)
		{
			if(currInterface.isAnnotationPresent(DiameterAvpDefinition.class))
			{
				avpDefitionCass = currInterface;
				break;
			}
		}
		
		return avpDefitionCass;
	}
	
	public static DiameterCommandDefinition getCommandDefinition(Class<?> clazz)
	{
		DiameterCommandDefinition commandDefition = commandDefsMap.get(clazz);
		if(commandDefition!=null)
			return commandDefition;
		
		List<Class<?>> allInterfaces=getAllInterfaces(clazz);
		for(Class<?> currInterface:allInterfaces)
		{
			if(currInterface.isAnnotationPresent(DiameterCommandDefinition.class))
			{
				commandDefition = currInterface.getAnnotation(DiameterCommandDefinition.class);
				break;
			}
		}
		
		if(commandDefition!=null)
			commandDefsMap.put(clazz, commandDefition);
		
		return commandDefition;
	}
	
	public static List<Class<?>> getAllInterfaces(Class<?> clazz) 
	{
		List<Class<?>> pendingQueue = new ArrayList<Class<?>>();
		List<Class<?>> result = new ArrayList<Class<?>>();
		
		pendingQueue.add(clazz);
		result.add(clazz);
		while (pendingQueue.size()>0) 
		{
			Class<?>[] interfaces = pendingQueue.remove(0).getInterfaces();
	
			for (int i = 0; i < interfaces.length; i++) 
			{
				if (!result.contains(interfaces[i])) 
				{
					result.add(interfaces[i]);
					pendingQueue.add(interfaces[i]);
				}
			}
		}
		
		return result;
	}
	
	public static List<Class<?>> getAllClasses(Class<?> clazz) 
	{
		List<Class<?>> pendingQueue = new ArrayList<Class<?>>();
		List<Class<?>> result = new ArrayList<Class<?>>();
		
		pendingQueue.add(clazz);
		while (pendingQueue.size()>0) 
		{
			Class<?> current=pendingQueue.remove(0);
			result.add(current);
			Class<?> parentClass = current.getSuperclass();
			if(parentClass!=null)
				pendingQueue.add(parentClass);
		}
		
		return result;
	}
	
	public static String toHex(ByteBuf buffer)
	{
		StringBuilder sb=new StringBuilder();
		int count=0;
		while(buffer.readableBytes()>0) 
		{
			sb.append(String.format("%02X", buffer.readByte()));		    
			count++;
			
			if(count%16==0)
				sb.append(".");					    
		}
		
		return sb.toString();
	}
}