package uk.co.strangeskies.modabi;

import uk.co.strangeskies.modabi.model.Model;

public class Binding<T> {
	private final Model<T> model;
	private final T data;

	public Binding(Model<T> model, T data) {
		this.model = model;
		this.data = data;
	}

	public Model<T> getModel() {
		return model;
	}

	public T getData() {
		return data;
	}
}
