package com.surelogic.sierra.tool.message;

import java.util.Collection;

import javax.xml.bind.annotation.XmlType;


@XmlType
public class Artifacts {
    private Collection<Artifact> artifact;

    public Collection<Artifact> getArtifact() {
        return artifact;
    }

    public void setArtifact(Collection<Artifact> artifact) {
        this.artifact = artifact;
    }
}
