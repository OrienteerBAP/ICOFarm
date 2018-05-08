package org.orienteer.component.transaction;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.time.Duration;
import org.orienteer.core.tasks.OTaskSession;
import org.orienteer.core.tasks.OTaskSessionRuntime;
import org.orienteer.model.Token;
import org.orienteer.service.IDBService;
import org.orienteer.tasks.LoadTokenTransactionsTask;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;

import java.math.BigInteger;

public class LoadTokenTransactionsPanel extends GenericPanel<Token> {

    @Inject
    private IDBService dbService;

    private final IModel<OTaskSession> sessionModel;

    public LoadTokenTransactionsPanel(String id, IModel<Token> model) {
        super(id, model);
        LoadTokenTransactionsTask task = dbService.getLoadTokenTransactionsTask(model.getObject());
        sessionModel = task != null ? Model.of(dbService.getRunningSessionForTask(task)) : Model.of();
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        AbstractAjaxTimerBehavior timerBehavior = createUpdateBehavior(5);
        Form<?> form = createForm("form");
        form.add(createBlockNumberField("blockNumber"));
        form.add(createSubmitButton("submit", timerBehavior));
        add(form);
        add(createLabel("loadTitle"));
        add(createLoadingInfoContainer("loadingInfo"));
        add(timerBehavior);
        setOutputMarkupPlaceholderTag(true);
    }

    private Form<?> createForm(String id) {
        return new Form<Object>(id) {
            @Override
            protected void onInitialize() {
                super.onInitialize();
                setOutputMarkupPlaceholderTag(true);
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(!isLoadingTransactions());
            }
        };
    }

    private Label createLabel(String id) {
        return new Label(id, getTitleModel()) {

            @Override
            protected void onInitialize() {
                super.onInitialize();
                setOutputMarkupPlaceholderTag(true);
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(!isLoadingTransactions());
            }
        };
    }

    private TextField<BigInteger> createBlockNumberField(String id) {
        return new RequiredTextField<>(id, BigInteger.class);
    }

    private AjaxButton createSubmitButton(String id, AbstractAjaxTimerBehavior timerBehavior) {
        return new AjaxButton(id, new ResourceModel("load.token.transactions.submit")) {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                OTaskSession session = executeTask();
                sessionModel.setObject(session);
                target.add(LoadTokenTransactionsPanel.this);
                timerBehavior.restart(target);
            }

            @SuppressWarnings("unchecked")
            private OTaskSession executeTask() {
                Token token = LoadTokenTransactionsPanel.this.getModelObject();
                BigInteger blockNumber = ((TextField<BigInteger>) getForm().get("blockNumber")).getModelObject();
                LoadTokenTransactionsTask task = dbService.createLoadTokenTransactionsTask(token, new DefaultBlockParameterNumber(blockNumber),
                        DefaultBlockParameterName.LATEST);
                OTaskSessionRuntime runtime = task.startNewSession();

                return runtime.getOTaskSessionPersisted();
            }
        };
    }

    private WebMarkupContainer createLoadingInfoContainer(String id) {
        return new WebMarkupContainer(id) {
            @Override
            protected void onInitialize() {
                super.onInitialize();
                setOutputMarkupPlaceholderTag(true);
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(isLoadingTransactions());
            }
        };
    }

    private AbstractAjaxTimerBehavior createUpdateBehavior(int seconds) {
        return new AbstractAjaxTimerBehavior(Duration.seconds(seconds)) {
            @Override
            protected void onTimer(AjaxRequestTarget target) {
                OTaskSession session = sessionModel.getObject();
                if (session != null) {
                    if (session.getStatus() == OTaskSession.Status.FINISHED) {
                        onTransactionsLoaded(target, getModelObject());
                        stop(target);
                    }
                } else stop(target);
            }
        };
    }

    protected void onTransactionsLoaded(AjaxRequestTarget target, Token token) {

    }

    private IModel<String> getTitleModel() {
        Token token = getModelObject();
        String name = token.getName(getLocale().getLanguage());
        String title = new ResourceModel("load.token.transactions.title").getObject();
        return Strings.isNullOrEmpty(name) ? Model.of(title + token.getName("en")) : Model.of(title + name);
    }

    private boolean isLoadingTransactions() {
        return sessionModel.getObject() != null && sessionModel.getObject().getStatus() == OTaskSession.Status.RUNNING;
    }
}
