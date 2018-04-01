package org.orienteer.service;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import org.danekja.java.util.function.serializable.SerializablePredicate;
import org.orienteer.ICOFarmModule;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.module.OWidgetsModule;
import org.orienteer.core.module.PerspectivesModule;
import org.orienteer.core.service.impl.DefaultFilterPredicateFactory;

import java.util.List;

public class ICOFarmFilterPredicateFactory extends DefaultFilterPredicateFactory {

    @Override
    public SerializablePredicate<OClass> getPredicateForClassesSearch() {
        SerializablePredicate<OClass> predicate = super.getPredicateForClassesSearch();
        return predicate.and((SerializablePredicate<? super OClass>) (c) -> {
            OSecurityUser user = OrienteerWebSession.get().getUser();
            if (user.hasRole("reader", true)) {
                String name = c.getName();
                return !name.equals(OWidgetsModule.OCLASS_WIDGET) && !name.equals(OWidgetsModule.OCLASS_DASHBOARD)
                        && !name.equals(PerspectivesModule.OCLASS_ITEM) && !name.equals(PerspectivesModule.OCLASS_PERSPECTIVE)
                        && !name.equals(ICOFarmModule.REGISTRATION);
            }
            return true;
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public SerializablePredicate<OProperty> getPredicateForListProperties() {
        OSecurityUser user = OrienteerWebSession.get().getUser();
        SerializablePredicate<OProperty> res = super.getPredicateForListProperties();
        if (user != null) {
            SerializablePredicate<OProperty> predicate = (p) -> {
                boolean result = true;
                if (user.hasRole(ICOFarmModule.INVESTOR_ROLE, true)) {
                    List<String> properties = ICOFarmModule.HIDDEN_PROPERTIES.get(p.getOwnerClass().getName());
                    result = properties == null || !properties.contains(p.getName());
                }
                return result;
            };
            res = compose(res, predicate);
        }
        return res;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SerializablePredicate<OProperty> getPredicateForTableProperties() {
        return compose(getPredicateForListProperties(), super.getPredicateForTableProperties());
    }
}
