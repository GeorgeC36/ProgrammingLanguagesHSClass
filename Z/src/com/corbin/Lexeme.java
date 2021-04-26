package com.corbin;

public class Lexeme {
    public enum Datatype {FLOAT, INT, STRING};
    private final TokenType type;
    private final int lineNumber;

    private final String stringValue;
    private final Integer intValue;
    private final Float floatValue;
    private Lexeme left;
    private Lexeme right;
    private boolean isConstant;
    private Datatype datatype;


    // Constructor for specials characters, keywords, operators, etc.
    public Lexeme(TokenType type, int lineNumber) {
        this.type = type;
        this.lineNumber = lineNumber;
        this.stringValue = null;
        this.intValue = null;
        this.floatValue = null;
        this.datatype = null;
    }

    // Constructor for Identifiers
    public Lexeme(TokenType type, String stringValue, int lineNumber) {
        this.type = type;
        this.lineNumber = lineNumber;
        this.stringValue = stringValue;
        this.intValue = null;
        this.floatValue = null;
        this.datatype = Datatype.STRING;
    }

    // Constructor for Integers
    public Lexeme(TokenType type, Integer intValue, int lineNumber) {
        this.type = type;
        this.lineNumber = lineNumber;
        this.intValue = intValue;
        this.stringValue = null;
        this.floatValue = null;
        this.datatype = Datatype.INT;
    }

    // Constructor for Floats
    public Lexeme(TokenType type, Float floatValue, int lineNumber) {
        this.type = type;
        this.lineNumber = lineNumber;
        this.floatValue = floatValue;
        this.stringValue = null;
        this.intValue = null;
        this.datatype = Datatype.FLOAT;
    }

    public boolean equals(Lexeme other) {
        return this.type == TokenType.IDENTIFIER &&
                this.type == other.type &&
                this.stringValue != null &&
                this.stringValue.equals(other.stringValue);
    }

    public TokenType getType() {
        return type;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getStringValue() {
	switch (datatype) {
	case FLOAT:
	    return String.valueOf(floatValue);
	case INT:
	    return String.valueOf(intValue);
	case STRING:
	    return stringValue;
	}
	return "";		// should never happen
    }

    public Integer getIntValue() {
	switch (datatype) {
	case FLOAT:
	    return floatValue.intValue();
	case INT:
	    return intValue;
	case STRING:
	    Z.error(lineNumber, "Type conversion error. Can't convert STRING to INT");
	    return 0;
	}
	return 0;		// should never happen
    }

    public Float getFloatValue() {
	switch (datatype) {
	case FLOAT:
	    return floatValue;
	case INT:
	    return (float) intValue;
	case STRING:
	    Z.error(lineNumber, "Type conversion error. Can't convert STRING to FLOAT");
	    return (float) 0.0;
	}
	return (float) 0.0;		// should never happen
    }

    public boolean getBooleanValue() {
	if (type == TokenType.TRUE) return true;
	if (datatype != null) {
	    switch (datatype) {
	    case FLOAT:
		return floatValue != 0;
	    case INT:
		return intValue != 0;
	    case STRING:
		return !stringValue.isEmpty();
	    }
	}
	return false;
    }

    public String toString() {
        return ("Type = " + type + ", Line Number = " + lineNumber
                + (intValue == null ? "" : (", Integer Value = " + intValue))
                + (floatValue == null ? "" : (", Float Value = " + floatValue))
                + (stringValue == null ? "" : (", String Value = " + stringValue)));
    }

    public String toSimpleString() {
        return type.toString();
    }

    public Lexeme getLeft() {
        return left;
    }

    public Lexeme getRight() {
        return right;
    }

    public boolean isConstant() {
        return isConstant;
    }

    public void setLeft(Lexeme left) {
        this.left = left;
    }

    public void setRight(Lexeme right) {
        this.right = right;
    }

    public void setIsConstant(boolean isConstant) {
        this.isConstant = isConstant;
    }

    public Datatype getDatatype() {
        return datatype;
    }

    public void setDatatype(Datatype datatype) {
        this.datatype = datatype;
    }
}
