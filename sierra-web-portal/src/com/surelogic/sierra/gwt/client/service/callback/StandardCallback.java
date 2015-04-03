package com.surelogic.sierra.gwt.client.service.callback;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.surelogic.sierra.gwt.client.util.ExceptionUtil;

public abstract class StandardCallback<T> implements AsyncCallback<T> {

    public void onFailure(final Throwable caught) {
        ExceptionUtil.handle(caught);
        doException(caught);
    }

    public final void onSuccess(final T result) {
        doSuccess(result);
    }

    protected abstract void doSuccess(T result);

    protected void doException(final Throwable cause) {
        // override for extra exception handling
    }

}
