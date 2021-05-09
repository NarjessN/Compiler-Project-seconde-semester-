package visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import SymbolTable.Scope;
import SymbolTable.SymboleTable;
import misc.HTMLParser.ArrayLoopRawContext;
import misc.HTMLParser.AttributeNodeContext;
import misc.HTMLParser.BodyContext;
import misc.HTMLParser.CommentContext;
import misc.HTMLParser.CommentNodeContext;
import misc.HTMLParser.ConditionalCommentContext;
import misc.HTMLParser.DefaultDirectiveContext;
import misc.HTMLParser.DocumentContext;
import misc.HTMLParser.ElementAttributesContext;
import misc.HTMLParser.ElementContentContext;
import misc.HTMLParser.ExpDirectiveContext;
import misc.HTMLParser.FilterExpressionContext;
import misc.HTMLParser.HalfElementContext;
import misc.HTMLParser.HeaderContext;
import misc.HTMLParser.IndexedArrayLoopContext;
import misc.HTMLParser.LoopStatementContext;
import misc.HTMLParser.MustacheContext;
import misc.HTMLParser.NodeContext;
import misc.HTMLParser.NormalElementContext;
import misc.HTMLParser.ParametrizedFilterContext;
import misc.HTMLParser.RawFilterContext;
import misc.HTMLParser.RawObjectLoopContext;
import misc.HTMLParser.ScriptContext;
import misc.HTMLParser.ScriptletOrSeaWsContext;
import misc.HTMLParser.SelfClosedElementContext;
import misc.HTMLParser.StmtDirectiveContext;
import misc.HTMLParser.StyleContext;
import misc.HTMLParser.TextNodeContext;
import models.*;
import models.directive.*;
import models.documents.*;
import models.enums.CommentType;
import models.expression.Expression;
import models.expression.ValueExpression;
import models.nodes.*;
import models.statements.*;


public class DocumentVisitor extends Visitor<AbstractASTNode>{ 

	protected static Stack<Boolean> switchExists;
	private Stack<Scope> scopesStack = new Stack<>();
	public DocumentVisitor() {
		if (switchExists == null)
			switchExists = new Stack<Boolean>();

	}
	@Override
	public AbstractASTNode visitScript(ScriptContext ctx) {
		return new ScriptNode(ctx.getText());
	}

	@Override
	public AbstractASTNode visitStyle(StyleContext ctx) {
		return new StyleNode(ctx.getText());
	}

	@Override
	public AbstractASTNode visitDocument(DocumentContext ctx) {
		System.out.print("in the visitDocument ");
		Scope s = new Scope(null);
		s.setId("global");
		scopesStack.push(s);
		System.out.print("print symbole table ");
		DocumentHeader header = (DocumentHeader) visit(ctx.getChild(0));
		DocumentBody body = (DocumentBody) visit(ctx.getChild(1));
		this.showSymboleTable();
		return new Document(header, body);
	}

	@Override
	public AbstractASTNode visitHeader(HeaderContext ctx) {
		System.out.print("in the  visitHeader");
		List<DocumentNode> headers = new ArrayList<DocumentNode>();
		DocumentHeader header = new DocumentHeader(headers);
		for (int index = 0; index < ctx.getChildCount(); index++) {
			if (ctx.getChild(index) instanceof CommentNodeContext)
				headers.add((DocumentNode) visit(ctx.getChild(index)));
			else
				headers.add(new TextNode(ctx.getChild(index).getText()));
		}
		return header;
	}

	@Override
	public AbstractASTNode visitComment(CommentContext ctx) {
		System.out.print("in the visitComment");
		return new CommentNode(ctx.HTML_COMMENT().getText(), CommentType.Normal);
	}

	@Override
	public AbstractASTNode visitConditionalComment(ConditionalCommentContext ctx) {
		System.out.print("in the  visitConditionalComment");
		return new CommentNode(ctx.HTML_CONDITIONAL_COMMENT().getText(), CommentType.Conditional);
	}

	@Override
	public AbstractASTNode visitBody(BodyContext ctx) {
		System.out.print("in the visitBody");
		List<DocumentNode> contents = new ArrayList<DocumentNode>();
		DocumentBody body = new DocumentBody(contents);
		for (int index = 0; index < ctx.getChildCount(); index++)
			contents.add((DocumentNode) visit(ctx.getChild(index)));
		return body;
	}

	@Override
	public AbstractASTNode visitExpDirective(ExpDirectiveContext ctx) {
		System.out.print("in the visitExpDirective");
	// if condition to know what the directive we have

        if(ctx.getChild(0).getText().equals("cp-if") ) {
			Scope scope = new Scope(scopesStack.peek());
			scope.setId(ctx.getChild(0).getText()+"_"+scope.hashCode());
			this.symboletable.addScope(scope);
			scopesStack.push(scope);

		}
		AbstractASTNode value = expressionVisitor.visit(ctx.getChild(3));

		return new Directive(ctx.getChild(0).getText(), value);
	}

	@Override
	public AbstractASTNode visitStmtDirective(StmtDirectiveContext ctx) {
		System.out.print("in the visitStmtDirective ");
		AbstractASTNode value = visit(ctx.getChild(3));
		return new Directive(ctx.getChild(0).getText(), value);

	}

	@Override
	public AbstractASTNode visitLoopStatement(LoopStatementContext ctx) {
		return visit(ctx.getChild(0));
	}

	@Override
	public AbstractASTNode visitArrayLoopRaw(ArrayLoopRawContext ctx) {
		System.out.print("in the visitArrayLoopRaw ");
		AbstractASTNode arrayToLoopOn = expressionVisitor.visit(ctx.getChild(2));
		String loopVariable = ctx.getChild(0).getText();
		return new ArrayLoopStatement(loopVariable, (ValueExpression) arrayToLoopOn);
	}

	@Override
	public AbstractASTNode visitIndexedArrayLoop(IndexedArrayLoopContext ctx) {
		System.out.print("in the visitIndexedArrayLoop ");
		AbstractASTNode arrayToLoopOn = expressionVisitor.visit(ctx.getChild(2));
		String loopVariable = ctx.getChild(0).getText();
		String indexVariable = ctx.getChild(4).getText();
		return new ArrayLoopStatement(loopVariable, indexVariable, (ValueExpression) arrayToLoopOn);
	}

	@Override
	public AbstractASTNode visitDefaultDirective(DefaultDirectiveContext ctx) {
		System.out.print("in the visitDefaultDirective ");
		return new Directive(ctx.getChild(0).getText());
	}

	@Override
	public AbstractASTNode visitScriptletOrSeaWs(ScriptletOrSeaWsContext ctx) {
		return new TextNode(ctx.getText());
	}

	@Override
	public AbstractASTNode visitRawObjectLoop(RawObjectLoopContext ctx) {
		System.out.print("in the visitRawObjectLoop ");
		AbstractASTNode objectToLoopOn = expressionVisitor.visit(ctx.getChild(4));
		String KeyVariable = ctx.getChild(0).getText();
		String valueVariable = ctx.getChild(2).getText();
		return new ObjectLoopStatement(KeyVariable, valueVariable, (ValueExpression) objectToLoopOn);
	}

	@Override
	public AbstractASTNode visitFilterExpression(FilterExpressionContext ctx) {
		System.out.print("in the visitFilterExpression ");
		Expression oprand = expressionVisitor.visit(ctx.getChild(0));
		FilterStatement filter = (FilterStatement) visit(ctx.getChild(1));
		filter.setOprand(oprand);
		return filter;
	}

	@Override
	public AbstractASTNode visitRawFilter(RawFilterContext ctx) {
		System.out.print("in the visitRawFilter ");
		AbstractASTNode filter = expressionVisitor.visit(ctx.getChild(1));
		return new FilterStatement((Expression) filter);
	}

	@Override
	public AbstractASTNode visitParametrizedFilter(ParametrizedFilterContext ctx) {
		System.out.print("in the visitParametrizedFilter ");
		AbstractASTNode filter = expressionVisitor.visit(ctx.getChild(1));
		List<Expression> parameters = new ArrayList<Expression>();
		for (int index = 0; index < ctx.getChild(3).getChildCount(); index += 2 ) {
			parameters.add(expressionVisitor.visit(ctx.getChild(3).getChild(index)));
		}
		return new FilterStatement((Expression) filter, parameters);
	}

	@Override
	public AbstractASTNode visitNode(NodeContext ctx) {
		System.out.print("in the visitNode ");
		return visit(ctx.getChild(0));
	}

	public List<AbstractASTNode> getContent(ElementContentContext ctx) {
		System.out.print("in the getContent ");
		List<AbstractASTNode> contents = new ArrayList<AbstractASTNode>();
		for (int index = 0; index < ctx.getChildCount(); index++) {
			if (ctx.getChild(index) instanceof NodeContext || ctx.getChild(index) instanceof MustacheContext)
				contents.add(visit(ctx.getChild(index)));

			else 
				contents.add(new TextNode(ctx.getChild(index).getText()));
		}

		return contents;
	}

	@Override
	public AbstractASTNode visitHalfElement(HalfElementContext ctx) {
		System.out.print("in the visitHalfElement ");
		String tagName = ctx.getChild(1).getText();
		List<AbstractASTNode> attributes = new ArrayList<AbstractASTNode>();
		
		if (ctx.getChild(2) instanceof ElementAttributesContext)
			attributes = getAttributes((ElementAttributesContext) ctx.getChild(2));
		
		ElementNode element = new ElementNode(tagName, attributes.toArray(new DocumentNode[attributes.size()]));
		return element;
	}
 // still understanding it
	@Override
	public AbstractASTNode visitNormalElement(NormalElementContext ctx) {
		System.out.print("in the visitNormalElement ");
		String tagName = ctx.getChild(1).getText();
		System.out.print(tagName);
		String tagClose;
		if (ctx.getChild(2) instanceof ElementAttributesContext) {
			tagClose = ctx.getChild(6).getText();
		} else
			tagClose = ctx.getChild(5).getText();
		if (! testName(tagName, tagClose))
			System.err.println("name does not match!");
		List<AbstractASTNode> attributes = new ArrayList<AbstractASTNode>();
		List<AbstractASTNode> contents = new ArrayList<AbstractASTNode>();;
		
		if (ctx.getChild(2) instanceof ElementAttributesContext)
			attributes = getAttributes((ElementAttributesContext) ctx.getChild(2));
//		System.out.print("print the attribute we have here ....."+attributes.get(0).toString());
//		if(attributes.size()!=0)
//		{
//			for(int i=0;i<attributes.size();i++)
//			System.out.print("print the attribut we have here ....."+attributes.get(i));
//		}
		boolean switchElement = false;
		for (AbstractASTNode node: attributes)
			if (node instanceof Directive && testName(((Directive) node).getName(), "cp-switch")) {
				switchExists.push(true);
				switchElement = true;
			}
		for (AbstractASTNode node: attributes)
			if (node instanceof Directive && (testName(((Directive) node).getName(), "cp-switch-case")||testName(((Directive) node).getName(), "cp-switchDefault")))
				if (switchExists.isEmpty())
					System.err.println("Invalid switch");
		if (ctx.getChild(3) instanceof ElementContentContext)
			contents = getContent((ElementContentContext) ctx.getChild(3));
		if (ctx.getChild(4) instanceof ElementContentContext)
			contents = getContent((ElementContentContext) ctx.getChild(4));
		if (switchElement)
			switchExists.pop();
		ElementNode element = new ElementNode(tagName, attributes.toArray(new DocumentNode[attributes.size()]), contents.toArray(new DocumentNode[contents.size()]));
		if(!scopesStack.peek().getId().equals("global"))
			scopesStack.pop();

		return element;
	}

	@Override
	public AbstractASTNode visitSelfClosedElement(SelfClosedElementContext ctx) {
		System.out.print("in the visitSelfClosedElement ");
		String tagName = ctx.getChild(1).getText();
		List<AbstractASTNode> attributes = new ArrayList<AbstractASTNode>();
		
		if (ctx.getChild(2) instanceof ElementAttributesContext)
			attributes = getAttributes((ElementAttributesContext) ctx.getChild(2));
		ElementNode element = new ElementNode(tagName, attributes.toArray(new DocumentNode[attributes.size()]));

		return element;
	}

	@Override
	public AbstractASTNode visitTextNode(TextNodeContext ctx) {


		return new TextNode(ctx.getText());
	}

	//help to store the visit the node "element attribute" and store in list {store attribute and directives }
	public List<AbstractASTNode> getAttributes(ElementAttributesContext ctx) {
		System.out.print("in the getAttributes  trying to do somthing ");
		List<AbstractASTNode> attributes = new ArrayList<AbstractASTNode>();
		for (int index = 0; index < ctx.getChildCount(); index++) {
			attributes.add(visit(ctx.getChild(index)));
		}
//		if(attributes.size()!=0)
//for(int i=0;i<attributes.size();i++)
//{
//System.out.print("try to go inside the attribute "+attributes.get(i));
//}
		return attributes;
	}

	@Override
	// help to visit and store name and vale for the attribute node like class id datamodel
	public AbstractASTNode visitAttributeNode(AttributeNodeContext ctx) {
		System.out.print("in the visitAttributeNode ");
		String name = ctx.getChild(0).getText();
		String value = null;
		if (ctx.getChildCount() > 1)
			value = ctx.getChild(2).getText();
//		System.out.print(name);
//		System.out.print(value);
		return new AttributeNode(name, value);
	}

	@Override
	public AbstractASTNode visitMustache(MustacheContext ctx) {
		System.out.print("in the visitMustache ");
		MustachNode mustache;
		if (ctx.getChildCount() > 2) {
			mustache = new MustachNode(expressionVisitor.visit(ctx.getChild(1)));
		}
		else
			mustache = new MustachNode();
		return mustache;
	}
	
	protected boolean testName(String openTag, String closeTag) {
		return openTag.compareToIgnoreCase(closeTag) == 0;
	}

}