package org.vaadin.tokenfield.client.ui;

import com.vaadin.terminal.gwt.client.communication.ServerRpc;

public interface TokenFieldServerRpc extends ServerRpc {

    public void deleteToken();
}
