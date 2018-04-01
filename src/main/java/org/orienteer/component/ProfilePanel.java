package org.orienteer.component;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.orienteer.core.component.BootstrapType;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.command.AjaxCommand;
import org.orienteer.core.component.command.EditCommand;
import org.orienteer.core.component.command.SaveODocumentCommand;
import org.orienteer.core.component.meta.ODocumentMetaPanel;
import org.orienteer.core.component.property.DisplayMode;
import org.orienteer.core.component.structuretable.OrienteerStructureTable;
import ru.ydn.wicket.wicketorientdb.model.OPropertyModel;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ProfilePanel extends AbstractICOFarmPanel<ODocument> {

    public ProfilePanel(String id, IModel<ODocument> model) {
        super(id, model);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        IModel<DisplayMode> displayMode = DisplayMode.VIEW.asModel();
        Form form = new Form("form");
        OrienteerStructureTable<ODocument, String> table = createProfileStructureTable("profileStructureTable", displayMode);
        addCommands(table, displayMode);
        form.add(table);
        add(form);
        add(new Label("extra", new ResourceModel("widget.profile.extra")));
    }

    private OrienteerStructureTable<ODocument, String> createProfileStructureTable(String id,
                                                                                      IModel<DisplayMode> modeModel) {
        return new OrienteerStructureTable<ODocument, String>(id, getModel(), createCriterias()) {
            @Override
            protected Component getValueComponent(String id, IModel<String> rowModel) {
                OClass oClass = getModelObject().getSchemaClass();
                OProperty property = oClass.getProperty(rowModel.getObject());
                return new ODocumentMetaPanel<String>(id, modeModel, getModel(), new OPropertyModel(property)) {
                    @Override
                    public IModel<String> newLabelModel() {
                        if (getPropertyObject().getName().equals("name")) {
                            return new ResourceModel("widget.profile.mail");
                        }
                        return super.newLabelModel();
                    }
                };
            }
        };
    }

    private void addCommands(OrienteerStructureTable<ODocument, String> table, IModel<DisplayMode> modeModel) {
        table.addCommand(new EditCommand<>(table, modeModel));
        table.addCommand(new SaveODocumentCommand(table, modeModel));
        table.addCommand(new AjaxCommand<ODocument>(new ResourceModel("widget.cancel"), table) {

            @Override
            public void onClick(Optional<AjaxRequestTarget> target) {
                if (target.isPresent()) {
                    modeModel.setObject(DisplayMode.VIEW);
                    target.get().add(table);
                }
            }

            @Override
            public BootstrapType getBootstrapType() {
                return BootstrapType.DANGER;
            }

            @Override
            public String getIcon() {
                return FAIconType.times.getCssClass();
            }

            @Override
            public boolean isVisible() {
                return modeModel.getObject() == DisplayMode.EDIT;
            }
        });
    }


    private IModel<List<String>> createCriterias() {
        List<String> list = new LinkedList<>();
        list.add("firstName");
        list.add("lastName");
        list.add("name");
        list.add("password");
        return new ListModel<>(list);
    }

    @Override
    protected IModel<String> getTitle() {
        return new ResourceModel("widget.profile.title");
    }
}
