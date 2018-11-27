package uk.co.strangeskies.modabi.schema.meta;

import static uk.co.strangeskies.reflection.token.TypeToken.forType;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.grammar.AnonymousSymbol;
import uk.co.strangeskies.modabi.grammar.Sequence;
import uk.co.strangeskies.modabi.schema.Permission;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface ModelBuilder {
  interface NameStep {
    PropertiesStep name(String string);

    PropertiesStep name(QualifiedName name);
  }

  interface PropertiesStep extends ChildrenStep {
    PropertiesStep partial();

    PropertiesStep permission(Permission permission);

    PropertiesStep baseModel(String name);

    PropertiesStep baseModel(QualifiedName name);

    default PropertiesStep type(Type type) {
      return type(forType(type));
    }

    PropertiesStep type(TypeToken<?> type);

    /**
     * Production rules for models should only produce {@link AnonymousSymbol
     * anonymous symbols}, including {@link ChildSymbol child symbols}.
     * 
     * @param production the base production rule from which a grammar can be built
     *                   for the model
     * @return the next build step
     */
    PropertiesStep production(Sequence production);
  }

  interface ChildrenStep extends ModelBuilder {
    ChildBuilder.PropertiesStep<ChildrenStep> addChild();

    default ChildrenStep addChild(
        Function<ChildBuilder.PropertiesStep<ChildrenStep>, ChildBuilder<ChildrenStep>> configuration) {
      return configuration.apply(addChild()).endChild();
    }
  }

  Optional<QualifiedName> getName();

  boolean isPartial();

  Optional<Permission> getPermission();

  Optional<QualifiedName> getBaseModel();

  Optional<TypeToken<?>> getDataType();

  Optional<Sequence> getProduction();

  Stream<? extends ChildBuilder<?>> getChildren();

  SchemaBuilder endModel();
}
