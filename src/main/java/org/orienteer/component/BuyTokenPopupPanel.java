package org.orienteer.component;

import java.math.BigDecimal;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.orienteer.core.component.command.AbstractModalWindowCommand;

import ru.ydn.wicket.wicketorientdb.model.SimpleNamingModel;

public abstract class BuyTokenPopupPanel extends Panel{
	private static final long serialVersionUID = 1L;
	private IModel<String> walletPassword;
	private IModel<String> ethSumm;
	
	public BuyTokenPopupPanel(String id, final ModalWindow modal,final AbstractModalWindowCommand<?> command) {
		super(id);
		walletPassword = new Model<String>();
		ethSumm = new Model<String>();
		modal.setMinimalHeight(300);
		modal.showUnloadConfirmation(false);
		Form<?> popupForm = new Form<Object>("popupForm");
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
				}
			}
			
		});
		add(popupForm);		
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
