package com.surelogic.sierra.tool.message;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlType
public class Metrics {
    private Collection<ClassMetric> clazz;

    @XmlElement(name = "classMetric")
    public Collection<ClassMetric> getClassMetric() {
        return clazz;
    }

    public void setClassMetric(Collection<ClassMetric> clazz) {
        this.clazz = clazz;
    }
}
