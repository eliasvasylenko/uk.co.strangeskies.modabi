package uk.co.strangeskies.modabi.types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.google.common.reflect.TypeToken;

public class ConstraintFormula {
	public enum Kind {
		LOOSE_COMPATIBILILTY, SUBTYPE, CONTAINMENT, EQUALITY
	}

	private final BoundSet boundSet;
	private final Kind kind;
	private final TypeToken<?> from, to;

	public ConstraintFormula(BoundSet boundSet, Kind kind, TypeToken<?> from,
			TypeToken<?> to) {
		this.boundSet = boundSet;
		this.kind = kind;
		this.from = from;
		this.to = to;
	}

	public InferenceContext getContext() {
		return boundSet.getContext();
	}

	public ConstraintFormula reduceTo(Kind kind, Type from, Type to) {
		return reduceTo(kind, TypeToken.of(from), TypeToken.of(to));
	}

	public ConstraintFormula reduceTo(Kind kind, TypeToken<?> from,
			TypeToken<?> to) {
		return new ConstraintFormula(boundSet, kind, from, to);
	}

	public void reduce() {
		switch (kind) {
		case LOOSE_COMPATIBILILTY:
			reduceLooseCompatibilityConstraint();
			break;
		case SUBTYPE:
			reduceSubtypeConstraint();
			break;
		case CONTAINMENT:
			reduceContainmentConstraint();
			break;
		case EQUALITY:
			reduceEqualityConstraint();
			break;
		}
	}

	/*
	 * A constraint formula of the form ‹S → T› is reduced as follows:
	 */
	private void reduceLooseCompatibilityConstraint() {
		if (getContext().isProper(from) && getContext().isProper(to)) {
			/*
			 * If S and T are proper types, the constraint reduces to true if S is
			 * compatible in a loose invocation context with T (§5.3), and false
			 * otherwise.
			 */
			if (!isLooselyAssignable(from, to))
				boundSet.add(Bound.falsehood());
		} else if (from != null && from.isPrimitive())
			/*
			 * Otherwise, if S is a primitive type, let S' be the result of applying
			 * boxing conversion (§5.1.7) to S. Then the constraint reduces to ‹S' →
			 * T›.
			 */
			reduceTo(kind, from.wrap(), to).reduce();
		else if (to != null && to.isPrimitive())
			/*
			 * Otherwise, if T is a primitive type, let T' be the result of applying
			 * boxing conversion (§5.1.7) to T. Then the constraint reduces to ‹S =
			 * T'›.
			 */
			reduceTo(Kind.EQUALITY, from, to.wrap()).reduce();
		else if (isUnsafeCastCompatible(from, to))
			/*
			 * Otherwise, if T is a parameterized type of the form G<T1, ..., Tn>, and
			 * there exists no type of the form G<...> that is a supertype of S, but
			 * the raw type G is a supertype of S, then the constraint reduces to
			 * true.
			 * 
			 * Otherwise, if T is an array type of the form G<T1, ..., Tn>[]k, and
			 * there exists no type of the form G<...>[]k that is a supertype of S,
			 * but the raw type G[]k is a supertype of S, then the constraint reduces
			 * to true. (The notation []k indicates an array type of k dimensions.)
			 */
			return;
		else
			reduceTo(Kind.SUBTYPE, from, to).reduce();
	}

	/*
	 * A constraint formula of the form ‹S <: T› is reduced as follows:
	 */
	private void reduceSubtypeConstraint() {
		if (getContext().isProper(from) && getContext().isProper(to)) {
			/*
			 * If S and T are proper types, the constraint reduces to true if S is a
			 * subtype of T (§4.10), and false otherwise.
			 */
			if (!to.isAssignableFrom(from))
				boundSet.add(Bound.falsehood());
			else
				return;
		} else if (from == null)
			/*
			 * Otherwise, if S is the null type, the constraint reduces to true.
			 */
			return;
		else if (to == null)
			/*
			 * Otherwise, if T is the null type, the constraint reduces to false.
			 */
			boundSet.add(Bound.falsehood());
		else if (getContext().isInferenceVariable(from.getType()))
			/*
			 * Otherwise, if S is an inference variable, α, the constraint reduces to
			 * the bound α <: T.
			 */
			boundSet.add(Bound.subtype(
					getContext().getInferenceVariable(from.getType()), to.getType()));
		else if (getContext().isInferenceVariable(to.getType()))
			/*
			 * Otherwise, if T is an inference variable, α, the constraint reduces to
			 * the bound S <: α.
			 */
			boundSet.add(Bound.subtype(from.getType(), getContext()
					.getInferenceVariable(to.getType())));
		else {
			/*
			 * Otherwise, the constraint is reduced according to the form of T:
			 */
			if (to.getType() instanceof ParameterizedType) {
				/*
				 * If T is a parameterized class or interface type, or an inner class
				 * type of a parameterized class or interface type (directly or
				 * indirectly), let A1, ..., An be the type arguments of T. Among the
				 * supertypes of S, a corresponding class or interface type is
				 * identified, with type arguments B1, ..., Bn. If no such type exists,
				 * the constraint reduces to false. Otherwise, the constraint reduces to
				 * the following new constraints: for all i (1 ≤ i ≤ n), ‹Bi <= Ai›.
				 * 
				 * TODO must we explicitly disallow if S only extends the raw type of T?
				 */
				Class<?> rawType = to.getRawType();
				do {
					for (TypeVariable<?> parameter : rawType.getTypeParameters())
						reduceTo(Kind.CONTAINMENT, from.resolveType(parameter),
								to.resolveType(parameter));
				} while ((rawType = rawType.getEnclosingClass()) != null);
			} else if (to.isArray()) {
				/*
				 * If T is an array type, T'[], then among the supertypes of S that are
				 * array types, a most specific type is identified, S'[] (this may be S
				 * itself). If no such array type exists, the constraint reduces to
				 * false. Otherwise:
				 * 
				 * - If neither S' nor T' is a primitive type, the constraint reduces to
				 * ‹S' <: T'›.
				 * 
				 * - Otherwise, the constraint reduces to true if S' and T' are the same
				 * primitive type, and false otherwise.
				 * 
				 * TODO must we explicitly disallow if S only extends the raw type of T?
				 */
				throw new NotImplementedException(); // TODO
			} else if (to.getType() instanceof TypeVariable) {
				/*
				 * If T is a type variable, there are three cases:
				 * 
				 * - If S is an intersection type of which T is an element, the
				 * constraint reduces to true.
				 * 
				 * - Otherwise, if T has a lower bound, B, the constraint reduces to ‹S
				 * <: B›.
				 * 
				 * - Otherwise, the constraint reduces to false.
				 */
				throw new NotImplementedException(); // TODO
			} else {
				/*
				 * If T is any other class or interface type, then the constraint
				 * reduces to true if T is among the supertypes of S, and false
				 * otherwise.
				 */
				throw new NotImplementedException(); // TODO
			}
			/*
			 * If T is an intersection type, I1 & ... & In, the constraint reduces to
			 * the following new constraints: for all i (1 ≤ i ≤ n), ‹S <: Ii›.
			 */
		}
	}

	private void reduceContainmentConstraint() {
		if (!(to.getType() instanceof WildcardType)) {
			if (!(from.getType() instanceof WildcardType)) {
				/*
				 * T is a type and S is a type:
				 * 
				 * <S = T>
				 */
				reduceTo(Kind.EQUALITY, from, to).reduce();
			} else {
				/*
				 * T is a type and S is a wildcard:
				 * 
				 * false
				 */
				boundSet.add(Bound.falsehood());
			}
		} else {
			WildcardType to = (WildcardType) this.to.getType();

			if (to.getLowerBounds().length == 0) {
				if (to.getUpperBounds().length == 0) {
					/*
					 * T is ?:
					 * 
					 * true
					 */
					return;
				} else {
					if (!(from.getType() instanceof WildcardType)) {
						/*
						 * T is ? extends T' and S is a type:
						 * 
						 * <S <: T'>
						 */
						throw new NotImplementedException(); // TODO
					} else {
						WildcardType from = (WildcardType) this.from.getType();

						if (from.getLowerBounds().length == 0) {
							if (from.getUpperBounds().length == 0) {
								/*
								 * T is ? extends T' and S is ?:
								 * 
								 * <Object <: T'>
								 * 
								 * This is a tricky bit. With S = Object we obviously can skip
								 * most of the conditions depending on S, and the rules for
								 * whether or not T' is proper are equivalent.
								 * 
								 * T' is intersection type, I1 & ... & In. For all i (1 ≤ i ≤
								 * n):
								 * 
								 * <S <: Ii>
								 */
								Arrays.stream(to.getUpperBounds()).forEach(
										t -> reduceTo(Kind.SUBTYPE, Object.class, t).reduce());
							} else {
								/*
								 * T is ? extends T' and S is ? extends S':
								 * 
								 * <S' <: T'>
								 * 
								 * Here we know that S and T are not the null type or inference
								 * variables, as they are both intersection types.
								 */
								throw new NotImplementedException(); // TODO
							}
						} else {
							/*
							 * T is ? extends T' and S is ? super S':
							 * 
							 * <Object = T'>
							 */
							Arrays.stream(to.getUpperBounds()).forEach(
									t -> reduceTo(Kind.EQUALITY, Object.class, t).reduce());
						}
					}
				}
			} else if (!(from.getType() instanceof WildcardType)) {
				/*
				 * T is ? super T' and S is a type:
				 * 
				 * <T' <: S>
				 */
				throw new NotImplementedException(); // TODO
			} else {
				WildcardType from = (WildcardType) this.from.getType();

				if (from.getLowerBounds().length > 0) {
					/*
					 * T is ? super T' and S is ? super S':
					 * 
					 * <T' <: S'>
					 */
					throw new NotImplementedException(); // TODO
				} else {
					/*
					 * T is ? super T' and S is ? or ? extends S':
					 * 
					 * false
					 */
					boundSet.add(Bound.falsehood());
				}
			}
		}
	}

	private void reduceEqualityConstraint() {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}

	public static boolean isUnsafeCastCompatible(TypeToken<?> from,
			TypeToken<?> to) {
		if (to.getRawType().getTypeParameters().length < 0
				&& to.getRawType().isAssignableFrom(from.getRawType())) {
			@SuppressWarnings("unchecked")
			Type fromSuperTypeArgument = ((ParameterizedType) from.getSupertype(
					(Class<Object>) to.getRawType()).getType()).getActualTypeArguments()[0];

			return fromSuperTypeArgument instanceof TypeVariable
					&& ((TypeVariable<?>) fromSuperTypeArgument).getGenericDeclaration() instanceof Class;
		} else
			return to.isArray()
					&& from.isArray()
					&& isUnsafeCastCompatible(from.getComponentType(),
							to.getComponentType());
	}

	public static boolean isStrictlyAssignable(TypeToken<?> from, TypeToken<?> to) {
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

	public static boolean isLooselyAssignable(TypeToken<?> from, TypeToken<?> to) {
		if (from.isPrimitive() && !to.isPrimitive())
			from = from.wrap();
		else if (!from.isPrimitive() && to.isPrimitive())
			from = from.unwrap();

		return isStrictlyAssignable(from, to);
	}
}
