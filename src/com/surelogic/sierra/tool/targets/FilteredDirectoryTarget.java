package com.surelogic.sierra.tool.targets;

import java.net.URI;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
public class FilteredDirectoryTarget extends DirectoryTarget {
  /**
   * For JAXB
   */
  public FilteredDirectoryTarget() {}
  private String[] inclusions; 
  private String[] exclusions; 
  
  public FilteredDirectoryTarget(Type type, URI loc, String[] inclusions, String[] exclusions) {
    super(type, loc);
    this.inclusions = inclusions;
    this.exclusions = exclusions;
  }
  
  private boolean initialized = false;
  private IPattern[] includePatterns = null;
  private IPattern[] excludePatterns = null;
  
  private synchronized void initPatterns() {
	  if (initialized) {
		  return;
	  }
	  initialized = true;
	  if (inclusions != null) {
		  includePatterns = createPatterns(inclusions);
	  }
      if (exclusions != null) {
		  excludePatterns = createPatterns(exclusions);
      }
  }
	  /*
	          if (relativePath.startsWith(ex)) {
	            break include; // now check exclusions
	          }
	        }      
	        return true; // Not included
	      }
	          if (relativePath.startsWith(ex)) {
	            return true;
	          }
	        }
	      }  
	      */
  
  private interface IPattern {
	  boolean matches(String path);
  }
  
  private static IPattern[] createPatterns(String[] patterns) {
	  IPattern[] result = new IPattern[patterns.length];
	  int i = 0;
	  StringBuilder sb = new StringBuilder();
	  for(final String p : patterns) {
		  sb.setLength(0);
		  if (p.startsWith("**/")) {
			  sb.append(".*"); // Match any prefix
			  addToPattern(sb, p.substring(2));
			  sb.append('$');  // Match the end
		  } else {
			  sb.append('^');  // Match the beginning
			  addToPattern(sb, p);
			  sb.append(".*"); // Match any suffix
		  }
		  final Pattern regex = Pattern.compile(sb.toString());
		  final Matcher match = regex.matcher("");
		  result[i] = new IPattern() {
			public boolean matches(String path) {
				match.reset(path);
				return match.matches();
			}			  
		  };
		  i++;
	  }
	  return result;
  }
  
  private static void addToPattern(StringBuilder sb, String p) {
	  StringTokenizer st = new StringTokenizer(p, "?*", true);
	  while (st.hasMoreTokens()) {
		  String frag = st.nextToken();
		  if (frag.equals("?")) {
			  sb.append('.');
		  }
		  else if (frag.equals("*")) {
			  sb.append("[^/]*");
		  }
		  else {
			  sb.append(Pattern.quote(frag));
		  }
	  }
  }
  
  public boolean exclude(String relativePath) {
	initPatterns();
   include:
    if (includePatterns != null) {
      for(IPattern p : excludePatterns) {
        if (p.matches(relativePath)) {
          break include; // now check exclusions
        }
      }      
      return true; // Not included
    }
    if (excludePatterns != null) {
      for(IPattern p : excludePatterns) {
        if (p.matches(relativePath)) {
          return true;
        }
      }
    }
    return false;
  }
  
  /**
   * For JAXB 
   */
  public void setExclusions(String[] ex) {
    exclusions = ex;
  }
    
  public String[] getExclusions() {
    return exclusions;
  }
  public void setInclusions(String[] ex) {
	inclusions = ex;
  }

  public String[] getInclusions() {
	return inclusions;
  }
}
