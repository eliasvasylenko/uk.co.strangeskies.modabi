package uk.co.strangeskies.modabi.schema;

public enum Permission {
  /**
   * A private model cannot be used as the base of a model or bound to outside the
   * schema to which it belongs.
   */
  PRIVATE,

  /**
   * A sealed model cannot be used as the base of a model outside the schema to
   * which it belongs, but it can be bound to.
   */
  SEALED,

  /**
   * An open model has no restrictions on usage outside the schema to which it
   * belongs. This is the default visibility of a model.
   */
  OPEN;
}
