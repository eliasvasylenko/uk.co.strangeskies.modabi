package uk.co.strangeskies.modabi.expression.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class ExecutionContext {
  private final List<Instruction> instructions;
  private final Object capture;
  private final Object[] arguments;

  private int instructionPointer;
  private final Deque<Object> stack;

  public ExecutionContext(List<Instruction> instructions, Object capture, Object[] arguments) {
    this.instructionPointer = 0;
    this.instructions = instructions;

    this.capture = capture;
    this.arguments = arguments;

    this.stack = new ArrayDeque<>();
  }

  public void push(Object item) {
    stack.push(item);
  }

  public List<Object> pop(int count) {
    List<Object> list = new ArrayList<>(count);
    for (int i = 0; i < count; i++)
      list.add(0, stack.pop());
    return list;
  }

  public Object pop() {
    return stack.pop();
  }

  public Object peek() {
    return stack.peek();
  }

  public void next() {
    int i;
    do {
      i = instructionPointer;
      instructions.get(i).execute(this);
    } while (i == instructionPointer++);
  }

  public Object pushCapture() {
    return capture;
  }

  public Object pushArgument(int i) {
    return arguments[i];
  }
}
