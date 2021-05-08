package visitors;

import java.util.Stack;

import SymbolTable.SymboleTable;
import misc.HTMLParserBaseVisitor;

public class Visitor<T> extends HTMLParserBaseVisitor<T> {
	static public SymboleTable symboletable = new SymboleTable();
	static DocumentVisitor documentVisitor = new DocumentVisitor();
	static ExpressionVisitor expressionVisitor = new ExpressionVisitor();
}
