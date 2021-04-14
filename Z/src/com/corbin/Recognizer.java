package com.corbin;

import java.util.ArrayList;
import java.util.Collections;

import static com.corbin.TokenType.*;

public class Recognizer {
    private static final boolean debug = true;

    // ---------- Instance Variables ----------
    ArrayList<Lexeme> lexemes;
    private int nextLexemeIndex = 0;
    private Lexeme currentLexeme;
    private Lexeme left;
    private Lexeme right;

    // ---------- Constructor ----------
    public Recognizer(ArrayList<Lexeme> lexemes) {
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
        if (check(expected)) advance();
        else {
            Z.error(currentLexeme, "Expected " + expected + " but found " + currentLexeme);
        }
        return null;
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
        if (statementListPending()) return statementList();
    }

    private Lexeme statementList() {    // TODO (CHECK)
        while (statementPending()) return statement();
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
        } else if (check(OPENPAREN)) {
            consume(OPENPAREN);
            booleanExpression();
            consume(CLOSEPAREN);
        }
    }

    private Lexeme functionDefinition() {
        Lexeme functionDef = functionDefinition();
        Lexeme glue = consume(GLUE);
        Lexeme identifier = consume(IDENTIFIER);
        Lexeme statementList = statementList();

        functionDef.setLeft(identifier);
        functionDef.setRight(glue);

        identifier.setLeft(consume(FUNC));
        identifier.setRight(consume(OPENPAREN));

        if (functionParameterListPending()) return functionParameterList();
        else return glue;

        glue.setLeft(consume(CLOSEPAREN));
        glue.setRight(glue);

        if (functionReturnTypePending()) {
            glue.setLeft(glue);
            glue.setLeft(consume(RETURNS));
            glue.setRight(functionReturnType());
        }

        glue.setRight(statementList);
        statementList.setLeft(consume(OPENBRACE));
        statementList.setRight(consume(CLOSEBRACE));

        return functionDef;
    }

    private Lexeme functionReturnType() {
        if (dataTypePending()) return dataType();
        else return consume(VOID);
    }

    private Lexeme functionParameterList() {
        Lexeme funcParamList = functionParameterList();
        Lexeme glue = consume(GLUE);

        funcParamList.setLeft(functionParameter());
        funcParamList.setRight(glue);
        glue.setLeft(consume(COMMA));
        glue.setRight(functionParameterList());

        return funcParamList;
    }

    private Lexeme functionParameter() {
        Lexeme funcParam = functionParameter();
        Lexeme glue = consume(GLUE);

        funcParam.setLeft(consume(IDENTIFIER));
        funcParam.setRight(glue);
        glue.setLeft(consume(COLON));
        glue.setRight(consume(dataType()));

        return funcParam;
    }

    private Lexeme assignment() {
        Lexeme identifier = consume(IDENTIFIER);
        Lexeme assignmentOperator = assignmentOperator();
        Lexeme expression = expression();

        if (arrayReferencePending()) {
            assignmentOperator.setLeft(arrayReference());
        } else {
            assignmentOperator.setLeft(identifier);
        }
        assignmentOperator.setRight(expression);

        return assignmentOperator;
    }

    private Lexeme assignmentOperator() {
        if (debug) System.out.println("-- assignmentOperator --");
        if (check(ASSIGN)) consume(ASSIGN);
        else if (check(PLUSASSIGN)) consume(PLUSASSIGN);
        else if (check(MINUSASSIGN)) consume(MINUSASSIGN);
        else if (check(TIMESASSIGN)) consume(TIMESASSIGN);
        else if (check(DIVIDEASSIGN)) consume(DIVIDEASSIGN);
        else if (check(MODASSIGN)) consume(MODASSIGN);
        else if (check(EXPASSIGN)) consume(EXPASSIGN);
        return null;
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

    private Lexeme dataType() {

        if (check(KW_STRING)) return consume(KW_STRING);
        else if (check(KW_INT)) return consume(KW_INT);
        else if (check(KW_FLOAT)) return consume(KW_FLOAT);
        else return arrayType();
    }

    private Lexeme arrayType() {
        Lexeme glue = consume(GLUE);

        arrayType().setLeft(consume(OPENBRACKET));
        arrayType().setRight(glue);

        glue.setLeft(dataType());
        glue.setRight(consume(CLOSEBRACKET));

        return arrayType();
    }

    private Lexeme expressionList() {
        Lexeme glue = consume(GLUE);
        Lexeme comma = consume(COMMA);
        Lexeme expressionList = expressionList();

        if (expressionPending()) return expression();
        if (check(COMMA)) {
            glue.setLeft(comma);
            glue.setRight(expressionList);
        }
    }

    private Lexeme expression() {
        if (primaryPending()) return primary();
        else if (unaryPending()) return unary();
        else return binary();
    }

    private Lexeme binary() {
        Lexeme binaryOperator = binaryOperator();

        binaryOperator.setLeft(expression());
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
        else if (check(EQUAL)) return consume(EQUAL);
    }

    private Lexeme unary() {
        if (prefixUnaryOperatorsPending()) {
            unary().setLeft(prefixUnaryOperators());
            unary().setRight(expression());
            return unary();
        } else return incrementExpression();
    }

    private Lexeme incrementExpression() {
        incrementExpression().setLeft(consume(IDENTIFIER));
        if (check(INCREMENT)) incrementExpression().setRight(consume(INCREMENT));
        else incrementExpression().setRight(consume(DECREMENT));
        return incrementExpression();
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
        else consume(IDENTIFIER); // TODO IS THIS RIGHT ?!
    }

    private Lexeme arrayReference() {
        Lexeme identifier = consume(IDENTIFIER);
        Lexeme expression = expression();
        Lexeme openBracket = consume(OPENBRACKET);
        Lexeme closeBracket = consume(CLOSEBRACKET);


        arrayReference().setLeft(identifier);
        arrayReference().setRight(expression);

        expression.setLeft(openBracket);
        expression.setRight(closeBracket);

        return arrayReference();
    }

    private Lexeme functionCall() {
        Lexeme glue = consume(GLUE);

        functionCall().setLeft(glue);
        glue.setLeft(consume(IDENTIFIER));
        glue.setRight(consume(OPENPAREN));
        functionCall().setRight(glue);
        if (argumentListPending()) glue.setLeft(argumentList());
        glue.setRight(consume(CLOSEPAREN));

        return functionCall();
    }

    private Lexeme argumentList() {
        Lexeme glue = consume(GLUE);

        if (check(IDENTIFIER)) {
            argumentList().setLeft(glue);
            glue.setLeft(consume(IDENTIFIER));
            glue.setRight(consume(COLON));
        }

        argumentList().setRight(glue);
        glue.setLeft(expression());
        if (check(COMMA)) {
            glue.setRight(glue);
            glue.setLeft(consume(COMMA));
            glue.setRight(argumentList());
        }

    }

    private void grouping() {
        if (debug) System.out.println("-- grouping --");
        consume(OPENPAREN);
        expression();
        consume(CLOSEPAREN);
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
