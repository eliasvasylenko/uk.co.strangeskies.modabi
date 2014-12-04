package uk.co.strangeskies.modabi.types;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.types.Bound.BoundVisitor;
import uk.co.strangeskies.modabi.types.Bound.PartialBoundVisitor;
import uk.co.strangeskies.modabi.types.ConstraintFormula.Kind;

import com.google.common.reflect.TypeResolver;

public class BoundSet {
	private final Set<Bound> bounds;

	public BoundSet() {
		bounds = new HashSet<>();
	}

	public Stream<Bound> stream() {
		return bounds.stream();
	}

	public BoundVisitor incorporate() {
		return new BoundIncorporator();
	}

	public void incorporate(ConstraintFormula constraintFormula) {
		constraintFormula.reduce(new BoundIncorporator(constraintFormula));
	}

	private class BoundIncorporator implements BoundVisitor {
		private final ConstraintFormula constraintFormula;

		public BoundIncorporator(ConstraintFormula constraintFormula) {
			this.constraintFormula = constraintFormula;
		}

		public BoundIncorporator() {
			this.constraintFormula = null;
		}

		public void acceptEquality(InferenceVariable a, InferenceVariable b) {
			Set<ConstraintFormula> constraintFormulae = new HashSet<>();

			bounds.forEach(o -> o.accept(new PartialBoundVisitor() {
				@Override
				public void acceptEquality(InferenceVariable a2, InferenceVariable b2) {
					acceptEquality(a2, b2);
					acceptEquality(b2, a2);
				}

				@Override
				public void acceptEquality(InferenceVariable a2, Type b2) {
					if (a.equals(a2))
						constraintFormulae.add(new ConstraintFormula(Kind.EQUALITY, b, b2));
					else {
						TypeResolver resolver = new TypeResolver().where(a, b);

						constraintFormulae.add(new ConstraintFormula(Kind.EQUALITY, a2,
								resolver.resolveType(b2)));
					}
				}

				@Override
				public void acceptSubtype(InferenceVariable a2, InferenceVariable b2) {
					acceptSubtype(a2, b2);
					acceptSubtype(b2, a2);
				}

				@Override
				public void acceptSubtype(InferenceVariable a2, Type b2) {
					if (a.equals(a2))
						constraintFormulae.add(new ConstraintFormula(Kind.SUBTYPE, b, b2));
					else {
						TypeResolver resolver = new TypeResolver().where(a, b);

						constraintFormulae.add(new ConstraintFormula(Kind.SUBTYPE, a2,
								resolver.resolveType(b2)));
					}
				}

				public void acceptSubtype(Type a2, InferenceVariable b2) {
					if (a.equals(a2))
						constraintFormulae.add(new ConstraintFormula(Kind.SUBTYPE, a2, b));
					else {
						TypeResolver resolver = new TypeResolver().where(a, b);

						constraintFormulae.add(new ConstraintFormula(Kind.SUBTYPE, resolver
								.resolveType(a2), b2));
					}
				}

				@Override
				public void acceptCaptureConversion(Map<Type, InferenceVariable> c2) {
					// TODO Auto-generated method stub
				}
			}));

			bounds.add(visitor -> visitor.acceptEquality(a, b));
			constraintFormulae.forEach(c -> incorporate(c));
		}

		public void acceptEquality(InferenceVariable a, Type b) {
			bounds.add(visitor -> visitor.acceptEquality(a, b));
		}

		public void acceptSubtype(InferenceVariable a, InferenceVariable b) {
			bounds.add(visitor -> visitor.acceptSubtype(a, b));
		}

		public void acceptSubtype(InferenceVariable a, Type b) {
			bounds.add(visitor -> visitor.acceptSubtype(a, b));
		}

		public void acceptSubtype(Type a, InferenceVariable b) {
			bounds.add(visitor -> visitor.acceptSubtype(a, b));
		}

		public void acceptFalsehood() {
			if (constraintFormula != null)
				throw new TypeInferenceException("Cannot reduce constraint ["
						+ constraintFormula + "] into bounds set [" + BoundSet.this + "].");
			else
				throw new TypeInferenceException(
						"Addition of falsehood into bounds set [" + BoundSet.this + "].");
		}

		public void acceptCaptureConversion(Map<Type, InferenceVariable> c) {
			bounds.add(visitor -> visitor.acceptCaptureConversion(c));
		}
	}
}
