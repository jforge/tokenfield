package org.vaadin.tokenfield.client.ui;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.ui.TextBox;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.ui.VFilterSelect;

/**
 * Client side widget which communicates with the server. Messages from the
 * server are shown as HTML and mouse clicks are sent to the server.
 */
public class VTokenField extends VFilterSelect {

    /** The client side widget identifier */
    protected String paintableId;

    /** Reference to the server connection object. */
    protected ApplicationConnection client;

    protected boolean readonly = false;
    protected boolean enabled = false;
    protected boolean after = false;

    /**
     * The constructor should first call super() to initialize the component and
     * then handle any initialization relevant to Vaadin.
     */
    public VTokenField() {
        super();
    }

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
                    client.updateVariable(paintableId, "del", true, true);
                    return;
                }
            }
        }

        super.onKeyDown(event);

    }

    /**
     * Called whenever an update is received from the server
     */
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        readonly = uidl.hasAttribute("readonly");
        enabled = !uidl.hasAttribute("disabled");
        after = uidl.hasAttribute("after");
        paintableId = uidl.getId();
        this.client = client;
        super.updateFromUIDL(uidl, client);
    }

}
