package com.mobius.software.telco.protocols.diameter.impl.primitives;
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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.mobius.software.telco.protocols.diameter.annotations.DiameterDecode;

import io.netty.buffer.ByteBuf;

public class DiameterQosFilterRule extends DiameterAsciiString
{
	private DiameterQosAction action;
	private DiameterRuleDirection direction;
	private InternetProtocol protocol;
	private DiameterRuleAddress from;
	private List<DiameterRulePorts> fromPorts;
	private DiameterRuleAddress to;
	private List<DiameterRulePorts> toPorts;
	
	private String dscpColor;
	private Long meteringRate;
	private String colorUnder;
	private String colorOver;
	
	//required for parser
	public DiameterQosFilterRule() 
	{
		super();
	}
		
	public DiameterQosFilterRule(Integer minLength,Integer maxLength) 
	{
		super(minLength,maxLength);		
	}
		
	public DiameterQosFilterRule(String rule,Integer minLength,Integer maxLength) throws ParseException 
	{
		super(rule,minLength,maxLength);	
		parseRule();
	}

	public DiameterQosFilterRule(DiameterQosAction action,DiameterRuleDirection direction,InternetProtocol protocol,DiameterRuleAddress from,List<DiameterRulePorts> fromPorts,DiameterRuleAddress to,List<DiameterRulePorts> toPorts,String dscpColor,Long meteringRate,String colorUnder,String colorOver,Integer minLength,Integer maxLength) throws ParseException 
	{
		super(buildRule(action,direction,protocol,from,fromPorts,to,toPorts,dscpColor,meteringRate,colorUnder,colorOver),minLength,maxLength);	
		this.action=action;
		this.direction=direction;
		this.protocol=protocol;
		this.from=from;
		this.fromPorts=fromPorts;
		this.to=to;
		this.toPorts=toPorts;
		this.dscpColor=dscpColor;
		this.meteringRate=meteringRate;
		this.colorUnder=colorUnder;
		this.colorOver=colorOver;
	}

	public String getRule()
	{
		return this.getRealValue();
	}
	
	public DiameterQosAction getAction() 
	{
		return action;
	}

	public DiameterRuleDirection getDirection() 
	{
		return direction;
	}

	public InternetProtocol getProtocol() 
	{
		return protocol;
	}

	public DiameterRuleAddress getFrom() 
	{
		return from;
	}

	public List<DiameterRulePorts> getFromPorts() 
	{
		return fromPorts;
	}

	public DiameterRuleAddress getTo() 
	{
		return to;
	}

	public List<DiameterRulePorts> getToPorts() 
	{
		return toPorts;
	}

	public String getDscpColor() 
	{
		return dscpColor;
	}

	public Long getMeteringRate() 
	{
		return meteringRate;
	}

	public String getColorUnder() 
	{
		return colorUnder;
	}

	public String getColorOver() 
	{
		return colorOver;
	}

	@DiameterDecode
	public String decode(ByteBuf buffer,Integer length) 
	{
		super.decode(buffer, length);
		if(getRealValue()!=null)
		{
			try
			{
				parseRule();
			}
			catch(ParseException ex)
			{
				return ex.getMessage();
			}
		}
		
		return null;
	}
		
	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getRule() == null) ? 0 : getRule().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
			
		if (getClass() != obj.getClass())
			return false;
			
		DiameterQosFilterRule other = (DiameterQosFilterRule) obj;
		if (getRule() == null) 
		{
			if (other.getRule() != null)
				return false;
		} 
		else if (!getRule().equals(other.getRule()))
			return false;
			
		return true;
	}
	
	private void parseRule() throws ParseException
	{
		String rule=getRule();
		
		//lets first remove spaces near commans
		rule=rule.replace(" ,", ",");
		rule=rule.replace(", ", ",");
				
		String[] segments = rule.split(" ");
		int errorOffset = 0;
		
		if(segments.length<8)
			throw new ParseException("Invalid rule " + rule, errorOffset);
		
		action=DiameterQosAction.fromString(segments[0]);
		if(action==null)
			throw new ParseException("Invalid action " + segments[0], errorOffset);
		
		errorOffset += segments[0].length()+1;
		
		direction=DiameterRuleDirection.fromString(segments[1]);
		if(direction==null)
			throw new ParseException("Invalid dir " + segments[1], errorOffset);
		
		errorOffset += segments[1].length()+1;
		
		protocol=InternetProtocol.fromString(segments[2]);
		if(protocol==null)
			throw new ParseException("Invalid proto " + segments[2], errorOffset);
		
		errorOffset += segments[2].length()+1;
		
		if(!segments[3].equalsIgnoreCase("from"))
			throw new ParseException("Invalid word, expecting from " + segments[3], errorOffset);
			
		errorOffset += segments[3].length()+1;
		
		from=new DiameterRuleAddress(segments[4]);
		errorOffset += segments[4].length()+1;
		
		Integer nextIndex=5;
		if(!segments[5].equals("to"))
		{
			String[] portSegments=segments[5].toString().split(",");
			nextIndex++;
			
			fromPorts=new ArrayList<DiameterRulePorts>();
			for(int i=0;i<portSegments.length;i++)
			{
				DiameterRulePorts currPorts=new DiameterRulePorts(portSegments[i]);
				fromPorts.add(currPorts);
			}
		}
		
		errorOffset += segments[5].length()+1;
		
		if(!segments[nextIndex].equalsIgnoreCase("to"))
			throw new ParseException("Invalid word, expecting from " + segments[nextIndex], errorOffset);
			
		errorOffset += segments[nextIndex++].length()+1;
		
		to=new DiameterRuleAddress(segments[nextIndex]);
		errorOffset += segments[nextIndex++].length()+1;
		
		if(nextIndex==segments.length)
			return;
		
		DiameterRuleOption option=DiameterRuleOption.fromString(segments[nextIndex]);
		if(option==null)
		{
			String[] portSegments=segments[nextIndex].toString().split(",");
			errorOffset += segments[nextIndex].length()+1;
			nextIndex++;
			
			toPorts=new ArrayList<DiameterRulePorts>();
			for(int i=0;i<portSegments.length;i++)
			{
				DiameterRulePorts currPorts=new DiameterRulePorts(portSegments[i]);
				toPorts.add(currPorts);
			}
		}
		
		while(nextIndex<segments.length)
		{
			switch(segments[nextIndex])
			{
				case "DSCP":
					if(nextIndex==segments.length-1)
						throw new ParseException("Invalid option " + segments[nextIndex], errorOffset);
	
					errorOffset += segments[nextIndex].length()+1;
					nextIndex++;
					
					dscpColor=segments[nextIndex];
					errorOffset += segments[nextIndex].length()+1;
					nextIndex++;
					break;
				case "metering":
					if(nextIndex>=segments.length-3)
						throw new ParseException("Invalid option " + segments[nextIndex], errorOffset);
	
					errorOffset += segments[nextIndex].length()+1;
					nextIndex++;
					
					try
					{
						meteringRate=Long.parseLong(segments[nextIndex]);
					}
					catch(NumberFormatException ex)
					{
						throw new ParseException("Invalid metering rate " + segments[nextIndex], errorOffset);						
					}
					
					errorOffset += segments[nextIndex].length()+1;
					nextIndex++;
					
					colorUnder = segments[nextIndex];
					errorOffset += segments[nextIndex].length()+1;
					nextIndex++;
					
					colorOver = segments[nextIndex];
					errorOffset += segments[nextIndex].length()+1;
					nextIndex++;					
					break;
				default:
					throw new ParseException("Invalid option " + segments[nextIndex], errorOffset);					
			}
		}
	}
	
	public static String buildRule(DiameterQosAction action,DiameterRuleDirection direction,InternetProtocol protocol,DiameterRuleAddress from,List<DiameterRulePorts> fromPorts,DiameterRuleAddress to,List<DiameterRulePorts> toPorts,String dscpColor,Long meteringRate,String colorUnder,String colorOver) throws IllegalArgumentException
	{
		if(action==null)
			throw new IllegalArgumentException("action can not be null");
		
		if(direction==null)
			throw new IllegalArgumentException("direction can not be null");
		
		if(protocol==null)
			throw new IllegalArgumentException("protocol can not be null");
		
		if(from==null)
			throw new IllegalArgumentException("src can not be null");
		
		if(to==null)
			throw new IllegalArgumentException("dst can not be null");
		
		Integer meterOptions=0;
		if(meteringRate!=null)
			meterOptions++;
		
		if(colorUnder!=null)
			meterOptions++;
		
		if(colorOver!=null)
			meterOptions++;
		
		if(meterOptions>0 && meterOptions!=3)
			throw new IllegalArgumentException("metering requires all parameters set: rate,color under and color over");
		
		StringBuilder sb=new StringBuilder();
		sb.append(action.getValue());
		sb.append(" ");
		sb.append(direction.getValue());
		sb.append(" ");
		sb.append(protocol.getValue());
		sb.append(" ");
		sb.append("from");
		sb.append(" ");
		sb.append(from.getValue());
		sb.append(" ");
		
		if(fromPorts!=null && fromPorts.size()>0)
		{
			sb.append(fromPorts.get(0).getValue());
			
			for(int i=1;i<fromPorts.size();i++)
			{
				sb.append(",");
				sb.append(fromPorts.get(i).getValue());					
			}
			
			sb.append(" ");			
		}
		
		sb.append("to");
		sb.append(" ");
		sb.append(to.getValue());
		
		if(toPorts!=null && toPorts.size()>0)
		{
			sb.append(" ");
			sb.append(toPorts.get(0).getValue());
			
			for(int i=1;i<toPorts.size();i++)
			{
				sb.append(",");
				sb.append(toPorts.get(i).getValue());					
			}	
		}
		
		if(dscpColor!=null)
		{
			sb.append(" ");		
			sb.append("DSCP");
			sb.append(" ");
			sb.append(dscpColor);
		}

		if(meteringRate!=null)
		{
			sb.append(" ");
			sb.append("metering");
			sb.append(" ");
			sb.append(meteringRate);
			sb.append(" ");
			sb.append(colorUnder);
			sb.append(" ");
			sb.append(colorOver);
		}

		return sb.toString();
	}
}