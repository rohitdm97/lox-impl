package io.github.rohitdm97.loximpl.core;

import java.util.List;

public interface LoxCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
