package uk.co.strangeskies.modabi;

import uk.co.strangeskies.gears.utilities.Factory;

public interface ElementDeclarationNodeBuilder extends
		Factory<ElementDeclarationNode> {
	public ElementDeclarationNodeBuilder name(String name);

	public ElementDeclarationNodeBuilder dataClass(Class<?> dataClass);

	public ElementDeclarationNodeBuilder addChild(DeclarationNode child);
}
