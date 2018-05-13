package org.orienteer.method.filter;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.model.IModel;
import org.orienteer.core.method.IMethodContext;
import org.orienteer.core.method.IMethodFilter;

public class ODocumentExistsFilter implements IMethodFilter {

    private boolean exists;

    @Override
    public IMethodFilter setFilterData(String filterData) {
        exists = Boolean.valueOf(filterData);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean isSupportedMethod(IMethodContext context) {
        IModel<ODocument> model = (IModel<ODocument>) context.getDisplayObjectModel();
        ODocument doc = model.getObject();
        return !doc.isEmpty();
    }
}
