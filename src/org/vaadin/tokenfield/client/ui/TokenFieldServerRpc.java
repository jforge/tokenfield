package org.vaadin.tokenfield.client.ui;

import com.vaadin.shared.communication.ServerRpc;

public interface TokenFieldServerRpc extends ServerRpc {

    public void deleteToken();
}
