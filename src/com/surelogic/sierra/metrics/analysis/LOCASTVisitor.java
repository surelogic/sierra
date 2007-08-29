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

public class LOCASTVisitor extends ASTVisitor {

	private long count = 0L;
	private Metrics metrics;
	private boolean reflect = false;

	public LOCASTVisitor(boolean reflect) {
		this.reflect = reflect;
		metrics = new Metrics();
	}

	@Override
	public boolean visit(AssertStatement node) {
		if (reflect) {
			System.out.println("Assert");
		}

		count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(ReturnStatement node) {
		if (reflect) {
			System.out.println("Return");
		}
		count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(BreakStatement node) {
		if (reflect) {
			System.out.println("Break");
		}
		count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		if (reflect) {
			System.out.println("Field" + node.toString());
		}
		count++;
		return super.visit(node);
	}

	// @Override
	// public boolean visit(AnnotationTypeDeclaration node) {
	// System.out.println("Annotation" + node.toString());
	// count++;
	// return super.visit(node);
	// }
	//
	// @Override
	// public boolean visit(AnnotationTypeMemberDeclaration node) {
	// if (reflect) System.out.println("Annotation type mem" + node.toString());
	// count++;
	// return super.visit(node);
	// }

	@Override
	public boolean visit(CatchClause node) {
		if (reflect) {
			System.out.println("Catch");
		}
		count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(ContinueStatement node) {
		if (reflect) {
			System.out.println("Continue");
		}
		count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(DoStatement node) {
		if (reflect) {
			System.out.println("Do ");
		}
		count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		if (reflect) {
			System.out.println("Enh For" + node.getExpression());
		}
		count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(ForStatement node) {
		if (reflect) {
			System.out.println("For " + node.getExpression());
		}
		count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(IfStatement node) {
		if (reflect) {
			System.out.println("If" + node.getExpression());
		}
		count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		if (reflect) {
			System.out.println("Class " + node.getName());
		}
		count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		if (reflect) {
			System.out.println("Import " + node.toString());
		}
		count++;
		return super.visit(node);
	}

	// @Override
	// public boolean visit(CompilationUnit node) {
	// if (reflect) System.out.println("Class ");
	// count++;
	// return super.visit(node);
	// }

	@Override
	public boolean visit(MethodDeclaration node) {
		if (reflect) {
			System.out.println("Method " + node.getName());
		}
		count++;
		return super.visit(node);
	}

	// @Override
	// public boolean visit(NormalAnnotation node) {
	// if (reflect) System.out.println("Annotation " + node.toString());
	// count++;
	// return super.visit(node);
	// }

	@Override
	public boolean visit(PackageDeclaration node) {
		if (reflect) {
			System.out.println("Package " + node.toString());
		}
		metrics.setPackageName(node.getName().toString());
		count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		if (reflect) {
			System.out.println("Annotation " + node.toString());
		}
		count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(SwitchCase node) {
		if (reflect) {
			System.out.println("Case");
		}
		count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(SwitchStatement node) {
		if (reflect) {
			System.out.println("Switch");
		}
		count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(TryStatement node) {
		if (reflect) {
			System.out.println("Try");
		}
		count++;
		return super.visit(node);
	}

	// @Override
	// public boolean visit(VariableDeclarationExpression node) {
	// if (reflect) System.out
	// .println("Variable Declaration Expression " + node.toString());
	//
	// return super.visit(node);
	// }

	// @Override
	// public boolean visit(VariableDeclarationFragment node) {
	// if (reflect) System.out.println("Variable Declaration Fragment " +
	// node.toString());
	// count++;
	// return super.visit(node);
	// }

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		if (reflect) {
			System.out.println("Variable Declaration Statement "
					+ node.toString());
		}
		count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(WhileStatement node) {
		if (reflect)
			System.out.println("while " + node.getExpression());
		count++;
		return super.visit(node);
	}

	@Override
	public boolean visit(ExpressionStatement node) {
		if (reflect) {
			System.out.println("Expression " + node.toString());
		}
		count++;
		return super.visit(node);
	}

	public Metrics getMetrics() {
		metrics.setLoc(count);
		return metrics;
	}
}