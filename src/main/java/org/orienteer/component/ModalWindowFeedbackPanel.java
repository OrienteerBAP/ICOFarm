package org.orienteer.component;

import org.apache.wicket.event.IEvent;
import org.orienteer.core.component.OrienteerFeedbackPanel;
import org.orienteer.core.web.OrienteerBasePage;

public class ModalWindowFeedbackPanel extends OrienteerFeedbackPanel {
    public ModalWindowFeedbackPanel(String id) {
        super(id);
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        OrienteerBasePage parent = findParent(OrienteerBasePage.class);
        parent.getFeedbacks().getFeedbackMessagesModel().detach();
    }


    @Override
    public void onEvent(IEvent<?> event) {

    }
}
