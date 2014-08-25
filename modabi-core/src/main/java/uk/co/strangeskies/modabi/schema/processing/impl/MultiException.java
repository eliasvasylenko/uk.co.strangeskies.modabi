package uk.co.strangeskies.modabi.schema.processing.impl;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MultiException extends RuntimeException {
	private static final long serialVersionUID = -5252945414846966968L;

	private final List<Throwable> causes;

	public MultiException(Collection<? extends Throwable> causes) {
		this.causes = new ArrayList<>(causes);
	}

	public MultiException(String message, Collection<? extends Throwable> causes) {
		super(message, causes.stream().findFirst().get());
		this.causes = new ArrayList<>(causes);
	}

	@Override
	public void printStackTrace() {
		printStackTrace(System.err);
	}

	@Override
	public void printStackTrace(PrintStream output) {
		printStackTrace(new PrintWriter(output));
	}

	@Override
	public void printStackTrace(PrintWriter output) {
		super.printStackTrace();
		int i = 0;
		for (Throwable cause : causes) {
			output.println("Cased by (" + i++ + " of " + causes.size() + "):");
			cause.printStackTrace(output);
		}
	}
}
