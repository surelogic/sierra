/*
 * Created on Jan 11, 2008
 */
package com.surelogic.sierra.tool;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.surelogic.common.SLProgressMonitor;
import com.surelogic.sierra.tool.message.ArtifactGenerator;
import com.surelogic.sierra.tool.message.Config;
import com.surelogic.sierra.tool.message.MessageWarehouse;

public class RemoteTool extends AbstractTool {
  public RemoteTool() {
    super("Remote", "1.0", "Remote", "Remote tool for running other tools in another JVM");
  }
  
  public Set<String> getArtifactTypes() {
    return Collections.emptySet();
  }
  
  public IToolInstance create(final Config config, final SLProgressMonitor monitor) {
    throw new UnsupportedOperationException("Generators can't be sent remotely");
  }
  
  public IToolInstance create(ArtifactGenerator generator, SLProgressMonitor monitor) {
    throw new UnsupportedOperationException("Generators can't be sent remotely");
  }

  public static void main(String[] args) {
    try {
      System.out.println("JVM started");
      JAXBContext ctx = JAXBContext.newInstance(Config.class);
      XMLInputFactory xmlif = XMLInputFactory.newInstance();         
      XMLStreamReader xmlr = xmlif.createXMLStreamReader(System.in);
      System.out.println("Created reader");
      Unmarshaller unmarshaller = ctx.createUnmarshaller();
      
      xmlr.nextTag();
      System.out.println("Finding next tag");
      xmlr.require(START_ELEMENT, null, "config");
      System.out.println("Checking for config");
      Config config = unmarshaller.unmarshal(xmlr, Config.class).getValue();
      
      //Config config = MessageWarehouse.getInstance().parseConfig(xmlr);
      /*
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      String line = br.readLine();
      while (line != null) {
        if (line.equals("\n")) {
          break;
        }
        System.out.println(line);
        line = br.readLine();
      }
      */
      System.out.println("Read config");
      System.out.println("Excluded tools = "+config.getExcludedToolsList());
    } catch (Throwable e) {
      System.out.println("Error: "+e.getMessage());
      System.exit(-1);
    }
  }
}
