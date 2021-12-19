package io.github.rohitdm97.loximpl.error;

import io.github.rohitdm97.loximpl.core.Engine;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Supplier;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ErrorReport {
    private final Engine engine;
    private final Logger log;
    private final List<Error> errors = new ArrayList<>();

    public void error(String message, int line, String sourceLine) {
        this.engine.setHadError(true);
        this.errors.add(new Error(message, line, sourceLine));
    }

    public void dump() {
        this.errors.forEach(this.log::error);
    }

    public void reset() {
        this.engine.setHadError(false);
        this.errors.clear();
    }

    @AllArgsConstructor
    class Error implements Supplier<String> {
        private String message;
        private int line;
        private String sourceLine;

        @Override
        public String get() {
            return String.format("%s\nat line %d\t%s", message, line, sourceLine);
        }
    }

}
