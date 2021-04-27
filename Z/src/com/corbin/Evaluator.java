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
	    return evalStatementList(tree.getLeft(), environment);

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
	default:
	}
	return null;
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
//		Z.error();
		return null;
	    }
	}
	return null;
    }

    private Lexeme evalStatementList(Lexeme statementList, Environments environment) {
	if (debug) System.out.println("Evaluating StatementList...");
	Lexeme result = null;
	while (statementList != null) {
	    if (statementList.getLeft() != null) {
		result = evalStatement(statementList.getLeft(), environment);
	    }
	    statementList = statementList.getRight();
	}
	return result;
    }

    private Lexeme evalStatement(Lexeme statement, Environments environment) {
	if (debug) System.out.println("Evaluating Statement..." + statement.getType());
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

    private Lexeme evalAssignment(Lexeme assignment, Environments environment) {
	Lexeme assignmentOperator = assignment.getLeft();
	Lexeme identifier = assignmentOperator.getLeft();
	Lexeme variable = environment.lookUp(identifier);
	Lexeme value = evalExpression(assignmentOperator.getRight(), environment);
	Lexeme result = null;
	if (identifier == null || value == null)
	    return null; // error message already printed

	final Datatype variableType = variable.getDatatype();
	// type conversion and error messages handled in get...Value() methods
	if (variableType == Datatype.FLOAT) {
	    result = new Lexeme(TokenType.FLOAT, value.getFloatValue(), identifier.getLineNumber());
	} else if (variableType == Datatype.INT) {
	    result = new Lexeme(TokenType.INT, value.getIntValue(), identifier.getLineNumber());
	} else if (variableType == Datatype.STRING) {
	    result = new Lexeme(TokenType.STRING, value.getStringValue(), identifier.getLineNumber());
	}
	environment.update(identifier, result);
	return result;
    }

    private Lexeme evalConditional(Lexeme statement, Environments environment) {
	// TODO Auto-generated method stub
	return null;
    }

    private Lexeme evalFunctionDefinition(Lexeme statement, Environments environment) {
	// TODO Auto-generated method stub
	return null;
    }

    private Lexeme evalIncrementExpression(Lexeme incrementExpression, Environments environment) {
	Lexeme result = null;
	if (incrementExpression.getLeft().getType() == TokenType.IDENTIFIER) {  // postincrement
	    Lexeme variable = incrementExpression.getLeft();
	    Lexeme value = environment.lookUp(variable);
	    TokenType operatorType = incrementExpression.getRight().getType();
	    switch (value.getType()) {
	    case INT: 
		result = new Lexeme (TokenType.INT, value.getIntValue() + (operatorType == TokenType.INCREMENT ? 1 : -1), incrementExpression.getLineNumber());
		break;
	    case FLOAT: 
		result = new Lexeme (TokenType.FLOAT, value.getFloatValue() + (operatorType == TokenType.INCREMENT ? 1 : -1), incrementExpression.getLineNumber());
		break;
	    case STRING:
		Z.error(variable, "Invalid type.  Can't increment or decrement STRING");
		result = value;
	    }
	    environment.update(variable, result);
	    return value;	// original value
	} else {		// preincrement
	    Lexeme variable = incrementExpression.getRight();
	    Lexeme value = environment.lookUp(variable);
	    TokenType operatorType = incrementExpression.getLeft().getType();
	    switch (value.getType()) {
	    case INT: 
		result = new Lexeme (TokenType.INT, value.getIntValue() + (operatorType == TokenType.INCREMENT ? 1 : -1), incrementExpression.getLineNumber());
		break;
	    case FLOAT: 
		result = new Lexeme (TokenType.FLOAT, value.getFloatValue() + (operatorType == TokenType.INCREMENT ? 1 : -1), incrementExpression.getLineNumber());
		break;
	    case STRING:
		Z.error(variable, "Invalid type.  Can't increment or decrement STRING");
		result = value;
	    }
	    environment.update(variable, result);
	    return result;	// updated value
	}
   }

    private Lexeme evalInitialization(Lexeme initialization, Environments environment) {
	Lexeme initializer = initialization.getLeft();
	Lexeme identifier = initializer.getLeft();
	if (identifier.getType() != TokenType.IDENTIFIER) {
	    identifier = identifier.getRight();
	}
	Lexeme right = initializer.getRight();
	Lexeme dataType = null;
	Lexeme result = null;
	if (initializer.getType() == TokenType.VARIABLE_INITIALIZER) {
	    if (right.getLeft().getType() == TokenType.DATA_TYPE) {
		dataType = right.getLeft().getLeft();
	    }
	    if (right.getRight() == null) {	// no initialization
		switch (dataType.getType()) {
		case KW_FLOAT:
		    identifier.setDatatype(Datatype.FLOAT);
		    result = new Lexeme(TokenType.FLOAT, (float) 0.0, initialization.getLineNumber());
		    break;
		case KW_INT:
		    identifier.setDatatype(Datatype.INT);
		    result = new Lexeme(TokenType.INT, 0, initialization.getLineNumber());
		    break;
		case KW_STRING:
		    identifier.setDatatype(Datatype.STRING);
		    result = new Lexeme(TokenType.STRING, "", initialization.getLineNumber());
		    break;
		//TODO: arrayType
		}
	    } else {
		Lexeme initializerExpression = right.getRight();
		if (initializerExpression.getType() == TokenType.GLUE) {
		    initializerExpression = initializerExpression.getRight();
		}
		result = evalInitializerExpression(initializerExpression, environment);
		if (dataType == null) {		// if no explicit datatype, use type of expression
		    identifier.setDatatype(result.getDatatype());
		} else {
		    switch (dataType.getType()) {
		    case KW_FLOAT:
			identifier.setDatatype(Datatype.FLOAT);
			if (result.getDatatype() == Datatype.STRING) {
			    Z.error(dataType, "Type mismatch.  Can't initialize FLOAT to STRING value");
			} else {
			    result = new Lexeme(TokenType.FLOAT, result.getFloatValue(), initialization.getLineNumber());
			}
			break;
		    case KW_INT:
			identifier.setDatatype(Datatype.INT);
			if (result.getDatatype() == Datatype.STRING) {
			    Z.error(dataType, "Type mismatch.  Can't initialize INT to STRING value");
			} else {
			    result = new Lexeme(TokenType.INT, result.getIntValue(), initialization.getLineNumber());
			}
			break;
		    case KW_STRING:
			identifier.setDatatype(Datatype.STRING);
			result = new Lexeme(TokenType.STRING, result.getStringValue(), initialization.getLineNumber());
			break;
		    //TODO: arrayType
		    }

		}
	    }
	}
	environment.insert(identifier, result);
	return result;
    }

    private Lexeme evalInitializerExpression(Lexeme initializerExpression, Environments environment) {
	return evalExpression(initializerExpression, environment);
    }

    private Lexeme evalInputStatement(Lexeme statement, Environments environment) {
	// TODO Auto-generated method stub
	return null;
    }

    private Lexeme evalLoop(Lexeme statement, Environments environment) {
	Environments loopEnvironment = new Environments(environment);
	final Lexeme loop = statement.getLeft();
	switch (loop.getType()) {
	case FOR_LOOP:
	    return evalForLoop(loop, loopEnvironment);
	case FOR_IN:
	    return evalForIn(loop, loopEnvironment);
	case WHILE_LOOP:
	    return evalWhile(loop, loopEnvironment);
	}
	Z.error(loop, "Unknown loop type");
	return null;
    }

    private Lexeme evalForLoop(Lexeme forLoop, Environments environment) {
	if (debug) System.out.println("Evaluating For Loop..." + forLoop.getType());
	Lexeme semi1 = forLoop.getRight();
	if (semi1.getLeft() != null) evalInitialization(semi1.getLeft(), environment);
	Lexeme semi2 = semi1.getRight();
	Lexeme expression = semi2.getLeft();
	Lexeme loopIncrement = semi2.getRight().getLeft();
	Lexeme statementList = semi2.getRight().getRight().getRight().getLeft();
	while (expression == null || evalExpression(expression, environment).getBooleanValue()) {
	    evalStatementList(statementList, environment);
	    if (loopIncrement != null) evalLoopIncrement(loopIncrement, environment);
	}
	return null;
    }

    private void evalLoopIncrement(Lexeme loopIncrement, Environments environment) {
	if (loopIncrement.getType() == TokenType.ASSIGNMENT) evalAssignment(loopIncrement, environment);
	else evalIncrementExpression(loopIncrement, environment);
    }

    private Lexeme evalForIn(Lexeme forInLoop, Environments environment) {
	Lexeme identifier = forInLoop.getLeft();
	Lexeme iterable = forInLoop.getRight().getLeft();	// TODO: arrays
	Lexeme statementList = forInLoop.getRight().getRight().getRight().getLeft();
	Lexeme start = evalExpression(iterable.getLeft(), environment);	
	Lexeme end = evalExpression(iterable.getRight(), environment);
	
	environment.insert(identifier, start);
	Lexeme value = start;
	while (value.getFloatValue() < end.getFloatValue()) {
	    evalStatementList(statementList, environment);
	    if (start.getDatatype() == Datatype.INT) {
		value = new Lexeme(TokenType.INT, value.getIntValue() + 1, forInLoop.getLineNumber());
	    } else {
		value = new Lexeme(TokenType.FLOAT, value.getFloatValue() + 1, forInLoop.getLineNumber());
	    }
	    environment.update(identifier, value);
	}
	return null;
    }

    private Lexeme evalWhile(Lexeme whileLoop, Environments environment) {
	if (debug) System.out.println("Evaluating While..." + whileLoop.getType());
	Lexeme expression = whileLoop.getLeft().getRight().getRight().getLeft();
	while (evalExpression(expression, environment).getBooleanValue()) {
	    evalStatementList(whileLoop.getRight().getRight().getLeft(), environment);
	}
	return null;
    }

    private Lexeme evalOutputStatement(Lexeme statement, Environments environment) {
	Lexeme expression = evalExpression(statement.getRight(), environment);
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

    private Lexeme evalExpression(Lexeme expression, Environments environment) {
	Lexeme orTerm = evalOrTerm(expression.getLeft(), environment);
	if (expression.getRight() == null || orTerm.getBooleanValue()) {
	    return orTerm;
	} else {
	    return evalExpression(expression.getRight().getRight(), environment);
	}
    }

    private Lexeme evalOrTerm(Lexeme orTerm, Environments environment) {
	Lexeme equalityTerm = evalEqualityTerm(orTerm.getLeft(), environment);
	if (orTerm.getRight() == null || !equalityTerm.getBooleanValue()) {
	    return equalityTerm;
	} else {
	    return evalOrTerm(orTerm.getRight().getRight(), environment);
	}
    }

    private Lexeme evalEqualityTerm(Lexeme equalityTerm, Environments environment) {
	Lexeme relationalTerm = evalRelationalTerm(equalityTerm.getLeft(), environment);
	if (equalityTerm.getRight() == null) return relationalTerm;	// no operator
	
	Lexeme otherTerm = evalRelationalTerm(equalityTerm.getRight().getRight(), environment);
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

    private Lexeme evalRelationalTerm(Lexeme relationalTerm, Environments environment) {
	Lexeme term = evalTerm(relationalTerm.getLeft(), environment);
	if (relationalTerm.getRight() == null) return term;	// no operator
	
	Lexeme otherTerm = evalRelationalTerm(relationalTerm.getRight().getRight(), environment);
	TokenType operatorType = relationalTerm.getRight().getLeft().getType();
	if (term.getDatatype() == Datatype.STRING || otherTerm.getDatatype() == Datatype.STRING) {
	    if (       operatorType == TokenType.GREATER      && term.getStringValue().compareTo(otherTerm.getStringValue()) >  0
		    || operatorType == TokenType.GREATEREQUAL && term.getStringValue().compareTo(otherTerm.getStringValue()) >= 0
		    || operatorType == TokenType.LESS         && term.getStringValue().compareTo(otherTerm.getStringValue()) <  0
		    || operatorType == TokenType.LESSEQUAL    && term.getStringValue().compareTo(otherTerm.getStringValue()) <= 0) {
		return new Lexeme(TokenType.TRUE, relationalTerm.getLineNumber());
	    }
	} else {	// INTs can be compared as FLOATs
	    if (       operatorType == TokenType.GREATER      && term.getFloatValue() >  otherTerm.getFloatValue()
		    || operatorType == TokenType.GREATEREQUAL && term.getFloatValue() >= otherTerm.getFloatValue()
		    || operatorType == TokenType.LESS         && term.getFloatValue() <  otherTerm.getFloatValue()
		    || operatorType == TokenType.LESSEQUAL    && term.getFloatValue() <= otherTerm.getFloatValue()) {
		return new Lexeme(TokenType.TRUE, relationalTerm.getLineNumber());
	    }
	}
	return new Lexeme(TokenType.FALSE, relationalTerm.getLineNumber());
    }

    private Lexeme evalTerm(Lexeme term, Environments environment) {
	Lexeme factor = evalFactor(term.getLeft(), environment);
	if (term.getRight() == null) return factor;	// no operator
	
	Lexeme otherTerm = evalTerm(term.getRight().getRight(), environment);
	TokenType operatorType = term.getRight().getLeft().getType();
	final Datatype factorType = factor.getDatatype();
	final Datatype otherTermType = otherTerm.getDatatype();
	if (factorType == null || otherTermType == null) {
	   Z.error(term, "Incompatible datatypes"); 
	   return new Lexeme(TokenType.INT, 0, term.getLineNumber());
	}

	if (factorType == Datatype.STRING || otherTermType == Datatype.STRING) {
	    if (operatorType == TokenType.PLUS) {
		return new Lexeme(TokenType.STRING, factor.getStringValue() + otherTerm.getStringValue(), term.getLineNumber());
	    } else {		// MINUS
		return new Lexeme(TokenType.STRING, factor.getStringValue().replaceFirst(otherTerm.getStringValue() + "$", ""), term.getLineNumber());
	    }
	} else if (factorType == Datatype.FLOAT || otherTermType == Datatype.FLOAT) {
	    if (operatorType == TokenType.PLUS) {
		return new Lexeme(TokenType.FLOAT, factor.getFloatValue() + otherTerm.getFloatValue(), term.getLineNumber());
	    } else {		// MINUS
		return new Lexeme(TokenType.FLOAT, factor.getFloatValue() - otherTerm.getFloatValue(), term.getLineNumber());
	    }
	} else if (factorType == Datatype.INT && otherTermType == Datatype.INT) {
	    if (operatorType == TokenType.PLUS) {
		return new Lexeme(TokenType.INT, factor.getIntValue() + otherTerm.getIntValue(), term.getLineNumber());
	    } else {		// MINUS
		return new Lexeme(TokenType.INT, factor.getIntValue() - otherTerm.getIntValue(), term.getLineNumber());
	    }
	}
	Z.error(term.getLeft(), "Incompatible Types. Can't perform " + operatorType + " operation on operands of type " 
		+ factorType + " and " + otherTermType);
	return new Lexeme(TokenType.INT, 0, term.getLineNumber());
    }

    private Lexeme evalFactor(Lexeme factor, Environments environment) {
	Lexeme powerTerm = evalPowerTerm(factor.getLeft(), environment);
	if (factor.getRight() == null) return powerTerm;	// no operator
	
	Lexeme otherTerm = evalFactor(factor.getRight().getRight(), environment);
	TokenType operatorType = factor.getRight().getLeft().getType();
	final Datatype powerTermType = powerTerm.getDatatype();
	final Datatype otherTermType = otherTerm.getDatatype();
	if (powerTermType == null || otherTermType == null) {
	   Z.error(factor, "Incompatible datatypes"); 
	   return new Lexeme(TokenType.INT, 0, factor.getLineNumber());
	}

	if (powerTermType == Datatype.STRING && otherTermType == Datatype.INT && operatorType == TokenType.TIMES) {
	    String result = "";
	    for (int i = 0; i < otherTerm.getIntValue(); i++) {
		result += powerTerm.getStringValue();
	    }
	    return new Lexeme(TokenType.STRING, result, factor.getLineNumber());
	} else if (powerTermType == Datatype.INT && otherTermType == Datatype.STRING && operatorType == TokenType.TIMES) {
	    String result = "";
	    for (int i = 0; i < powerTerm.getIntValue(); i++) {
		result += otherTerm.getStringValue();
	    }
	    return new Lexeme(TokenType.STRING, result, factor.getLineNumber());
	} else if (powerTermType == Datatype.FLOAT || otherTermType == Datatype.FLOAT) {
	    if (operatorType == TokenType.TIMES) {
		return new Lexeme(TokenType.FLOAT, powerTerm.getFloatValue() * otherTerm.getFloatValue(), factor.getLineNumber());
	    } else if (operatorType == TokenType.DIVIDE) {
		return new Lexeme(TokenType.FLOAT, powerTerm.getFloatValue() / otherTerm.getFloatValue(), factor.getLineNumber());
	    } else {		// MOD
		return new Lexeme(TokenType.FLOAT, powerTerm.getFloatValue() % otherTerm.getFloatValue(), factor.getLineNumber());
	    }
	} else if (powerTermType == Datatype.INT && otherTermType == Datatype.INT) {
	    if (operatorType == TokenType.TIMES) {
		return new Lexeme(TokenType.INT, powerTerm.getIntValue() * otherTerm.getIntValue(), factor.getLineNumber());
	    } else if (operatorType == TokenType.DIVIDE) {
		return new Lexeme(TokenType.INT, powerTerm.getIntValue() / otherTerm.getIntValue(), factor.getLineNumber());
	    } else {		// MOD
		return new Lexeme(TokenType.INT, powerTerm.getIntValue() % otherTerm.getIntValue(), factor.getLineNumber());
	    }
	}
	Z.error(factor, "Incompatible Types. Can't perform " + operatorType + " operation on operands of type " 
		+ powerTermType + " and " + otherTermType);
	return new Lexeme(TokenType.INT, 0, factor.getLineNumber());
    }

    private Lexeme evalPowerTerm(Lexeme powerTerm, Environments environment) {
	Lexeme unaryTerm = evalUnaryTerm(powerTerm.getLeft(), environment);
	if (powerTerm.getRight() == null) return unaryTerm;	// no operator
	
	Lexeme otherTerm = evalPowerTerm(powerTerm.getRight().getRight(), environment);
	TokenType operatorType = powerTerm.getRight().getLeft().getType();
	final Datatype unaryTermType = unaryTerm.getDatatype();
	final Datatype otherTermType = otherTerm.getDatatype();
	if (unaryTermType == null || otherTermType == null
		|| unaryTermType == Datatype.STRING || otherTermType == Datatype.STRING) {
	   Z.error(powerTerm, "Incompatible datatypes"); 
	   return new Lexeme(TokenType.INT, 0, powerTerm.getLineNumber());
	}
	double result = Math.pow(unaryTerm.getFloatValue(), otherTerm.getFloatValue());
	if (unaryTermType == Datatype.INT && otherTermType == Datatype.INT) {
	    return new Lexeme(TokenType.INT, (int) result, powerTerm.getLineNumber());
	} else { 	// at least one is a FLOAT
	    return new Lexeme(TokenType.FLOAT, (float) result, powerTerm.getLineNumber());
	}
    }

    private Lexeme evalUnaryTerm(Lexeme unaryTerm, Environments environment) {
	final Lexeme left = unaryTerm.getLeft();
	final Lexeme right = unaryTerm.getRight();
	final TokenType leftType = left.getType();
	if (leftType == TokenType.PLUS) {
	    return evalSimpleTerm(right, environment);
	}
	Lexeme result = null;
	if (leftType == TokenType.MINUS) {
	    Lexeme simpleTerm = evalSimpleTerm(right, environment);
	    switch (simpleTerm.getType()) {
	    case INT: 
		return new Lexeme (TokenType.INT, - simpleTerm.getIntValue(), unaryTerm.getLineNumber());
	    case FLOAT: 
		return new Lexeme (TokenType.FLOAT, - simpleTerm.getFloatValue(), unaryTerm.getLineNumber());
	    case STRING:
		Z.error(simpleTerm, "Invalid type.  Can't negate STRING");
		return new Lexeme (TokenType.INT, 0, unaryTerm.getLineNumber());
	    }
	} else if (leftType == TokenType.NOT) {
	    Lexeme simpleTerm = evalSimpleTerm(right, environment);
	    return new Lexeme (simpleTerm.getBooleanValue() ? TokenType.FALSE : TokenType.TRUE, unaryTerm.getLineNumber());
	} else if (leftType == TokenType.INCREMENT || leftType == TokenType.DECREMENT) {
	    Lexeme variable = evalVariable(right, environment);
	    Lexeme value = environment.lookUp(variable);
	    switch (value.getType()) {
	    case INT: 
		result = new Lexeme (TokenType.INT, value.getIntValue() + (leftType == TokenType.INCREMENT ? 1 : -1), unaryTerm.getLineNumber());
		break;
	    case FLOAT: 
		result = new Lexeme (TokenType.FLOAT, value.getFloatValue() + (leftType == TokenType.INCREMENT ? 1 : -1), unaryTerm.getLineNumber());
		break;
	    case STRING:
		Z.error(variable, "Invalid type.  Can't increment or decrement STRING");
		result = new Lexeme (TokenType.INT, 0, unaryTerm.getLineNumber());
	    }
	    environment.update(variable, result);
	    return result;	
	}
	Lexeme simpleTerm = evalSimpleTerm(left, environment);
	if (right == null) {
	    return simpleTerm;		// no operator
	} else if (right.getType() == TokenType.INCREMENT || right.getType() == TokenType.DECREMENT) {
	    if (simpleTerm.getType() == TokenType.VARIABLE) {
		Lexeme value = environment.lookUp(simpleTerm);
		switch (value.getType()) {
		case INT:
		    result = new Lexeme(TokenType.INT, value.getIntValue() + (right.getType() == TokenType.INCREMENT ? 1 : -1), unaryTerm.getLineNumber());
		    break;
		case FLOAT:
		    result = new Lexeme(TokenType.FLOAT, value.getFloatValue() + (right.getType() == TokenType.INCREMENT ? 1 : -1), unaryTerm.getLineNumber());
		    break;
		case STRING:
		    Z.error(simpleTerm, "Invalid type.  Can't increment or decrement STRING");
		    result = new Lexeme(TokenType.INT, 0, unaryTerm.getLineNumber());
		}
		environment.update(simpleTerm, result);
		return value;				// value before increment or decrement
	    } else {
		Z.error(simpleTerm, "Can't INCREMENT or DECREMENT non-variable");
	    }
	}
	Z.error(unaryTerm, "Invalid syntax");
	return new Lexeme(TokenType.INT, 0, simpleTerm.getLineNumber());
    }

    private Lexeme evalSimpleTerm(Lexeme simpleTerm, Environments environment) {
	final Lexeme left = simpleTerm.getLeft();
	switch (left.getType()) {
	case FUNCTION_CALL:
	    return evalFunctionCall(left, environment);
	case GROUPING:
	    return evalGrouping(left, environment);
	case LITERAL:
	    return evalLiteral(left, environment);
	case VARIABLE:
	    return environment.lookUp(evalVariable(left, environment));
	default:
	    Z.error(simpleTerm, "Unrecognized value");
	    return new Lexeme(TokenType.INT, 0, simpleTerm.getLineNumber());
	}
    }

    private Lexeme evalFunctionCall(Lexeme functionCall, Environments environment) {
	Z.error(functionCall, "Function call TBD");
	return new Lexeme(TokenType.INT, 0, functionCall.getLineNumber());
    }

    private Lexeme evalGrouping(Lexeme grouping, Environments environment) {
	return evalExpression(grouping.getRight().getLeft(), environment);
    }

    private Lexeme evalLiteral(Lexeme literal, Environments environment) {
	Lexeme left = literal.getLeft();
	if (left.getType() == TokenType.BOOLEAN_LITERAL) {
	    return evalBooleanLiteral(left, environment);
	} else {
	    return left;	// NUMBER or STRING
	}
    }

    private Lexeme evalBooleanLiteral(Lexeme booleanLiteral, Environments environment) {
	return booleanLiteral.getLeft();
    }

    private Lexeme evalVariable(Lexeme variable, Environments environment) {
	return variable.getLeft();
	//TODO: arraryReference
    }
    
}
