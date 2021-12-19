package io.github.rohitdm97.loximpl.core;

import io.github.rohitdm97.loximpl.error.ErrorReport;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class Engine {
    private final ErrorReport report;
    private final Interpreter interpreter;
    @Setter
    private boolean hadError = false;

    public Engine() {
        this.report = new ErrorReport(this, log);
        this.interpreter = new Interpreter(this.report);
    }

    public void run(String source) {
        report.reset();

        Scanner scanner = new Scanner(source, report);
        scanner.scan();
        if (hadError) {
            report.dump();
            return;
        }
        Parser parser = new Parser(scanner.getTokens(), report);
        final List<Stmt> statements = parser.parse();
        if (hadError) {
            report.dump();
            return;
        }
        interpreter.interpret(statements);
        if (hadError) {
            report.dump();
            return;
        }
    }

}
