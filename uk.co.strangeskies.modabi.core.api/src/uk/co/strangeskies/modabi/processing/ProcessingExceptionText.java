package uk.co.strangeskies.modabi.processing;

import java.lang.reflect.Executable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.ModabiExceptionText;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.schema.BindingChildNode;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.text.LocalizedString;
import uk.co.strangeskies.utilities.text.LocalizedText;

public interface ProcessingExceptionText extends LocalizedText<ProcessingExceptionText> {
	ModabiExceptionText modabiException();

	LocalizedString bindingObjects(Collection<? extends Object> bindingObjectStack);

	LocalizedString bindingNodes(Collection<? extends SchemaNode<?, ?>> bindingNodeStack);

	LocalizedString noModelFound(QualifiedName modelName);

	LocalizedString noModelFound(QualifiedName modelName, Collection<? extends Model<?>> candidates, TypeToken<?> type);

	LocalizedString mustHaveChildren(QualifiedName name, InputBindingStrategy strategy);

	LocalizedString cannotInvoke(Executable inputMethod, TypeToken<?> targetType, SchemaNode<?, ?> node,
			List<?> parameters);

	LocalizedString mustHaveData(QualifiedName node);

	LocalizedString noFormatFound();

	LocalizedString noFormatFoundFor(String id);

	LocalizedString noProviderFound(TypeToken<?> type);

	default LocalizedString mustHaveDataWithinRange(BindingChildNode<?, ?, ?> node) {
		return mustHaveDataWithinRange(node.name(), Range.compose(node.occurrences()));
	}

	LocalizedString mustHaveDataWithinRange(QualifiedName name, String compose);

	default LocalizedString cannotBindRemainingData(DataSource dataSource) {
		return cannotBindRemainingData(dataSource.stream().map(Objects::toString).collect(Collectors.toList()));
	}

	LocalizedString cannotBindRemainingData(List<String> dataSource);

	LocalizedString mustSupplyAttemptItems();

	LocalizedString unexpectedProblemProcessing(Object data, Model<?> model);
}
