package io.github.rohitdm97.loximpl.core;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
class LoxFunction implements LoxCallable {

    private final Stmt.Function declaration;
    private final Environment closure;

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlockUsingEnvironment(declaration.body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("<fn %s>", declaration.name.lexeme);
    }

}
