package io.github.rohitdm97.loximpl.core;

import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    // not nullable
    private final Environment parent;
    private final Map<String, Object> values = new HashMap<>();
    // sentinel to store un-initialized variables
    private static final Object UNINITIALIZED = new Object();

    public static Environment DEFAULT = new Environment(new NoOp());

    // only for noop
    private Environment() {
        this.parent = this;
    }

    public Environment(@NonNull Environment parent) {
        this.parent = parent;
    }

    boolean isDefined(String name) {
        return values.containsKey(name) || parent.isDefined(name);
    }

    boolean isInitialized(String name) {
        return values.get(name) != UNINITIALIZED || parent.isInitialized(name);
    }

    Object get(String name) {
        if (values.containsKey(name)) {
            return values.get(name);
        }
        return parent.get(name);
    }

    void declare(String name) {
        values.put(name, UNINITIALIZED);
    }

    void define(String name, Object value) {
        values.put(name, value);
    }

    void assign(String name, Object value) {
        if (values.containsKey(name)) {
            values.put(name, value);
        }
        parent.assign(name, value);
    }

    private static class NoOp extends Environment {
        public NoOp() {
        }

        @Override
        boolean isDefined(String name) {
            return false;
        }

        @Override
        boolean isInitialized(String name) {
            return false;
        }

        @Override
        Object get(String name) {
            return null;
        }

        @Override
        void declare(String name) {
            throw new RuntimeException("this should not happen");
        }

        @Override
        void define(String name, Object value) {
            throw new RuntimeException("this should not happen");
        }

        @Override
        void assign(String name, Object value) {
        }
    }

}
