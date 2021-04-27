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

//        if (debug) System.out.println("    -- check: looking for " + expected + ", have " + currentLexeme + " --");
        return currentLexeme.getType() == expected;
    }

    private Lexeme consume(TokenType expected) {
//        if (debug) System.out.println("-- consume " + expected + " --");
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
//            if (debug)
//                System.out.println("    -- checkNext: looking for " + type + ", next is " + lexemes.get(nextLexemeIndex).getType() + " --");
            return lexemes.get(nextLexemeIndex).getType() == type;
        }
    }

    private void advance() {
        do {
            currentLexeme = lexemes.get(nextLexemeIndex);
            nextLexemeIndex++;
        } while (currentLexeme.getType() == LINECOMMENT);
//        if (debug) System.out.println("-- advance: currentLexeme = " + currentLexeme + " --");
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
        else if (inputStatementPending()) return inputStatement();
        else if (outputStatementPending()) return outputStatement();
        else if (returnStatementPending()) return returnStatement();
        else return conditional();
    }

    private Lexeme returnStatement() {
	Lexeme ret = consume(RETURN);
	ret.setLeft(expression());
	return ret;
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
        Lexeme prior = glue1;

        while (caseStatementPending()) {
            Lexeme glue2 = new Lexeme(GLUE, currentLexeme.getLineNumber());
            prior.setRight(glue2);
            glue2.setLeft(caseStatement());
            prior = glue2;
        }
        prior.setRight(consume(CLOSEBRACE));

        return switchCaseStatements;
    }

    private Lexeme caseStatement() {
        Lexeme caseStatement = new Lexeme(CASE_STATEMENT, currentLexeme.getLineNumber());

        if (check(CASE)) {
            caseStatement.setLeft(consume(CASE));
            Lexeme expression = expression();
            Lexeme colon = consume(COLON);
            caseStatement.setRight(colon);
            colon.setLeft(expression);
            colon.setRight(statementList());
        } else {
            Lexeme defaultCase = consume(DEFAULT);
            Lexeme colon = consume(COLON);
            caseStatement.setLeft(colon);
            colon.setLeft(defaultCase);
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
        ifElseStatements.setLeft(ifStatement());

        Lexeme node = ifElseStatements;
        while (elseIfStatementPending()) {
            Lexeme glue = new Lexeme(GLUE, currentLexeme.getLineNumber());
            node.setRight(glue);
            node = glue;
            node.setLeft(elseIfStatement());
        }

        if (elseStatementPending()) node.setRight(elseStatement());
        return ifElseStatements;
    }

    private Lexeme elseStatement() {
	// only the statement list is needed by Evaluator.  No need to store other tokens.
        consume(ELSE);
        consume(OPENBRACE);
        Lexeme statementList = statementList();	
        consume(CLOSEBRACE);
        return statementList;
    }

    private Lexeme elseIfStatement() {
        consume(ELSE);
        return ifStatement();
    }

    private Lexeme ifStatement() {
	// only the expression and statement list are needed by Evaluator.  No need to store other tokens.
        Lexeme ifStatement = new Lexeme(IF_STATEMENT, currentLexeme.getLineNumber());
        consume(IF);
        
        boolean parens = check(OPENPAREN);
        if (parens) consume(OPENPAREN);       
        ifStatement.setLeft(expression());
        if (parens) consume(CLOSEPAREN);
      
        consume(OPENBRACE);
        ifStatement.setRight(statementList());
        consume(CLOSEBRACE);

        return ifStatement;
    }

    private Lexeme loop() {
        Lexeme loop = new Lexeme(LOOP, currentLexeme.getLineNumber());
        if (forLoopPending()) loop.setLeft(forLoop());
        else if (forInPending()) loop.setLeft(forIn());
        else loop.setLeft(whileLoop());
        return loop;
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
        glue2.setLeft(expression());
        glue2.setRight(consume(CLOSEPAREN));

        Lexeme glue3 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        whileLoop.setRight(glue3);
        glue3.setLeft(consume(OPENBRACE));
        Lexeme glue4 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        glue3.setRight(glue4);

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
        glue2.setRight(glue3);

        glue3.setLeft(statementList());
        glue3.setRight(consume(CLOSEBRACE));

        return forIn;
    }

    private Lexeme iterable() {
        if (rangePending()) return range();
        else return consume(IDENTIFIER);
    }

    private Lexeme range() {
        Lexeme startingValue = expression();
        Lexeme ellipsis = consume(ELLIPSIS);

        ellipsis.setLeft(startingValue);
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
        // make it an initialization
        Lexeme initialization = new Lexeme(INITIALIZATION, currentLexeme.getLineNumber());
        Lexeme variableInitializer = new Lexeme(VARIABLE_INITIALIZER, currentLexeme.getLineNumber());
        initialization.setLeft(variableInitializer);
        Lexeme viGlue1 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        viGlue1.setRight(assignment.getLeft().getLeft());
        variableInitializer.setLeft(viGlue1);
        Lexeme viGlue2 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        viGlue2.setLeft(new Lexeme(assignment.getLeft().getType(), assignment.getLeft().getLineNumber()));
        viGlue2.setRight(assignment.getLeft().getRight());
        variableInitializer.setRight(viGlue2);
        Lexeme semi1 = consume(SEMICOLON);
        semi1.setLeft(initialization);
        forLoop.setRight(semi1);

        Lexeme booleanExpression = null;
        if (booleanExpressionPending()) booleanExpression = expression();
        Lexeme semi2 = consume(SEMICOLON);
        semi2.setLeft(booleanExpression);
        semi1.setRight(semi2);

        Lexeme loopIncrement = null;
        if (loopIncrementPending()) loopIncrement = loopIncrement();
        Lexeme closeParen = consume(CLOSEPAREN);
        closeParen.setLeft(loopIncrement);
        semi2.setRight(closeParen);

        Lexeme glue2 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        closeParen.setRight(glue2);
        glue2.setLeft(consume(OPENBRACE));
        Lexeme glue3 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        glue2.setRight(glue3);

        glue3.setLeft(statementList());
        glue3.setRight(consume(CLOSEBRACE));

        return forLoop;
    }

    private Lexeme loopIncrement() {
        if (assignmentPending()) return assignment();
        else return incrementExpression();
    }
    
    private Lexeme inputStatement() {
	Lexeme inputStatement = new Lexeme(INPUT_STATEMENT, currentLexeme.getLineNumber());
	inputStatement.setLeft(consume(INPUT));
	inputStatement.setRight(variable());
	return inputStatement;
    }
    
    private Lexeme outputStatement() {
	Lexeme outputStatement = new Lexeme(OUTPUT_STATEMENT, currentLexeme.getLineNumber());
	outputStatement.setLeft(consume(OUTPUT));
	outputStatement.setRight(expression());
	return outputStatement;
    }

    private Lexeme functionDefinition() {
        Lexeme functionDefinition = new Lexeme(FUNCTION_DEFINITION, currentLexeme.getLineNumber());
        consume(FUNC);
        functionDefinition.setLeft(consume(IDENTIFIER));
        Lexeme functionBody = new Lexeme(TokenType.FUNCTION_BODY, currentLexeme.getLineNumber());
        functionDefinition.setRight(functionBody);
        
        consume(OPENPAREN);
        if (functionParameterListPending()) functionBody.setLeft(functionParameterList());
        consume(CLOSEPAREN);

        Lexeme glue = new Lexeme(GLUE, currentLexeme.getLineNumber());
        functionBody.setRight(glue);        
        if (check(RETURNS)) {
            consume(RETURNS);
            glue.setLeft(functionReturnType());
        }

        consume(OPENBRACE);
        glue.setRight(statementList());
        consume(CLOSEBRACE);

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
	Lexeme assignment = new Lexeme(ASSIGNMENT, currentLexeme.getLineNumber());
        Lexeme leftSide = arrayReferencePending() ? arrayReference() : consume(IDENTIFIER);
        Lexeme assignmentOperator = assignmentOperator();
        assignmentOperator.setLeft(leftSide);
        assignmentOperator.setRight(expression());
        assignment.setLeft(assignmentOperator);

        return assignment;
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
	Lexeme initialization = new Lexeme(TokenType.INITIALIZATION, currentLexeme.getLineNumber());
        if (variableInitializerPending()) initialization.setLeft(variableInitializer());
        else initialization.setLeft(constantInitializer());
        return initialization;
    }

    private Lexeme constantInitializer() {
        Lexeme constantInitializer = new Lexeme(TokenType.CONSTANT_INITIALIZER, currentLexeme.getLineNumber());
        Lexeme glue1 = new Lexeme(GLUE, currentLexeme.getLineNumber());
        constantInitializer.setLeft(glue1);
        glue1.setLeft(consume(CONST));
        Lexeme identifier = consume(IDENTIFIER);
        identifier.setIsConstant(true);
        glue1.setRight(identifier);

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
        Lexeme expressionList = new Lexeme(EXPRESSION_LIST, currentLexeme.getLineNumber());
        expressionList.setLeft(expression());
        if (check(COMMA)) {
            Lexeme glue = new Lexeme(GLUE, currentLexeme.getLineNumber());
            expressionList.setRight(glue);
            glue.setLeft(consume(COMMA));
            glue.setRight(expressionList());
        }
        return expressionList;
    }

    private Lexeme expression() {
        Lexeme expression = new Lexeme(EXPRESSION, currentLexeme.getLineNumber());
        expression.setLeft(orTerm());
        if (check(OR)) {
            Lexeme glue = new Lexeme(GLUE, currentLexeme.getLineNumber());
            expression.setRight(glue);
            glue.setLeft(consume(OR));
            glue.setRight(expression());
        }
        return expression;
    }

    private Lexeme orTerm() {
        Lexeme orTerm = new Lexeme(OR_TERM, currentLexeme.getLineNumber());
        orTerm.setLeft(equalityTerm());
        if (check(AND)) {
            Lexeme glue = new Lexeme(GLUE, currentLexeme.getLineNumber());
            orTerm.setRight(glue);
            glue.setLeft(consume(AND));
            glue.setRight(orTerm());
        }
        return orTerm;
    }

    private Lexeme equalityTerm() {
        Lexeme equalityTerm = new Lexeme(EQUALITY_TERM, currentLexeme.getLineNumber());
        equalityTerm.setLeft(relationalTerm());
        Lexeme glue = new Lexeme(GLUE, currentLexeme.getLineNumber());
        if (check(EQUAL)) {
            equalityTerm.setRight(glue);
            glue.setLeft(consume(EQUAL));
            glue.setRight(equalityTerm());
        } else if (check(NOTEQUAL)) {
            equalityTerm.setRight(glue);
            glue.setLeft(consume(NOTEQUAL));
            glue.setRight(equalityTerm());
        }
        return equalityTerm;
    }

    private Lexeme relationalTerm() {
        Lexeme relationalTerm = new Lexeme(RELATIONAL_TERM, currentLexeme.getLineNumber());
        relationalTerm.setLeft(term());
        Lexeme glue = new Lexeme(GLUE, currentLexeme.getLineNumber());
        if (check(GREATER)) {
            relationalTerm.setRight(glue);
            glue.setLeft(consume(GREATER));
            glue.setRight(relationalTerm());
        } else if (check(GREATEREQUAL)) {
            relationalTerm.setRight(glue);
            glue.setLeft(consume(GREATEREQUAL));
            glue.setRight(relationalTerm());
        } else if (check(LESS)) {
            relationalTerm.setRight(glue);
            glue.setLeft(consume(LESS));
            glue.setRight(relationalTerm());
        } else if (check(LESSEQUAL)) {
            relationalTerm.setRight(glue);
            glue.setLeft(consume(LESSEQUAL));
            glue.setRight(relationalTerm());
        }
        return relationalTerm;
    }

    private Lexeme term() {
        Lexeme term = new Lexeme(TERM, currentLexeme.getLineNumber());
        term.setLeft(factor());
        Lexeme glue = new Lexeme(GLUE, currentLexeme.getLineNumber());
        if (check(PLUS)) {
            term.setRight(glue);
            glue.setLeft(consume(PLUS));
            glue.setRight(term());
        } else if (check(MINUS)) {
            term.setRight(glue);
            glue.setLeft(consume(MINUS));
            glue.setRight(term());
        }
        return term;
    }

    private Lexeme factor() {
        Lexeme factor = new Lexeme(FACTOR, currentLexeme.getLineNumber());
        factor.setLeft(powerTerm());
        Lexeme glue = new Lexeme(GLUE, currentLexeme.getLineNumber());
        if (check(TIMES)) {
            factor.setRight(glue);
            glue.setLeft(consume(TIMES));
            glue.setRight(factor());
        } else if (check(DIVIDE)) {
            factor.setRight(glue);
            glue.setLeft(consume(DIVIDE));
            glue.setRight(factor());
        } else if (check(MOD)) {
            factor.setRight(glue);
            glue.setLeft(consume(MOD));
            glue.setRight(factor());
        }
        return factor;
    }

    private Lexeme powerTerm() {
        Lexeme powerTerm = new Lexeme(POWER_TERM, currentLexeme.getLineNumber());
        powerTerm.setLeft(unaryTerm());
        if (check(EXP)) {
            Lexeme glue = new Lexeme(GLUE, currentLexeme.getLineNumber());
            powerTerm.setRight(glue);
            glue.setLeft(consume(EXP));
            glue.setRight(powerTerm());
        }
        return powerTerm;
    }

    private Lexeme unaryTerm() {
        Lexeme unaryTerm = new Lexeme(UNARY_TERM, currentLexeme.getLineNumber());
        if (check(INCREMENT)) {
            unaryTerm.setLeft(consume(INCREMENT));
            unaryTerm.setRight(variable());
        } else if (check(DECREMENT)) {
            unaryTerm.setLeft(consume(DECREMENT));
            unaryTerm.setRight(variable());
        } else if (check(PLUS)) {
            unaryTerm.setLeft(consume(PLUS));
            unaryTerm.setRight(simpleTerm());
        } else if (check(MINUS)) {
            unaryTerm.setLeft(consume(MINUS));
            unaryTerm.setRight(simpleTerm());
        } else if (check(NOT)) {
            unaryTerm.setLeft(consume(NOT));
            unaryTerm.setRight(simpleTerm());
        } else {
            unaryTerm.setLeft(simpleTerm());
            if (check(INCREMENT)) {
        	unaryTerm.setRight(consume(INCREMENT));
            }
            else if (check(DECREMENT)) {
        	unaryTerm.setRight(consume(DECREMENT));
            }
        }
        return unaryTerm;
    }

    private Lexeme simpleTerm() {
        Lexeme simpleTerm = new Lexeme(SIMPLE_TERM, currentLexeme.getLineNumber());
        if (literalPending()) simpleTerm.setLeft(literal());
        else if (groupingPending()) simpleTerm.setLeft(grouping());
        else if (functionCallPending()) simpleTerm.setLeft(functionCall());
        else simpleTerm.setLeft(variable());
        return simpleTerm;
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

    private Lexeme variable() {
	Lexeme variable = new Lexeme(VARIABLE, currentLexeme.getLineNumber());
	if (arrayReferencePending()) {
	    variable.setLeft(arrayReference());
	} else {
	    variable.setLeft(consume(IDENTIFIER));
	}
	return variable;
    }

    private Lexeme arrayReference() {
        Lexeme arrayReference = new Lexeme(ARRAY_REFERENCE, currentLexeme.getLineNumber());
        Lexeme glue = new Lexeme(GLUE, currentLexeme.getLineNumber());
        arrayReference.setLeft(consume(IDENTIFIER));
        arrayReference.setRight(glue);

        glue.setLeft(consume(OPENBRACKET));
        Lexeme expression = expression();
        Lexeme closeBracket = consume(CLOSEBRACKET);
        glue.setRight(closeBracket);

        closeBracket.setLeft(expression);

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
        Lexeme grouping = new Lexeme(GROUPING, currentLexeme.getLineNumber());
        grouping.setLeft(consume(OPENPAREN));
        Lexeme expression = expression();
        Lexeme closeParen = consume(CLOSEPAREN);
        grouping.setRight(closeParen);

        closeParen.setLeft(expression);

        return grouping;
    }

    private Lexeme literal() {
	Lexeme literal = new Lexeme(TokenType.LITERAL, currentLexeme.getLineNumber());
        if (check(INT)) literal.setLeft(consume(INT));
        else if (check(FLOAT)) literal.setLeft(consume(FLOAT));
        else if (booleanLiteralPending()) literal.setLeft(booleanLiteral());
        else literal.setLeft(consume(STRING));
        return literal;
    }

    private Lexeme booleanLiteral() {
        if (check(TRUE)) return consume(TRUE);
        else return consume(FALSE);
    }

    // ---------- Pending Methods ----------

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
                || inputStatementPending()
                || outputStatementPending()
                || conditionalPending()
        	|| returnStatementPending();
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

    private boolean inputStatementPending() {
        return check(INPUT);
    }

    private boolean outputStatementPending() {
        return check(OUTPUT);
    }

    private boolean whileLoopPending() {
        return check(WHILE);
    }

    private boolean forInPending() {
        return check(FOR) && checkNext(IDENTIFIER);
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
//        if (debug) System.out.println("  -- literalPending --");
        return check(INT)
                || check(FLOAT)
                || booleanLiteralPending()
                || check(STRING);
    }

    private boolean booleanLiteralPending() {
        return check(TRUE)
                || check(FALSE);
    }

    private boolean returnStatementPending() {
	return check(RETURN);
    }

    public static void printTree(Lexeme root) {
        String printableTree = getPrintableTree(root, 1);
        System.out.println(printableTree);
    }

    private static String getPrintableTree(Lexeme root, int level) {
        String treeString = root.toSimpleString();
        switch (root.getType()) {
            case IDENTIFIER:
                treeString += " (" + root.getStringValue() + ")";
                break;
            case STRING:
                treeString += " (\"" + root.getStringValue() + "\")";
                break;
            case INT:
                treeString += " (" + root.getIntValue() + ")";
                break;
            case FLOAT:
                treeString += " (" + root.getFloatValue() + ")";
                break;
            default:
                break;
        }

        StringBuilder spacer = new StringBuilder("\n");
        spacer.append(String.join("", Collections.nCopies(level, "    ")));

        if (root.getLeft() != null)
            treeString += spacer + "with left child: " + getPrintableTree(root.getLeft(), level + 1);
        if (root.getRight() != null)
            treeString += spacer + "and right child: " + getPrintableTree(root.getRight(), level + 1);

        return treeString;
    }

}
