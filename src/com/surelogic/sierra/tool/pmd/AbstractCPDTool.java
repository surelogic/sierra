package com.surelogic.sierra.tool.pmd;

import java.io.File;
import java.net.URI;
import java.util.*;

import net.sourceforge.pmd.cpd.CPD;
import net.sourceforge.pmd.cpd.Language;
import net.sourceforge.pmd.cpd.LanguageFactory;
import net.sourceforge.pmd.cpd.Match;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.tool.AbstractTool;
import com.surelogic.sierra.tool.AbstractToolInstance;
import com.surelogic.sierra.tool.IToolInstance;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.targets.IToolTarget;

public abstract class AbstractCPDTool extends AbstractTool {
  public AbstractCPDTool(String version) {
    super("CPD", version, "CPD", "");
  }

  @Override
  protected IToolInstance create(ArtifactGenerator generator,
                                 SLProgressMonitor monitor, boolean close) {
    return new AbstractToolInstance(this, generator, monitor, close) {
      @Override
      protected void execute() throws Exception {      
        // Modified from CPD.main()
        boolean skipDuplicateFiles = true;
        String languageString = "java";
        String encodingString = System.getProperty("file.encoding");
        int minimumTokens = 100;
        
        LanguageFactory f = new LanguageFactory();
        Language language = f.createLanguage(languageString);
        CPD cpd = new CPD(minimumTokens, language);
        cpd.setEncoding(encodingString);
        if (skipDuplicateFiles) {
            cpd.skipDuplicates();
        }
        for(IToolTarget t : getSrcTargets()) {
          for(URI loc : t.getFiles()) {
            File file = new File(loc);
            if (file.exists()) {   
              cpd.add(file);
            }
          }
        }
        cpd.go();
        Iterator<Match> matches = cpd.getMatches();
        while (matches.hasNext()) {
          Match m = matches.next();
          m.getFirstMark().getTokenSrcID();          
          m.getSecondMark().getTokenSrcID();
        }
      }
    };
  }
}
