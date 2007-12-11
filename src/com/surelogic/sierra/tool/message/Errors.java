package com.surelogic.sierra.tool.message;

import java.util.Collection;

import javax.xml.bind.annotation.XmlType;


@XmlType
public class Errors {
    private Collection<Error> errors;

    public Collection<Error> getErrors() {
        return errors;
    }

    public void setErrors(Collection<Error> errors) {
        this.errors = errors;
    }
}
