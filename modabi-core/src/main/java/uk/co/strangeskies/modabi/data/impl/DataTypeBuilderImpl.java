package uk.co.strangeskies.modabi.data.impl;

import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.data.DataTypeBuilder;
import uk.co.strangeskies.modabi.data.DataTypeConfigurator;

public class DataTypeBuilderImpl implements DataTypeBuilder {
	@Override
	public DataTypeConfigurator<Object> configure() {
		return new DataTypeConfigurator<Object>() {
			private String name;

			@Override
			public DataType<Object> create() {
				if (name == null)
					throw new NullPointerException();

				return new DataType<Object>() {
					@Override
					public String getParseMethod() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public String getName() {
						return name;
					}

					@Override
					public String getFactoryMethod() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public Class<?> getFactoryClass() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public DataType<Object> getBaseType() {
						// TODO Auto-generated method stub
						return null;
					}
				};
			}

			@Override
			public DataTypeConfigurator<Object> name(String name) {
				this.name = name;

				return this;
			}

			@Override
			public <U> DataTypeConfigurator<U> baseType(DataType<U> from) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public DataTypeConfigurator<Object> parseMethod(String name) {
				// TODO Auto-generated method stub
				return this;
			}

			@Override
			public DataTypeConfigurator<Object> factoryClass(Class<?> factoryClass) {
				// TODO Auto-generated method stub
				return this;
			}

			@Override
			public DataTypeConfigurator<Object> factoryMethod(String name) {
				// TODO Auto-generated method stub
				return this;
			}
		};
	}
}
