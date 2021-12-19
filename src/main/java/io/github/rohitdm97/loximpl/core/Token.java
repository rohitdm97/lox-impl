package io.github.rohitdm97.loximpl.core;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }

}
