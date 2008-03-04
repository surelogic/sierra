package com.surelogic.sierra.tool.message;

import java.util.Collection;

import javax.xml.bind.annotation.XmlType;


@XmlType
public class Errors {
    private Collection<Error> error;

    public Collection<Error> getError() {
        return error;
    }

    public void setErrors(Collection<Error> error) {
        this.error = error;
    }
}
