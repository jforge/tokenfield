package org.vaadin.tokenfield;

import java.util.Random;
import java.util.Set;

import org.vaadin.tokenfield.TokenField.InsertPosition;

import com.vaadin.Application;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

public class TokenfieldDemo extends Application {
    // TODO make more real-life examples:
    // - email address entry
    // - tag entry, icon + caption -> icon + tooltip

    @Override
    public void init() {
        setMainWindow(new DemoWindow());
    }

    class DemoWindow extends Window {
        DemoWindow() {
            // Just add some spacing so it looks nicer
            ((VerticalLayout) getContent()).setSpacing(true);

            {
                /*
                 * This is the most basic use case using all defaults; it's
                 * empty to begin with, the user can enter new tokens.
                 */

                Panel p = new Panel("Basic");
                addComponent(p);

                TokenField f = new TokenField("Add tags");
                p.addComponent(f);
            }

            {
                /*
                 * This example uses to selects to dynamically change the insert
                 * position and the layout used.
                 */

                Panel p = new Panel("Layout and InsertPosition");
                addComponent(p);

                HorizontalLayout controls = new HorizontalLayout();
                p.addComponent(controls);

                // generate container
                Container tokens = generateTestContainer();

                // w/ datasource, no configurator
                final TokenField f = new TokenField();
                f.setContainerDataSource(tokens);
                f.setNewTokensAllowed(false);
                f.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
                f.setInputPrompt("firstname.lastname@example.com");
                p.addComponent(f);

                final NativeSelect lo = new NativeSelect("Layout");
                lo.setImmediate(true);
                lo.addItem(HorizontalLayout.class);
                lo.addItem(VerticalLayout.class);
                lo.addItem(GridLayout.class);
                lo.addItem(CssLayout.class);
                lo.setNullSelectionAllowed(false);
                lo.setValue(f.getLayout().getClass());
                lo.addListener(new ValueChangeListener() {
                    public void valueChange(ValueChangeEvent event) {
                        try {
                            Layout l = (Layout) ((Class) event.getProperty()
                                    .getValue()).newInstance();
                            if (l instanceof GridLayout) {
                                ((GridLayout) l).setColumns(3);
                            }
                            f.setLayout(l);
                        } catch (Exception e) {
                            getMainWindow().showNotification("Ouch!",
                                    "Could not make a " + lo.getValue());
                            lo.setValue(f.getLayout().getClass());
                            e.printStackTrace();
                        }
                    }
                });
                controls.addComponent(lo);

                final NativeSelect ip = new NativeSelect("InsertPosition");
                ip.setImmediate(true);
                ip.addItem(InsertPosition.AFTER);
                ip.addItem(InsertPosition.BEFORE);
                ip.setNullSelectionAllowed(false);
                ip.setValue(f.getTokenInsertPosition());
                ip.addListener(new ValueChangeListener() {
                    public void valueChange(ValueChangeEvent event) {
                        f
                                .setTokenInsertPosition((InsertPosition) ip
                                        .getValue());
                    }
                });
                controls.addComponent(ip);

                final CheckBox cb = new CheckBox("Read-only");
                cb.setImmediate(true);
                cb.setValue(f.isReadOnly());
                cb.addListener(new ValueChangeListener() {
                    public void valueChange(ValueChangeEvent event) {
                        f.setReadOnly(cb.booleanValue());
                    }
                });
                controls.addComponent(cb);

            }

            {
                /*
                 * In this example a container with generated contacts is used.
                 * The input has filtering (a.k.a suggestions) enabled, and the
                 * added token button is configured so that it is in the
                 * standard "Name <email>" -format.
                 */

                Panel p = new Panel("Customize token button");
                addComponent(p);

                // generate container
                Container tokens = generateTestContainer();

                // we want this to be vertical
                VerticalLayout lo = new VerticalLayout();
                lo.setSpacing(true);

                final TokenField f = new TokenField(lo) {
                    protected void configureTokenButton(Object tokenId,
                            Button button) {
                        // otherwise default, but change caption
                        super.configureTokenButton(tokenId, button);
                        button.setCaption(getTokenCaption(tokenId) + "<"
                                + tokenId + ">");
                    }
                };
                f.setContainerDataSource(tokens); // datasource
                f.setNewTokensAllowed(false); // let's not add new emails now
                f.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS); // suggest
                f.setTokenCaptionPropertyId("name"); // use name in input
                f.setInputPrompt("Contact name");
                p.addComponent(f);
            }

            {
                /*
                 * Here, onTokenInput() and onTokenClicked() is customized.
                 */
                Panel p = new Panel("Add / remove actions");
                addComponent(p);

                final TokenField f = new TokenField() {

                    protected void onTokenInput(Object tokenId) {
                        Set<Object> set = (Set<Object>) getValue();
                        if (set != null && set.contains(tokenId)) {
                            getWindow().showNotification("Duplicate");
                        } else {
                            getWindow().showNotification("Added");
                            super.addToken(tokenId);
                        }
                    }

                    protected void onTokenClicked(final Object tokenId) {
                        final TokenField field = this;
                        final Window w = new Window("Are you sure?");
                        w.setStyleName("light");
                        w.setResizable(false);
                        w.center();
                        w.setModal(true);
                        getWindow().addWindow(w);

                        Button remove = new Button("Remove "
                                + field.getTokenCaption(tokenId),
                                new Button.ClickListener() {
                                    public void buttonClick(ClickEvent event) {
                                        field.removeToken(tokenId);
                                        DemoWindow.this.removeWindow(w);
                                    }
                                });
                        w.addComponent(remove);

                        Button cancel = new Button("Cancel",
                                new Button.ClickListener() {
                                    public void buttonClick(ClickEvent event) {
                                        DemoWindow.this.removeWindow(w);
                                    }
                                });
                        cancel.setStyleName(Button.STYLE_LINK);
                        w.addComponent(cancel);

                    }
                };
                f.setNewTokensAllowed(true);
                p.addComponent(f);
            }

            {
                Panel p = new Panel("Data binding (property data source)");
                addComponent(p);

                // generate container
                Container tokens = generateTestContainer();

                // just for layout; ListSelect left, TokenField right
                HorizontalLayout lo = new HorizontalLayout();
                lo.setSpacing(true);
                p.setContent(lo);

                // A regular list select
                ListSelect list = new ListSelect();
                p.addComponent(list);
                list.setImmediate(true);
                list.setMultiSelect(true);
                list.setContainerDataSource(tokens);

                // TokenField bound to the ListSelect above, CssLayout so that
                // it wraps nicely.
                final TokenField f = new TokenField(new CssLayout());
                f.setContainerDataSource(tokens);
                // f.setNewTokensAllowed(false);
                f.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
                f.setPropertyDataSource(list);
                p.addComponent(f);
            }
        }
    }

    /* Used to generate example contents */
    private static final String[] firstnames = new String[] { "John", "Mary",
            "Joe", "Sarah", "Jeff", "Jane", "Peter", "Marc", "Robert", "Paula",
            "Lenny", "Kenny", "Nathan", "Nicole", "Laura", "Jos", "Josie",
            "Linus" };
    private static final String[] lastnames = new String[] { "Torvalds",
            "Smith", "Adams", "Black", "Wilson", "Richards", "Thompson",
            "McGoff", "Halas", "Jones", "Beck", "Sheridan", "Picard", "Hill",
            "Fielding", "Einstein" };

    private Container generateTestContainer() {

        IndexedContainer tokens = new IndexedContainer();
        tokens.addContainerProperty("name", String.class, null);

        Random r = new Random(5);
        for (int i = 0; i < 50;) {
            String fn = firstnames[(int) (r.nextDouble() * firstnames.length)];
            String ln = lastnames[(int) (r.nextDouble() * lastnames.length)];
            String name = fn + " " + ln;
            String email = fn.toLowerCase() + "." + ln.toLowerCase()
                    + "@example.com";
            Item item = tokens.addItem(email);
            if (item != null) {
                i++;
                item.getItemProperty("name").setValue(name);
            }
        }
        return tokens;
    }

}
