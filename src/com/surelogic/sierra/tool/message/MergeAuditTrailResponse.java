package com.surelogic.sierra.tool.message;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType
@XmlRootElement
public class MergeAuditTrailResponse {
    private List<MergeResponse> trail;

    public List<MergeResponse> getTrail() {
        if (trail == null) {
            trail = new ArrayList<MergeResponse>();
        }

        return trail;
    }

    public void setTrail(List<MergeResponse> trail) {
        this.trail = trail;
    }
}
