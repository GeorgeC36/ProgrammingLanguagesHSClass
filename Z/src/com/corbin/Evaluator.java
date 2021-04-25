package com.corbin;

import com.corbin.Lexeme.Datatype;

public class Evaluator {
    private final boolean debug = false;

    public Lexeme eval(Lexeme tree, Environments environment) {
	if (tree == null) {
	    return null;
	}

	switch (tree.getType()) {
	case PROGRAM:
	    return eval(tree.getLeft(), environment);

	case STATEMENT_LIST:
	    return evalStatementList(tree, environment);

	case STATEMENT:
	    return evalStatement(tree, environment);

	// Self-Evaluating Types
	case INT:
	case FLOAT:
	case STRING:
	case TRUE:
	case LINECOMMENT:
	    return tree;

	// Simple Binary Operators
	case PLUS:
	case MINUS:
	case TIMES:
	case DIVIDE:
	case EXP:
	case MOD:
	case PLUSASSIGN:
	case MINUSASSIGN:
	case TIMESASSIGN:
	case DIVIDEASSIGN:
	case EXPASSIGN:
	case MODASSIGN:
	case GREATER:
	case LESS:
	case EQUAL:
	case NOTEQUAL:
	case GREATEREQUAL:
	case LESSEQUAL:
	    return evalBinaryOperator(tree, environment);
	}
    }

    private Lexeme evalBinaryOperator(Lexeme tree, Environments environment) {
	if (debug)
	    System.out.println("Evaluating BinaryOperator...");
	switch (tree.getType()) {
	case PLUS:
	    return evalPlus(tree, environment);

	default:
	    Z.error(tree, "Unrecognized error " + tree.toSimpleString());
	    return null;
	}
    }

    private Lexeme evalPlus(Lexeme tree, Environments environment) {
	if (debug)
	    System.out.println("Evaluating Plus...");
	Lexeme left = eval(tree.getLeft(), environment);
	Lexeme right = eval(tree.getRight(), environment);
	TokenType lType = left.getType();
	TokenType rType = right.getType();

	if (lType == TokenType.INT) {
	    switch (rType) {
	    case INT:
		return new Lexeme(TokenType.INT, left.getIntValue() + right.getIntValue(), left.getLineNumber());
	    case FLOAT:
	    case STRING:
	    default:
		Z.error();
		return null;
	    }
	}
    }

    private Lexeme evalStatementList(Lexeme statementList, Environments environment) {
	if (debug)
	    System.out.println("Evaluating StatementList...");
	Lexeme result = null;
	while (statementList != null) {
	    result = eval(statementList.getLeft(), environment);
	    statementList = statementList.getRight();
	}
	return result;
    }

    private Lexeme evalStatement(Lexeme tree, Environments environment) {
	if (debug)
	    System.out.println("Evaluating Statement...");
	final Lexeme statement = tree.getLeft();
	switch (statement.getType()) {
	case ASSIGNMENT:
	    return evalAssignment(statement, environment);
	case CONDITIONAL:
	    return evalConditional(statement, environment);
	case FUNCTION_CALL:
	    return evalFunctionCall(statement, environment);
	case FUNCTION_DEFINITION:
	    return evalFunctionDefinition(statement, environment);
	case INCREMENT_EXPRESSION:
	    return evalIncrementExpression(statement, environment);
	case INITIALIZATION:
	    return evalInitialization(statement, environment);
	case INPUT_STATEMENT:
	    return evalInputStatement(statement, environment);
	case LOOP:
	    return evalLoop(statement, environment);
	case OUTPUT_STATEMENT:
	    return evalOutputStatement(statement, environment);
	}

	return null;
    }

    private Lexeme evalAssignment(Lexeme statement, Environments environment) {
	Lexeme assignment = statement.getLeft();
	Lexeme target = environment.lookUp(assignment.getLeft());
	Lexeme value = evalExpression(assignment.getRight());
	Lexeme result = null;
	if (target == null || value == null)
	    return null; // error message already printed

	final Datatype targetType = target.getDatatype();
	// type conversion and error messages handled in get...Value() methods
	if (targetType == Datatype.FLOAT) {
	    result = new Lexeme(TokenType.FLOAT, value.getFloatValue(), target.getLineNumber());
	} else if (targetType == Datatype.INT) {
	    result = new Lexeme(TokenType.INT, value.getIntValue(), target.getLineNumber());
	} else if (targetType == Datatype.STRING) {
	    result = new Lexeme(TokenType.STRING, value.getStringValue(), target.getLineNumber());
	}
	environment.update(target, result);
	return result;
    }

    private Lexeme evalConditional(Lexeme statement, Environments environment) {
	// TODO Auto-generated method stub
	return null;
    }

    private Lexeme evalFunctionCall(Lexeme statement, Environments environment) {
	// TODO Auto-generated method stub
	return null;
    }

    private Lexeme evalFunctionDefinition(Lexeme statement, Environments environment) {
	// TODO Auto-generated method stub
	return null;
    }

    private Lexeme evalIncrementExpression(Lexeme statement, Environments environment) {
	// TODO Auto-generated method stub
	return null;
    }

    private Lexeme evalInitialization(Lexeme statement, Environments environment) {
	// TODO Auto-generated method stub
	return null;
    }

    private Lexeme evalInputStatement(Lexeme statement, Environments environment) {
	// TODO Auto-generated method stub
	return null;
    }

    private Lexeme evalLoop(Lexeme statement, Environments environment) {
	// TODO Auto-generated method stub
	return null;
    }

    private Lexeme evalOutputStatement(Lexeme statement, Environments environment) {
	Lexeme expression = evalExpression(statement.getRight());
	switch (expression.getDatatype()) {
	case FLOAT:
	    System.out.println(expression.getFloatValue());
	    break;
	case INT:
	    System.out.println(expression.getIntValue());
	    break;
	case STRING:
	    System.out.println(expression.getStringValue());
	    break;
	}
	return expression;
    }

    private Lexeme evalExpression(Lexeme expression) {
	Lexeme orTerm = evalOrTerm(expression.getLeft());
	if (expression.getRight() == null || orTerm.getBooleanValue()) {
	    return orTerm;
	} else {
	    return evalExpression(expression.getRight().getRight());
	}
    }

    private Lexeme evalOrTerm(Lexeme orTerm) {
	Lexeme equalityTerm = evalEqualityTerm(orTerm.getLeft());
	if (orTerm.getRight() == null || !equalityTerm.getBooleanValue()) {
	    return equalityTerm;
	} else {
	    return evalOrTerm(orTerm.getRight().getRight());
	}
    }

    private Lexeme evalEqualityTerm(Lexeme equalityTerm) {
	Lexeme relationalTerm = evalRelationalTerm(equalityTerm.getLeft());
	if (equalityTerm.getRight() == null) return relationalTerm;	// no operator
	
	Lexeme otherTerm = evalRelationalTerm(equalityTerm.getRight().getRight());
	TokenType operatorType = equalityTerm.getRight().getLeft().getType();
	if (relationalTerm.getDatatype() == Datatype.STRING || otherTerm.getDatatype() == Datatype.STRING) {
	    if (operatorType == TokenType.EQUAL &&  relationalTerm.getStringValue().equals(otherTerm.getStringValue())
		                                || !relationalTerm.getStringValue().equals(otherTerm.getStringValue())) {
		return new Lexeme(TokenType.TRUE, relationalTerm.getLineNumber());
	    }
	} else {	// INTs can be compared as FLOATs
	    if (operatorType == TokenType.EQUAL && relationalTerm.getFloatValue() == otherTerm.getFloatValue()
		                                || relationalTerm.getFloatValue() != otherTerm.getFloatValue()) {
		return new Lexeme(TokenType.TRUE, relationalTerm.getLineNumber());
	    }
	}
	return new Lexeme(TokenType.FALSE, relationalTerm.getLineNumber());
    }

    private Lexeme evalRelationalTerm(Lexeme relationalTerm) {
	Lexeme term = evalTerm(relationalTerm.getLeft());
	if (relationalTerm.getRight() == null) return term;	// no operator
	
	Lexeme otherTerm = evalRelationalTerm(relationalTerm.getRight().getRight());
	TokenType operatorType = relationalTerm.getRight().getLeft().getType();
	if (relationalTerm.getDatatype() == Datatype.STRING || otherTerm.getDatatype() == Datatype.STRING) {
	    if (       operatorType == TokenType.GREATER      && relationalTerm.getStringValue().compareTo(otherTerm.getStringValue()) >  0
		    || operatorType == TokenType.GREATEREQUAL && relationalTerm.getStringValue().compareTo(otherTerm.getStringValue()) >= 0
		    || operatorType == TokenType.LESS         && relationalTerm.getStringValue().compareTo(otherTerm.getStringValue()) <  0
		    || operatorType == TokenType.LESSEQUAL    && relationalTerm.getStringValue().compareTo(otherTerm.getStringValue()) <= 0) {
		return new Lexeme(TokenType.TRUE, relationalTerm.getLineNumber());
	    }
	} else {	// INTs can be compared as FLOATs
	    if (       operatorType == TokenType.GREATER      && relationalTerm.getFloatValue() >  otherTerm.getFloatValue()
		    || operatorType == TokenType.GREATEREQUAL && relationalTerm.getFloatValue() >= otherTerm.getFloatValue()
		    || operatorType == TokenType.LESS         && relationalTerm.getFloatValue() <  otherTerm.getFloatValue()
		    || operatorType == TokenType.LESSEQUAL    && relationalTerm.getFloatValue() <= otherTerm.getFloatValue()) {
		return new Lexeme(TokenType.TRUE, relationalTerm.getLineNumber());
	    }
	}
	return new Lexeme(TokenType.FALSE, relationalTerm.getLineNumber());
    }

    private Lexeme evalTerm(Lexeme term) {
	Lexeme factor = evalFactor(term.getLeft());
	if (term.getRight() == null) return factor;	// no operator
	
	Lexeme otherTerm = evalTerm(term.getRight().getRight());
	TokenType operatorType = term.getRight().getLeft().getType();
	final Datatype termType = term.getDatatype();
	final Datatype otherTermType = otherTerm.getDatatype();
	if (termType == null || otherTermType == null) {
	   Z.error(term.getLineNumber(), "Incompatible datatypes"); 
	   return new Lexeme(TokenType.INT, 0, term.getLineNumber());
	}

	if (termType == Datatype.STRING || otherTermType == Datatype.STRING) {
	    if (operatorType == TokenType.PLUS) {
		return new Lexeme(TokenType.STRING, term.getStringValue() + otherTerm.getStringValue(), term.getLineNumber());
	    } else {		// MINUS
		return new Lexeme(TokenType.STRING, term.getStringValue().replaceFirst(otherTerm.getStringValue() + "$", ""), term.getLineNumber());
	    }
	} else if (termType == Datatype.FLOAT || otherTermType == Datatype.FLOAT) {
	    if (operatorType == TokenType.PLUS) {
		return new Lexeme(TokenType.FLOAT, term.getFloatValue() + otherTerm.getFloatValue(), term.getLineNumber());
	    } else {		// MINUS
		return new Lexeme(TokenType.FLOAT, term.getFloatValue() - otherTerm.getFloatValue(), term.getLineNumber());
	    }
	} else if (termType == Datatype.INT && otherTermType == Datatype.INT) {
		return new Lexeme(TokenType.FLOAT, term.getIntValue() + otherTerm.getIntValue(), term.getLineNumber());
	    } else {		// MINUS
		return new Lexeme(TokenType.FLOAT, term.getIntValue() - otherTerm.getIntValue(), term.getLineNumber());
	    }
    }

    private Lexeme evalFactor(Lexeme left) {
	// TODO Auto-generated method stub
	return null;
    }

}
