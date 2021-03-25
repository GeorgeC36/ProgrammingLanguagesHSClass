package com.corbin;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Z {
    static boolean hadSyntaxError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
//        System.out.println(Arrays.toString(args));
        try {
            if (singlePathProvided(args)) runFile(args[0]);
            else {
                System.out.println("Usage: z scratchFile.z");
                System.exit(64);
            }
        } catch (IOException exception) {
            throw new IOException(exception.toString());
        }
    }

    private static void runFile(String path) throws IOException {
        String sourceCode = getSourceCodeFromFile(path);
        run(sourceCode);

        if (hadSyntaxError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    private static void run(String sourceCode) {
        Lexer lexer = new Lexer(sourceCode);
        ArrayList<Lexeme> lexemes = lexer.lex();
        printLexemes(lexemes);
        NuRecognizer recognizer = new NuRecognizer(lexemes);
    }

    private static void printLexemes(ArrayList<Lexeme> lexemes) {
        for (Lexeme lexeme : lexemes) System.out.println(lexeme);
    }

    private static String getSourceCodeFromFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        return new String(bytes, Charset.defaultCharset());
    }

    private static boolean singlePathProvided(String[] args) {
        return args.length == 1;
    }

    public static void error(int lineNumber, String message) {
        System.out.println("Cmon Man You Messed Up: " + message + " on line: " + lineNumber);
    }

    public static void error(Lexeme lexeme, String message) {
        if (lexeme.getType() == TokenType.EOF) {
            report(lexeme.getLineNumber(), "at end of file", message);
        } else {
            report(lexeme.getLineNumber(), "at '" + lexeme + "'", message);
        }
    }

    private static void report(int lineNumber, String where, String message) {
        System.err.println("[line " + lineNumber + "] Error " + where + ": " + message);
        hadSyntaxError = true;
    }
}
