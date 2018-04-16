package org.orienteer.component;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.orienteer.core.component.command.AbstractModalWindowCommand;

import ru.ydn.wicket.wicketorientdb.model.SimpleNamingModel;

public abstract class BuyTokenPopupPanel extends Panel{
	private static final long serialVersionUID = 1L;
	private IModel<String> walletPassword;
	private IModel<String> ethSumm;
	private FeedbackPanel feedbackPanel;
	private Form<?> popupForm;
	
	public BuyTokenPopupPanel(String id, final ModalWindow modal,final AbstractModalWindowCommand<?> command) {
		super(id);
        feedbackPanel = new FeedbackPanel("feedback",new ContainerFeedbackMessageFilter(this)){
        	protected String getCSSClass(final FeedbackMessage message){
        		return "alert alert-danger";
        	}
        };
        configFeedbackPanel(feedbackPanel);
		add(feedbackPanel);
        
		walletPassword = new Model<String>();
		ethSumm = new Model<String>();
		modal.setMinimalHeight(300);
		modal.setMinimalWidth(500);
		modal.showUnloadConfirmation(false);
		popupForm = new Form<Object>("popupForm");
		PasswordTextField password = new PasswordTextField("walletPassword",walletPassword);
		TextField ethSummField = new TextField("ethSumm",ethSumm);
		popupForm.add(password);
		popupForm.add(ethSummField);
		popupForm.add(new AjaxButton("submitButton",getButtonTitle(),popupForm)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				if (onSubmitForm(target)){
					command.onAfterModalSubmit();
					modal.close(target);
				}else{
					target.add(BuyTokenPopupPanel.this);
				}
			}
			@Override
			protected void onError(AjaxRequestTarget target) {
				target.add(BuyTokenPopupPanel.this);
				super.onError(target);
			}
			
		});
		add(popupForm);		
	}
    protected void configFeedbackPanel(FeedbackPanel panel) {
    	panel.setOutputMarkupPlaceholderTag(true);
        panel.setMaxMessages(2);
        panel.setEscapeModelStrings(false);
        //panel.add(AttributeModifier.append("class", ""));
    }
	
	public abstract SimpleNamingModel<String> getButtonTitle();
	public abstract boolean onSubmitForm(AjaxRequestTarget target);
	
	public IModel<String> getWalletPassword() {
		return walletPassword;
	}

	public void setWalletPassword(IModel<String> walletPassword) {
		this.walletPassword = walletPassword;
	}

	public IModel<String> getEthSumm() {
		return ethSumm;
	}

	public void setEthSumm(IModel<String> ethSumm) {
		this.ethSumm = ethSumm;
	}
}
