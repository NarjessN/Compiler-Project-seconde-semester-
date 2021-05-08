package SymbolTable;

import java.util.ArrayList;

public class SymboleTable {
    private ArrayList<Scope> scopes = new ArrayList();
    public void addScope(Scope scope) {
        this.scopes.add(scope);
    }
}
