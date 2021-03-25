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
        return assignmentPending()
                || initializationPending() 
                || expressionPending()
                || functionDefinitionPending()
                || loopPending()
                || conditionalPending();
    }

    private boolean conditionalPending() {
        return ifElseStatementsPending() || switchCaseStatementsPending();
    }

    private boolean switchCaseStatementsPending() {
        return switchStatementPending();
    }

    private boolean caseStatementPending() {
        return check(CASE) || check(DEFAULT);
    }

    private boolean switchStatementPending() {
        return check(SWITCH);
    }

    private boolean ifElseStatementsPending() {
        return ifStatementPending();
    }

    private boolean elseStatementPending() {
        return check(ELSE) && check(OPENBRACE);
    }

    private boolean elseIfStatementPending() {
        return check(ELSE) && !checkNext(OPENBRACE);
    }

    private boolean ifStatementPending() {
        return check(IF);
    }

    private boolean loopPending() {
        return forLoopPending()
                || forInPending()
                || whileLoopPending();
    }

    private boolean whileLoopPending() {
        return check(WHILE);
    }

    private boolean forInPending() {
        return check(FOR) && check(IDENTIFIER);
    }

    private boolean iterablePending() {
        return rangePending() || check(IDENTIFIER);
    }

    private boolean rangePending() {
        return expressionPending() && check(ELLIPSIS);
    }

    private boolean forLoopPending() {
        return check(FOR) && check(OPENPAREN);
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
        return booleanExpressionPending() && (checkNext(AND) || checkNext(OR));
    }

    private boolean conjunctionPending() {
        return check(AND) || check(OR);
    }

    private boolean simpleBooleanPending() {
        return expressionPending()
                && (checkNext(GREATER)
                || checkNext(GREATEREQUAL)
                || checkNext(LESS)
                || checkNext(LESSEQUAL)
                || checkNext(NOTEQUAL)
                || checkNext(EQUAL));
    }

    private boolean unaryBooleanPending() {
        return booleanLiteralPending()
                || check(NOT)
                || check(OPENPAREN)
                || check(IDENTIFIER);
    }

    private boolean functionDefinitionPending() {
        return check(FUNC);
    }

    private boolean functionReturnTypePending() {
        return dataTypePending()
                || check(VOID);
    }

    private boolean functionParameterListPending() {
        return functionParameterPending();
    }

    private boolean functionParameterPending() {
        return check(IDENTIFIER) && checkNext(COLON);
    }

    private boolean assignmentPending() {
        return check(IDENTIFIER)
                && (checkNext(ASSIGN)
                || checkNext(PLUSASSIGN)
                || checkNext(MINUSASSIGN)
                || checkNext(TIMESASSIGN)
                || checkNext(DIVIDEASSIGN)
                || checkNext(MODASSIGN)
                || checkNext(EXPASSIGN));
    }



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
        return expressionPending();
    }

    private boolean initializationPending() {
        return variableInitializerPending() || constantInitializerPending();
    }

    private boolean constantInitializerPending() {
        return check(CONST);
    }

    private boolean variableInitializerPending() {
        return check(VAR);
    }

    private boolean initializerExpressionPending() {
        return expressionPending() || arrayInitializerPending();
    }

    private boolean arrayInitializerPending() {
        return check(OPENBRACKET);
    }

    private boolean dataTypePending() {
        return check(KW_STRING) || check(INT) || check(FLOAT) || arrayTypePending();
    }

    private boolean arrayTypePending() {
        return check(OPENBRACKET);
    }

    private boolean expressionPending() {
        return primaryPending() || unaryPending() || binaryPending();
    }

    private boolean binaryPending() {
        return expressionPending()
                && (checkNext(PLUS) || checkNext(MINUS) || checkNext(TIMES) || checkNext(DIVIDE) || checkNext(EXP) || checkNext(MOD)
                || checkNext(GREATER) || checkNext(GREATEREQUAL) || checkNext(LESS) || checkNext(LESSEQUAL)
                || checkNext(NOTEQUAL) || checkNext(EQUAL));
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
        return prefixUnaryOperatorsPending() || incrementExpressionPending();
    }

    private boolean incrementExpressionPending() {
        return check(IDENTIFIER) && (checkNext(INCREMENT) || checkNext(DECREMENT))
                || incrementOperatorsPending() && checkNext(IDENTIFIER);
    }

    private boolean incrementOperatorsPending() {
        return check(INCREMENT) || check(DECREMENT);
    }

    private boolean prefixUnaryOperatorsPending() {
        return check(PLUS)
                || check(MINUS)
                || check(NOT);
    }

    private boolean primaryPending() {
        return literalPending()
                || groupingPending()
                || functionCallPending()
                || arrayReferencePending()
                || tupleElementReferencePending()
                || check(IDENTIFIER);
    }

    private boolean tupleElementReferencePending() {
        return tupleExpressionPending();
    }

    private boolean tupleExpressionPending() {
        return check(OPENPAREN);
    }

    private boolean tupleListPending() {
        return tupleValuePending();
    }

    private boolean tupleValuePending() {
        return (check(IDENTIFIER) && checkNext(COLON))
                || expressionPending();
    }

    private boolean arrayReferencePending() {
        return check(IDENTIFIER) && checkNext(OPENBRACKET);
    }

    private boolean functionCallPending() {
        return check(IDENTIFIER) && checkNext(OPENPAREN);
    }

    private boolean argumentListPending() {
        return check(IDENTIFIER) && checkNext(COLON)
                || expressionListPending();
    }

    private boolean groupingPending() {
        return check(OPENPAREN);
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
