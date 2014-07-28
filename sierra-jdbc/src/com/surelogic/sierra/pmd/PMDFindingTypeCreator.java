package com.surelogic.sierra.pmd;

import java.io.File;
import java.io.IOException;

import com.surelogic.sierra.setup.AbstractFindingTypeCreator;

public class PMDFindingTypeCreator extends AbstractFindingTypeCreator {
    static final File missing = new File(
            "./src/com/surelogic/sierra/pmd/missingFindingTypes.txt");

    public static void main(String[] args) throws IOException {
        new PMDFindingTypeCreator().process(missing).finish();
    }

    @Override
    public void finish() throws IOException {
        // TODO Auto-generated method stub

    }
}
