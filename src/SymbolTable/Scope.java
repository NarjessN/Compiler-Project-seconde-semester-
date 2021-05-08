package SymbolTable;

public class Scope {
int  id ;
Scope parent;

    public Scope(int  id, Scope parent) {
        this.id = id;
        this.parent = parent;
    }
//map or linkedlist for symbole in the scope ....
}
