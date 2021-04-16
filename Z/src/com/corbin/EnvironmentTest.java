package com.corbin;

public class EnvironmentTest {

    public static void main(String[] args) {
        Environments global = new Environments(null);
        Environments levelOne = new Environments(global);
        Environments levelTwo = new Environments(levelOne);
        Environments levelThree = new Environments(levelTwo);

        Lexeme gID = new Lexeme(TokenType.IDENTIFIER, "g", 0);
        Lexeme gValString = new Lexeme(TokenType.STRING, "Top", 0);

        Lexeme aID = new Lexeme(TokenType.IDENTIFIER, "a", 1);
        Lexeme aIDAlso = new Lexeme(TokenType.IDENTIFIER, "a", 1);
        Lexeme aValSeven = new Lexeme(TokenType.INT, 7, 1);
        Lexeme aValFour = new Lexeme(TokenType.INT, 4, 1);
        Lexeme aValTwo = new Lexeme(TokenType.INT, 2, 1);
        Lexeme aValFloat = new Lexeme(TokenType.FLOAT, 3.0F, 1);
        Lexeme aValString = new Lexeme(TokenType.STRING, "World", 1);

        Lexeme cID = new Lexeme(TokenType.IDENTIFIER, "c", 2);
        Lexeme cValFloat = new Lexeme(TokenType.FLOAT, 5.0F, 2);
        Lexeme cValString = new Lexeme(TokenType.STRING, "Hello", 2);

        System.out.println("Adding to Environment!");
        global.insert(aID, aValSeven);
        global.insert(gID, gValString);
        levelOne.insert(cID, cValFloat);
        levelOne.insert(aID, aValString);
        levelTwo.insert(aID, aValTwo);
        levelThree.insert(cID, cValString);

        System.out.println("\n\nPrinting Starting Environment\n");
        global.print();
        levelOne.print();
        levelTwo.print();
        levelThree.print();

        System.out.println("\n\nChanges to Level 2!\n");
        System.out.println("\tBefore Changes");
        levelTwo.print();
        System.out.println(levelTwo.lookUp(aID));
        levelTwo.update(aID, aValFour);
        System.out.println("\n\tAfter Changes");
        System.out.println(levelTwo.lookUp(aID));
        System.out.println(levelThree.lookUp(cID));
        levelTwo.print();

        System.out.println("\n\nChanges to Level 3!\n");
        System.out.println("\tBefore Changes");
        levelThree.print();
        levelThree.insert(aID, aValFloat);
        System.out.println(levelThree.lookUp(cID));
        System.out.println(levelThree.lookUp(aID));
        System.out.println("\n\tAfter Changes");
        System.out.println(levelThree.lookUp(cID));
        System.out.println(levelThree.lookUp(aID));
        levelThree.print();

        System.out.println("\n\nChanges to Global!\n");
        System.out.println("\tBefore Changes");
        global.print();
        System.out.println(levelThree.lookUp(aID));
        global.update(aID, aValTwo);
        System.out.println("\n\tAfter Changes");
        System.out.println(levelThree.lookUp(aID));
        System.out.println("  NOTE: aID Look up of different object but same name id name");
        System.out.println(levelThree.lookUp(aIDAlso));
        global.print();

        System.out.println("\n\nLooking Up IDs and their Values!\n");

        System.out.println("\tLooking Up from Global\n");
        System.out.println(global.lookUp(gID));
        System.out.println(global.lookUp(aID));
        System.out.println(global.lookUp(cID));

        System.out.println("\n\tLooking Up from Level One\n");
        System.out.println(levelOne.lookUp(gID));
        System.out.println(levelOne.lookUp(aID));
        System.out.println(levelOne.lookUp(cID));

        System.out.println("\n\tLooking Up from Level Two\n");
        System.out.println(levelTwo.lookUp(gID));
        System.out.println(levelTwo.lookUp(aID));
        System.out.println(levelTwo.lookUp(cID));

        System.out.println("\n\tLooking Up from Level Three\n");
        System.out.println(levelThree.lookUp(gID));
        System.out.println(levelThree.lookUp(aID));
        System.out.println(levelThree.lookUp(cID));
    }
}
