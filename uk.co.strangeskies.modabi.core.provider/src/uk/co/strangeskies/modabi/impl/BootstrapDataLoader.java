package uk.co.strangeskies.modabi.impl;

import static java.util.Arrays.asList;
import static uk.co.strangeskies.modabi.ModabiException.MESSAGES;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;
import static uk.co.strangeskies.reflection.token.TypedObject.typedObject;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.MetaSchema;
import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.Primitive;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.DataLoader;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypedObject;
import uk.co.strangeskies.utility.Enumeration;

/**
 * The {@link BaseSchema base-schema} and {@link MetaSchema meta-schema} are a
 * fundamental dependency when declaring any {@link Schema schema}. They are
 * themselves are no exception, and are in fact circularly dependent on their
 * own definitions in tricky ways.
 * 
 * <p>
 * This presents an obvious problem when we declare them for the first time.
 * They will not be available until we finish declaring them, but we cannot
 * finish declaring them until they are available.
 * 
 * <p>
 * The root of this problem lies with {@link ValueResolution#DECLARATION_TIME
 * declaration-time value resolution}. In particular, we need to bind certain
 * values as part of the schema declarations, but the usual binding process is
 * dependent on the base-schema and meta-schema being fully defined and
 * available already.
 * 
 * <p>
 * The bootstrap data loader attempts to avoid this issue by manually providing
 * value bindings for any values in the base-schema and meta-schema which need
 * to be resolved at declaration time. In some cases this may mean providing
 * proxies until the real values become available.
 * 
 * @author Elias N Vasylenko
 */
public class BootstrapDataLoader implements DataLoader {
  private class TargetModelSkeletonObject {
    private final QualifiedName name;
    private final TypeToken<? extends Model<?>> type;

    public TargetModelSkeletonObject(QualifiedName name) {
      this.name = name;

      switch (name.getName()) {
      case "model":
        type = new TypeToken<Model<Model<?>>>() {};
        break;
      case "binding":
        type = new TypeToken<Model<BindingPoint<?>>>() {};
        break;
      case "schema":
        type = new TypeToken<Model<Schema>>() {};
        break;
      default:
        type = null;
      }
    }

    boolean isReady() {
      return isComplete();
    }

    Model<?> provideObject() {
      Model<?> model = baseSchema.models().get(name);

      if (model == null)
        model = metaSchema.models().get(name);

      if (model == null)
        throw new ModabiException(MESSAGES.noBootstrapModelFound(name));

      return model;
    }

    public boolean hasThisType() {
      return type != null;
    }

    public TypeToken<? extends Model<?>> getThisType() {
      return type;
    }
  }

  private Map<QualifiedName, TypedObject<? extends Model<?>>> targetModels = new HashMap<>();
  private BaseSchema baseSchema;
  private MetaSchema metaSchema;

  public void setComplete(BaseSchema baseSchema, MetaSchema metaSchema) {
    this.baseSchema = baseSchema;
    this.metaSchema = metaSchema;

    targetModels.values().stream().map(TypedObject::getObject).forEach(Model::name);
    targetModels = null;
  }

  public boolean isComplete() {
    return targetModels == null;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public <T> List<TypedObject<? extends T>> loadData(Model<T> node, DataSource data) {
    Namespace namespace = new Namespace(BaseSchema.class.getPackage(), LocalDate.of(2014, 1, 1));

    if (node.name().getNamespace().equals(namespace)) {
      switch (node.name().getName()) {
      case "configure":
        return Collections.emptyList();

      case "dataType":
        return (List) asList(
            typedObject(
                forClass(Primitive.class),
                Enumeration.valueOf(Primitive.class, data.get(Primitive.STRING))));

      case "targetId":
        List<QualifiedName> targetId = new ArrayList<>();
        while (!data.isComplete()) {
          targetId.add(data.get(Primitive.QUALIFIED_NAME));
        }
        return (List) asList(typedObject(new TypeToken<List<QualifiedName>>() {}, targetId));

      case "inline":
        return (List) asList(typedObject(forClass(Boolean.class), data.get(Primitive.BOOLEAN)));

      case "isExternal":
        return (List) asList(typedObject(forClass(Boolean.class), data.get(Primitive.BOOLEAN)));

      case "enumType":
        return (List) asList(typedObject(new TypeToken<Class<Enum>>() {}, Enum.class));

      case "enumerationType":
        return (List) asList(
            typedObject(new TypeToken<Class<Enumeration>>() {}, Enumeration.class));

      case "targetModel":
        QualifiedName name = data.get(Primitive.QUALIFIED_NAME);

        return (List) asList(targetModels.computeIfAbsent(name, n -> {
          BootstrapDataLoader.TargetModelSkeletonObject skeleton = new TargetModelSkeletonObject(
              name);

          return typedObject(
              (TypeToken<Model<?>>) skeleton.getThisType(),
              (Model<?>) Proxy.newProxyInstance(
                  Model.class.getClassLoader(),
                  new Class[] { Model.class },
                  new InvocationHandler() {
                    private BootstrapDataLoader.TargetModelSkeletonObject skeletonReference = skeleton;
                    private Model<?> model;

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args)
                        throws Throwable {
                      if (skeletonReference != null) {
                        if (skeletonReference.isReady()) {
                          model = skeletonReference.provideObject();
                          skeletonReference = null;
                        } else {
                          if (method.getName().equals("getThisType")
                              && skeletonReference.hasThisType()) {
                            return skeletonReference.getThisType().getType();
                          }
                          throw new IllegalStateException(
                              "Proxy for target model '" + name + "' is not ready yet");
                        }
                      }

                      return method.invoke(model, args);
                    }
                  }));
        }));
      }
    }

    throw new ModabiException(MESSAGES.noBootstrapValueFound(node.name()));
  }
}
