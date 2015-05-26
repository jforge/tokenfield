TokenField is a [Vaadin](http://vaadin.com) component for selecting multiple 'tokens' using a ComboBox (which by default actually looks like a TextField w/ suggestions) - essentially a multi-select ComboBox. Example usecases include tagging and email address selection.

Discussion thread at the [Vaadin Forum](http://vaadin.com/forum/-/message_boards/message/118245)

The field is very configurable, as can be seen in the [demo](http://marc.virtuallypreinstalled.com/TokenField/).

Features include:
  * tokens can be inserted before/after input (over/under/etc depending on layout)
  * layout can be changed
  * suggestions from container
  * auto add new to container
  * disallow tokens not in container
  * custom action on add (+ detect if token is in container)
  * custom configuring of the token button (style, caption, etc)
  * custom action on remove
  * built in style for either TextField or ComboBox look
  * built in styles for buttons, default and "emphasize"