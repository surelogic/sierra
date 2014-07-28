package com.surelogic.sierra.pmd;

import java.sql.Connection;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class PMD2_0_0ToolInfoGenerator extends AbstractPMDToolInfoGenerator {

    protected PMD2_0_0ToolInfoGenerator(Connection conn, String version) {
        super(conn, version);
    }

    private static final String PMD_URI = "http://pmd.sourceforge.net/ruleset/2.0.0";

    private static final String RULE = "rule";
    private static final String IGNORE = "deprecated";

    boolean ignore;

    @Override
    protected String getPMDURI() {
        return PMD_URI;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        if (RULE.equals(localName)
                && Boolean.valueOf(attributes.getValue(IGNORE))) {
            ignore = true;
        } else {
            super.startElement(uri, localName, qName, attributes);
        }
    }

    @Override
    public final void characters(char[] ch, int start, int length)
            throws SAXException {
        if (!ignore) {
            super.characters(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (RULE.equals(localName) && ignore) {
            ignore = false;
        } else {
            super.endElement(uri, localName, qName);
        }
    }

}
