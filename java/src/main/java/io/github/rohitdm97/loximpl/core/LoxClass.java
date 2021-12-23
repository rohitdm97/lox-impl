package io.github.rohitdm97.loximpl.core;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

// class is callable, it returns a new instance of the class
@RequiredArgsConstructor
public class LoxClass implements LoxCallable {

    private final String name;
    private final LoxClass superClass;
    private final Map<String, LoxFunction> methods;

    @Override
    public String toString() {
        return String.format("<class %s>", name);
    }

    @Override
    public int arity() {
        LoxFunction initializer = findMethod("init");
        if (initializer == null) return 0;
        return initializer.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        final LoxInstance instance = new LoxInstance(this);
        LoxFunction initializer = findMethod("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }

    public LoxFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        if (superClass != null) {
            return superClass.findMethod(name);
        }

        return null;
    }

}
