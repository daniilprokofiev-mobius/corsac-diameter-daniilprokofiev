package com.mobius.software.telco.protocols.diameter.impl.primitives.rfc5778;
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

import com.mobius.software.telco.protocols.diameter.annotations.DiameterAvpImplementation;
import com.mobius.software.telco.protocols.diameter.annotations.DiameterValidate;
import com.mobius.software.telco.protocols.diameter.impl.primitives.DiameterGroupedAvpImpl;
import com.mobius.software.telco.protocols.diameter.impl.primitives.rfc4004.MIPAlgorithmTypeImpl;
import com.mobius.software.telco.protocols.diameter.impl.primitives.rfc4004.MIPMSALifetimeImpl;
import com.mobius.software.telco.protocols.diameter.impl.primitives.rfc4004.MIPReplayModeImpl;
import com.mobius.software.telco.protocols.diameter.impl.primitives.rfc4004.MIPSessionKeyImpl;
import com.mobius.software.telco.protocols.diameter.primitives.rfc4004.MIPAlgorithmType;
import com.mobius.software.telco.protocols.diameter.primitives.rfc4004.MIPAlgorithmTypeEnum;
import com.mobius.software.telco.protocols.diameter.primitives.rfc4004.MIPMSALifetime;
import com.mobius.software.telco.protocols.diameter.primitives.rfc4004.MIPReplayMode;
import com.mobius.software.telco.protocols.diameter.primitives.rfc4004.MIPReplayModeEnum;
import com.mobius.software.telco.protocols.diameter.primitives.rfc4004.MIPSessionKey;
import com.mobius.software.telco.protocols.diameter.primitives.rfc5778.MIPMNHAMSA;
import com.mobius.software.telco.protocols.diameter.primitives.rfc5778.MIPMNHASPI;

import io.netty.buffer.ByteBuf;

/**
*
* @author yulian oifa
*
*/
@DiameterAvpImplementation(code = 492L, vendorId = -1L)
public class MIPMNHAMSAImpl extends DiameterGroupedAvpImpl implements MIPMNHAMSA
{
	private MIPSessionKey mipSessionKey;
	
	private MIPMSALifetime mipMSALifetime;
	
	private MIPMNHASPI mipMNHASPI;
	
	private MIPAlgorithmType mipAlgorithmType;
	
	private MIPReplayMode mipReplayMode;
	
	public MIPMNHAMSAImpl(ByteBuf mipSessionKey, Long mipMSALifetime)
	{
		if(mipSessionKey==null)
			throw new IllegalArgumentException("MIP-Session-Key is required");
		
		if(mipMSALifetime==null)
			throw new IllegalArgumentException("MIP-HA-to-MN-SPI is required");
		
		this.mipSessionKey = new MIPSessionKeyImpl(mipSessionKey, null, null);
		
		this.mipMSALifetime = new MIPMSALifetimeImpl(mipMSALifetime, null, null);	
	}
	
	@Override
	public ByteBuf getMIPSessionKey() 
	{
		if(mipSessionKey == null)
			return null;
		
		return mipSessionKey.getValue();
	}

	@Override
	public void setMIPSessionKey(ByteBuf value) 
	{
		if(value==null)
			throw new IllegalArgumentException("MIP-Session-Key is required");
		
		this.mipSessionKey = new MIPSessionKeyImpl(value, null, null);
	}	
			
	public Long getMIPMSALifetime()
	{
		if(mipMSALifetime == null)
			return null;
		
		return mipMSALifetime.getUnsigned();
	}
	
	public void setMIPMSALifetime(Long value)
	{
		if(value==null)
			throw new IllegalArgumentException("MIP-MSA-Lifetime is required");
		
		this.mipMSALifetime = new MIPMSALifetimeImpl(value, null, null);		
	}
	
	public Long getMIPMNHASPI()
	{
		if(mipMNHASPI == null)
			return null;
		
		return mipMNHASPI.getUnsigned();
	}
	
	public void setMIPMNHASPI(Long value)
	{
		if(value==null)
			this.mipMNHASPI = null;
		else
			this.mipMNHASPI = new MIPMNHASPIImpl(value, null, null);		
	}
	
	public MIPAlgorithmTypeEnum getMIPAlgorithmType()
	{
		if(mipAlgorithmType == null)
			return null;
		
		return mipAlgorithmType.getEnumerated(MIPAlgorithmTypeEnum.class);
	}
	
	public void setMIPAlgorithmType(MIPAlgorithmTypeEnum value)
	{
		if(value==null)
			this.mipAlgorithmType = null;
		else
			this.mipAlgorithmType = new MIPAlgorithmTypeImpl(value, null, null);
	}
	
	public MIPReplayModeEnum getMIPReplayMode()
	{
		if(mipReplayMode == null)
			return null;
		
		return mipReplayMode.getEnumerated(MIPReplayModeEnum.class);
	}
	
	public void setMIPReplayMode(MIPReplayModeEnum value)
	{
		if(value==null)
			this.mipReplayMode = null;
		else
			this.mipReplayMode = new MIPReplayModeImpl(value, null, null);
	}		
	
	@DiameterValidate
	public String validate()
	{
		if(mipSessionKey==null)
			return "MIP-Session-Key is required";
		
		if(mipMSALifetime==null)
			return "MIP-MSA-Lifetime is required";
		
		return null;
	}
}