package io.github.rohitdm97.loximpl;

import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This is class that facilitates Read Evaluate Print Loop for Applcation
 */
@RequiredArgsConstructor
public class Repl {

    private final Application app;

    public void start() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            System.out.print("> ");
            while ((line = reader.readLine()) != null) {
                app.run(line);
                System.out.print(">  ");
            }
        }
    }

}
