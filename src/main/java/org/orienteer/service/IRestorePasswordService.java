package org.orienteer.service;

import com.google.inject.ImplementedBy;
import org.orienteer.model.ICOFarmUser;

import javax.annotation.Nonnull;

@ImplementedBy(RestorePasswordServiceImpl.class)
public interface IRestorePasswordService {
    public void restoreUserPassword(@Nonnull ICOFarmUser user);
}
