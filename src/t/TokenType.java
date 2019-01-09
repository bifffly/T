package t;

public enum TokenType {
    // Bracketing Tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_SQ, RIGHT_SQ, LEFT_CURLY, RIGHT_CURLY, COMMA,
    DOT, SEMICOLON, COLON,

    // Arithmetic Operators
    PLUS, MINUS, STAR, SLASH, MOD,

    // Increment Operators
    INCR, DECR,

    // Comparison Operators
    LESS, LESS_EQ, GREATER, GREATER_EQ, EQ,

    // Negative Comparison Operators
    NOT_EQ,

    // Boolean Operators
    AND, OR, NOT,

    // Functional Operators
    ASSIGN, PERFORM, UNDERSCORE,

    // Literals
    ID, NUMBER, STRING, BOOLEAN,

    // Keywords
    FN, STRUCT, RETURN, THIS, SUPER, NIL, INCLUDE, EXCLUDE, MATCH, IF, ELIF,
    ELSE, FOR, WHILE, BLOCK, NAMESPACE, ERROR, TRY, CATCH, PRIVATE, PUBLIC,
    PROTECTED, ENUM, EXTENDS, CONS,

    // Types
    REAL, CHAR, BOOL, VOID, FREE,

    // End of File
    EOF
}
