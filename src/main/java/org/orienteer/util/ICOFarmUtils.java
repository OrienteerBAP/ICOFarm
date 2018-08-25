package org.orienteer.util;

import com.orientechnologies.orient.core.metadata.security.OSecurityRole;
import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.model.ICOFarmUser;
import org.orienteer.model.Token;
import org.orienteer.module.ICOFarmModule;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.*;
import java.util.stream.Collector;

public final class ICOFarmUtils {

    public static Map<String, Object> getUserMacros(ICOFarmUser user) {
        return getUserMacros(user.getDocument());
    }

    public static Map<String, Object> getUserMacros(ODocument doc) {
        Map<String, Object> map = new HashMap<>(1);
        map.put("firstName", doc.field(ICOFarmUser.OPROPERTY_FIRST_NAME));
        map.put("lastName", doc.field(ICOFarmUser.OPROPERTY_LAST_NAME));
        map.put("email", doc.field(ICOFarmUser.OPROPERTY_EMAIL));
        return map;
    }

    public static boolean isAdmin(OSecurityUser user) {
        return user.getRoles().stream().map(OSecurityRole::getMode)
                .anyMatch(mode -> mode == OSecurityRole.ALLOW_MODES.ALLOW_ALL_BUT);
    }

    public static Date computeTimestamp(EthBlock.Block block) {
        return new Date(1000 * block.getTimestamp().longValue());
    }

    public static BigInteger toWei(BigDecimal value, Token token) {
        if (token.isEthereumCurrency()) {
            return token.getEtherCostAs(Convert.Unit.WEI).toBigInteger();
        }
        BigDecimal wei = token.getEtherCostAs(Convert.Unit.WEI);
        return wei.multiply(value, MathContext.UNLIMITED).toBigInteger();
    }

    public static <T> Collector<T, List<List<T>>, List<List<T>>> getCollectorForGroupList(int num) {
        return Collector.of(
                LinkedList::new,

                (list, element) -> {
                    List<T> innerList = !list.isEmpty() ? list.get(list.size() - 1) : null;
                    if (innerList == null || innerList.size() == num) {
                        innerList = new ArrayList<>(num);
                        list.add(innerList);
                    }

                    innerList.add(element);
                },

                (a, b) -> a
        );
    }

    public static boolean isEthereumCurrency(Token token) {
        return token.getAddress().equals(ICOFarmModule.ZERO_ADDRESS);
    }
}
