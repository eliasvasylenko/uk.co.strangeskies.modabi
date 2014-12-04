package uk.co.strangeskies.modabi.types;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.strangeskies.modabi.types.ConstraintFormula.Kind;

import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeResolver;
import com.google.common.reflect.TypeToken;

public class InferenceProcessor {
	private final Invokable<?, ?> invokable;
	private final Type result;
	private final List<Type> arguments;

	private final BoundSet bounds;

	private Boolean strictParameterApplicability;
	private Boolean looseParameterApplicability;
	private Boolean variableArityParameterApplicability;

	public InferenceProcessor(Invokable<?, ?> invokable, Type result,
			Type... arguments) {
		this(invokable, result, Arrays.asList(arguments));
	}

	@SuppressWarnings("unchecked")
	public InferenceProcessor(Invokable<?, ?> invokable, Type target,
			List<Type> arguments) {
		this.invokable = invokable;
		this.result = target;
		this.arguments = arguments;

		bounds = new BoundSet();

		Map<TypeVariable<?>, InferenceVariable> inferenceVariables = new HashMap<>();
		Map<Parameter, Type> parameterTypes = new HashMap<>();

		for (TypeVariable<?> variable : invokable.getTypeParameters())
			inferenceVariables.put(variable, new InferenceVariable(
					(TypeVariable<? extends Executable>) variable));

		for (TypeVariable<?> variable : invokable.getTypeParameters())
			resolver = resolver.where(variable.getTypeVariable(), Unique.class);

		for (TypeVariable<?> typeParameter : invokable.getTypeParameters())
			inferenceVariables.put(typeParameter,
					new InferenceVariable(typeParameter));

		for (TypeVariable<?> typeParameter : invokable.getTypeParameters()) {
			boolean anyProper = false;
			for (Type bound : typeParameter.getBounds()) {
				anyProper = anyProper || isProper(bound);
				bounds.incorporate().acceptSubtype(
						inferenceVariables.get(typeParameter), bound);
			}
			if (!anyProper)
				bounds.incorporate().acceptSubtype(
						inferenceVariables.get(typeParameter), Object.class);
		}
	}

	public boolean verifyStrictParameterApplicability() {
		if (strictParameterApplicability == null) {
			strictParameterApplicability = verifyLooseParameterApplicability();
			// TODO && make sure no boxing/unboxing occurs!
		}

		return strictParameterApplicability;
	}

	public boolean verifyLooseParameterApplicability() {
		if (looseParameterApplicability == null) {
			looseParameterApplicability = !invokable.isVarArgs()
					&& verifyVariableArityParameterApplicability();
		}

		return looseParameterApplicability;
	}

	public boolean verifyVariableArityParameterApplicability() {
		List<Parameter> parameters = invokable.getParameters();
		if (variableArityParameterApplicability == null) {
			if (invokable.isVarArgs()) {
				variableArityParameterApplicability = parameters.size() - 1 <= arguments
						.size();
			} else {
				variableArityParameterApplicability = parameters.size() == arguments
						.size();
			}

			if (variableArityParameterApplicability) {
				int parameterIndex = 0;
				for (Type argument : arguments) {
					bounds.incorporate(new ConstraintFormula(Kind.LOOSE_COMPATIBILILTY,
							argument, invokable.getParameters().get(parameterIndex).getType()
									.getType()));
					if (parameterIndex < parameters.size() - 1)
						parameterIndex++;
				}

				variableArityParameterApplicability = new Resolution(bounds).verify();
			}
		}

		return variableArityParameterApplicability;
	}

	@Override
	public boolean isProper(Type type) {
		if (type == null)
			return false;

		class Unique {}

		TypeResolver resolver = new TypeResolver();

		for (InferenceVariable variable : getInferenceVariables())
			resolver = resolver.where(variable.getTypeVariable(), Unique.class);

		TypeResolver resolver = new TypeResolver();
		return TypeToken.of(resolver.resolveType(type)).equals(TypeToken.of(type));
	}
}
