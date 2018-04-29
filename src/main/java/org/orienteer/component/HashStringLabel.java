package org.orienteer.component;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

public class HashStringLabel extends Label {

    private int length;

    public HashStringLabel(String id, int length) {
        this(id, null, length);
    }


    public HashStringLabel(String id, IModel<String> model) {
        this(id, model, 0);
    }

    public HashStringLabel(String id, IModel<String> model, int length) {
        super(id, model);
        this.length = length > 0 ? length : 16;
    }


    @Override
    public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
        String hash = getDefaultModelObjectAsString();
        replaceComponentTagBody(markupStream, openTag, prepareHashForDisplay(hash));
    }

    protected String prepareHashForDisplay(String hash) {
        return hash.length() > length ? hash.substring(0, length) + "..." : hash;
    }
}
