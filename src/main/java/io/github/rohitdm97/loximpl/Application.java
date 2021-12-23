package io.github.rohitdm97.loximpl;

import io.github.rohitdm97.loximpl.core.Engine;
import lombok.Setter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Application {

    enum Mode {
        FILE, PROMPT
    }

    private final Engine engine = new Engine();

    @Setter
    private Mode mode;
    private String source;

    public static void main(String[] args) {
        try {
            new Application().run(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void run(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        }
        if (args.length == 1) {
            setMode(Mode.FILE);
            loadSource(args[0]);
            run(source);
        } else {
            setMode(Mode.PROMPT);
            Repl repl = new Repl(this);
            repl.start();
        }
    }

    public void run(String source) {
        engine.run(source);
    }

    private void loadSource(String file) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(file));
        this.source = new String(bytes, Charset.defaultCharset());
    }

}
