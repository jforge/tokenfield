package org.vaadin.tokenfield;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import com.vaadin.data.Container;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.AbstractSelect.NewItemHandler;
import com.vaadin.ui.Button.ClickEvent;

/*- 
 * TODO
 * 
 *  + should perhaps implement Container.Editor (or .Viewer)
 *  + possibility to [react to|stop] add and remove token actions
 *  
 -*/

/**
 * A kind of multiselect ComboBox. When the user selects a token (or inputs a
 * new token, TokenField defaults to allowing new tokens), the value is added as
 * a clickable "token button" before or after the input box. Duplicate
 * selections are not allowed.
 * <p>
 * TokenField defaults to using HorizontalLayout, but virtually any Layout can
 * be used.
 * </p>
 * <p>
 * The token buttons can be styled using a custom {@link ButtonConfigurator} -
 * the {@link DefaultButtonConfigurator} (that can be extended) sets the
 * caption, description, and stylename for the button.
 * </p>
 * <p>
 * The content of the input (ComboBox) can be bound to a Container datasource,
 * and filtering can be used. Note that the TokenField can select values that
 * are not present in the ComboBox.<br/>
 * Also note that if you use {@link #setTokenCaptionPropertyId(Object)} (to use
 * a specific property as token caption) AND allow new tokens to be input (
 * {@link #setNewTokensAllowed(boolean)}, you should probably use a custom
 * {@link NewItemHandler) ({@link #setNewTokenHandler(NewItemHandler)}) in
 * order to provide a sensible caption for the new token.
 * </p>
 * <p>
 * TokenField is a full-fledged field - it can be bound to a Property
 * datasource, and supports buffering.
 * </p>
 * <p>
 * 
 * </p>
 * <p>
 * Note that term <i>token</i> as used in the API is often interchangeable with
 * the term <i>item</i> seen elsewhere in the Vaadin API; e.g
 * {@link #setTokenCaption(Object, String)} works exactly as
 * {@link ComboBox#setItemCaption(Object, String)}, and <code>tokenId</code> is
 * the same as <code>itemId</code>.
 * </p>
 */
public class TokenField extends CustomField implements Container.Editor {

    private static final long serialVersionUID = -4718188396491718742L;

    public enum InsertPosition {
        /**
         * Tokens will be added after the input
         */
        AFTER,
        /**
         * Add tokens before the input
         */
        BEFORE
    }

    private Layout layout;

    private ComboBox cb = new ComboBox();

    private InsertPosition instertPosition = InsertPosition.BEFORE;

    private LinkedList<Button> buttons = new LinkedList<Button>();

    private ButtonConfigurator configurator = new DefaultButtonConfigurator();

    /**
     * Create a new TokenField with a caption and a {@link InsertPosition}.
     * 
     * @param caption
     *            the desired caption
     * @param insertPosition
     *            the desired insert position
     */
    public TokenField(String caption, InsertPosition insertPosition) {
        this();
        this.instertPosition = insertPosition;
        setCaption(caption);
    }

    /**
     * Create a new TokenField with a caption.
     * 
     * @param caption
     *            the desired caption
     */
    public TokenField(String caption) {
        this();
        setCaption(caption);
    }

    /**
     * Create a new TokenField.
     * 
     */
    public TokenField() {
        this(new HorizontalLayout());
        ((HorizontalLayout) layout).setSpacing(true);
        ((HorizontalLayout) layout).setWidth(null);
    }

    /**
     * Create a new TokenField with a caption and a given layout.
     * 
     * @param caption
     *            the desired caption
     * @param lo
     *            the desired layout
     */
    public TokenField(String caption, Layout lo) {
        this(lo);
        setCaption(caption);
    }

    /**
     * Create a new TokenField with a caption, a given layout, and the specified
     * token insert position.
     * 
     * @param caption
     *            the desired caption
     * @param lo
     *            the desired layout
     * @param insertPosition
     *            the desired token insert position
     */
    public TokenField(String caption, Layout lo, InsertPosition insertPosition) {
        this(lo);
        setCaption(caption);
        this.instertPosition = insertPosition;
    }

    /**
     * Create a new TokenField with the given layout, and the specified token
     * insert position.
     * 
     * @param lo
     *            the desired layout
     * @param insertPosition
     *            the desired token insert position
     */
    public TokenField(Layout lo, InsertPosition insertPosition) {
        this(lo);
        this.instertPosition = insertPosition;
    }

    /**
     * Create a new TokenField with the given layout.
     * 
     * @param lo
     *            the desired layout
     */
    public TokenField(Layout lo) {
        setStyleName("tokenfield");
        layout = lo;
        setCompositionRoot(layout);
        setWidth(null);

        cb.setImmediate(true);
        cb.setNewItemsAllowed(true);
        cb.setNullSelectionAllowed(false);
        cb.addListener(new ComboBox.ValueChangeListener() {

            private static final long serialVersionUID = 4370326413130922134L;

            public void valueChange(
                    com.vaadin.data.Property.ValueChangeEvent event) {
                final Object val = event.getProperty().getValue();
                if (val != null) {
                    Set<Object> set = (Set<Object>) getValue();
                    if (set == null) {
                        set = new LinkedHashSet<Object>();
                    }
                    if (set.contains(val)) {
                        cb.setValue(null);
                        return;
                    }
                    HashSet<Object> newSet = new LinkedHashSet<Object>(set);
                    newSet.add(val);
                    addValue(val);
                    setValue(newSet);
                    cb.setValue(null);
                }
                cb.focus();

            }
        });

        layout.addComponent(cb);

    }

    private void rebuild() {
        layout.removeAllComponents();
        if (instertPosition == InsertPosition.AFTER) {
            layout.addComponent(cb);
        }
        for (Button b2 : buttons) {
            layout.addComponent(b2);
        }
        if (instertPosition == InsertPosition.BEFORE) {
            layout.addComponent(cb);
        }
    }

    protected void setInternalValue(Object newValue) {
        System.err.println("setInternalValue(" + newValue + ")");
        super.setInternalValue(newValue);
        layout.removeAllComponents();
        buttons.clear();
        layout.addComponent(cb);
        Set<Object> vals = (Set<Object>) newValue;
        if (vals != null) {
            for (Object id : vals) {
                addValue(id);
            }
        }
    }

    private void addValue(final Object val) {
        Button b = new Button();
        configurator.configureTokenButton(this, val, b);
        b.addListener(new Button.ClickListener() {

            private static final long serialVersionUID = -1943432188848347317L;

            public void buttonClick(ClickEvent event) {
                layout.removeComponent(event.getButton());
                buttons.remove(event.getButton());
                Set<Object> set = (Set<Object>) getValue();
                LinkedHashSet<Object> newSet = new LinkedHashSet<Object>(set);
                newSet.remove(val);
                if (set == null || !newSet.containsAll(set)) {
                    setValue(newSet);
                }
            }
        });
        buttons.add(b);

        if (layout instanceof AbstractOrderedLayout) {
            ((AbstractOrderedLayout) layout).addComponent(b,
                    (instertPosition == InsertPosition.AFTER ? buttons.size()
                            : buttons.size() - 1));
        } else if (instertPosition == InsertPosition.BEFORE) {
            layout.replaceComponent(cb, b);
            layout.addComponent(cb);
        } else {
            rebuild();
        }

    }

    /**
     * Gets the current token {@link InsertPosition}.<br/>
     * The token buttons are be placed at this position, relative to the input
     * box.
     * 
     * @see #setTokenInsertPosition(InsertPosition)
     * @see InsertPosition
     * @return the current token insert position
     */
    public InsertPosition getTokenInsertPosition() {
        return instertPosition;
    }

    /**
     * Sets the token {@link InsertPosition}.<br/>
     * The token buttons will be placed at this position, relative to the input
     * box.
     * 
     * @see #getTokenInsertPosition()
     * @see InsertPosition
     */
    public void setTokenInsertPosition(InsertPosition insertPosition) {
        if (this.instertPosition != insertPosition) {
            this.instertPosition = insertPosition;
            rebuild();
        }
    }

    /**
     * Sets the Container data source used for the input box. This works exactly
     * as {@link ComboBox#setContainerDataSource(Container)}.
     * 
     * @see ComboBox#setContainerDataSource(Container)
     * @param c
     *            the token container data source
     */
    public void setContainerDataSource(Container c) {
        cb.setContainerDataSource(c);
    }

    /**
     * Gets the Container data source currently used for the input box. This
     * works exactly as {@link ComboBox#getContainerDataSource()}.
     * 
     * @see ComboBox#getContainerDataSource()
     * @return the container used to tokens
     */
    public Container getContainerDataSource() {
        return cb.getContainerDataSource();
    }

    public void setNewTokensAllowed(boolean allowNewTokens) {
        cb.setNewItemsAllowed(allowNewTokens);
    }

    public boolean isNewTokensAllowed() {
        return cb.isNewItemsAllowed();
    }

    public void setFilteringMode(int filteringMode) {
        cb.setFilteringMode(filteringMode);
    }

    public int getFilteringMode() {
        return cb.getFilteringMode();
    }

    public void focus() {
        cb.focus();
    }

    public String getInputPrompt() {
        return cb.getInputPrompt();
    }

    public String getTokenCaption(Object tokenId) {
        return cb.getItemCaption(tokenId);
    }

    public int getTokenCaptionMode() {
        return cb.getItemCaptionMode();
    }

    public Object getTokenCaptionPropertyId() {
        return cb.getItemCaptionPropertyId();
    }

    public Resource getTokenIcon(Object tokenId) {
        return cb.getItemIcon(tokenId);
    }

    public Object getTokenIconPropertyId() {
        return cb.getItemIconPropertyId();
    }

    public Collection getTokenIds() {
        return cb.getItemIds();
    }

    public NewItemHandler getNewTokenHandler() {
        return cb.getNewItemHandler();
    }

    public int getTabIndex() {
        return cb.getTabIndex();
    }

    public void setInputPrompt(String inputPrompt) {
        cb.setInputPrompt(inputPrompt);
    }

    public void setTokenCaption(Object tokenId, String caption) {
        cb.setItemCaption(tokenId, caption);
    }

    public void setTokenCaptionMode(int mode) {
        cb.setItemCaptionMode(mode);
    }

    public void setTokenCaptionPropertyId(Object propertyId) {
        cb.setItemCaptionPropertyId(propertyId);
    }

    public void setTokenIcon(Object tokenId, Resource icon) {
        cb.setItemIcon(tokenId, icon);
    }

    public void setTokenIconPropertyId(Object propertyId) {
        cb.setItemIconPropertyId(propertyId);
    }

    public void setNewTokenHandler(NewItemHandler newItemHandler) {
        cb.setNewItemHandler(newItemHandler);
    }

    public void setTabIndex(int tabIndex) {
        cb.setTabIndex(tabIndex);
    }

    @Override
    public Class<?> getType() {
        return Set.class;
    }

    public ButtonConfigurator getButtonConfigurator() {
        return configurator;
    }

    public void setButtonConfigurator(ButtonConfigurator configurator) {
        if (configurator == null) {
            this.configurator = new DefaultButtonConfigurator();
        } else {
            this.configurator = configurator;
        }
    }

    public static class DefaultButtonConfigurator implements ButtonConfigurator {

        public void configureTokenButton(TokenField source, Object tokenId,
                Button button) {
            button.setCaption(source.getTokenCaption(tokenId) + " ⊠");
            button.setIcon(source.getTokenIcon(tokenId));
            button.setDescription("Click to remove");
            button.setStyleName(Button.STYLE_LINK);
        }

    }

    public interface ButtonConfigurator {
        public void configureTokenButton(TokenField source, Object tokenId,
                Button button);
    }
}
