package org.vaadin.tokenfield.client.ui;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.ui.TextBox;
import com.vaadin.client.ui.VFilterSelect;

public class VTokenField extends VFilterSelect {

    protected boolean after = false;

    protected List<DeleteListener> listeners = new LinkedList<DeleteListener>();

    public void onKeyDown(KeyDownEvent event) {
        if (!enabled || readonly) {
            return;
        }
        int kc = event.getNativeKeyCode();
        if (kc == KeyCodes.KEY_BACKSPACE || kc == KeyCodes.KEY_DELETE) {
            if (event.getSource() instanceof TextBox
                    && "".equals(((TextBox) event.getSource()).getText())) {
                if ((kc == KeyCodes.KEY_BACKSPACE && !after)
                        || (kc == KeyCodes.KEY_DELETE && after)) {
                    fireDeleteListeners();
                    return;
                }
            }
        }

        super.onKeyDown(event);

    }

    private void fireDeleteListeners() {
        for (DeleteListener l : listeners) {
            l.onDelete();
        }
    }

    public void addListener(DeleteListener l) {
        listeners.add(l);
    }

    public void removeListener(DeleteListener l) {
        listeners.remove(l);
    }

    public interface DeleteListener {
        public void onDelete();
    }

}
