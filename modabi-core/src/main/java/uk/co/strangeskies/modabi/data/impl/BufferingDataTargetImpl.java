package uk.co.strangeskies.modabi.data.impl;

import java.math.BigDecimal;
import java.math.BigInteger;

import uk.co.strangeskies.modabi.data.BufferedDataSource;
import uk.co.strangeskies.modabi.data.BufferingDataTarget;

public class BufferingDataTargetImpl implements BufferingDataTarget {
	@Override
	public BufferingDataTargetImpl binary(byte[] value) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public BufferingDataTargetImpl string(String value) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public BufferingDataTargetImpl integer(BigInteger value) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public BufferingDataTargetImpl decimal(BigDecimal value) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public BufferingDataTargetImpl intValue(int value) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public BufferingDataTargetImpl longValue(long value) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public BufferingDataTargetImpl floatValue(float value) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public BufferingDataTargetImpl doubleValue(double value) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public BufferingDataTargetImpl booleanValue(boolean value) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public BufferedDataSource buffer() {
		return null;
	}
}
