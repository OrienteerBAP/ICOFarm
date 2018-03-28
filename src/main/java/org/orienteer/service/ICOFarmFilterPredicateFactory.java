package org.orienteer.service;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import org.danekja.java.util.function.serializable.SerializablePredicate;
import org.orienteer.ICOFarmModule;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.module.OWidgetsModule;
import org.orienteer.core.module.PerspectivesModule;
import org.orienteer.core.service.impl.DefaultFilterPredicateFactory;

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
}
