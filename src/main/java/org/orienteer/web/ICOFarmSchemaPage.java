package org.orienteer.web;

import org.orienteer.core.MountPath;
import org.orienteer.core.web.schema.SchemaPage;
import ru.ydn.wicket.wicketorientdb.security.OSecurityHelper;
import ru.ydn.wicket.wicketorientdb.security.OrientPermission;
import ru.ydn.wicket.wicketorientdb.security.RequiredOrientResource;
import ru.ydn.wicket.wicketorientdb.security.RequiredOrientResources;

@MountPath(value="/schema", alt={"/classes"})
@RequiredOrientResources({
        @RequiredOrientResource(value = OSecurityHelper.SCHEMA, permissions = OrientPermission.READ),
        @RequiredOrientResource(value = OSecurityHelper.SYSTEM_CLUSTERS, permissions = OrientPermission.READ)
})
public class ICOFarmSchemaPage extends SchemaPage {

    public ICOFarmSchemaPage() {
        super();
    }
}
