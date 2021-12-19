package io.github.rohitdm97.loximpl.core;

import io.github.rohitdm97.loximpl.error.ErrorReport;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

import static io.github.rohitdm97.loximpl.core.TokenType.BANG;
import static io.github.rohitdm97.loximpl.core.TokenType.BANG_EQUAL;
import static io.github.rohitdm97.loximpl.core.TokenType.COMMA;
import static io.github.rohitdm97.loximpl.core.TokenType.DOT;
import static io.github.rohitdm97.loximpl.core.TokenType.EOF;
import static io.github.rohitdm97.loximpl.core.TokenType.EQUAL;
import static io.github.rohitdm97.loximpl.core.TokenType.EQUAL_EQUAL;
import static io.github.rohitdm97.loximpl.core.TokenType.GREATER;
import static io.github.rohitdm97.loximpl.core.TokenType.GREATER_EQUAL;
import static io.github.rohitdm97.loximpl.core.TokenType.IDENTIFIER;
import static io.github.rohitdm97.loximpl.core.TokenType.LEFT_BRACE;
import static io.github.rohitdm97.loximpl.core.TokenType.LEFT_PAREN;
import static io.github.rohitdm97.loximpl.core.TokenType.LESS;
import static io.github.rohitdm97.loximpl.core.TokenType.LESS_EQUAL;
import static io.github.rohitdm97.loximpl.core.TokenType.MINUS;
import static io.github.rohitdm97.loximpl.core.TokenType.NUMBER;
import static io.github.rohitdm97.loximpl.core.TokenType.PLUS;
import static io.github.rohitdm97.loximpl.core.TokenType.RIGHT_BRACE;
import static io.github.rohitdm97.loximpl.core.TokenType.RIGHT_PAREN;
import static io.github.rohitdm97.loximpl.core.TokenType.SEMICOLON;
import static io.github.rohitdm97.loximpl.core.TokenType.SLASH;
import static io.github.rohitdm97.loximpl.core.TokenType.STAR;
import static io.github.rohitdm97.loximpl.core.TokenType.STRING;

@Log4j2
class Scanner {


    private final String source;
    private final ErrorReport report;
    @Getter
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;

    public Scanner(String source, ErrorReport report) {
        this.report = report;
        this.source = source;
    }

    void scan() {
        while (shouldScan()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
    }

    private boolean shouldScan() {
        // add early quit if required
        return current < source.length();
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;

            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;

            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;

            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;

            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;

            case '/':
                if (match('/')) {
                    // discard comment content for this line
                    while (shouldScan() && peek() != '\n') advance();
                } else {
                    addToken(SLASH);
                }

            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;

            case '"': scanString(); break;

            default:
                if (isDigit(c)) {
                    scanNumber();
                    break;
                } else if (isAlpha(c)) {
                    scanIdentifier();
                    break;
                }

                report.error("Unexpected character " + c, line, "");
                return;
        }
    }

    private boolean match(char check) {
        if (!shouldScan()) return false;
        if (source.charAt(current) != check) {
            return false;
        }
        current++;
        return true;
    }

    private char peek() {
        if (!shouldScan()) return '\0';
        return this.source.charAt(current);
    }

    private char peek2() {
        if (current + 1 >= source.length()) return '\0';
        return this.source.charAt(current + 1);
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        tokens.add(new Token(type, source.substring(start, current), literal, line));
    }

    private void scanString() {
        while (shouldScan() && peek() != '"') {
            if (peek() == '\n') line++;
            advance();
        }

        if (!shouldScan()) {
            report.error("Unterminated string.", line, "");
            return;
        }

        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private void scanNumber() {
        while (isDigit(peek())) {
            advance();
        }

        if (peek() == '.' && isDigit(peek2())) {
            advance(); // for "."

            while (isDigit(peek())) {
                advance();
            }
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void scanIdentifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = KeywordStore.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

}

