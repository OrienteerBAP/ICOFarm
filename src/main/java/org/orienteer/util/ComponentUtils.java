package org.orienteer.util;

import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.model.Token;
import org.orienteer.model.Wallet;

import java.util.Locale;

public final class ComponentUtils {

    public static ChoiceRenderer<Token> getChoiceRendererForTokens() {
        return new ChoiceRenderer<Token>() {
            @Override
            public Object getDisplayValue(Token token) {
                String name = token.getName(OrienteerWebSession.get().getLocale().toLanguageTag());
                if (name == null) name = token.getName(Locale.ENGLISH.toLanguageTag());
                return name + " - " + token.getSymbol();
            }
        };
    }

    public static ChoiceRenderer<Wallet> getChoiceRendererForWallets() {
        return new ChoiceRenderer<Wallet>() {
            @Override
            public Object getDisplayValue(Wallet wallet) {
                return wallet.getName();
            }
        };
    }
}
