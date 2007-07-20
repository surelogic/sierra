package com.surelogic.sierra.tool.analyzer;


public class JDTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		JDTest jdTest = new JDTest();
		jdTest.LaunchJDepend();

	}

	public void LaunchJDepend() {

		// JDepend jDepend = new JDepend();
		// try {
		// jDepend.addDirectory("C:\\work\\TigerTesting\\spscommon");
		// Collection<?> test = jDepend.analyze();
		//
		// Iterator<?> testIterator = test.iterator();
		//
		// while (testIterator.hasNext()) {
		// Object o = testIterator.next();
		//
		// if (o instanceof JavaPackage) {
		// JavaPackage javaPackage = (JavaPackage) o;
		// System.out.println("Success " + javaPackage.getName()
		// + " : " + javaPackage.instability());
		// }
		// }
		//
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
	}

}
