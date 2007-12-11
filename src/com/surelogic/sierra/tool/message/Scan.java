package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlRootElement
@XmlType(propOrder =  {
    "uid", "toolOutput", "config"}
)
public class Scan {
    private String uid;
    private Config config;
    private ToolOutput toolOutput;

    public Scan() {
        // Nothing to do
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public ToolOutput getToolOutput() {
        return toolOutput;
    }

    public void setToolOutput(ToolOutput toolOutput) {
        this.toolOutput = toolOutput;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) +
            ((toolOutput == null) ? 0 : toolOutput.hashCode());
        result = (prime * result) + ((config == null) ? 0 : config.hashCode());

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final Scan other = (Scan) obj;

        if (toolOutput == null) {
            if (other.toolOutput != null) {
                return false;
            }
        } else if (!toolOutput.equals(other.toolOutput)) {
            return false;
        }

        if (config == null) {
            if (other.config != null) {
                return false;
            }
        }

        return true;
    }
}
