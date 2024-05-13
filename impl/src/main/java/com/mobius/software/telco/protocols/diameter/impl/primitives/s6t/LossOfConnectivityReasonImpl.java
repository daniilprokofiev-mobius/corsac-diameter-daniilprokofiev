package com.mobius.software.telco.protocols.diameter.impl.primitives.s6t;
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

import com.mobius.software.telco.protocols.diameter.impl.primitives.DiameterEnumeratedImpl;
import com.mobius.software.telco.protocols.diameter.primitives.s6t.LossOfConnectivityReason;
import com.mobius.software.telco.protocols.diameter.primitives.s6t.LossOfConnectivityReasonEnum;

/**
*
* @author yulian oifa
*
*/
public class LossOfConnectivityReasonImpl extends DiameterEnumeratedImpl<LossOfConnectivityReasonEnum> implements LossOfConnectivityReason
{
	protected LossOfConnectivityReasonImpl()
	{
		super();
	}

	protected LossOfConnectivityReasonImpl(Integer minValue, Integer maxValue)
	{
		super(minValue, maxValue);
	}

	public LossOfConnectivityReasonImpl(LossOfConnectivityReasonEnum value, Integer minValue, Integer maxValue)
	{
		super(value, minValue, maxValue);
	}
}