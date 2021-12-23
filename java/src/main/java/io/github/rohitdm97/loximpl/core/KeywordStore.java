package io.github.rohitdm97.loximpl.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.github.rohitdm97.loximpl.core.TokenType.AND;
import static io.github.rohitdm97.loximpl.core.TokenType.CLASS;
import static io.github.rohitdm97.loximpl.core.TokenType.ELSE;
import static io.github.rohitdm97.loximpl.core.TokenType.FALSE;
import static io.github.rohitdm97.loximpl.core.TokenType.FOR;
import static io.github.rohitdm97.loximpl.core.TokenType.FUN;
import static io.github.rohitdm97.loximpl.core.TokenType.IF;
import static io.github.rohitdm97.loximpl.core.TokenType.NIL;
import static io.github.rohitdm97.loximpl.core.TokenType.OR;
import static io.github.rohitdm97.loximpl.core.TokenType.PRINT;
import static io.github.rohitdm97.loximpl.core.TokenType.RETURN;
import static io.github.rohitdm97.loximpl.core.TokenType.SUPER;
import static io.github.rohitdm97.loximpl.core.TokenType.THIS;
import static io.github.rohitdm97.loximpl.core.TokenType.TRUE;
import static io.github.rohitdm97.loximpl.core.TokenType.VAR;
import static io.github.rohitdm97.loximpl.core.TokenType.WHILE;

public class KeywordStore {

    private static final Map<String, TokenType> keywords;

    static {
        final HashMap<String, TokenType> words = new HashMap<>();
        words.put("and", AND);
        words.put("class", CLASS);
        words.put("else", ELSE);
        words.put("false", FALSE);
        words.put("for", FOR);
        words.put("fun", FUN);
        words.put("if", IF);
        words.put("nil", NIL);
        words.put("or", OR);
        words.put("print", PRINT);
        words.put("return", RETURN);
        words.put("super", SUPER);
        words.put("this", THIS);
        words.put("true", TRUE);
        words.put("var", VAR);
        words.put("while", WHILE);

        keywords = Collections.unmodifiableMap(words);
    }

    public static boolean containsKey(String key) {
        return keywords.containsKey(key);
    }

    public static TokenType get(String key) {
        return keywords.get(key);
    }

}
