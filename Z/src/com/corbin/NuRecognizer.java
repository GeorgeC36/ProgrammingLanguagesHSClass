package com.corbin;

import java.util.ArrayList;

import static com.corbin.TokenType.*;

public class NuRecognizer {
    private static final boolean debug = true;

    // ---------- Instance Variables ----------
    ArrayList<Lexeme>  lexemes;
    private int nextLexemeIndex = 0;
    private Lexeme currentLexeme;

    // ---------- Constructor ----------
    public NuRecognizer(ArrayList<Lexeme> lexemes) {
        this.lexemes = lexemes;
        advance();
    }

    // ---------- Utility Methods ----------
    private boolean check(TokenType expected) {
        return currentLexeme.getType() == expected;
    }

    private void consume(TokenType expected) {
        if (check(expected)) advance();
        else {
            Z.error(currentLexeme, "Expected " + expected + " but found " + currentLexeme);
        }
    }

    private boolean checkNext(TokenType type) {
        if (nextLexemeIndex >= lexemes.size()) {
            return false;
        } else {
            return lexemes.get(nextLexemeIndex).getType() == type;
        }
    }

    private void advance() {
        do {
            currentLexeme = lexemes.get(nextLexemeIndex);
            nextLexemeIndex++;
        } while (currentLexeme.getType() == LINECOMMENT);
    }

    // ---------- Consumption Methods ----------


    // ---------- Pending Methods ----------
    private boolean programPending() {
        return check(EOF) || statementListPending();
    }

    private boolean statementListPending() {
        return statementPending();
    }

    private boolean statementPending() {
        return expressionPending()
                || initializationPending() 
                || assignmentPending() 
                || functionDefinitionPending()
                || loopPending()
                || conditionalPending();
    }

    private boolean conditionalPending() {
        return ifElseStatementsPending() || switchCaseStatementsPending();
    }

    private boolean switchCaseStatementsPending() {
        return switchStatementPending() && check(OPENBRACE) && caseStatementPending() && check(CLOSEBRACE);
    }

    private boolean caseStatementPending() {
        return check(CASE) && expressionPending() && check(COLON) && statementListPending()
                || check(DEFAULT) && check(COLON) && statementListPending();
    }

    private boolean switchStatementPending() {
        return check(SWITCH) && check(OPENPAREN) && expressionPending() && check(CLOSEPAREN)
                || check(SWITCH) && expressionPending();
    }

    private boolean ifElseStatementsPending() {
        return ifStatementPending() && elseIfStatementPending() && elseStatementPending()
                || ifStatementPending() && elseStatementPending()
                || ifStatementPending();
    }

    private boolean elseStatementPending() {
        return check(ELSE) && check(OPENBRACE) && statementListPending() && check(CLOSEBRACE);
    }

    private boolean elseIfStatementPending() {
        return check(ELSE) && ifStatementPending();
    }

    private boolean ifStatementPending() {
        return check(IF) && check(OPENPAREN) && booleanExpressionPending() && check(CLOSEPAREN) && check(OPENBRACE) && statementListPending() && check(CLOSEBRACE)
                || check(IF) && booleanExpressionPending() && check(OPENBRACE) && statementListPending() && check(CLOSEBRACE);
    }

    private boolean loopPending() {
        return forLoopPending()
                || forInPending()
                || whileLoopPending();
    }

    private boolean whileLoopPending() {
        return check(WHILE) && check(OPENPAREN) && booleanExpressionPending() && check(CLOSEPAREN) && check(OPENBRACE) && statementListPending() && check(CLOSEBRACE);
    }

    private boolean forInPending() {
        return check(FOR) && check(IDENTIFIER) && check(IN) && iterablePending() && check(OPENBRACE) && statementListPending() && check(CLOSEBRACE);
    }

    private boolean iterablePending() {
        return rangePending() || check(IDENTIFIER);
    }

    private boolean rangePending() {
        return expressionPending() && check(ELLIPSIS) && expressionPending();
    }

    private boolean forLoopPending() {
        return check(FOR) && check(OPENPAREN) && assignmentPending() && check(SEMICOLON) && booleanExpressionPending() && check(SEMICOLON) && loopIncrementPending() && check(CLOSEPAREN) && check(OPENBRACE) && statementListPending() && check(CLOSEBRACE)
            || check(FOR) && check(OPENPAREN) && booleanExpressionPending() && check(SEMICOLON) && loopIncrementPending() && check(CLOSEPAREN) && check(OPENBRACE) && statementListPending() && check(CLOSEBRACE)
            || check(FOR) && check(OPENPAREN) && assignmentPending() && check(SEMICOLON) && loopIncrementPending() && check(CLOSEPAREN) && check(OPENBRACE) && statementListPending() && check(CLOSEBRACE)
            || check(FOR) && check(OPENPAREN) && assignmentPending() && check(SEMICOLON) && booleanExpressionPending() && check(CLOSEPAREN) && check(OPENBRACE) && statementListPending() && check(CLOSEBRACE)
            || check(FOR) && check(OPENPAREN) && assignmentPending() && check(CLOSEPAREN) && check(OPENBRACE) && statementListPending() && check(CLOSEBRACE)
            || check(FOR) && check(OPENPAREN) && booleanExpressionPending() && check(CLOSEPAREN) && check(OPENBRACE) && statementListPending() && check(CLOSEBRACE)
            || check(FOR) && check(OPENPAREN) && loopIncrementPending() && check(CLOSEPAREN) && check(OPENBRACE) && statementListPending() && check(CLOSEBRACE);
    }

    private boolean loopIncrementPending() {
        return assignmentPending() || incrementExpressionPending();
    }

    private boolean booleanExpressionPending() {
        return unaryBooleanPending()
                || simpleBooleanPending()
                || binaryBooleanPending();
    }

    private boolean binaryBooleanPending() {
        return booleanExpressionPending() && conjunctionPending() && booleanExpressionPending();
    }

    private boolean conjunctionPending() {
        return check(AND) || check(OR);
    }

    private boolean simpleBooleanPending() {
        return expressionPending() && comparatorPending() && expressionPending();
    }

    private boolean unaryBooleanPending() {
        return check(IDENTIFIER)
                || booleanLiteralPending()
                || check(NOT) && booleanExpressionPending()
                || check(OPENPAREN) && booleanExpressionPending() && check(CLOSEPAREN);
    }

    private boolean functionDefinitionPending() {
        return check(FUNC) && check(IDENTIFIER) && check(OPENPAREN) && functionParameterListPending() && check(CLOSEPAREN) && check(OPENBRACE) && statementListPending() && check(CLOSEBRACE)
                || check(FUNC) && check(IDENTIFIER) && check(OPENPAREN) && check(CLOSEPAREN) && check(OPENBRACE) && statementListPending() && check(CLOSEBRACE)
                || check(FUNC) && check(IDENTIFIER) && check(OPENPAREN) && functionParameterListPending() && check(CLOSEPAREN) && check(RETURNS) && functionReturnTypePending() && check(OPENBRACE) && statementListPending() && check(CLOSEBRACE)
                || check(FUNC) && check(IDENTIFIER) && check(OPENPAREN) && check(CLOSEPAREN) && check(RETURNS) && functionReturnTypePending() && check(OPENBRACE) && statementListPending() && check(CLOSEBRACE);
    }

    private boolean functionReturnTypePending() {
        return dataTypePending()
                || check(VOID);
    }

    private boolean functionParameterListPending() {
        return functionParameterPending()
                || functionParameterPending() && check(COMMA) && functionParameterListPending();
    }

    private boolean functionParameterPending() {
        return check(IDENTIFIER) && check(COLON) && dataTypePending();
    }

    private boolean assignmentPending() {
        return check(IDENTIFIER) && assignmentOperatorPending() && expressionPending();
    }
//"=" | "+=" | "-=" | "*=" | "/=" | "%=" | "^="
    private boolean assignmentOperatorPending() {
        return check(ASSIGN)
                || check(PLUSASSIGN)
                || check(MINUSASSIGN)
                || check(TIMESASSIGN)
                || check(DIVIDEASSIGN)
                || check(MODASSIGN)
                || check(EXPASSIGN);
    }

    private boolean expressionListPending() {
        return expressionPending()
                || expressionPending() && check(COMMA) && expressionListPending();
    }

    private boolean initializationPending() {
        return variableInitializerPending()
                || constantInitializerPending();
    }

    private boolean constantInitializerPending() {
        return check(CONST) && check(IDENTIFIER) && check(COLON) && dataTypePending() && check(ASSIGN) && initializerExpressionPending();
    }

    private boolean variableInitializerPending() {
        return check(VAR) && check(IDENTIFIER) && check(COLON) && dataTypePending()
                || check(VAR) && check(IDENTIFIER) && check(COLON) && dataTypePending() && check(ASSIGN) && initializerExpressionPending()
                || check(VAR) && check(IDENTIFIER) && check(ASSIGN) && initializerExpressionPending();
    }

    private boolean initializerExpressionPending() {
        return expressionPending() || arrayInitializerPending();
    }

    private boolean arrayInitializerPending() {
        return check(OPENBRACKET) && expressionListPending() && check(CLOSEBRACKET)
                || check(OPENBRACKET) && check(CLOSEBRACKET);
    }

    private boolean dataTypePending() {
        return check(KW_STRING) || check(INT) || check(FLOAT) || arrayTypePending();

    }

    private boolean arrayTypePending() {
        return check(OPENBRACKET) && dataTypePending() && check(CLOSEBRACKET);
    }

    private boolean expressionPending() {
        return primaryPending() || unaryPending() || binaryPending();
    }

    private boolean binaryPending() {
        return expressionPending() && binaryOperatorPending() && expressionPending();
    }

    private boolean binaryOperatorPending() {
        return comparatorPending() || mathematicalOperatorPending();
    }

    private boolean mathematicalOperatorPending() {
        return check(PLUS)
                || check(MINUS)
                || check(TIMES)
                || check(DIVIDE)
                || check(EXP)
                || check(MOD);
    }

    private boolean comparatorPending() {
        return check(GREATER)
                || check(GREATEREQUAL)
                || check(LESS)
                || check(LESSEQUAL)
                || check(NOTEQUAL)
                || check(EQUAL);
    }

    private boolean unaryPending() {
        return (prefixUnaryOperatorsPending() && expressionPending()) || incrementExpressionPending();
    }

    private boolean incrementExpressionPending() {
        return check(IDENTIFIER) && incrementOperatorsPending()
                || incrementOperatorsPending() && check(IDENTIFIER);
    }

    private boolean incrementOperatorsPending() {
        return check(INCREMENT)
                || check(DECREMENT);
    }

    private boolean prefixUnaryOperatorsPending() {
        return check(PLUS)
                || check(MINUS)
                || check(NOT);
    }

    private boolean primaryPending() {
        return check(IDENTIFIER)
                || literalPending()
                || groupingPending()
                || functionCallPending()
                || arrayReferencePending()
                || tupleElementReferencePending();
    }

    private boolean tupleElementReferencePending() {
        return tupleExpressionPending() && check(NUMBER)
                || tupleExpressionPending() && check(IDENTIFIER);
    }

    private boolean tupleExpressionPending() {
        return tupleListPending();
    }

    private boolean tupleListPending() {
        return tupleValuePending()
                || tupleValuePending() && check(COMMA) && tupleListPending();
    }

    private boolean tupleValuePending() {
        return check(IDENTIFIER) && check(COLON) && expressionPending();
    }

    private boolean arrayReferencePending() {
        return check(IDENTIFIER)
                || check(OPENBRACKET)
                || check(CLOSEBRACKET);
    }

    private boolean functionCallPending() {
        return check(IDENTIFIER)
                || check(OPENPAREN)
                || check(CLOSEPAREN);
    }

    private boolean groupingPending() {
        return check(OPENPAREN)
                || check(CLOSEPAREN);
    }

    private boolean literalPending() {
        return check(NUMBER)
                || booleanLiteralPending()
                || check(STRING);
    }

    private boolean booleanLiteralPending() {
        return check(TRUE)
                || check(FALSE);
    }

}
