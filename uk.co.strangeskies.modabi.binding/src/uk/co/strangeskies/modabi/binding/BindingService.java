package uk.co.strangeskies.modabi.binding;

import uk.co.strangeskies.reflection.token.TypedObject;;

public interface BindingService {
  InputBinder<?> bindInput();

  <T> OutputBinder<? super T> bindOutput(T data);

  default <T extends TypedObject<T>> OutputBinder<? super T> bindOutput(T data) {
    return bindOutput(data).from(data.getThisTypeToken());
  }
}
