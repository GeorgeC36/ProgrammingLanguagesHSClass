package com.corbin;

import java.util.ArrayList;
import java.util.Collections;

import static com.corbin.TokenType.*;

public class Parser {
    private static final boolean debug = true;

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
        else return null;
    }

    private Lexeme statementList() {    // TODO (CHECK)
        if (statementPending()) return statement();
        else return statementList();
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
        switchCaseStatements().setLeft(switchStatement());
        switchCaseStatements().setRight(caseStatement());
        caseStatement().setLeft(consume(OPENBRACE));
        caseStatement().setRight(consume(CLOSEBRACE));

        return switchCaseStatements();
    }

    private Lexeme caseStatement() {
        Lexeme switchStatement = switchStatement();
        Lexeme openBrace = consume(OPENBRACE);
        Lexeme caseStatement = caseStatement();
        Lexeme closeBrace = consume(CLOSEBRACE);
        Lexeme colon = consume(COLON);

        if (check(CASE)) {
            caseStatement().setLeft(consume(CASE));
            caseStatement().setRight(consume(COLON));
            colon.setLeft(expression());
            colon.setRight(statementList());
            return caseStatement();
        } else {
            colon.setLeft(consume(DEFAULT));
            colon.setRight(statementList());
            return colon;
        }
    }

    private Lexeme switchStatement() {
        switchStatement().setLeft(consume(SWITCH));
        switchStatement().setRight(expression());
        if (checkNext(OPENPAREN)) {
            expression().setLeft(consume(OPENPAREN));
            expression().setLeft(consume(CLOSEPAREN));
        }
        return switchStatement();
    }

    private Lexeme ifElseStatements() {
        Lexeme glue = new Lexeme(GLUE, 0);

        ifElseStatements().setLeft(ifStatement());
        ifElseStatements().setRight(glue);
        if (elseIfStatementPending()) glue.setLeft(elseIfStatement());
        if (elseStatementPending()) glue.setRight(elseStatement());

        return ifElseStatements();
    }

    private Lexeme elseStatement() {
        elseStatement().setLeft(consume(ELSE));
        elseStatement().setRight(statementList());

        statementList().setLeft(consume(OPENBRACE));
        statementList().setRight(consume(CLOSEBRACE));

        return elseStatement();
    }

    private Lexeme elseIfStatement() {
        elseIfStatement().setLeft(consume(ELSE));
        elseIfStatement().setRight(ifStatement());

        return elseIfStatement();
    }

    private Lexeme ifStatement() {
        Lexeme glue = new Lexeme(GLUE, 0);
        Lexeme statementList = statementList();
        Lexeme booleanExpression = booleanExpression();

        ifStatement().setLeft(glue);
        ifStatement().setRight(statementList);

        statementList.setLeft(consume(OPENBRACE));
        statementList.setRight(consume(CLOSEBRACE));

        glue.setLeft(consume(IF));
        glue.setRight(booleanExpression);

        if (checkNext(OPENPAREN)) {
            booleanExpression.setLeft(consume(OPENPAREN));
            booleanExpression.setRight(consume(CLOSEPAREN));
        }

        return ifStatement();
    }

    private Lexeme loop() {
        if (forLoopPending()) return forLoop();
        else if (forInPending()) return forIn();
        else return whileLoop();
    }

    private Lexeme whileLoop() {
        Lexeme glue = new Lexeme(GLUE, 0);
        Lexeme booleanExpression = booleanExpression();
        Lexeme statementList = statementList();

        whileLoop().setLeft(glue);
        whileLoop().setRight(statementList);

        statementList.setLeft(consume(OPENBRACE));
        statementList.setRight(consume(CLOSEBRACE));

        glue.setLeft(consume(WHILE));
        glue.setRight(booleanExpression);

        booleanExpression.setLeft(consume(OPENPAREN));
        booleanExpression.setRight(consume(CLOSEPAREN));

        return whileLoop();
    }

    private Lexeme forIn() {
        Lexeme glue = new Lexeme(GLUE, 0);
        Lexeme identifier = consume(IDENTIFIER);
        Lexeme statementList = statementList();

        forIn().setLeft(identifier);
        forIn().setRight(glue);

        identifier.setLeft(consume(FOR));
        identifier.setRight(consume(IN));

        glue.setLeft(iterable());
        glue.setRight(statementList);

        statementList.setLeft(consume(OPENBRACE));
        statementList.setRight(consume(CLOSEBRACE));

        return forIn();
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
        Lexeme glue = new Lexeme(GLUE, 0);
        Lexeme semi = consume(SEMICOLON);
        Lexeme cParen = consume(CLOSEPAREN);

        forLoop().setLeft(glue);
        forLoop().setRight(semi);
        if (assignmentPending()) semi.setLeft(assignment());
        semi.setRight(semi);
        if (booleanExpressionPending()) semi.setLeft(booleanExpression());
        semi.setRight(consume(CLOSEPAREN));
        if (loopIncrementPending()) cParen.setLeft(loopIncrement());
        cParen.setRight(statementList());
        statementList().setLeft(consume(CLOSEBRACE));
        statementList().setRight(consume(OPENBRACE));

        return forLoop();
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
        conjunction().setLeft(booleanExpression());
        conjunction().setRight(booleanExpression());

        return conjunction();
    }

    private Lexeme conjunction() {
        if (check(AND)) return consume(AND);
        else return consume(OR);
    }

    private Lexeme simpleBoolean() {
        comparator().setLeft(expression());
        comparator().setRight(expression());

        return comparator();
    }

    private Lexeme unaryBoolean() {
        if (check(IDENTIFIER)) return consume(IDENTIFIER);
        else if (booleanLiteralPending()) return booleanLiteral();
        else if (check(NOT)) {
            unaryBoolean().setLeft(consume(NOT));
            unaryBoolean().setRight(booleanExpression());
            return unaryBoolean();
        } else {
            booleanExpression().setLeft(consume(OPENPAREN));
            booleanExpression().setRight(consume(CLOSEPAREN));
            return booleanExpression();
        }
    }

    private Lexeme functionDefinition() {
        Lexeme functionDef = functionDefinition();
        Lexeme glue = new Lexeme(GLUE, 0);
        Lexeme identifier = consume(IDENTIFIER);
        Lexeme statementList = statementList();

        functionDef.setLeft(identifier);
        functionDef.setRight(glue);

        identifier.setLeft(consume(FUNC));
        identifier.setRight(consume(OPENPAREN));

        if (functionParameterListPending()) glue.setLeft(functionParameterList());

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
        Lexeme glue = new Lexeme(GLUE, 0);

        funcParamList.setLeft(functionParameter());
        funcParamList.setRight(glue);
        glue.setLeft(consume(COMMA));
        glue.setRight(functionParameterList());

        return funcParamList;
    }

    private Lexeme functionParameter() {
        Lexeme funcParam = functionParameter();
        Lexeme glue = new Lexeme(GLUE, 0);

        funcParam.setLeft(consume(IDENTIFIER));
        funcParam.setRight(glue);
        glue.setLeft(consume(COLON));
        glue.setRight(dataType());

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
        Lexeme glue = new Lexeme(GLUE, 0);

        constantInitializer().setLeft(glue);
        glue.setLeft(consume(CONST));
        glue.setRight(consume(IDENTIFIER));

        constantInitializer().setRight(glue);
        if (check(COLON)) {
            glue.setLeft(glue);
            glue.setLeft(consume(COLON));
            glue.setRight(dataType());
        }
        glue.setRight(glue);
        glue.setLeft(consume(ASSIGN));
        glue.setRight(initializerExpression());

        return constantInitializer();
    }

    private Lexeme variableInitializer() {
        Lexeme glue = new Lexeme(GLUE, 0);
        Lexeme glue2 = new Lexeme(GLUE, 0);
        Lexeme result = new Lexeme(GLUE, 0);
        Lexeme var = consume(VAR);
        Lexeme identifier = consume(IDENTIFIER);
        Lexeme colon = consume(COLON);
        Lexeme assign = consume(ASSIGN);

        result.setRight(glue);

        if (check(COLON)) {
            result.setLeft(identifier);

            identifier.setLeft(var);
            identifier.setRight(colon);

            glue.setLeft(dataType());
            glue.setRight(glue2);

            glue.setLeft(assign);
            glue.setRight(initializerExpression());

            return result;

        } else {
            result.setLeft(glue);
            glue.setLeft(var);
            glue.setRight(identifier);

            result.setRight(glue2);
            glue.setLeft(assign);
            glue.setRight(initializerExpression());

            return result;
        }
    }

    private Lexeme initializerExpression() {
        if (expressionPending()) return expression();
        else return arrayInitializer();
    }

    private Lexeme arrayInitializer() {
        Lexeme glue = new Lexeme(GLUE, 0);

        arrayInitializer().setLeft(consume(OPENBRACKET));
        arrayInitializer().setRight(glue);
        if (expressionListPending()) glue.setLeft(expressionList());
        glue.setRight(consume(CLOSEBRACKET));

        return arrayInitializer();
    }

    private Lexeme dataType() {
        if (check(KW_STRING)) return consume(KW_STRING);
        else if (check(KW_INT)) return consume(KW_INT);
        else if (check(KW_FLOAT)) return consume(KW_FLOAT);
        else return arrayType();
    }

    private Lexeme arrayType() {
        Lexeme glue = new Lexeme(GLUE, 0);

        arrayType().setLeft(consume(OPENBRACKET));
        arrayType().setRight(glue);

        glue.setLeft(dataType());
        glue.setRight(consume(CLOSEBRACKET));

        return arrayType();
    }

    private Lexeme expressionList() {
        Lexeme glue = new Lexeme(GLUE, 0);
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
        else return consume(EQUAL);
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
        else return consume(IDENTIFIER);
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
        Lexeme glue = new Lexeme(GLUE, 0);

        functionCall().setLeft(glue);
        glue.setLeft(consume(IDENTIFIER));
        glue.setRight(consume(OPENPAREN));
        functionCall().setRight(glue);
        if (argumentListPending()) glue.setLeft(argumentList());
        glue.setRight(consume(CLOSEPAREN));

        return functionCall();
    }

    private Lexeme argumentList() {
        Lexeme glue = new Lexeme(GLUE, 0);

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
        return argumentList();
    }

    private Lexeme grouping() {
        Lexeme expression = expression();
        expression.setLeft(consume(OPENPAREN));
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
