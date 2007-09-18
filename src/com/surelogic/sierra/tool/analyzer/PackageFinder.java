package com.surelogic.sierra.tool.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;

public class PackageFinder {

	private static final PackageFinder INSTANCE = new PackageFinder();

	public static PackageFinder getInstance() {
		return INSTANCE;
	}

	public String getPackage(File target) {

		try {
			BufferedReader in = new BufferedReader(new FileReader(target));

			StringBuffer buffer = new StringBuffer();
			String line = null;
			while (null != (line = in.readLine())) {
				buffer.append("\t" + line);
				buffer.append("\n");
			}
			in.close();

			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			final String text = buffer.toString();
			parser.setSource(text.toCharArray());
			final CompilationUnit node = (CompilationUnit) parser
					.createAST(null);

			PackageVisitor visitor = new PackageVisitor();
			node.accept(visitor);
			return visitor.getPackageName();
		} catch (IOException e) {
			// Handle exception
		}

		return null;

	}

	static class PackageVisitor extends ASTVisitor {
		private static String packageName = "Default Package";

		@Override
		public boolean visit(PackageDeclaration node) {
			packageName = node.getName().toString();
			return super.visit(node);
		}

		public String getPackageName() {
			return packageName;
		}
	}

}
