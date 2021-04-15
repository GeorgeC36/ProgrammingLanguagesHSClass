package com.corbin;

import java.util.ArrayList;
import java.util.Collections;

import static com.corbin.TokenType.*;

public class Parser {
    private static final boolean debug = false;

    // ---------- Instance Variables ----------
    ArrayList<Lexeme> lexemes;
    private int nextLexemeIndex = 0;
    private Lexeme currentLexeme;

    // ---------- Constructor ----------
    public Parser(ArrayList<Lexeme> lexemes) {
        this.lexemes = lexemes;
        advance();
    }

    // ---------- Utility Methods ----------
    private boolean check(TokenType expected) {

        if (debug) System.out.println("    -- check: looking for " + expected + ", have " + currentLexeme + " --");
        return currentLexeme.getType() == expected;
    }

    private Lexeme consume(TokenType expected) {
        if (debug) System.out.println("-- consume " + expected + " --");
        Lexeme lexeme = currentLexeme;
        if (check(expected)) advance();
        else {
            Z.error(currentLexeme, "Expected " + expected + " but found " + currentLexeme);
        }
        return lexeme;
    }

    private boolean checkNext(TokenType type) {
        if (nextLexemeIndex >= lexemes.size()) {
            return false;
        } else {
            if (debug)
                System.out.println("    -- checkNext: looking for " + type + ", next is " + lexemes.get(nextLexemeIndex).getType() + " --");
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
    public Lexeme program() {
        if (statementListPending()) {
            Lexeme program = new Lexeme(PROGRAM, currentLexeme.getLineNumber());
            program.setLeft(statementList());
            // right = null
            return program;
        } else return null;
    }

    private Lexeme statementList() {
        Lexeme statementList = new Lexeme(STATEMENT_LIST, currentLexeme.getLineNumber());
        while (statementPending()) {
            statementList.setLeft(statement());
            if (debug) printTree(statementList.getLeft());
            statementList.setRight(statementList());
        }
        return statementList;
    }

    private Lexeme statement() {
        if (functionCallPending()) return functionCall();
        else if (initializationPending()) return initialization();
        else if (incrementExpressionPending()) return incrementExpression();
        else if (assignmentPending()) return assignment();
        else if (functionDefinitionPending()) return functionDefinition();
        else if (loopPending()) return loop();
        else return conditional();
    }

    private Lexeme conditional() {
        if (ifElseStatementsPending()) return ifElseStatements();
        else return switchCaseStatements();
    }

    private Lexeme switchCaseStatements() {
        Lexeme switchCaseStatements = new Lexeme(SWITCH_CASE_STATEMENTS, currentLexeme.getLineNumber());
        switchCaseStatements.setLeft(switchStatement());

        Lexeme glue1 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        switchCaseStatements.setRight(glue1);
        glue1.setLeft(consume(OPENBRACE));
        Lexeme glue2 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        glue1.setRight(glue2);

        glue2.setLeft(caseStatement());
        glue2.setRight(consume(CLOSEBRACE));

        return switchCaseStatements;
    }

    private Lexeme caseStatement() {
        Lexeme caseStatement = new Lexeme(CASE_STATEMENT, currentLexeme.getLineNumber());

        if (check(CASE)) {
            caseStatement.setLeft(consume(CASE));
            Lexeme colon = consume(COLON);
            caseStatement.setRight(colon);
            colon.setLeft(expression());
            colon.setRight(statementList());
        } else {
            Lexeme colon = consume(COLON);
            caseStatement.setLeft(colon);
            colon.setLeft(consume(DEFAULT));
            colon.setRight(statementList());
        }

        return caseStatement;
    }

    private Lexeme switchStatement() {
        Lexeme switchStatement = new Lexeme(SWITCH_STATEMENT, currentLexeme.getLineNumber());
        Lexeme glue1 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        Lexeme glue2 = new Lexeme(GLUE, currentLexeme.getLineNumber());

        switchStatement.setLeft(consume(SWITCH));
        switchStatement.setRight(glue1);

        boolean parens = false;
        if (check(OPENPAREN)) {
            glue1.setLeft(consume(OPENPAREN));
            parens = true;
        }
        glue1.setRight(glue2);
        glue2.setLeft(expression());
        if (parens) {
            glue2.setRight(consume(CLOSEPAREN));
        }

        return switchStatement;
    }

    private Lexeme ifElseStatements() {
        Lexeme ifElseStatements = new Lexeme(IF_ELSE_STATEMENTS, currentLexeme.getLineNumber());
        Lexeme glue = new Lexeme(GLUE, currentLexeme.getLineNumber());

        ifElseStatements.setLeft(ifStatement());
        ifElseStatements.setRight(glue);
        if (elseIfStatementPending()) glue.setLeft(elseIfStatement());
        if (elseStatementPending()) glue.setRight(elseStatement());

        return ifElseStatements;
    }

    private Lexeme elseStatement() {
        Lexeme elseStatement = new Lexeme(ELSE_STATEMENT, currentLexeme.getLineNumber());

        elseStatement.setLeft(consume(ELSE));

        Lexeme glue1 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        elseStatement.setRight(glue1);
        glue1.setLeft(consume(OPENBRACE));
        Lexeme glue2 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        glue1.setRight(glue2);

        glue2.setLeft(statementList());
        glue2.setRight(consume(CLOSEBRACE));

        return elseStatement;
    }

    private Lexeme elseIfStatement() {
        Lexeme elseIfStatement = new Lexeme(ELSE_IF_STATEMENT, currentLexeme.getLineNumber());
        elseIfStatement.setLeft(consume(ELSE));
        elseIfStatement.setRight(ifStatement());

        return elseIfStatement;
    }

    private Lexeme ifStatement() {
        Lexeme ifStatement = new Lexeme(IF_STATEMENT, currentLexeme.getLineNumber());
        Lexeme glue0 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        Lexeme glue1 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        Lexeme glue2 = new Lexeme(GLUE, currentLexeme.getLineNumber());

        ifStatement.setLeft(glue0);
        glue0.setLeft(consume(IF));
        glue0.setRight(glue1);

        boolean parens = false;
        if (check(OPENPAREN)) {
            glue1.setLeft(consume(OPENPAREN));
            parens = true;
        }
        glue1.setRight(glue2);
        glue2.setLeft(booleanExpression());
        if (parens) {
            glue2.setRight(consume(CLOSEPAREN));
        }

        Lexeme glue3 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        ifStatement.setRight(glue3);
        glue3.setLeft(consume(OPENBRACE));
        Lexeme glue4 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        glue4.setRight(glue4);

        glue4.setLeft(statementList());
        glue4.setRight(consume(CLOSEBRACE));

        return ifStatement;
    }

    private Lexeme loop() {
        if (forLoopPending()) return forLoop();
        else if (forInPending()) return forIn();
        else return whileLoop();
    }

    private Lexeme whileLoop() {
        Lexeme whileLoop = new Lexeme(WHILE_LOOP, currentLexeme.getLineNumber());
        Lexeme glue0 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        Lexeme glue1 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        Lexeme glue2 = new Lexeme(GLUE, currentLexeme.getLineNumber());

        whileLoop.setLeft(glue0);
        glue0.setLeft(consume(WHILE));
        glue0.setRight(glue1);


        glue1.setLeft(consume(OPENPAREN));
        glue1.setRight(glue2);
        glue2.setLeft(booleanExpression());
        glue2.setRight(consume(CLOSEPAREN));

        Lexeme glue3 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        whileLoop.setRight(glue3);
        glue3.setLeft(consume(OPENBRACE));
        Lexeme glue4 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        glue4.setRight(glue4);

        glue4.setLeft(statementList());
        glue4.setRight(consume(CLOSEBRACE));

        return whileLoop;
    }

    private Lexeme forIn() {
        Lexeme forIn = new Lexeme(FOR_IN, currentLexeme.getLineNumber());
        Lexeme glue = new Lexeme(GLUE, currentLexeme.getLineNumber());
        Lexeme startFor = consume(FOR);
        Lexeme identifier = consume(IDENTIFIER);

        forIn.setLeft(identifier);
        identifier.setLeft(startFor);
        identifier.setRight(consume(IN));
        forIn.setRight(glue);

        glue.setLeft(iterable());

        Lexeme glue2 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        glue.setRight(glue2);
        glue2.setLeft(consume(OPENBRACE));
        Lexeme glue3 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        glue3.setRight(glue3);

        glue3.setLeft(statementList());
        glue3.setRight(consume(CLOSEBRACE));

        return forIn;
    }

    private Lexeme iterable() {
        if (rangePending()) return range();
        else return consume(IDENTIFIER);
    }

    private Lexeme range() {
        Lexeme ellipsis = consume(ELLIPSIS);

        ellipsis.setLeft(expression());
        ellipsis.setRight(expression());

        return ellipsis;
    }

    private Lexeme forLoop() {
        Lexeme forLoop = new Lexeme(FOR_LOOP, currentLexeme.getLineNumber());
        Lexeme glue = new Lexeme(GLUE, currentLexeme.getLineNumber());

        forLoop.setLeft(glue);
        glue.setLeft(consume(FOR));
        glue.setRight(consume(OPENPAREN));

        Lexeme assignment = null;
        if (assignmentPending()) assignment = assignment();
        Lexeme semi1 = consume(SEMICOLON);
        semi1.setLeft(assignment);
        forLoop.setRight(semi1);

        Lexeme booleanExpression = null;
        if (assignmentPending()) booleanExpression = booleanExpression();
        Lexeme semi2 = consume(SEMICOLON);
        semi2.setLeft(booleanExpression);
        semi1.setRight(semi2);

        Lexeme loopIncrement = null;
        if (assignmentPending()) loopIncrement = loopIncrement();
        Lexeme closeParen = consume(CLOSEPAREN);
        closeParen.setLeft(loopIncrement);
        semi2.setRight(closeParen);

        Lexeme glue2 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        closeParen.setRight(glue2);
        glue2.setLeft(consume(OPENBRACE));
        Lexeme glue3 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        glue3.setRight(glue3);

        glue3.setLeft(statementList());
        glue3.setRight(consume(CLOSEBRACE));

        return forLoop;
    }

    private Lexeme loopIncrement() {
        if (assignmentPending()) return assignment();
        else return incrementExpression();
    }

    private Lexeme booleanExpression() {
        if (simpleBooleanPending()) return simpleBoolean();
        else if (binaryBooleanPending()) return binaryBoolean();
        else return unaryBoolean();
    }

    private Lexeme binaryBoolean() {
        Lexeme leftBooleanExpression = booleanExpression();
        Lexeme conjunction = conjunction();
        conjunction.setLeft(leftBooleanExpression);
        conjunction.setRight(booleanExpression());

        return conjunction;
    }

    private Lexeme conjunction() {
        if (check(AND)) return consume(AND);
        else return consume(OR);
    }

    private Lexeme simpleBoolean() {
        Lexeme leftExpression = expression();
        Lexeme comparator = comparator();
        comparator.setLeft(leftExpression);
        comparator.setRight(expression());

        return comparator;
    }

    private Lexeme unaryBoolean() {
        Lexeme unaryBoolean = new Lexeme(UNARY_BOOLEAN, currentLexeme.getLineNumber());
        if (check(IDENTIFIER)) unaryBoolean.setLeft(consume(IDENTIFIER));
        else if (booleanLiteralPending()) unaryBoolean.setLeft(booleanLiteral());
        else if (check(NOT)) {
            unaryBoolean.setLeft(consume(NOT));
            unaryBoolean.setRight(booleanExpression());
        } else {
            Lexeme glue = new Lexeme(GLUE, currentLexeme.getLineNumber());
            unaryBoolean.setLeft(consume(OPENPAREN));
            unaryBoolean.setRight(glue);
            glue.setLeft(booleanExpression());
            glue.setRight(consume(CLOSEPAREN));
        }
        return unaryBoolean;
    }

    private Lexeme functionDefinition() {
        Lexeme functionDefinition = new Lexeme(FUNCTION_DEFINITION, currentLexeme.getLineNumber());
        Lexeme func = consume(FUNC);
        Lexeme identifier = consume(IDENTIFIER);

        functionDefinition.setLeft(identifier);
        identifier.setLeft(func);
        identifier.setRight(consume(CLOSEPAREN));

        Lexeme glue1 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        functionDefinition.setRight(glue1);

        if (functionParameterListPending()) glue1.setLeft(functionParameterList());
        Lexeme endDef = consume(CLOSEPAREN);
        glue1.setRight(endDef);
        if (check(RETURNS)) {
            Lexeme glue2 = new Lexeme(GLUE, currentLexeme.getLineNumber());
            endDef.setLeft(glue2);
            glue2.setLeft(consume(RETURNS));
            glue2.setRight(functionReturnType());
        }

        Lexeme glue3 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        endDef.setRight(glue3);
        glue3.setLeft(consume(OPENBRACE));
        Lexeme glue4 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        glue3.setRight(glue4);

        glue4.setLeft(statementList());
        glue4.setRight(consume(CLOSEBRACE));

        return functionDefinition;
    }

    private Lexeme functionReturnType() {
        if (dataTypePending()) return dataType();
        else return consume(VOID);
    }

    private Lexeme functionParameterList() {
        Lexeme functionParameterList = new Lexeme(FUNCTION_PARAMETER_LIST, currentLexeme.getLineNumber());
        functionParameterList.setLeft(functionParameter());
        if (check(COMMA)) {
            Lexeme glue1 = new Lexeme(GLUE, currentLexeme.getLineNumber());
            functionParameterList.setRight(glue1);
            glue1.setLeft(consume(COMMA));
            glue1.setRight(functionParameterList());
        }

        return functionParameterList;
    }

    private Lexeme functionParameter() {
        Lexeme functionParameter = new Lexeme(FUNCTION_PARAMETER, currentLexeme.getLineNumber());
        functionParameter.setLeft(consume(IDENTIFIER));
        Lexeme glue1 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        functionParameter.setRight(glue1);

        glue1.setLeft(consume(COLON));
        glue1.setRight(dataType());

        return functionParameter;
    }

    private Lexeme assignment() {
        Lexeme leftSide = arrayReferencePending() ? arrayReference() : consume(IDENTIFIER);
        Lexeme assignmentOperator = assignmentOperator();
        assignmentOperator.setLeft(leftSide);
        assignmentOperator.setRight(expression());

        return assignmentOperator;
    }

    private Lexeme assignmentOperator() {
        if (check(ASSIGN)) return consume(ASSIGN);
        else if (check(PLUSASSIGN)) return consume(PLUSASSIGN);
        else if (check(MINUSASSIGN)) return consume(MINUSASSIGN);
        else if (check(TIMESASSIGN)) return consume(TIMESASSIGN);
        else if (check(DIVIDEASSIGN)) return consume(DIVIDEASSIGN);
        else if (check(MODASSIGN)) return consume(MODASSIGN);
        else return consume(EXPASSIGN);
    }

    private Lexeme initialization() {
        if (variableInitializerPending()) return variableInitializer();
        else return constantInitializer();
    }

    private Lexeme constantInitializer() {
        Lexeme constantInitializer = new Lexeme(TokenType.CONSTANT_INITIALIZER, currentLexeme.getLineNumber());
        Lexeme glue1 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        constantInitializer.setLeft(glue1);
        glue1.setLeft(consume(CONST));
        glue1.setRight(consume(IDENTIFIER));

        Lexeme glue2 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        constantInitializer.setRight(glue2);

        if (check(COLON)) {
            Lexeme glue3 = new Lexeme(GLUE, currentLexeme.getLineNumber());
            glue2.setLeft(glue3);
            glue3.setLeft(consume(COLON));
            glue3.setRight(dataType());
        }

        Lexeme glue4 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        glue2.setRight(glue4);
        glue4.setLeft(consume(ASSIGN));
        glue4.setRight(initializerExpression());

        return constantInitializer;
    }

    private Lexeme variableInitializer() {
        Lexeme variableInitializer = new Lexeme(VARIABLE_INITIALIZER, currentLexeme.getLineNumber());
        Lexeme var = consume(VAR);
        Lexeme identifier = consume(IDENTIFIER);

        Lexeme glue1 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        variableInitializer.setRight(glue1);

        if (check(COLON)) {
            variableInitializer.setLeft(identifier);
            identifier.setLeft(var);
            identifier.setRight(consume(COLON));
            glue1.setLeft(dataType());
            if (check(ASSIGN)) {
                Lexeme glue2 = new Lexeme(GLUE, currentLexeme.getLineNumber());
                glue1.setRight(glue2);
                glue2.setLeft(consume(ASSIGN));
                glue2.setRight(initializerExpression());
            }
        } else {
            Lexeme glue3 = new Lexeme(GLUE, currentLexeme.getLineNumber());
            variableInitializer.setLeft(glue3);
            glue3.setLeft(var);
            glue3.setRight(identifier);

            if (check(ASSIGN)) {
                glue1.setLeft(consume(ASSIGN));
                glue1.setRight(initializerExpression());

            } else {
                Z.error(currentLexeme.getLineNumber(), "Variable declaration without type or initialization");
            }
        }
        return variableInitializer;
    }

    private Lexeme initializerExpression() {
        if (expressionPending()) return expression();
        else return arrayInitializer();
    }

    private Lexeme arrayInitializer() {
        Lexeme arrayInitializer = new Lexeme(ARRAY_INITIALIZER, currentLexeme.getLineNumber());

        arrayInitializer.setLeft(consume(OPENBRACKET));
        Lexeme glue = new Lexeme(GLUE, currentLexeme.getLineNumber());
        arrayInitializer.setRight(glue);
        if (expressionListPending()) glue.setLeft(expressionList());
        glue.setRight(consume(CLOSEBRACKET));

        return arrayInitializer;
    }

    private Lexeme dataType() {
        if (check(KW_STRING)) return consume(KW_STRING);
        else if (check(KW_INT)) return consume(KW_INT);
        else if (check(KW_FLOAT)) return consume(KW_FLOAT);
        else return arrayType();
    }

    private Lexeme arrayType() {
        Lexeme arrayType = new Lexeme(ARRAY_TYPE, currentLexeme.getLineNumber());

        arrayType.setLeft(consume(OPENBRACKET));
        Lexeme glue = new Lexeme(GLUE, currentLexeme.getLineNumber());
        arrayType.setRight(glue);

        glue.setLeft(dataType());
        glue.setRight(consume(CLOSEBRACKET));

        return arrayType;
    }

    private Lexeme expressionList() {
        Lexeme glue = new Lexeme(GLUE, currentLexeme.getLineNumber());
        Lexeme comma = consume(COMMA);
        Lexeme expressionList = expressionList();

        expressionList.setLeft(expression());
        if (check(COMMA)) {
            glue.setLeft(comma);
            glue.setRight(expressionList);
        }

        return expressionList;
    }

    private Lexeme expression() {
        if (primaryPending()) return primary();
        else if (unaryPending()) return unary();
        else return binary();
    }

    private Lexeme binary() {
        Lexeme leftExpression = expression();
        Lexeme binaryOperator = binaryOperator();

        binaryOperator.setLeft(leftExpression);
        binaryOperator.setRight(expression());
        return binaryOperator;
    }

    private Lexeme binaryOperator() {
        if (comparatorPending()) return comparator();
        else return mathematicalOperator();
    }

    private Lexeme mathematicalOperator() {
        if (check(PLUS)) return consume(PLUS);
        else if (check(MINUS)) return consume(MINUS);
        else if (check(TIMES)) return consume(TIMES);
        else if (check(DIVIDE)) return consume(DIVIDE);
        else if (check(EXP)) return consume(EXP);
        else return consume(MOD);
    }

    private Lexeme comparator() {
        if (check(GREATER)) return consume(GREATER);
        else if (check(GREATEREQUAL)) return consume(GREATEREQUAL);
        else if (check(LESS)) return consume(LESS);
        else if (check(LESSEQUAL)) return consume(LESSEQUAL);
        else if (check(NOTEQUAL)) return consume(NOTEQUAL);
        else return consume(EQUAL);
    }

    private Lexeme unary() {
        if (prefixUnaryOperatorsPending()) {
            Lexeme unary = new Lexeme(UNARY, currentLexeme.getLineNumber());
            unary.setLeft(prefixUnaryOperators());
            unary.setRight(expression());

            return unary;

        } else return incrementExpression();
    }

    private Lexeme incrementExpression() {
        Lexeme incrementExpression = new Lexeme(INCREMENT_EXPRESSION, currentLexeme.getLineNumber());
        if (check(IDENTIFIER)) {
            incrementExpression.setLeft(consume(IDENTIFIER));
            if (check(INCREMENT)) incrementExpression.setRight(consume(INCREMENT));
            else incrementExpression.setRight(consume(DECREMENT));
        } else {
            if (check(INCREMENT)) incrementExpression.setLeft(consume(INCREMENT));
            else incrementExpression.setLeft(consume(DECREMENT));
            incrementExpression.setRight(consume(IDENTIFIER));
        }
        return incrementExpression;
    }

    private Lexeme prefixUnaryOperators() {
        if (check(PLUS)) return consume(PLUS);
        else if (check(MINUS)) return consume(MINUS);
        else return consume(NOT);
    }

    private Lexeme primary() {
        if (literalPending()) return literal();
        else if (groupingPending()) return grouping();
        else if (functionCallPending()) return functionCall();
        else if (arrayReferencePending()) return arrayReference();
        else return consume(IDENTIFIER);
    }

    private Lexeme arrayReference() {
        Lexeme arrayReference = new Lexeme(ARRAY_REFERENCE, currentLexeme.getLineNumber());
        arrayReference.setLeft(consume(IDENTIFIER));
        Lexeme expression = expression();
        arrayReference.setRight(expression);

        expression.setLeft(consume(OPENBRACKET));
        expression.setRight(consume(CLOSEBRACKET));

        return arrayReference;
    }

    private Lexeme functionCall() {
        Lexeme functionCall = new Lexeme(FUNCTION_CALL, currentLexeme.getLineNumber());
        Lexeme glue1 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        functionCall.setLeft(glue1);
        glue1.setLeft(consume(IDENTIFIER));
        glue1.setRight(consume(OPENPAREN));

        Lexeme glue2 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        functionCall.setRight(glue2);
        if (argumentListPending()) glue2.setLeft(argumentList());
        glue2.setRight(consume(CLOSEPAREN));

        return functionCall;
    }

    private Lexeme argumentList() {
        Lexeme argumentList = new Lexeme(ARGUMENT_LIST, currentLexeme.getLineNumber());

        if (check(IDENTIFIER)) {
            Lexeme glue1 = new Lexeme(GLUE, currentLexeme.getLineNumber());
            argumentList.setLeft(glue1);
            glue1.setLeft(consume(IDENTIFIER));
            glue1.setRight(consume(COLON));
        }

        Lexeme glue2 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        argumentList.setRight(glue2);
        glue2.setLeft(expression());

        if (check(COMMA)) {
            Lexeme glue3 = new Lexeme(GLUE, currentLexeme.getLineNumber());
            glue2.setRight(glue3);
            glue3.setLeft(consume(COMMA));
            glue3.setRight(argumentList());
        }

        return argumentList;
    }

    private Lexeme grouping() {
        Lexeme openParen = consume(OPENPAREN);
        Lexeme expression = expression();
        expression.setLeft(openParen);
        expression.setRight(consume(CLOSEPAREN));

        return expression;
    }

    private Lexeme literal() {
        if (check(INT)) return consume(INT);
        else if (check(FLOAT)) return consume(FLOAT);
        else if (booleanLiteralPending()) return booleanLiteral();
        else return consume(STRING);
    }

    private Lexeme booleanLiteral() {
        if (check(TRUE)) return consume(TRUE);
        else return consume(FALSE);
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
        return functionCallPending()
                || initializationPending()
                || incrementExpressionPending()
                || assignmentPending()
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
        return arrayReferencePending() ||
                (check(IDENTIFIER)
                        && (checkNext(ASSIGN)
                        || checkNext(PLUSASSIGN)
                        || checkNext(MINUSASSIGN)
                        || checkNext(TIMESASSIGN)
                        || checkNext(DIVIDEASSIGN)
                        || checkNext(MODASSIGN)
                        || checkNext(EXPASSIGN)));
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
                || check(IDENTIFIER);
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


    public static void printTree(Lexeme root) {
        String printableTree = getPrintableTree(root, 1);
        System.out.println(printableTree);
    }

    private static String getPrintableTree(Lexeme root, int level) {
        String treeString = root.toSimpleString();

        StringBuilder spacer = new StringBuilder("\n");
        spacer.append(String.join("", Collections.nCopies(level, "\t")));

        if (root.getLeft() != null)
            treeString += spacer + "with left child: " + getPrintableTree(root.getLeft(), level + 1);
        if (root.getRight() != null)
            treeString += spacer + "and right child: " + getPrintableTree(root.getRight(), level + 1);

        return treeString;
    }

}
