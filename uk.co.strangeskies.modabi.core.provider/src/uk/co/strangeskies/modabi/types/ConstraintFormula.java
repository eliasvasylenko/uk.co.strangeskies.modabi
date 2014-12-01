package uk.co.strangeskies.modabi.types;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import javax.lang.model.type.IntersectionType;

import jdk.internal.dynalink.linker.TypeBasedGuardingDynamicLinker;

import com.google.common.reflect.TypeToken;

public class ConstraintFormula {
	public enum Kind {
		LOOSE_COMPATIBILILTY, SUBTYPE, CONTAINMENT, EQUALITY
	}

	private final Kind kind;

	private final TypeToken<?> from, to;

	public ConstraintFormula(Kind kind, Type from, Type to) {
		this(kind, TypeToken.of(from), TypeToken.of(to));
	}

	public ConstraintFormula(Kind kind, TypeToken<?> from, TypeToken<?> to) {
		this.kind = kind;
		this.from = from;
		this.to = to;
	}

	public BoundSet reduce() {
		switch (kind) {
		case LOOSE_COMPATIBILILTY:
			return reduceLooseTypeCompatibility();
		case SUBTYPE:
			return reduceReferenceSubtype();
		case CONTAINMENT:
			return reduceContainmentConstraint();
		case EQUALITY:
			break;
		}
	}

	private BoundSet reduceContainmentConstraint() {
		if (!(to.getType() instanceof WildcardType))
			if (!(from.getType() instanceof WildcardType))
				return new ConstraintFormula(Kind.EQUALITY, from, to).reduce();
			else
				return new BoundSet(false);

		WildcardType to = (WildcardType) this.to.getType();

		if (to.getUpperBounds().length == 0)
			if (to.getLowerBounds().length == 0)
				return new BoundSet(true);
			else
				Type.
	}

	private BoundSet reduceReferenceSubtype() {
		if (isProper(from) && isProper(to))
			return new BoundSet(to.isAssignableFrom(from));
		else if (isNullType(from))
			return new BoundSet(true);
		else if (isNullType(to))
			return new BoundSet(false);
		else if (isInferenceVariable(from))
			return new BoundSet(Bound.subtype(getInferenceVariable(from),
					to.getType()));
		else if (isInferenceVariable(to))
			return new BoundSet(Bound.subtype(from.getType(),
					getInferenceVariable(to)));

		;
	}

	private boolean isInferenceVariable(TypeToken<?> type) {
		// TODO Auto-generated method stub
		return false;
	}

	private InferenceVariable getInferenceVariable(TypeToken<?> type) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean isNullType(TypeToken<?> type) {
		// TODO Auto-generated method stub
		return false;
	}

	private BoundSet reduceLooseTypeCompatibility() {
		if (isProper(from) && isProper(to))
			return new BoundSet(isLooselyAssignable(from, to));
		else if (from.isPrimitive())
			return new ConstraintFormula(kind, from.wrap(), to).reduce();
		else if (to.isPrimitive())
			return new ConstraintFormula(Kind.EQUALITY, from, to.wrap()).reduce();
		else
			return new ConstraintFormula(Kind.SUBTYPE, from, to).reduce();
	}

	private boolean isStrictlyAssignable(TypeToken<?> from, TypeToken<?> to) {
		if (from.isPrimitive())
			if (to.isPrimitive())
				return to.wrap().isAssignableFrom(to.wrap());
			else
				return false;
		else if (to.isPrimitive())
			return false;
		else
			return to.isAssignableFrom(from.wrap());
	}

	private boolean isLooselyAssignable(TypeToken<?> from, TypeToken<?> to) {
		if (from.isPrimitive() && !to.isPrimitive())
			from = from.wrap();
		else if (!from.isPrimitive() && to.isPrimitive())
			from = from.unwrap();

		return isStrictlyAssignable(from, to);
	}

	private boolean isProper(TypeToken<?> type) {
		// TODO Auto-generated method stub
		return false;
	}
}
