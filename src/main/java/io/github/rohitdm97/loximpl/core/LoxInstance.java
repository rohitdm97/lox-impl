package io.github.rohitdm97.loximpl.core;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class LoxInstance {

    private final LoxClass klass;
    private final Map<String, Value> fields = new HashMap<>();

    @Override
    public String toString() {
        return String.format("<instance %s>", klass.toString());
    }

    Value get(String name) {
        if (fields.containsKey(name)) {
            return fields.get(name);
        }

        LoxFunction method = klass.findMethod(name);
        if (method != null) return new Value(method.bind(this));

        return null;
    }

    public void setField(String key, Object value) {
        fields.put(key, new Value(value));
    }

    @RequiredArgsConstructor
    public static class Value {
        private final Object object;

        public Object unwrap() {
            return object;
        }
    }

}
