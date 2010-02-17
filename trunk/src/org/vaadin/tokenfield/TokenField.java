package org.vaadin.tokenfield;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.AbstractSelect.NewItemHandler;
import com.vaadin.ui.Button.ClickEvent;

/**
 * 
 * A kind of multiselect ComboBox. When the user selects a token (or inputs a
 * new token, TokenField defaults to allowing new tokens), the value is added as
 * a clickable "token button" before or after the input box. Duplicate
 * selections are not allowed.
 * 
 * <p>
 * TokenField defaults to using HorizontalLayout, but virtually any Layout can
 * be used.
 * </p>
 * 
 * <p>
 * Can be customized in several ways by overriding certain methods. When the
 * user select or enters a new token, the following happens:
 * <ul>
 * <li>If the token is new (not in the container) and new tokens are not allowed
 * ( {@link #setNewTokensAllowed(boolean) }), nothing happens - otherwise</li>
 * <li>{@link #onTokenInput(Object)} is called, by default it just calls</li>
 * <li>{@link #addToken(Object)} which will eventually cause a call to</li>
 * <li>{@link #configureTokenButton(Object, Button)}</li>
 * <li>finally, if the token is new, it's added to the container if
 * {@link #setRememberNewTokens(boolean)} is on - this means previous method
 * calls can know whether or not the token is new by examining the container.</li>
 * </ul>
 * Custom functionality when adding and removing tokens, such as showing a
 * notification for duplicates or confirming removal, is done by overriding
 * {@link #onTokenInpu(Object)} and {@link #onTokenClicked(Object)}
 * respectively.<br/>
 * The token buttons can be styled customized by overriding
 * {@link #configureTokenButton(TokenField, Object, Button).
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
 * 
 * <p>
 * TokenField is a full-fledged field - it can be bound to a Property
 * datasource, and supports buffering.
 * </p>
 * 
 * <p>
 * Note "token" and "tokenId" is often used interchangeably in the documentation
 * - usually the token is just a string that is the id as well. The term
 * <i>Token</i> as used in the method names is often interchangeable with the
 * term <i>item</i> seen elsewhere in the Vaadin API; e.g
 * {@link #setTokenCaption(Object, String)} works exactly as
 * {@link ComboBox#setItemCaption(Object, String)}, and <code>tokenId</code> is
 * the same as <code>itemId</code>.
 * </p>
 * 
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

    public static final String STYLE_TOKENFIELD = "tokenfield";
    public static final String STYLE_TOKENTEXTFIELD = "tokentextfield";
    
    public static final String STYLE_BUTTON_EMPHAZISED = "emphasize";
    
    /**
     * The layout currently in use
     */
    protected Layout layout;

    /**
     * The ComboBox used for input - should probably not be touched.
     */
    protected ComboBox cb = new ComboBox();

    /**
     * Current insert position
     */
    protected InsertPosition insertPosition = InsertPosition.BEFORE;

    /**
     * Maps the tokenId (itemId) to the token button
     */
    protected LinkedHashMap<Object, Button> buttons = new LinkedHashMap<Object, Button>();

    protected boolean rememberNewTokens = true;

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
        this.insertPosition = insertPosition;
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
        this.insertPosition = insertPosition;
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
        this.insertPosition = insertPosition;
    }

    /**
     * Create a new TokenField with the given layout.
     * 
     * @param lo
     *            the desired layout
     */
    public TokenField(Layout lo) {
        setStyleName(STYLE_TOKENFIELD + " " + STYLE_TOKENTEXTFIELD);

        cb.setImmediate(true);
        cb.setNewItemsAllowed(true);
        cb.setNullSelectionAllowed(false);
        cb.addListener(new ComboBox.ValueChangeListener() {

            private static final long serialVersionUID = 4370326413130922134L;

            public void valueChange(
                    com.vaadin.data.Property.ValueChangeEvent event) {
                final Object tokenId = event.getProperty().getValue();
                if (tokenId != null) {
                    onTokenInput(tokenId);
                }
                cb.setValue(null);
                cb.focus();
            }
        });

        cb.setNewItemHandler(new NewItemHandler() {

            private static final long serialVersionUID = 1L;

            // This is essentially what the ComboBox.DefaultNewItemHandler does,
            // but we'll first delegate adding token button, then add to
            // container.
            public void addNewItem(String tokenId) {
                if (isReadOnly()) {
                    throw new Property.ReadOnlyException();
                }
                onTokenInput(tokenId);
                if (rememberNewTokens) {
                    if (cb.addItem(getTokenCaption(tokenId)) != null) {
                        // Sets the caption property, if used
                        if (getTokenCaptionPropertyId() != null) {
                            try {
                                cb.getContainerProperty(tokenId,
                                        getTokenCaptionPropertyId()).setValue(
                                        tokenId);
                            } catch (final Property.ConversionException ignored) {
                                /*
                                 * The conversion exception is safely ignored,
                                 * the caption is just missing
                                 */
                            }
                        }
                    }
                }
            }

        });

        setLayout(lo);

    }

    /*
     * Rebuilds from scratch
     */
    private void rebuild() {
        layout.removeAllComponents();
        if (!isReadOnly() && insertPosition == InsertPosition.AFTER) {
            layout.addComponent(cb);
        }
        for (Button b2 : buttons.values()) {
            layout.addComponent(b2);
        }
        if (!isReadOnly() && insertPosition == InsertPosition.BEFORE) {
            layout.addComponent(cb);
        }
        if (layout instanceof HorizontalLayout) {
            ((HorizontalLayout) layout).setExpandRatio(cb, 1.0f);
        }
    }

    /*
     * Essentially rebuilds from scratch when the internal value is set; this
     * could be more intelligent, but since only the simplest additions can be
     * made w/o rebuild, this is ok for now.
     * 
     * @see org.vaadin.tokenfield.CustomField#setInternalValue(java.lang.Object)
     */
    protected void setInternalValue(Object newValue) {
        super.setInternalValue(newValue);
        layout.removeAllComponents();
        buttons.clear();
        layout.addComponent(cb);
        Set<Object> vals = (Set<Object>) newValue;
        if (vals != null) {
            for (Object id : vals) {
                addTokenButton(id);
            }
        }
    }

    /**
     * Called when the user is adding a new token via the UI; called after the
     * newItemHandler. Can be used to make customize the adding process; e.g to
     * notify that the token was not added because it's duplicate, to ask for
     * additional information, or to dissalow addition due to some heuristics
     * (not both A and Q).<br/>
     * The default is to call {@link #addToken(Object)} which will add the token
     * if it's not a duplicate.
     * 
     * @param tokenId
     *            the token id selected (or input)
     */
    protected void onTokenInput(Object tokenId) {
        addToken(tokenId);
    }

    /**
     * Called when the token button is clicked, which by default removes the
     * token by calling {@link #removeToken(Object)}. The behavior can be
     * customized, e.g present a confirmation dialog.
     * 
     * @param tokenId
     *            the id of the token that was clicked
     */
    protected void onTokenClicked(Object tokenId) {
        removeToken(tokenId);
    }

    private void addTokenButton(final Object val) {
        Button b = new Button();
        configureTokenButton(val, b);
        b.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = -1943432188848347317L;

            public void buttonClick(ClickEvent event) {
                onTokenClicked(val);
            }
        });
        buttons.put(val, b);

        if (insertPosition == InsertPosition.BEFORE) {
            layout.replaceComponent(cb, b);
            layout.addComponent(cb);
        } else {
            layout.addComponent(b);
        }
        if (layout instanceof HorizontalLayout) {
            ((HorizontalLayout) layout).setExpandRatio(cb, 1.0f);
        }

    }

    /**
     * Adds a token if that token does not already exist.
     * <p>
     * Note that tokens are not automatically added to the token container. This
     * means you can add tokens without adding them to the container (that might
     * be bound to some data store), and without making them available to the
     * user in the suggestion dropdown. <br/>
     * This also means that when new tokens are disallowed (
     * {@link #setNewTokensAllowed(boolean)}) you can programmatically add
     * tokens that the user can not add him/herself. <br/>
     * Consider adding the token to the container before calling
     * {@link #addToken(Object)} if you're using a custom captions based on
     * container/item properties, or if you want the token to be available to
     * the user as a suggestion later.
     * </p>
     * 
     * @param tokenId
     *            the token to add
     */
    public void addToken(Object tokenId) {
        Set<Object> set = (Set<Object>) getValue();
        if (set == null) {
            set = new LinkedHashSet<Object>();
        }
        if (set.contains(tokenId)) {
            return;
        }
        HashSet<Object> newSet = new LinkedHashSet<Object>(set);
        newSet.add(tokenId);
        addTokenButton(tokenId);
        setValue(newSet);
    }

    /**
     * Removes the given token.
     * <p>
     * Note that the token is not removed from the container, so if it exists in
     * the container, the token will still be available to the user.
     * </p>
     * 
     * @param tokenId
     *            the token to remove
     */
    public void removeToken(Object tokenId) {
        Button button = buttons.get(tokenId);
        layout.removeComponent(button);
        buttons.remove(button);
        Set<Object> set = (Set<Object>) getValue();
        LinkedHashSet<Object> newSet = new LinkedHashSet<Object>(set);
        newSet.remove(tokenId);
        if (set == null || !newSet.containsAll(set)) {
            setValue(newSet);
        }
    }

    /**
     * Configures the token button.
     * <p>
     * By default, the caption, icon, description, and style is set. Override to
     * customize.<br/>
     * Note that the default click-listener is added elsewhere and can not be
     * changed here.
     * </p>
     * 
     * @param tokenId
     *            the token this button pertains to
     * @param button
     *            the button to be configured
     */
    protected void configureTokenButton(Object tokenId, Button button) {
        button.setCaption(getTokenCaption(tokenId) + " ×");
        button.setIcon(getTokenIcon(tokenId));
        button.setDescription("Click to remove");
        button.setStyleName(Button.STYLE_LINK);
    }

    /**
     * Gets the layout currently in use.
     * 
     * @return the current layout
     */
    public Layout getLayout() {
        return layout;
    }

    /**
     * Sets layout used for laying out the tokens and the input.
     * 
     * @param newLayout
     *            the layout to use
     */
    public void setLayout(Layout newLayout) {
        if (layout != null) {
            layout.removeAllComponents();
        }
        layout = newLayout;
        setCompositionRoot(layout);
        rebuild();
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
        return insertPosition;
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
        if (this.insertPosition != insertPosition) {
            this.insertPosition = insertPosition;
            rebuild();
        }
    }

    public void setReadOnly(boolean readOnly) {
        if (readOnly == isReadOnly()) {
            return;
        }
        for (Button b : buttons.values()) {
            b.setReadOnly(readOnly);
        }
        super.setReadOnly(readOnly);
        if (readOnly) {
            layout.removeComponent(cb);
        } else {
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

    /**
     * Sets whether or not tokens entered by the user that not present in the
     * container are allowed. When true, the token is added, and if
     * {@link #setRememberNewTokens(boolean)} is true, the new token will be
     * added to the container as well.
     * 
     * @see #setNewTokenHandler(NewItemHandler)
     * @param allowNewTokens
     *            true to allow tokens that are not in the container
     */
    public void setNewTokensAllowed(boolean allowNewTokens) {
        cb.setNewItemsAllowed(allowNewTokens);
    }

    /**
     * Checks whether or not new tokens are allowed
     * 
     * @see #setNewTokensAllowed(boolean)
     * @return
     */
    public boolean isNewTokensAllowed() {
        return cb.isNewItemsAllowed();
    }

    /**
     * If true, new tokens entered by the user are automatically added to the
     * container.
     * 
     * @return true if tokens are automatically added
     */
    public boolean isRememberNewTokens() {
        return rememberNewTokens;
    }

    /**
     * Provided new tokens are allowed ({@link #setNewTokensAllowed(boolean)}),
     * this sets whether or not new tokens entered by the user are automatically
     * added to the container.
     * 
     * @param rememberNewTokens
     *            true to add new tokens automatically
     */
    public void setRememberNewTokens(boolean rememberNewTokens) {
        this.rememberNewTokens = rememberNewTokens;
    }

    /**
     * Works as {@link ComboBox#setFilteringMode(int)}.
     * 
     * @see ComboBox#setFilteringMode(int)
     * @param filteringMode
     *            the desired filtering mode
     */
    public void setFilteringMode(int filteringMode) {
        cb.setFilteringMode(filteringMode);
    }

    /**
     * Works as {@link ComboBox#getFilteringMode()}.
     * 
     * @see ComboBox#getFilteringMode()
     * @param filteringMode
     *            the desired filtering mode
     */
    public int getFilteringMode() {
        return cb.getFilteringMode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.vaadin.tokenfield.CustomField#focus()
     */
    public void focus() {
        cb.focus();
    }

    /**
     * Gets the input prompt; works as {@link ComboBox#getInputPrompt()}.
     * 
     * @see ComboBox#getInputPrompt()
     * @return the current input prompt
     */
    public String getInputPrompt() {
        return cb.getInputPrompt();
    }

    /**
     * Gets the caption for the given token; the caption can be based on a
     * property, just as in a ComboBox. Note that the string representation of
     * the tokenId itself is always used if the container does not contain the
     * id.
     * 
     * @param tokenId
     *            the id of the token
     * @return the caption
     */
    public String getTokenCaption(Object tokenId) {
        if (cb.containsId(tokenId)) {
            return cb.getItemCaption(tokenId);
        } else {
            return "" + tokenId;
        }
    }

    /**
     * @see ComboBox#getItemCaptionMode()
     * @return the current caption mode
     */
    public int getTokenCaptionMode() {
        return cb.getItemCaptionMode();
    }

    /**
     * @see ComboBox#getItemCaptionPropertyId()
     * @return the current caption property id
     */
    public Object getTokenCaptionPropertyId() {
        return cb.getItemCaptionPropertyId();
    }

    /**
     * @see ComboBox#getItemIcon(Object)
     * @return the icon for the given resource
     */

    public Resource getTokenIcon(Object tokenId) {
        return cb.getItemIcon(tokenId);
    }

    /**
     * @see ComboBox#getItemIconPropertyId()
     * @return the current item icon property id
     */

    public Object getTokenIconPropertyId() {
        return cb.getItemIconPropertyId();
    }

    /**
     * Gets all tokenIds currenlty in the token container.
     * 
     * @return a collection of all tokenIds in the container
     */
    public Collection getTokenIds() {
        return cb.getItemIds();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.vaadin.tokenfield.CustomField#getTabIndex()
     */
    public int getTabIndex() {
        return cb.getTabIndex();
    }

    /*-
    @Override
    public void setHeight(String height) {
        this.layout.setHeight(height);
        super.setHeight(height);
    }

    @Override
    public void setWidth(String width) {
        this.layout.setWidth(width);
        super.setWidth(width);
    }
    -*/

    @Override
    public void setHeight(float height, int unit) {
        if (this.layout != null) {
            this.layout.setHeight(height, unit);
        }
        super.setHeight(height, unit);
    }

    @Override
    public void setWidth(float width, int unit) {
        if (this.layout != null) {
            this.layout.setWidth(width, unit);
        }
        super.setWidth(width, unit);
    }

    @Override
    public void setSizeFull() {
        if (this.layout != null) {
            this.layout.setSizeFull();
        }
        super.setSizeFull();
    }

    @Override
    public void setSizeUndefined() {
        if (this.layout != null) {
            this.layout.setSizeUndefined();
        }
        super.setSizeUndefined();
    }

    public void setInputHeight(String height) {
        this.cb.setHeight(height);
    }

    public void setInputWidth(String width) {
        this.cb.setWidth(width);
    }

    public void setInputHeight(float height, int unit) {
        this.cb.setHeight(height, unit);
    }

    public void setInputWidth(float width, int unit) {
        this.cb.setWidth(width, unit);
    }

    public void setInputSizeFull() {
        this.cb.setSizeFull();
    }

    public void setInputSizeUndefined() {
        this.cb.setSizeUndefined();
    }

    /**
     * Sets the input prompt; works as {@link ComboBox#setInputPrompt(String)}.
     * 
     * @see ComboBox#setInputPrompt(String)
     * @return the current input prompt
     */
    public void setInputPrompt(String inputPrompt) {
        cb.setInputPrompt(inputPrompt);
    }

    /**
     * sets the caption for the given token.
     * 
     * @see ComboBox#setItemCaption(Object, String)
     * @param tokenId
     *            token whose caption to set
     * @param caption
     *            the desired caption
     */
    public void setTokenCaption(Object tokenId, String caption) {
        cb.setItemCaption(tokenId, caption);
    }

    /**
     * @see ComboBox#setItemCaptionMode(int)
     * @param mode
     */
    public void setTokenCaptionMode(int mode) {
        cb.setItemCaptionMode(mode);
    }

    /**
     * @see ComboBox#setItemCaptionPropertyId(Object)
     * @param propertyId
     */
    public void setTokenCaptionPropertyId(Object propertyId) {
        cb.setItemCaptionPropertyId(propertyId);
    }

    /**
     * @see ComboBox#setItemIcon(Object, Resource)
     * @param tokenId
     * @param icon
     */
    public void setTokenIcon(Object tokenId, Resource icon) {
        cb.setItemIcon(tokenId, icon);
    }

    /**
     * @see ComboBox#setItemIconPropertyId(Object)
     * @param propertyId
     */
    public void setTokenIconPropertyId(Object propertyId) {
        cb.setItemIconPropertyId(propertyId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.vaadin.tokenfield.CustomField#setTabIndex(int)
     */
    public void setTabIndex(int tabIndex) {
        cb.setTabIndex(tabIndex);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.vaadin.tokenfield.CustomField#getType()
     */
    @Override
    public Class<?> getType() {
        return Set.class;
    }

}
