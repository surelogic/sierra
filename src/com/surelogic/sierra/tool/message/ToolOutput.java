package com.surelogic.sierra.tool.message;

import javax.xml.bind.annotation.XmlType;


@XmlType(propOrder =  {
    "metrics", "artifacts", "errors"}
)
public class ToolOutput {
    private Metrics metrics;
    private Artifacts artifacts;
    private Errors errors;

    public Metrics getMetrics() {
        return metrics;
    }

    public void setMetrics(Metrics metrics) {
        this.metrics = metrics;
    }

    public Artifacts getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(Artifacts artifacts) {
        this.artifacts = artifacts;
    }

    public Errors getErrors() {
        return errors;
    }

    public void setErrors(Errors errors) {
        this.errors = errors;
    }
}
