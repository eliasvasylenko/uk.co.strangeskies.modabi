package uk.co.strangeskies.modabi.data.io.structured;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.data.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.data.io.TerminatingDataSource;
import uk.co.strangeskies.modabi.data.io.TerminatingDataTarget;

public interface BufferedStructuredInput extends StructuredInput {
	class BufferedStructuredInputImpl implements BufferedStructuredInput {
		private final List<BufferedStructuredInputImpl> children;
		private final Map<String, BufferedDataSource> properties;
		private BufferedDataSource content;
		private int index;
		private final String name;

		public BufferedStructuredInputImpl(String name) {
			children = new ArrayList<>();
			properties = new HashMap<>();
			index = 0;
			this.name = name;
		}

		@Override
		public String nextChild() {
			return children.get(index++).getName();
		}

		private String getName() {
			return name;
		}

		@Override
		public Set<String> properties() {
			return properties.keySet();
		}

		@Override
		public TerminatingDataSource propertyData(String name) {
			return properties.get(name);
		}

		@Override
		public TerminatingDataSource content() {
			return content;
		}

		@Override
		public boolean endElement() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void reset() {
			for (BufferedStructuredInputImpl child : children)
				child.reset();
			for (BufferedDataSource property : properties.values())
				property.reset();
			content.reset();
		}
	}

	void reset();

	static BufferingStructuredOutput from() {
		return new BufferingStructuredOutput() {
			BufferedStructuredInputImpl root;

			@Override
			public TerminatingDataTarget property(String name) {
				BufferingDataTarget target = BufferedDataSource.from();

				targetOperations.add(o -> target.buffer().pipe(o.property(name))
						.terminate());

				return target;
			}

			@Override
			public void endElement() {
				targetOperations.add(StructuredOutput::endElement);
			}

			@Override
			public TerminatingDataTarget content() {
				BufferingDataTarget target = BufferedDataSource.from();

				targetOperations
						.add(o -> target.buffer().pipe(o.content()).terminate());

				return target;
			}

			@Override
			public void childElement(String name) {
				targetOperations.add(o -> o.childElement(name));
			}

			@Override
			public BufferedStructuredInput buffer() {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}
}
