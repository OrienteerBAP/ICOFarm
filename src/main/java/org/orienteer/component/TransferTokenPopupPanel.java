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

public abstract class TransferTokenPopupPanel extends Panel{
	private static final long serialVersionUID = 1L;
	private FeedbackPanel feedbackPanel;

	private IModel<String> walletPassword;
	private IModel<String> tokenQuantity;
	private IModel<String> targetWalletAddress;
	private Form<?> popupForm;
	
	public TransferTokenPopupPanel(String id, final ModalWindow modal,final AbstractModalWindowCommand<?> command) {
		super(id);
        feedbackPanel = new FeedbackPanel("feedback",new ContainerFeedbackMessageFilter(this)){
        	protected String getCSSClass(final FeedbackMessage message){
        		return "alert alert-danger";
        	}
        };		

	
	    configFeedbackPanel(feedbackPanel);
		add(feedbackPanel);
	    
		walletPassword = new Model<String>();
		tokenQuantity = new Model<String>();
		targetWalletAddress = new Model<String>();
		modal.setMinimalHeight(300);
		modal.setMinimalWidth(500);
		modal.showUnloadConfirmation(false);
		popupForm = new Form<Object>("popupForm");
		PasswordTextField password = new PasswordTextField("walletPassword",walletPassword);
		TextField tokenQuantityField = new TextField("tokenQuantity",tokenQuantity);
		TextField targetWalletAddressField = new TextField("targetWalletAddress",targetWalletAddress);
		popupForm.add(password);
		popupForm.add(tokenQuantityField);
		popupForm.add(targetWalletAddressField);
		popupForm.add(new AjaxButton("submitButton",getButtonTitle(),popupForm)
		{
			private static final long serialVersionUID = 1L;
	
			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				if (onSubmitForm(target)){
					command.onAfterModalSubmit();
					modal.close(target);
				}else{
					target.add(TransferTokenPopupPanel.this);
				}
			}
			@Override
			protected void onError(AjaxRequestTarget target) {
				target.add(TransferTokenPopupPanel.this);
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
	public IModel<String> getTokenQuantity() {
		return tokenQuantity;
	}
	public void setTokenQuantity(IModel<String> tokenQuantity) {
		this.tokenQuantity = tokenQuantity;
	}
	public IModel<String> getTargetWalletAddress() {
		return targetWalletAddress;
	}
	public void setTargetWalletAddress(IModel<String> targetWalletAddress) {
		this.targetWalletAddress = targetWalletAddress;
	}

}
