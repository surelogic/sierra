package com.surelogic.sierra.tool.message;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlRootElement
@XmlType
public class Timeseries {
    private List<String> timeseries;

    public List<String> getTimeseries() {
        return timeseries;
    }

    public void setTimeseries(List<String> timeseries) {
        this.timeseries = timeseries;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) +
            ((timeseries == null) ? 0 : timeseries.hashCode());

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

        final Timeseries other = (Timeseries) obj;

        if (timeseries == null) {
            if (other.timeseries != null) {
                return false;
            }
        } else if (!timeseries.equals(other.timeseries)) {
            return false;
        }

        return true;
    }
}
