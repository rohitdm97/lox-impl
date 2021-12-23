package io.github.rohitdm97.loximpl.core;

import io.github.rohitdm97.loximpl.error.ErrorReport;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class Engine {
    private final ErrorReport report;
    private final Interpreter interpreter;
    private Status status = Status.INITIAL;

    public Engine() {
        this.report = new ErrorReport(this, log);
        this.interpreter = new Interpreter(this.report);
    }

    public void run(String source) {
        report.reset();
        status = Status.INITIAL;

        Scanner scanner = new Scanner(source, report);
        scanner.scan();
        if (status == Status.ERROR) {
            report.dump();
            return;
        }
        Parser parser = new Parser(scanner.getTokens(), report);
        final List<Stmt> statements = parser.parse();
        if (status == Status.ERROR) {
            report.dump();
            return;
        }
        Resolver resolver = new Resolver(interpreter, report);
        resolver.resolve(statements);
        if (status == Status.ERROR) {
            report.dump();
            return;
        }

        status = Status.RUN_STARTED;
        interpreter.interpret(statements);
        if (status == Status.RUNTIME_ERROR) {
            report.dump();
            return;
        }
    }

    public void setHadError() {
        switch (status) {
            case SHUTDOWN:
                new IllegalStateException("Engine has started shutting down").printStackTrace();
                return;
            case INITIAL:
            case ERROR:
                status = Status.ERROR;
                return;
            case RUN_STARTED:
            case RUNTIME_ERROR:
                status = Status.RUNTIME_ERROR;
                return;
            default:
                status = Status.SHUTDOWN;
                log.error("Unknown error at unknown stage, shutting down");
        }
    }

    private enum Status {
        INITIAL,
        ERROR,
        RUN_STARTED,
        RUNTIME_ERROR,
        SHUTDOWN
    }

}
