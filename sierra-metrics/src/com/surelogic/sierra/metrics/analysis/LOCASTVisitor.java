package com.surelogic.sierra.metrics.analysis;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import com.surelogic.sierra.metrics.model.Metrics;

/**
 * Visitor for counting LOC
 * 
 * @author Tanmay.Sinha
 */
public class LOCASTVisitor extends ASTVisitor {

	private static final boolean DEBUG = false;

	private long f_count = 0L;
	private final Metrics f_metrics;

	public LOCASTVisitor() {
		f_metrics = new Metrics();
	}

	@Override
	public boolean visit(AssertStatement node) {
		if (DEBUG) {
			System.out.println("Assert");
		}
		f_count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(ReturnStatement node) {
		if (DEBUG) {
			System.out.println("Return");
		}
		f_count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(BreakStatement node) {
		if (DEBUG) {
			System.out.println("Break");
		}
		f_count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		if (DEBUG) {
			System.out.println("Field" + node.toString());
		}
		f_count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(CatchClause node) {
		if (DEBUG) {
			System.out.println("Catch");
		}
		f_count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(ContinueStatement node) {
		if (DEBUG) {
			System.out.println("Continue");
		}
		f_count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(DoStatement node) {
		if (DEBUG) {
			System.out.println("Do ");
		}
		f_count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		if (DEBUG) {
			System.out.println("Enh For" + node.getExpression());
		}
		f_count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(ForStatement node) {
		if (DEBUG) {
			System.out.println("For " + node.getExpression());
		}
		f_count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(IfStatement node) {
		if (DEBUG) {
			System.out.println("If" + node.getExpression());
		}
		f_count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		if (DEBUG) {
			System.out.println("Class " + node.getName());
		}
		f_count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		if (DEBUG) {
			System.out.println("Import " + node.toString());
		}
		f_count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if (DEBUG) {
			System.out.println("Method " + node.getName());
		}
		f_count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		if (DEBUG) {
			System.out.println("Package " + node.toString());
		}
		f_metrics.setPackageName(node.getName().toString());
		f_count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		if (DEBUG) {
			System.out.println("Annotation " + node.toString());
		}
		f_count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(SwitchCase node) {
		if (DEBUG) {
			System.out.println("Case");
		}
		f_count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(SwitchStatement node) {
		if (DEBUG) {
			System.out.println("Switch");
		}
		f_count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(TryStatement node) {
		if (DEBUG) {
			System.out.println("Try");
		}
		f_count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		if (DEBUG) {
			System.out.println("Variable Declaration Statement "
					+ node.toString());
		}
		f_count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(WhileStatement node) {
		if (DEBUG)
			System.out.println("while " + node.getExpression());
		f_count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(ExpressionStatement node) {
		if (DEBUG) {
			System.out.println("Expression " + node.toString());
		}
		f_count++;
		return super.visit(node);
	}

	public Metrics getMetrics() {
		f_metrics.setLoc(f_count);
		return f_metrics;
	}
}