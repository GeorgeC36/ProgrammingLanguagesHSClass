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
	if (identifiers.contains(identifier)) {		// need to handle var inside loop
	    update(identifier, value);
	} else {
	    identifiers.add(identifier);
	    values.add(value);
	}
    }

    public void update(Lexeme target, Lexeme newValue) {
        for (int i = 0; i < identifiers.size(); i++) {
            if (identifiers.get(i).equals(target)) {
                if (identifiers.get(i).isConstant() && !values.get(i).equals(newValue)) {   // allow const inside loop
                    Z.error(target, "Identifier cannot be modified");
                    return;
                }
                values.set(i, newValue);
                return;
            }
        }
        if (parent != null) {
            parent.update(target, newValue);
        } else {
            Z.error(target, "Variable " + target + " is undefined and therefore cannot be updated.");
        }
    }

    public Lexeme lookUp(Lexeme target) {
        Lexeme value = softLookUp(target);
        if (value != null) {
            return value;
        }

        Z.error(target, "Variable " + target + " is undefined.");
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

    public void print() {
        System.out.println("Environment id is " + hashCode() + "\nParent id is " + (parent == null ? " none " : parent.hashCode()));
        for (int i = 0; i < identifiers.size(); i++) {
            System.out.print(identifiers.get(i).getStringValue() + "\t");
            Lexeme val = values.get(i);
            switch (val.getType()) {
                case INT:
                    System.out.println(val.getIntValue());
                    break;
                case FLOAT:
                    System.out.println(val.getFloatValue());
                    break;
                case STRING:
                    System.out.println(val.getStringValue());
                    break;
            }
        }
    }
}

