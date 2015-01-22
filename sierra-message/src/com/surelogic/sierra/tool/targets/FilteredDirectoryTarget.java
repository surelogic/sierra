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
		  final String s = sb.toString();
		  //System.out.println("Compiling '"+p+"' as "+s);
		  
		  final Pattern regex = Pattern.compile(s);
		  final Matcher match = regex.matcher("");
		  result[i] = new IPattern() {
			@Override
			public boolean matches(String path) {
				match.reset(path);
				return match.matches();
			}			  
			@Override
			public String toString() {
				return p;
			}
		  };
		  i++;
	  }
	  return result;
  }
  
  private static void addToPattern(StringBuilder sb, String p) {
	  StringTokenizer st = new StringTokenizer(p, "?*", true);
	  boolean lastWasStar = false; // To handle **
	  
	  while (st.hasMoreTokens()) {
		  //System.out.println("Pattern so far: "+sb);
		  String frag = st.nextToken();
		  if (lastWasStar) {
			  lastWasStar = false;
			  
			  if (frag.equals("*")) {
				  // Found **
				  sb.append(".*");
				  continue;
			  } else {
				  sb.append("[^/]*");
				  // Now handle frag
			  }
		  }
		  if (frag.equals("?")) {
			  sb.append('.');
		  }
		  else if (frag.equals("*")) {
			  lastWasStar = true;
		  }
		  else {
			  sb.append(Pattern.quote(frag));
		  }
	  }
	  if (lastWasStar) {
		  sb.append("[^/]*");
	  }
  }
  
  @Override
  public boolean exclude(String relativePath) {
	initPatterns();
   include:
    if (includePatterns != null) {
      for(IPattern p : includePatterns) {
        if (p.matches(relativePath)) {
          return false;
          //break include; // now check exclusions
        }
      }      
      //WRONG return true; // Not included
    }
    if (excludePatterns != null) {
      for(IPattern p : excludePatterns) {
        if (p.matches(relativePath)) {
          //System.out.println("Excluded by "+p+": "+relativePath);
          return true;
        }
      }
      //System.out.println("Not excluded: "+relativePath);
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
