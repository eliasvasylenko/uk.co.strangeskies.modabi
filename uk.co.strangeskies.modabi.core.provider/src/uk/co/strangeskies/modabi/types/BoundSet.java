package uk.co.strangeskies.modabi.types;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.co.strangeskies.modabi.types.Bound.BoundVisitor;
import uk.co.strangeskies.modabi.types.Bound.PartialBoundVisitor;
import uk.co.strangeskies.modabi.types.ConstraintFormula.Kind;

import com.google.common.reflect.TypeResolver;

public class BoundSet {
	private final InferenceContext context;

	private final Set<Bound> bounds;

	public BoundSet(InferenceContext context) {
		this.context = context;
		bounds = new HashSet<>();
	}

	public InferenceContext getContext() {
		return context;
	}

	public void incorporate(ConstraintFormula constraintFormula) {
		constraintFormula.reduce(context, new BoundIncorporator(constraintFormula));
	}

	private class BoundIncorporator implements BoundVisitor<Void> {
		private final ConstraintFormula constraintFormula;

		public BoundIncorporator(ConstraintFormula constraintFormula) {
			this.constraintFormula = constraintFormula;
		}

		public Void acceptEquality(InferenceVariable a, InferenceVariable b) {
			Set<ConstraintFormula> constraintFormulae = new HashSet<>();

			bounds.forEach(o -> o.accept(new PartialBoundVisitor<Void>() {
				@Override
				public Void acceptEquality(InferenceVariable a2, InferenceVariable b2) {
					acceptEquality(a2, b2.getTypeVariable());
					acceptEquality(b2, a2.getTypeVariable());
					return null;
				}

				@Override
				public Void acceptEquality(InferenceVariable a2, Type b2) {
					if (a.equals(a2))
						constraintFormulae.add(new ConstraintFormula(Kind.EQUALITY, b
								.getTypeVariable(), b2));
					else {
						TypeResolver resolver = new TypeResolver().where(
								a.getTypeVariable(), b.getTypeVariable());

						constraintFormulae.add(new ConstraintFormula(Kind.EQUALITY, a2
								.getTypeVariable(), resolver.resolveType(b2)));
					}
					return null;
				}

				@Override
				public Void acceptSubtype(InferenceVariable a2, InferenceVariable b2) {
					acceptSubtype(a2, b2.getTypeVariable());
					acceptSubtype(b2, a2.getTypeVariable());
					return null;
				}

				@Override
				public Void acceptSubtype(InferenceVariable a2, Type b2) {
					if (a.equals(a2))
						constraintFormulae.add(new ConstraintFormula(Kind.SUBTYPE, b
								.getTypeVariable(), b2));
					else {
						TypeResolver resolver = new TypeResolver().where(
								a.getTypeVariable(), b.getTypeVariable());

						constraintFormulae.add(new ConstraintFormula(Kind.SUBTYPE, a2
								.getTypeVariable(), resolver.resolveType(b2)));
					}
					return null;
				}

				public Void acceptSubtype(Type a2, InferenceVariable b2) {
					if (a.equals(a2))
						constraintFormulae.add(new ConstraintFormula(Kind.SUBTYPE, a2, b
								.getTypeVariable()));
					else {
						TypeResolver resolver = new TypeResolver().where(
								a.getTypeVariable(), b.getTypeVariable());

						constraintFormulae.add(new ConstraintFormula(Kind.SUBTYPE, resolver
								.resolveType(a2), b2.getTypeVariable()));
					}
					return null;
				}

				@Override
				public Void acceptCaptureConversion(Map<Type, InferenceVariable> c2) {
					// TODO Auto-generated method stub
					return null;
				}
			}));

			bounds.add(v -> v.acceptEquality(a, b));
			constraintFormulae.forEach(BoundSet.this::incorporate);

			return null;
		}

		public Void acceptEquality(InferenceVariable a, Type b) {
			bounds.add(v -> v.acceptEquality(a, b));
			return null;
		}

		public Void acceptSubtype(InferenceVariable a, InferenceVariable b) {
			bounds.add(v -> v.acceptSubtype(a, b));
			return null;
		}

		public Void acceptSubtype(InferenceVariable a, Type b) {
			bounds.add(v -> v.acceptSubtype(a, b));
			return null;
		}

		public Void acceptSubtype(Type a, InferenceVariable b) {
			bounds.add(v -> v.acceptSubtype(a, b));
			return null;
		}

		public Void acceptFalsehood() {
			throw new TypeInferenceException("Cannot reduce constraint ["
					+ constraintFormula + "] into bounds set [" + BoundSet.this + "].");
		}

		public Void acceptCaptureConversion(Map<Type, InferenceVariable> c) {
			bounds.add(v -> v.acceptCaptureConversion(c));
			return null;
		}
	}
}
