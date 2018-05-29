package org.orienteer.component.visualizer;

import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.orienteer.component.HashStringLabel;
import org.orienteer.core.component.property.DisplayMode;
import org.orienteer.core.component.visualizer.AbstractSimpleVisualizer;
import org.orienteer.core.component.widget.document.ODocumentPropertiesWidget;

public class HashVisualizer extends AbstractSimpleVisualizer {

    public static final String NAME = "hash-string";

    public HashVisualizer() {
        super(NAME, false, OType.STRING);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> Component createComponent(String id, DisplayMode mode, IModel<ODocument> documentModel,
                                         IModel<OProperty> propertyModel, IModel<V> valueModel) {
        switch (propertyModel.getObject().getType()) {
            case STRING:
                if (mode == DisplayMode.EDIT) {
                    return new TextField<>(id, (IModel<String>) valueModel);
                }

                return new HashStringLabel(id, (IModel<String>) valueModel) {
                    @Override
                    protected String prepareHashForDisplay(String hash) {
                        if (findParent(ODocumentPropertiesWidget.class) != null) {
                            return hash;
                        }
                        return super.prepareHashForDisplay(hash);
                    }
                };
            default:
                return null;
        }
    }
}
