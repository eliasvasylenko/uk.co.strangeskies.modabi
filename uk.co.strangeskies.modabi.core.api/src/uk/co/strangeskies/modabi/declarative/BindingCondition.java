package uk.co.strangeskies.modabi.declarative;

import uk.co.strangeskies.modabi.schema.bindingconditions.RequiredCondition;

public @interface BindingCondition {
	@SuppressWarnings("rawtypes")
	Class<? extends uk.co.strangeskies.modabi.schema.BindingCondition> value() default RequiredCondition.class;
}
