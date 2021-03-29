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

        if (debug) System.out.println("    -- check: looking for " + expected + ", have " + currentLexeme + " --");
        return currentLexeme.getType() == expected;
    }

    private void consume(TokenType expected) {
        if (debug) System.out.println("-- consume " + expected + " --");
        if (check(expected)) advance();
        else {
            Z.error(currentLexeme, "Expected " + expected + " but found " + currentLexeme);
        }
    }

    private boolean checkNext(TokenType type) {
        if (nextLexemeIndex >= lexemes.size()) {
            return false;
        } else {
            if (debug) System.out.println("    -- checkNext: looking for " + type + ", next is " + lexemes.get(nextLexemeIndex).getType() + " --");
            return lexemes.get(nextLexemeIndex).getType() == type;
        }
    }

    private void advance() {
        do {
            currentLexeme = lexemes.get(nextLexemeIndex);
            nextLexemeIndex++;
        } while (currentLexeme.getType() == LINECOMMENT);
        if (debug) System.out.println("-- advance: currentLexeme = " + currentLexeme + " --");
    }

    // ---------- Consumption Methods ----------
    public void program() {
        if (debug) System.out.println("-- program --");
        if (statementListPending()) statementList();
        System.out.println("-- END OF PROGRAM --");
    }

    private void statementList() {
        if (debug) System.out.println("-- statementList --");
        while (statementPending()) statement();
    }

    private void statement() {
        if (debug) System.out.println("-- statement --");
        if (assignmentPending()) assignment();
        else if (initializationPending()) initialization();
        else if (expressionPending()) expression();
        else if (functionDefinitionPending()) functionDefinition();
        else if (loopPending()) loop();
        else if (conditionalPending()) conditional();
        if (debug) System.out.println("== End of statement ==");
   }

    private void conditional() {
        if (debug) System.out.println("-- conditional --");
        if (ifElseStatementsPending()) ifElseStatements();
        else if (switchCaseStatementsPending()) switchCaseStatements();
    }

    private void switchCaseStatements() {
        if (debug) System.out.println("-- switchCaseStatements --");
        switchStatement();
        consume(OPENBRACE);
        while (caseStatementPending()) caseStatement();
        consume(CLOSEBRACE);
    }

    private void caseStatement() {
        if (debug) System.out.println("-- caseStatement --");
        if (check(CASE)) {
            consume(CASE);
            expression();
            consume(COLON);
            statementList();
        } else if (check(DEFAULT)) {
            consume(DEFAULT);
            consume(COLON);
            statementList();
        }
    }

    private void switchStatement() {
        if (debug) System.out.println("-- switchStatement --");
        consume(SWITCH);
        if (checkNext(OPENPAREN)) {
            consume(OPENPAREN);
            expression();
            consume(CLOSEPAREN);
        } else if (expressionPending()) expression();
    }

    private void ifElseStatements() {
        if (debug) System.out.println("-- ifElseStatements --");
        ifStatement();
        if (elseIfStatementPending()) elseIfStatement();
        if (elseStatementPending()) elseStatement();
    }

    private void elseStatement() {
        if (debug) System.out.println("-- elseStatement --");
        consume(ELSE);
        consume(OPENBRACE);
        statementList();
        consume(CLOSEBRACE);
    }

    private void elseIfStatement() {
        if (debug) System.out.println("-- elseIfStatement --");
        consume(ELSE);
        ifStatement();
    }

    private void ifStatement() {
        if (debug) System.out.println("-- ifStatement --");
        consume(IF);
        if (check(OPENPAREN)) {
            consume(OPENPAREN);
            if (booleanExpressionPending()) booleanExpression();
            else Z.error(currentLexeme.getLineNumber(), "Missing boolean expression in if statement");
            consume(CLOSEPAREN);
        } else if (booleanExpressionPending()) booleanExpression();
        else Z.error(currentLexeme.getLineNumber(), "Missing boolean expression in if statement");
        consume(OPENBRACE);
        statementList();
        consume(CLOSEBRACE);
    }

    private void loop() {
        if (debug) System.out.println("-- loop --");
        if (forLoopPending()) forLoop();
        else if (forInPending()) forIn();
        else if (whileLoopPending()) whileLoop();
    }

    private void whileLoop() {
        if (debug) System.out.println("-- whileLoop --");
        consume(WHILE);
        consume(OPENPAREN);
        booleanExpression();
        consume(CLOSEPAREN);
        consume(OPENBRACE);
        statementList();
        consume(CLOSEBRACE);
    }

    private void forIn() {
        if (debug) System.out.println("-- forIn --");
        consume(FOR);
        consume(IDENTIFIER);
        consume(IN);
        iterable();
        consume(OPENBRACE);
        statementList();
        consume(CLOSEBRACE);
    }

    private void iterable() {
        if (debug) System.out.println("-- iterable --");
        if (rangePending()) range();
        else if (check(IDENTIFIER)) consume(IDENTIFIER);
    }

    private void range() {
        if (debug) System.out.println("-- range --");
        primary();
        consume(ELLIPSIS);
        primary();
    }

    private void forLoop() {
        if (debug) System.out.println("-- forLoop --");
        consume(FOR);
        consume(OPENPAREN);
        if (assignmentPending()) {
            assignment();
            consume(SEMICOLON);
        }
        if (booleanExpressionPending()) {
            booleanExpression();
            consume(SEMICOLON);
        }
        if (loopIncrementPending()) {
            loopIncrement();
        }
        consume(CLOSEPAREN);
        consume(OPENBRACE);
        statementList();
        consume(CLOSEBRACE);
    }

    private void loopIncrement() {
        if (debug) System.out.println("-- loopIncrement --");
        if (assignmentPending()) assignment();
        else if (incrementExpressionPending()) incrementExpression();
    }

    private void booleanExpression() {
        if (debug) System.out.println("-- booleanExpression --");
        if (simpleBooleanPending()) simpleBoolean();
        else if (binaryBooleanPending()) binaryBoolean();
        else if (unaryBooleanPending()) unaryBoolean();
    }

    private void binaryBoolean() {
        if (debug) System.out.println("-- binaryBoolean --");
        booleanExpression();
        comparator();
        booleanExpression();
    }

    private void simpleBoolean() {
        if (debug) System.out.println("-- simpleBoolean --");
        expression();
        comparator();
        expression();
    }

    private void unaryBoolean() {
        if (debug) System.out.println("-- unaryBoolean --");
        if (check(IDENTIFIER)) consume(IDENTIFIER);
        else if (booleanLiteralPending()) booleanLiteral();
        else if (check(NOT)) {
            consume(NOT);
            booleanExpression();
        }
        else if (check(OPENPAREN)) {
            consume(OPENPAREN);
            booleanExpression();
            consume(CLOSEPAREN);
        }
    }

    private void functionDefinition() {
        if (debug) System.out.println("-- functionDefinition --");
        consume(FUNC);
        consume(IDENTIFIER);
        consume(OPENPAREN);
        if (functionParameterListPending()) functionParameterList();
        consume(CLOSEPAREN);
        if (check(RETURNS)) {
            consume(RETURNS);
            functionReturnType();
        }
        consume(OPENBRACE);
        statementList();
        consume(CLOSEBRACE);
    }

    private void functionReturnType() {
        if (debug) System.out.println("-- functionReturnType --");
        if (dataTypePending()) dataType();
        else if (check(VOID)) consume(VOID);
    }

    private void functionParameterList() {
        if (debug) System.out.println("-- functionParameterList --");
        functionParameter();
        if (check(COMMA)) {
            consume(COMMA);
            functionParameterList();
        }
    }

    private void functionParameter() {
        if (debug) System.out.println("-- functionParameter --");
        consume(IDENTIFIER);
        consume(COLON);
        dataType();
    }

    private void assignment() {
        if (debug) System.out.println("-- assignment --");
        if (arrayReferencePending()) arrayReference();
        else {
            consume(IDENTIFIER);
        }
        assignmentOperator();
        expression();
    }

    private void assignmentOperator() {
        if (debug) System.out.println("-- assignmentOperator --");
        if (check(ASSIGN)) consume(ASSIGN);
        else if (check(PLUSASSIGN)) consume(PLUSASSIGN);
        else if (check(MINUSASSIGN)) consume(MINUSASSIGN);
        else if (check(TIMESASSIGN)) consume(TIMESASSIGN);
        else if (check(DIVIDEASSIGN)) consume(DIVIDEASSIGN);
        else if (check(MODASSIGN)) consume(MODASSIGN);
        else if (check(EXPASSIGN)) consume(EXPASSIGN);
    }

    private void initialization() {
        if (debug) System.out.println("-- initialization --");
        if (variableInitializerPending()) variableInitializer();
        else if (constantInitializerPending()) constantInitializer();
    }

    private void constantInitializer() {
        if (debug) System.out.println("-- constantInitializer --");
        consume(CONST);
        consume(IDENTIFIER);
        if (check(COLON)) {
            consume(COLON);
            dataType();
        }
        consume(ASSIGN);
        initializerExpression();
    }

    private void variableInitializer() {
        if (debug) System.out.println("-- variableInitializer --");
        boolean legalSyntax = false;
        consume(VAR);
        consume(IDENTIFIER);
        if (check(COLON)) {
            consume(COLON);
            dataType();
            legalSyntax = true;
        }
        if (check(ASSIGN)) {
            consume(ASSIGN);
            initializerExpression();
            legalSyntax = true;
        }
        if (!legalSyntax) {
            Z.error(currentLexeme.getLineNumber(), "Variable declaration without type or initialization");
        }
    }

    private void initializerExpression() {
        if (debug) System.out.println("-- initializerExpression --");
        if (expressionPending()) expression();
        if (arrayInitializerPending()) arrayInitializer();
    }

    private void arrayInitializer() {
        if (debug) System.out.println("-- arrayInitializer --");
        consume(OPENBRACKET);
        if (expressionListPending()) expressionList();
        consume(CLOSEBRACKET);
    }

    private void dataType() {
        if (debug) System.out.println("-- dataType --");
        if (check(KW_STRING)) consume(KW_STRING);
        else if (check(KW_INT)) consume(KW_INT);
        else if (check(KW_FLOAT)) consume(KW_FLOAT);
        else if (arrayTypePending()) arrayType();
    }

    private void arrayType() {
        if (debug) System.out.println("-- arrayType --");
        consume(OPENBRACKET);
        dataType();
        consume(CLOSEBRACKET);
    }

    private void expressionList() {
        if (debug) System.out.println("-- expressionList --");
        if (expressionPending()) expression();
        if (check(COMMA)) {
            consume(COMMA);
            expressionList();
        }
    }

    private void expression() {
        if (debug) System.out.println("-- expression --");
        if (primaryPending()) primary();
        else if (unaryPending()) unary();
        else if (binaryPending()) binary();
    }

    private void binary() {
        if (debug) System.out.println("-- binary --");
        expression();
        binaryOperator();
        expression();
    }

    private void binaryOperator() {
        if (debug) System.out.println("-- binaryOperator --");
        if (comparatorPending()) comparator();
        else if (mathematicalOperatorPending()) mathematicalOperator();
    }

    private void mathematicalOperator() {
        if (debug) System.out.println("-- mathematicalOperator --");
        if (check(PLUS)) consume(PLUS);
        else if (check(MINUS)) consume(MINUS);
        else if (check(MINUS)) consume(MINUS);
        else if (check(TIMES)) consume(TIMES);
        else if (check(DIVIDE)) consume(DIVIDE);
        else if (check(EXP)) consume(EXP);
        else if (check(MOD)) consume(MOD);
    }

    private void comparator() {
        if (debug) System.out.println("-- comparator --");
        if (check(GREATER)) consume(GREATER);
        else if (check(GREATEREQUAL)) consume(GREATEREQUAL);
        else if (check(LESS)) consume(LESS);
        else if (check(LESSEQUAL)) consume(LESSEQUAL);
        else if (check(NOTEQUAL)) consume(NOTEQUAL);
        else if (check(EQUAL)) consume(EQUAL);
    }

    private void unary() {
        if (debug) System.out.println("-- unary --");
        if (prefixUnaryOperatorsPending()) {
            prefixUnaryOperators();
            expression();
        } else if (incrementExpressionPending()) incrementExpression();
    }

    private void incrementExpression() {
        if (debug) System.out.println("-- incrementExpression --");
        if (check(INCREMENT)) consume(INCREMENT);
        else if (check(DECREMENT)) consume(DECREMENT);
    }

    private void prefixUnaryOperators() {
        if (debug) System.out.println("-- prefixUnaryOperators --");
        if (check(PLUS)) consume(PLUS);
        else if (check(MINUS)) consume(MINUS);
        else if (check(NOT)) consume(NOT);
    }

    private void primary() {
        if (debug) System.out.println("-- primary --");
        if (literalPending()) literal();
        else if (groupingPending()) grouping();
        else if (functionCallPending()) functionCall();
        else if (arrayReferencePending()) arrayReference();
        else if (tupleElementReferencePending()) tupleElementReference();
        else if (check(IDENTIFIER)) consume(IDENTIFIER);
    }

    private void arrayReference() {
        if (debug) System.out.println("-- arrayReference --");
        consume(IDENTIFIER);
        consume(OPENBRACKET);
        expression();
        consume(CLOSEBRACKET);
    }

    private void functionCall() {
        if (debug) System.out.println("-- functionCall --");
        consume(IDENTIFIER);
        consume(OPENPAREN);
        if (argumentListPending()) argumentList();
        consume(CLOSEPAREN);
    }

    private void argumentList() {
        if (debug) System.out.println("-- argumentList --");
        if (check(IDENTIFIER)) {
            consume(IDENTIFIER);
            consume(COLON);
        }
        expression();
        if (check(COMMA)) {
            consume(COMMA);
            argumentList();
        }
    }

    private void grouping() {
        if (debug) System.out.println("-- grouping --");
        consume(OPENPAREN);
        expression();
        consume(CLOSEPAREN);
    }

    private void literal() {
        if (debug) System.out.println("-- literal --");
        if (check(INT)) consume(INT);
        else if (check(FLOAT)) consume(FLOAT);
        else if (booleanLiteralPending()) booleanLiteral();
        else if (check(STRING)) consume(STRING);
    }

    private void booleanLiteral() {
        if (debug) System.out.println("-- booleanLiteral --");
        if (check(TRUE)) consume(TRUE);
        else if (check(FALSE)) consume(FALSE);
    }

    private void tupleElementReference() {
        if (debug) System.out.println("-- tupleElementReference --");
        tupleExpression();
        if (check(INT)) consume(INT);
        else if (check(IDENTIFIER)) consume(IDENTIFIER);
    }

    private void tupleExpression() {
        if (debug) System.out.println("-- tupleExpression --");
        consume(OPENPAREN);
        tupleList();
        consume(CLOSEPAREN);
    }

    private void tupleList() {
        if (debug) System.out.println("-- tupleList --");
        tupleValue();
        if (check(COMMA)) {
            consume(COMMA);
            tupleList();
        }
    }

    private void tupleValue() {
        if (debug) System.out.println("-- tupleValue --");
        if (check(IDENTIFIER)) {
            consume(IDENTIFIER);
            consume(COLON);
            expression();
        }
        expression();
    }

    // ---------- Pending Methods ----------
    private boolean programPending() {
        return check(EOF) || statementListPending();
    }

    private boolean statementListPending() {
        return statementPending();
    }

    private boolean statementPending() {
        if (debug) System.out.println("  -- statementPending --");
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
        return check(ELSE) && checkNext(OPENBRACE);
    }

    private boolean elseIfStatementPending() {
        return check(ELSE) && !checkNext(OPENBRACE);
    }

    private boolean ifStatementPending() {
        return check(IF);
    }

    private boolean loopPending() {
        if (debug) System.out.println("  -- loopPending --");
        return forLoopPending()
                || forInPending()
                || whileLoopPending();
    }

    private boolean whileLoopPending() {
        return check(WHILE);
    }

    private boolean forInPending() {
        return check(FOR) && checkNext(IDENTIFIER);
    }

    private boolean iterablePending() {
        return rangePending() || check(IDENTIFIER);
    }

    private boolean rangePending() {
        return checkNext(ELLIPSIS) && primaryPending();
    }

    private boolean forLoopPending() {
        return check(FOR) && checkNext(OPENPAREN);
    }

    private boolean loopIncrementPending() {
        return assignmentPending() || incrementExpressionPending();
    }

    private boolean booleanExpressionPending() {
        return simpleBooleanPending()
                || binaryBooleanPending()
                || unaryBooleanPending();
    }

    private boolean binaryBooleanPending() {
        return (checkNext(AND) || checkNext(OR)) && booleanExpressionPending();
    }

    private boolean conjunctionPending() {
        return check(AND) || check(OR);
    }

    private boolean simpleBooleanPending() {
        return (checkNext(GREATER)
                || checkNext(GREATEREQUAL)
                || checkNext(LESS)
                || checkNext(LESSEQUAL)
                || checkNext(NOTEQUAL)
                || checkNext(EQUAL))
                && expressionPending();
    }

    private boolean unaryBooleanPending() {
        return check(NOT)
                || check(OPENPAREN)
                || check(IDENTIFIER)
                || booleanLiteralPending();
    }

    private boolean functionDefinitionPending() {
        if (debug) System.out.println("  -- functionDefinitionPending --");
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
        if (debug) System.out.println("  -- assignmentPending --");
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
        return check(KW_STRING) || check(KW_INT) || check(KW_FLOAT) || arrayTypePending();
    }

    private boolean arrayTypePending() {
        return check(OPENBRACKET);
    }

    private boolean expressionPending() {
        if (debug) System.out.println("  -- expressionPending --");
        return primaryPending() || unaryPending() || binaryPending();
    }

    private boolean binaryPending() {
        if (debug) System.out.println("  -- binaryPending --");
        return (checkNext(PLUS) || checkNext(MINUS) || checkNext(TIMES) || checkNext(DIVIDE) || checkNext(EXP) || checkNext(MOD)
                || checkNext(GREATER) || checkNext(GREATEREQUAL) || checkNext(LESS) || checkNext(LESSEQUAL)
                || checkNext(NOTEQUAL) || checkNext(EQUAL))
            && expressionPending();
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
        if (debug) System.out.println("  -- literalPending --");
        return check(INT)
        	    || check(FLOAT)
                || booleanLiteralPending()
                || check(STRING);
    }

    private boolean booleanLiteralPending() {
        return check(TRUE)
                || check(FALSE);
    }

}
