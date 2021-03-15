package com.corbin;

public class Lexeme {
    private final TokenType type;
    private final int lineNumber;

    private final String stringValue;
    private final Integer intValue;
    private final Float floatValue;

    // Constructor for specials characters, keywords, operators, etc.
    public Lexeme(TokenType type, int lineNumber) {
        this.type = type;
        this.lineNumber = lineNumber;
        this.stringValue = null;
        this.intValue = null;
        this.floatValue = null;
    }

    // Constructor for Identifiers
    public Lexeme(TokenType type, String stringValue, int lineNumber) {
        this.type = type;
        this.lineNumber = lineNumber;
        this.stringValue = stringValue;
        this.intValue = null;
        this.floatValue = null;
    }

    // Constructor for Integers
    public Lexeme(TokenType type, Integer intValue, int lineNumber) {
        this.type = type;
        this.lineNumber = lineNumber;
        this.intValue = intValue;
        this.stringValue = null;
        this.floatValue = null;
    }

    // Constructor for Floats
    public Lexeme(TokenType type, Float floatValue, int lineNumber) {
        this.type = type;
        this.lineNumber = lineNumber;
        this.floatValue = floatValue;
        this.stringValue = null;
        this.intValue = null;
    }

    public TokenType getType() {
        return type;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getStringValue() {
        return stringValue;
    }

    public Integer getIntValue() {
        return intValue;
    }

    public Float getFloatValue() {
        return floatValue;
    }

    public String toString() {
        return ("Type = " + type + ", Line Number = " + lineNumber
                + intValue == null ? "" : ("Integer Value = " + intValue)
                + floatValue == null ? "" : ("Float Value = " + floatValue)
                + stringValue == null ? "" : ("String Value = " + stringValue));
    }
}
