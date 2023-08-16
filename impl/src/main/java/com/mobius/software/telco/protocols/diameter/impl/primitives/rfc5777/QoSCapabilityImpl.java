package com.mobius.software.telco.protocols.diameter.impl.primitives.rfc5777;
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

import com.mobius.software.telco.protocols.diameter.annotations.DiameterAvpDefinition;
import com.mobius.software.telco.protocols.diameter.annotations.DiameterValidate;
import com.mobius.software.telco.protocols.diameter.impl.primitives.DiameterGroupedAvpImpl;
import com.mobius.software.telco.protocols.diameter.primitives.rfc5777.QoSCapability;
import com.mobius.software.telco.protocols.diameter.primitives.rfc5777.QoSProfileTemplate;

/**
*
* @author yulian oifa
*
*/

@DiameterAvpDefinition(code = 578L, vendorId = -1L, name = "QoS-Capability")
public class QoSCapabilityImpl extends DiameterGroupedAvpImpl implements QoSCapability
{
	private List<QoSProfileTemplate> qoSProfileTemplate;
	
	protected QoSCapabilityImpl()
	{
		
	}
	
	public QoSCapabilityImpl(List<QoSProfileTemplate> qoSProfileTemplate)
	{
		if(qoSProfileTemplate == null || qoSProfileTemplate.size()==0)
			throw new IllegalArgumentException("QoS-Profile-Template is required");
		
		this.qoSProfileTemplate = qoSProfileTemplate;
	}
	
	public List<QoSProfileTemplate> getQoSProfileTemplate()
	{
		return qoSProfileTemplate;
	}
	
	public void setQoSProfileTemplate(List<QoSProfileTemplate> value)
	{
		if(qoSProfileTemplate == null || qoSProfileTemplate.size()==0)
			throw new IllegalArgumentException("QoS-Profile-Template is required");
		
		this.qoSProfileTemplate = value;
	}
	
	@DiameterValidate
	public String validate()
	{
		if(qoSProfileTemplate == null || qoSProfileTemplate.size()==0)
			return "QoS-Profile-Template is required";
		
		return null;
	}
}