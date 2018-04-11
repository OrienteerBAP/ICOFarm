package org.orienteer.util;

import com.orientechnologies.orient.core.metadata.security.OSecurityRole;
import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.model.ICOFarmUser;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class ICOFarmUtils {

    public static Map<Object, Object> getUserMacros(ICOFarmUser user) {
        return getUserMacros(user.getDocument());
    }

    public static Map<Object, Object> getUserMacros(ODocument doc) {
        Map<Object, Object> map = new HashMap<>(1);
        map.put("firstName", doc.field(ICOFarmUser.FIRST_NAME));
        map.put("lastName", doc.field(ICOFarmUser.LAST_NAME));
        map.put("email", doc.field(ICOFarmUser.EMAIL));
        return map;
    }

    public static boolean isAdmin(OSecurityUser user) {
        return user.getRoles().stream().map(OSecurityRole::getMode)
                .anyMatch(mode -> mode == OSecurityRole.ALLOW_MODES.ALLOW_ALL_BUT);
    }

    public static Date computeTimestamp(EthBlock.Block block) {
        return new Date(1000 * block.getTimestamp().longValue());
    }
}
