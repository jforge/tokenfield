package org.vaadin.tokenfield;

import java.util.Set;

import org.vaadin.tokenfield.TokenField.InsertPosition;

import com.vaadin.Application;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class TokenfieldDemo extends Application {
    @Override
    public void init() {
        final Window mainWindow = new Window("Tokenfield Demo");
        ((VerticalLayout) mainWindow.getContent()).setSpacing(true);
        setMainWindow(mainWindow);

        IndexedContainer tokens = new IndexedContainer();
        tokens.addContainerProperty("name", String.class, null);
        tokens.addItem("asd.foo@example.com").getItemProperty("name").setValue(
                "Asd Foo");
        tokens.addItem("bar.huu@example.com").getItemProperty("name").setValue(
                "Bar Huu");
        tokens.addItem("cool@example.com").getItemProperty("name").setValue(
                "Cool");
        tokens.addItem("demo@example.com").getItemProperty("name").setValue(
                "Demo");
        tokens.addItem("example@example.com").getItemProperty("name").setValue(
                "Example");

        {
            // w/ datasource, no configurator
            final TokenField f = new TokenField();
            f.setContainerDataSource(tokens);
            f.setNewTokensAllowed(false);
            f.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
            mainWindow.addComponent(f);
        }

        {
            // w/ datasource + configurator
            final TokenField f = new TokenField() {
                protected void configureTokenButton(TokenField source,
                        Object value, Button button) {
                    button.setCaption(source.getTokenCaption(value) + "<"
                            + value + ">");
                }
            };
            f.setContainerDataSource(tokens);
            f.setNewTokensAllowed(false);
            f.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
            f.setTokenCaptionPropertyId("name");

            mainWindow.addComponent(f);
        }

        {
            final TokenField f2 = new TokenField("Add Right",
                    InsertPosition.AFTER);
            f2.setNewTokensAllowed(true);
            mainWindow.addComponent(f2);
        }
        {
            final TokenField f3 = new TokenField("Add Over",
                    new VerticalLayout(), InsertPosition.BEFORE);
            f3.setNewTokensAllowed(true);
            mainWindow.addComponent(f3);
        }

        {
            final TokenField f4 = new TokenField("Add Under",
                    new VerticalLayout(), InsertPosition.AFTER);
            f4.setNewTokensAllowed(true);
            mainWindow.addComponent(f4);
        }

        {
            final TokenField f5 = new TokenField("Grid", new GridLayout(3, 3)) {
                protected void configureTokenButton(TokenField source,
                        Object value, Button button) {
                    button.setCaption("" + value);
                    button.setWidth("195px");
                }

                protected void removeToken(Object tokenId) {
                    Button b = buttons.get(tokenId);
                    getWindow().showNotification("Removed " + b.getCaption());
                    super.removeToken(tokenId);
                }

            };
            f5.setNewTokensAllowed(true);
            mainWindow.addComponent(f5);
        }

        {
            final TokenField f = new TokenField("CSS", new CssLayout()) {

                @Override
                protected void addToken(Object tokenId) {
                    Set<Object> set = (Set<Object>) getValue();
                    if (set != null && set.contains(tokenId)) {
                        getWindow().showNotification("Duplicate");
                    }
                    super.addToken(tokenId);
                }

            };
            f.setNewTokensAllowed(true);
            mainWindow.addComponent(f);
        }

        {
            ListSelect list = new ListSelect();
            mainWindow.addComponent(list);
            list.setImmediate(true);
            list.setMultiSelect(true);
            list.setContainerDataSource(tokens);

            final TokenField f6 = new TokenField();
            f6.setContainerDataSource(tokens);
            f6.setNewTokensAllowed(false);
            f6.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
            f6.setPropertyDataSource(list);
            mainWindow.addComponent(f6);
        }

    }

}
