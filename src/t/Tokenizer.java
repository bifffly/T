package t;

import t.types.Char;
import t.types.Bool;
import t.types.Real;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static t.TokenType.*;

public class Tokenizer {
    private final String text;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private static final Map<String, TokenType> keywords;
    private static final Map<String, Bool> booleans;

    static {
        keywords = new HashMap<>();
        keywords.put("fn", FN);
        keywords.put("struct", STRUCT);
        keywords.put("return", RETURN);
        keywords.put("this", THIS);
        keywords.put("super", SUPER);
        keywords.put("nil", NIL);
        keywords.put("include", INCLUDE);
        keywords.put("exclude", EXCLUDE);
        keywords.put("match", MATCH);
        keywords.put("if", IF);
        keywords.put("elif", ELIF);
        keywords.put("else", ELSE);
        keywords.put("for", FOR);
        keywords.put("while", WHILE);
        keywords.put("block", BLOCK);
        keywords.put("namespace", NAMESPACE);
        keywords.put("error", ERROR);
        keywords.put("try", TRY);
        keywords.put("catch", CATCH);
        keywords.put("enum", ENUM);
        keywords.put("real", REAL);
        keywords.put("char", CHAR);
        keywords.put("bool", BOOL);
        keywords.put("void", VOID);
        keywords.put("and", AND);
        keywords.put("or", OR);
        keywords.put("extends", EXTENDS);
        keywords.put("free", FREE);
        keywords.put("cons", CONS);
        keywords.put("private", PRIVATE);
        keywords.put("public", PUBLIC);
        keywords.put("protected", PROTECTED);

        booleans = new HashMap<>();
        booleans.put("true", new Bool(true));
        booleans.put("false", new Bool(false));
    }

    Tokenizer(String text) {
        this.text = text;
    }

    //=========================================================================
    private boolean isAtEnd() {
        return current >= text.length();
    }

    private char advance() {
        current++;
        return text.charAt(current - 1);
    }

    private boolean match(char expected) {
        if (isAtEnd()) {
            return false;
        }
        if (text.charAt(current) != expected) {
            return false;
        }
        else {
            current++;
            return true;
        }
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        else {
            return text.charAt(current);
        }
    }

    private char peekNext() {
        if (current + 1 >= text.length()) {
            return '\0';
        }
        else {
            return text.charAt(current + 1);
        }
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String lexeme = text.substring(start, current);
        tokens.add(new Token(type, lexeme, literal, line));
    }

    public List<Token> tokenize() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '[': addToken(LEFT_SQ); break;
            case ']': addToken(RIGHT_SQ); break;
            case '{': addToken(LEFT_CURLY); break;
            case '}': addToken(RIGHT_CURLY); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case ';': addToken(SEMICOLON); break;
            case ':': addToken(COLON); break;
            case '_': addToken(UNDERSCORE); break;
            case '+': addToken(match('+') ? INCR : PLUS); break;
            case '*': addToken(STAR); break;
            case '/': addToken(SLASH); break;
            case '%': addToken(MOD); break;
            case '-': {
                if (match('>')) {
                    addToken(PERFORM);
                }
                else if (match('-')) {
                    addToken(DECR);
                }
                else {
                    addToken(MINUS);
                }
            } break;
            case '<': {
                if (match('=')) {
                    addToken(LESS_EQ);
                }
                else {
                    addToken(LESS);
                }
            } break;
            case '>': {
                if (match('=')) {
                    addToken(GREATER_EQ);
                }
                else {
                    addToken(GREATER);
                }
            } break;
            case '=': {
                if (match('=')) {
                    addToken(EQ);
                }
                else {
                    addToken(ASSIGN);
                }
            } break;
            case '!': {
                if (match('=')) {
                    addToken(NOT_EQ);
                }
                else {
                    addToken(NOT);
                }
            } break;
            case '#': {
                while (peek() != '\n' && !isAtEnd()) {
                    advance();
                }
            } break;
            case ' ':
            case '\r':
            case '\t': break;
            case '\n': line++; break;
            case '"': stringDoubleQuote(); break;
            case '\'': stringSingleQuote(); break;
            default: {
                if (isDigit(c)) {
                    number();
                }
                else if (isAlpha(c)) {
                    identifier();
                }
                else {
                    T.error(line, "Unexpected character.");
                }
                break;
            }
        }
    }

    private boolean isDigit(char c) {
        return (c >= '0') && (c <= '9');
    }

    private void number() {
        while (isDigit(peek())) {
            advance();
        }
        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) {
                advance();
            }
        }
        String lexeme = text.substring(start, current);
        addToken(NUMBER, new Real(Double.parseDouble(lexeme)));
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }
        String lexeme = text.substring(start, current);
        if (booleans.containsKey(lexeme)) {
            addToken(BOOLEAN, booleans.get(lexeme));
        }
        else if (keywords.containsKey(lexeme)) {
            addToken(keywords.get(lexeme));
        }
        else {
            addToken(ID);
        }
    }

    private void stringDoubleQuote() {
        StringBuilder sb = new StringBuilder();
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
            }
            if (peek() == '\\') {
                advance();
                switch (peek()) {
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case '0': sb.append('\0'); break;
                    default: break;
                }
                advance();
            }
            sb.append(advance());
        }
        if (isAtEnd()) {
            T.error(line, "Unterminated string.");
            return;
        }
        advance();
        String lexeme = sb.toString();
        addToken(STRING, new Char(lexeme));
    }

    private void stringSingleQuote() {
        while (peek() != '\'' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
            }
            advance();
        }
        if (isAtEnd()) {
            T.error(line, "Unterminated string.");
            return;
        }
        advance();
        String lexeme = text.substring(start + 1, current - 1);
        addToken(STRING, new Char(lexeme));
    }
}
