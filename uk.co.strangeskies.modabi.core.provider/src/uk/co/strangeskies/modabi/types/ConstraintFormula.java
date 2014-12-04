package uk.co.strangeskies.modabi.types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashSet;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uk.co.strangeskies.modabi.types.Bound.BoundVisitor;

import com.google.common.reflect.TypeToken;

public class ConstraintFormula {
	public enum Kind {
		LOOSE_COMPATIBILILTY, SUBTYPE, CONTAINMENT, EQUALITY
	}

	private final Kind kind;
	private final Type from, to;

	public ConstraintFormula(Kind kind, Type from, Type to) {
		this.kind = kind;
		this.from = from;
		this.to = to;
	}

	@Override
	public String toString() {
		return "Constraint '" + kind + "' between '" + from + "' and '" + to + "'.";
	}

	public void reduce(BoundVisitor boundConsumer) {
		switch (kind) {
		case LOOSE_COMPATIBILILTY:
			reduceLooseCompatibilityConstraint(boundConsumer);
			break;
		case SUBTYPE:
			reduceSubtypeConstraint(boundConsumer);
			break;
		case CONTAINMENT:
			reduceContainmentConstraint(boundConsumer);
			break;
		case EQUALITY:
			reduceEqualityConstraint(boundConsumer);
			break;
		default:
			throw new AssertionError();
		}
	}

	/*
	 * A constraint formula of the form ‹S → T› is reduced as follows:
	 */
	private void reduceLooseCompatibilityConstraint(BoundVisitor boundConsumer) {
		TypeToken<?> toToken = to == null ? null : TypeToken.of(to);
		TypeToken<?> fromToken = from == null ? null : TypeToken.of(from);

		if (context.isProper(from) && context.isProper(to)) {
			/*
			 * If S and T are proper types, the constraint reduces to true if S is
			 * compatible in a loose invocation context with T (§5.3), and false
			 * otherwise.
			 */
			if (!isLooselyAssignable(from, to))
				boundConsumer.acceptFalsehood();
		} else if (from != null && fromToken.isPrimitive())
			/*
			 * Otherwise, if S is a primitive type, let S' be the result of applying
			 * boxing conversion (§5.1.7) to S. Then the constraint reduces to ‹S' →
			 * T›.
			 */
			new ConstraintFormula(Kind.LOOSE_COMPATIBILILTY, fromToken.wrap()
					.getType(), to).reduce(boundConsumer);
		else if (to != null && toToken.isPrimitive())
			/*
			 * Otherwise, if T is a primitive type, let T' be the result of applying
			 * boxing conversion (§5.1.7) to T. Then the constraint reduces to ‹S =
			 * T'›.
			 */
			new ConstraintFormula(Kind.EQUALITY, from, toToken.wrap().getType())
					.reduce(boundConsumer);
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
			/*
			 * Otherwise, the constraint reduces to ‹S <: T›.
			 */
			new ConstraintFormula(Kind.SUBTYPE, from, to).reduce(boundConsumer);
	}

	/*
	 * A constraint formula of the form ‹S <: T› is reduced as follows:
	 */
	private void reduceSubtypeConstraint(BoundVisitor boundConsumer) {
		TypeToken<?> toToken = to == null ? null : TypeToken.of(to);
		TypeToken<?> fromToken = from == null ? null : TypeToken.of(from);

		if (context.isProper(from) && context.isProper(to)) {
			/*
			 * If S and T are proper types, the constraint reduces to true if S is a
			 * subtype of T (§4.10), and false otherwise.
			 */
			if (!toToken.isAssignableFrom(from))
				boundConsumer.acceptFalsehood();
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
			boundConsumer.acceptFalsehood();
		else if (from instanceof InferenceVariable)
			/*
			 * Otherwise, if S is an inference variable, α, the constraint reduces to
			 * the bound α <: T.
			 */
			if (to instanceof InferenceVariable)
				boundConsumer.acceptSubtype((InferenceVariable) from,
						(InferenceVariable) to);
			else
				boundConsumer.acceptSubtype((InferenceVariable) from, to);
		else if (to instanceof InferenceVariable)
			/*
			 * Otherwise, if T is an inference variable, α, the constraint reduces to
			 * the bound S <: α.
			 */
			boundConsumer.acceptSubtype(from, (InferenceVariable) to);
		else {
			/*
			 * Otherwise, the constraint is reduced according to the form of T:
			 */
			if (to instanceof ParameterizedType) {
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
				Class<?> rawType = toToken.getRawType();
				do {
					for (TypeVariable<?> parameter : rawType.getTypeParameters())
						new ConstraintFormula(Kind.CONTAINMENT, fromToken.resolveType(
								parameter).getType(), toToken.resolveType(parameter).getType())
								.reduce(boundConsumer);
				} while ((rawType = rawType.getEnclosingClass()) != null);
			} else if (toToken.isArray()) {
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
			} else if (to instanceof TypeVariable) {
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

	/*
	 * A constraint formula of the form ‹S <= T›, where S and T are type arguments
	 * (§4.5.1), is reduced as follows:
	 */
	private void reduceContainmentConstraint(BoundVisitor boundConsumer) {
		if (!(to instanceof WildcardType)) {
			/*
			 * If T is a type:
			 */
			if (!(from instanceof WildcardType)) {
				/*
				 * If S is a type, the constraint reduces to ‹S = T›.
				 */
				new ConstraintFormula(Kind.EQUALITY, from, to).reduce(boundConsumer);
			} else {
				/*
				 * If S is a wildcard, the constraint reduces to false.
				 */
				boundConsumer.acceptFalsehood();
			}
		} else {
			WildcardType to = (WildcardType) this.to;

			if (to.getLowerBounds().length == 0) {
				if (to.getUpperBounds().length == 0) {
					/*
					 * If T is a wildcard of the form ?, the constraint reduces to true.
					 */
					return;
				} else {
					/*
					 * If T is a wildcard of the form ? extends T':
					 */
					if (!(from instanceof WildcardType)) {
						/*
						 * If S is a type, the constraint reduces to ‹S <: T'›.
						 */
						throw new NotImplementedException(); // TODO
					} else {
						WildcardType from = (WildcardType) this.from;

						if (from.getLowerBounds().length == 0) {
							if (from.getUpperBounds().length == 0) {
								/*
								 * If S is a wildcard of the form ?, the constraint reduces to
								 * ‹Object <: T'›.
								 *
								 * This is a tricky bit. With S = Object we obviously can skip
								 * most of the conditions depending on S, and the rules for
								 * whether or not T' is proper are equivalent.
								 *
								 * T' is intersection type, I1 & ... & In. For all i (1 â‰¤ i
								 * â‰¤ n):
								 *
								 * <S <: Ii>
								 */
								Arrays.stream(to.getUpperBounds()).forEach(
										t -> new ConstraintFormula(Kind.SUBTYPE, Object.class, t)
												.reduce(boundConsumer));
							} else {
								/*
								 * If S is a wildcard of the form ? extends S', the constraint
								 * reduces to ‹S' <: T'›.
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
							 * If S is a wildcard of the form ? super S', the constraint
							 * reduces to ‹Object = T'›.
							 *
							 * <Object = T'>
							 */
							Arrays.stream(to.getUpperBounds()).forEach(
									t -> new ConstraintFormula(Kind.EQUALITY, Object.class, t)
											.reduce(boundConsumer));
						}
					}
				}
			} else {
				/*
				 * If T is a wildcard of the form ? super T':
				 */
				if (!(from instanceof WildcardType)) {
					/*
					 * If S is a type, the constraint reduces to ‹T' <: S›.
					 */
					throw new NotImplementedException(); // TODO
				} else {
					WildcardType from = (WildcardType) this.from;

					if (from.getLowerBounds().length > 0) {
						/*
						 * If S is a wildcard of the form ? super S', the constraint reduces
						 * to ‹T' <: S'›.
						 */
						throw new NotImplementedException(); // TODO
					} else {
						/*
						 * Otherwise, the constraint reduces to false.
						 */
						boundConsumer.acceptFalsehood();
					}
				}
			}
		}
	}

	private void reduceEqualityConstraint(BoundVisitor boundConsumer) {
		TypeToken<?> toToken = to == null ? null : TypeToken.of(to);
		TypeToken<?> fromToken = from == null ? null : TypeToken.of(from);

		if (from instanceof WildcardType && to instanceof WildcardType) {
			/*
			 * A constraint formula of the form ‹S = T›, where S and T are type
			 * arguments (§4.5.1), is reduced as follows:
			 */
			WildcardType from = (WildcardType) this.from;
			WildcardType to = (WildcardType) this.to;

			if (from.getLowerBounds().length == 0) {
				if (from.getUpperBounds().length == 0) {
					if (to.getLowerBounds().length == 0) {
						if (to.getUpperBounds().length == 0) {
							/*
							 * If S has the form ? and T has the form ?, the constraint
							 * reduces to true.
							 */
							return;
						} else {
							/*
							 * If S has the form ? and T has the form ? extends T', the
							 * constraint reduces to ‹Object = T'›.
							 */
							for (Type t : to.getUpperBounds())
								new ConstraintFormula(Kind.EQUALITY, Object.class, t)
										.reduce(boundConsumer);
						}
					}
				} else if (to.getLowerBounds().length == 0) {
					if (to.getUpperBounds().length == 0) {
						/*
						 * If S has the form ? extends S' and T has the form ?, the
						 * constraint reduces to ‹S' = Object›.
						 */
						for (Type s : from.getUpperBounds())
							new ConstraintFormula(Kind.EQUALITY, s, Object.class)
									.reduce(boundConsumer);
					} else {
						/*
						 * If S has the form ? extends S' and T has the form ? extends T',
						 * the constraint reduces to ‹S' = T'›.
						 */
						if (!new HashSet<>(Arrays.asList(from.getUpperBounds()))
								.equals(new HashSet<>(Arrays.asList(to.getUpperBounds()))))
							boundConsumer.acceptFalsehood();
					}
				}
			} else if (to.getLowerBounds().length > 0) {
				/*
				 * If S has the form ? super S' and T has the form ? super T', the
				 * constraint reduces to ‹S' = T'›.
				 */
				if (!new HashSet<>(Arrays.asList(from.getLowerBounds()))
						.equals(new HashSet<>(Arrays.asList(to.getLowerBounds()))))
					boundConsumer.acceptFalsehood();
			} else {
				/*
				 * Otherwise, the constraint reduces to false.
				 */
				boundConsumer.acceptFalsehood();
			}
		} else {
			/*
			 * A constraint formula of the form ‹S = T›, where S and T are types, is
			 * reduced as follows:
			 */
			if (context.isProper(from) && context.isProper(to)) {
				/*
				 * If S and T are proper types, the constraint reduces to true if S is
				 * the same as T (§4.3.4), and false otherwise.
				 */
				if (!from.equals(to))
					boundConsumer.acceptFalsehood();
			} else if (from instanceof InferenceVariable) {
				/*
				 * Otherwise, if S is an inference variable, α, the constraint reduces
				 * to the bound α = T.
				 */
				if (to instanceof InferenceVariable)
					boundConsumer.acceptEquality((InferenceVariable) from,
							(InferenceVariable) to);
				else
					boundConsumer.acceptEquality((InferenceVariable) from, to);
			} else if (to instanceof InferenceVariable) {
				/*
				 * Otherwise, if T is an inference variable, α, the constraint reduces
				 * to the bound S = α.
				 */
				boundConsumer.acceptEquality((InferenceVariable) to, from);
			} else if (fromToken.isArray() && toToken.isArray()) {
				/*
				 * Otherwise, if S and T are array types, S'[] and T'[], the constraint
				 * reduces to ‹S' = T'›.
				 */
				new ConstraintFormula(Kind.EQUALITY, fromToken.getComponentType()
						.getType(), toToken.getComponentType().getType())
						.reduce(boundConsumer);
			} else if (fromToken.getRawType().equals(toToken.getRawType())) {
				/*
				 * Otherwise, if S and T are class or interface types with the same
				 * erasure, where S has type arguments B1, ..., Bn and T has type
				 * arguments A1, ..., An, the constraint reduces to the following new
				 * constraints: for all i (1 ≤ i ≤ n), ‹Bi = Ai›.
				 */
				for (TypeVariable<?> type : fromToken.getRawType().getTypeParameters())
					new ConstraintFormula(Kind.EQUALITY, fromToken.resolveType(type)
							.getType(), toToken.resolveType(type).getType())
							.reduce(boundConsumer);
			}
		}
	}

	public static boolean isUnsafeCastCompatible(Type from, Type to) {
		TypeToken<?> toToken = to == null ? null : TypeToken.of(to);
		TypeToken<?> fromToken = from == null ? null : TypeToken.of(from);

		if (toToken.getRawType().getTypeParameters().length < 0
				&& toToken.getRawType().isAssignableFrom(fromToken.getRawType())) {
			@SuppressWarnings("unchecked")
			Type fromSuperTypeArgument = ((ParameterizedType) fromToken.getSupertype(
					(Class<Object>) toToken.getRawType()).getType())
					.getActualTypeArguments()[0];

			return fromSuperTypeArgument instanceof TypeVariable
					&& ((TypeVariable<?>) fromSuperTypeArgument).getGenericDeclaration() instanceof Class;
		} else
			return toToken.isArray()
					&& fromToken.isArray()
					&& isUnsafeCastCompatible(fromToken.getComponentType().getType(),
							toToken.getComponentType().getType());
	}

	public static boolean isStrictlyAssignable(Type from, Type to) {
		TypeToken<?> toToken = to == null ? null : TypeToken.of(to);
		TypeToken<?> fromToken = from == null ? null : TypeToken.of(from);

		if (fromToken.isPrimitive())
			if (toToken.isPrimitive())
				return toToken.wrap().isAssignableFrom(toToken.wrap());
			else
				return false;
		else if (toToken.isPrimitive())
			return false;
		else
			return toToken.isAssignableFrom(fromToken.wrap());
	}

	public static boolean isLooselyAssignable(Type from, Type to) {
		TypeToken<?> toToken = to == null ? null : TypeToken.of(to);
		TypeToken<?> fromToken = from == null ? null : TypeToken.of(from);

		if (fromToken.isPrimitive() && !toToken.isPrimitive())
			fromToken = fromToken.wrap();
		else if (!fromToken.isPrimitive() && toToken.isPrimitive())
			fromToken = fromToken.unwrap();

		return isStrictlyAssignable(from, to);
	}
}
