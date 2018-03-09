package org.orienteer.component;

import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.CollectionModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.validation.validator.DateValidator;
import org.orienteer.core.CustomAttribute;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.component.meta.AbstractComplexModeMetaPanel;
import org.orienteer.core.component.property.DisplayMode;
import org.orienteer.core.component.structuretable.AbstractStructureTableToolbar;
import org.orienteer.core.component.structuretable.OrienteerStructureTable;
import org.orienteer.core.component.visualizer.IVisualizer;
import org.orienteer.core.util.ODocumentTextChoiceProvider;
import org.orienteer.model.Transaction;
import org.wicketstuff.select2.Select2Choice;
import ru.ydn.wicket.wicketorientdb.model.OPropertyModel;
import ru.ydn.wicket.wicketorientdb.model.OPropertyNamingModel;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.orienteer.ICOFarmModule.*;
import static org.orienteer.core.component.meta.OClassMetaPanel.BOOTSTRAP_SELECT2_THEME;

public class TransactionPanel extends AbstractICOFarmPanel<ODocument> {

    private final OrienteerStructureTable<ODocument, String> structureTable;

    public TransactionPanel(String id, IModel<ODocument> model) {
        super(id, model);
        IModel<List<String>> criterias = newCriteriasModel();

        IModel<DisplayMode> displayModeIModel = DisplayMode.EDIT.asModel();
        Form form = new Form("form");
        structureTable = new OrienteerStructureTable<ODocument, String>("structureTable", getModel(), criterias) {
            @Override
            protected Component getValueComponent(String id, IModel<String> rowModel) {
                OProperty property = getModel().getObject().getSchemaClass().getProperty(rowModel.getObject());
                return new TransactionMetaPanel<>(id, displayModeIModel, getModel(), new OPropertyModel(property));
            }

            @Override
            protected void initialize() {
                super.initialize();
                addBottomToolbar(getCommandsToolbar());
                Component component = get("bottomToolbars");
                component.add(AttributeModifier.replace("align", "center"));
                component.add(AttributeModifier.replace("class", "add-transaction-commands"));
            }

            @Override
            public void addTopToolbar(AbstractStructureTableToolbar<ODocument> toolbar) {
                if (!toolbar.equals(getCommandsToolbar()))
                    super.addTopToolbar(toolbar);
            }
        };
        form.add(structureTable);
        add(form);
    }

    public OrienteerStructureTable<ODocument, String> getStructureTable() {
        return structureTable;
    }

    @Override
    protected String getTitleCssClasses() {
        return "text-uppercase";
    }

    @Override
    protected String getCssClasses() {
        return super.getCssClasses() + " center-block";
    }

    @Override
    protected IModel<String> getTitle() {
        return new ResourceModel("widget.transaction.add");
    }

    private IModel<List<String>> newCriteriasModel() {
        return new ListModel<>(Transaction.getUserFields());
    }

    private static class TransactionMetaPanel<V> extends AbstractComplexModeMetaPanel<ODocument, DisplayMode, OProperty, V> {

        public TransactionMetaPanel(String id, IModel<DisplayMode> modeModel,
                                    IModel<ODocument> entityModel, IModel<OProperty> criteryModel) {
            super(id, modeModel, entityModel, criteryModel);
        }

        @Override
        protected V getValue(ODocument entity, OProperty critery) {
            return entity.field(critery.getName());
        }

        @Override
        protected void setValue(ODocument entity, OProperty critery, V value) {
            entity.field(critery.getName(), value);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Component resolveComponent(String id, DisplayMode mode, OProperty property) {
            Component component;
            IVisualizer visualizer = getVisualizer(property);
            if (mode == DisplayMode.EDIT) {
                switch (property.getName()) {
                    case OPROPERTY_TRANSACTION_FROM_CURRENCY:
                    case OPROPERTY_TRANSACTION_TO_CURRENCY:
                        component = newCurrencyChoice(id, (IModel<ODocument>) getModel());
                        break;
                    case OPROPERTY_TRANSACTION_DATETIME:
                        IModel<Date> valueModel = (IModel<Date>) getValueModel();
                        DateTimeField field = (DateTimeField) visualizer.createComponent(id, mode, getEntityModel(), getPropertyModel(), getValueModel());
                        field.add(DateValidator.minimum(valueModel.getObject()));
                        component = field;
                        break;
                    default:
                        component = visualizer.createComponent(id, mode, getEntityModel(), getPropertyModel(), getValueModel());
                }
            } else component = visualizer.createComponent(id, mode, getEntityModel(), getPropertyModel(), getValueModel());

            if (component != null && component instanceof FormComponent) {
                ((FormComponent) component).setRequired(true);
            }
            return component;
        }

        private IVisualizer getVisualizer(OProperty property) {
            String visualizationComponent = CustomAttribute.VISUALIZATION_TYPE.getValue(property);
            return OrienteerWebApplication.get().getUIVisualizersRegistry()
                    .getComponentFactory(property.getType(), visualizationComponent);
        }

        @Override
        protected IModel<String> newLabelModel() {
            return new OPropertyNamingModel(getPropertyObject());
        }

        private Select2Choice<ODocument> newCurrencyChoice(String id, IModel<ODocument> model) {
            IModel<Collection<String>> classesModel = new CollectionModel<>(Collections.singletonList(CURRENCY));
            Select2Choice<ODocument> choice = new Select2Choice<>(id, model, new ODocumentTextChoiceProvider(classesModel));
            choice.getSettings()
                    .setWidth("100%")
                    .setCloseOnSelect(true)
                    .setTheme(BOOTSTRAP_SELECT2_THEME);
            choice.setOutputMarkupPlaceholderTag(true);
            return choice;
        }
    }
}
