package com.corbin;

import java.util.ArrayList;

public class Environments {

    // -------------- Instance Variables --------------
    private Environments parent;

    ArrayList<Lexeme> identifiers;
    ArrayList<Lexeme> values;

    // -------------- Constructor --------------

    public Environments(Environments parent) {
        this.parent = parent;
        this.identifiers = new ArrayList<>();
        this.values = new ArrayList<>();
    }

    // -------------- Public Environment Methods --------------

    public void insert(Lexeme identifier, Lexeme value) {
        identifiers.add(identifier);
        values.add(value);      // Think about re-declaring a variable (evaluator)
    }

    public void update(Lexeme target, Lexeme newValue) {
        for (int i = 0; i < identifiers.size(); i++) {
            if (identifiers.get(i).equals(target)) {
                values.set(i, newValue);
                return;
            }
        }
        if (parent != null) {
            parent.update(target, newValue);
        } else {
            Z.error(target, "Variable " + target + "is undefined and therefore cannot be updated.");
        }
    }

    public Lexeme lookUp(Lexeme target) {
        Lexeme value = softLookUp(target);
        if (value != null) {
            return value;
        }

        Z.error(target, "Variable " + target + "is undefined.");
        return null;
    }

    // -------------- Helper Methods --------------

    private Lexeme softLookUp(Lexeme target) {
        for (int i = 0; i < identifiers.size(); i++) {
            if (identifiers.get(i).equals(target)) {
                return values.get(i);
            }
        }
        if (parent != null) {
            return parent.softLookUp(target);
        } else {
            return null;
        }
    }

    // -------------- toString --------------


}
