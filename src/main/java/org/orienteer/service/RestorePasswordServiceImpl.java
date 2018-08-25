package org.orienteer.service;

import com.google.inject.Inject;

import org.orienteer.mail.model.OMail;
import org.orienteer.mail.model.OPreparedMail;
import org.orienteer.mail.service.IOMailService;
import org.orienteer.model.ICOFarmUser;
import org.orienteer.resource.ICOFarmRestorePasswordResource;
import org.orienteer.util.ICOFarmUtils;

import javax.annotation.Nonnull;
import java.util.Map;

public class RestorePasswordServiceImpl implements IRestorePasswordService {

    @Inject
    private IOMailService mailService;

    @Inject
    private IDBService dbService;

    @Override
    public void restoreUserPassword(@Nonnull ICOFarmUser user) {
       dbService.createRestoreStatusForUser(user);
       dbService.createRestorePasswordScheduler(user);
       sendRestoreLink(user);
    }

    @Override
    public void clearRestoring(@Nonnull ICOFarmUser user) {
        dbService.clearRestoreStatusForUser(user);
    }

    private void sendRestoreLink(ICOFarmUser user) {
        OMail mail = dbService.getMailByName("restore");
        Map<String, Object> macros = ICOFarmUtils.getUserMacros(user);
        macros.put("link", ICOFarmRestorePasswordResource.getLinkForUser(user));
        mailService.sendMailAsync(new OPreparedMail(mail, macros).addRecipient(user.getEmail()));
    }

}
