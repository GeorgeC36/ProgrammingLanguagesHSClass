package com.corbin;

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

            // Self-Evaluatiing Types
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
        if (debug) System.out.println("Evaluating BinaryOperator...");
        switch (tree.getType()) {
            case PLUS:
                return evalPlus(tree, environment);

            default:
                Z.error(tree, "Unrecognized error " + tree.toSimpleString());
                return null;
        }
    }

    private Lexeme evalStatementList(Lexeme statementList, Environments environment) {
        if (debug) System.out.println("Evaluating StatementList...");
        Lexeme result = null;
        while (statementList != null) {
            result = eval(statementList.getLeft(), environment);
            statementList = statementList.getRight();
        }
        return result;
    }

}
