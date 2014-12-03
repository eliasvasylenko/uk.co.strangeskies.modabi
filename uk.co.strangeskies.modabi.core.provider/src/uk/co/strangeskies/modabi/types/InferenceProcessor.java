package uk.co.strangeskies.modabi.types;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.strangeskies.modabi.types.ConstraintFormula.Kind;

import com.google.common.reflect.Invokable;
import com.google.common.reflect.Parameter;
import com.google.common.reflect.TypeResolver;
import com.google.common.reflect.TypeToken;

public class InferenceProcessor implements InferenceContext {
	private final Invokable<?, ?> invokable;
	private final Type target;
	private final List<Type> arguments;

	private final Map<TypeVariable<?>, InferenceVariable> inferenceVariables;
	private final BoundSet bounds;

	private Boolean strictParameterApplicability;
	private Boolean looseParameterApplicability;
	private Boolean variableArityParameterApplicability;

	public InferenceProcessor(Invokable<?, ?> invokable, Type target,
			Type... arguments) {
		this(invokable, target, Arrays.asList(arguments));
	}

	public InferenceProcessor(Invokable<?, ?> invokable, Type target,
			List<Type> arguments) {
		this.invokable = invokable;
		this.target = target;
		this.arguments = arguments;

		inferenceVariables = new HashMap<>();
		bounds = new BoundSet(this);

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
	public boolean isInferenceVariable(Type type) {
		return inferenceVariables.keySet().contains(type);
	}

	@Override
	public InferenceVariable getInferenceVariable(Type type) {
		return inferenceVariables.get((TypeVariable<?>) type);
	}

	@Override
	public Collection<InferenceVariable> getInferenceVariables() {
		return inferenceVariables.values();
	}

	@Override
	public boolean isProper(Type type) {
		if (type == null)
			return false;

		class Unique {}

		TypeResolver resolver = new TypeResolver();

		for (InferenceVariable variable : getInferenceVariables())
			resolver = resolver.where(variable.getTypeVariable(), Unique.class);

		return TypeToken.of(resolver.resolveType(type)).equals(TypeToken.of(type));
	}
}
