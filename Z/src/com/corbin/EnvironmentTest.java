package com.corbin;

public class EnvironmentTest {

    public static void main(String[] args) {
        Environments global = new Environments(null);
        Environments levelOne = new Environments(global);
        Environments levelTwo = new Environments(levelOne);
        Environments levelThree = new Environments(levelTwo);

        Lexeme aID = new Lexeme(TokenType.IDENTIFIER, "a", 1);
        Lexeme aVal = new Lexeme(TokenType.INT, 7, 1);

        Lexeme bID = new Lexeme(TokenType.IDENTIFIER, "a", 1);
        Lexeme bVal = new Lexeme(TokenType.INT, 7, 1);

        global.insert(aID, aVal);
        global.print();
    }
}
