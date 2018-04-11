package org.orienteer.service;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.function.OFunction;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.schedule.OScheduledEvent;
import com.orientechnologies.orient.core.schedule.OScheduledEventBuilder;
import com.orientechnologies.orient.core.schedule.OScheduler;
import org.orienteer.ICOFarmApplication;
import org.orienteer.model.ICOFarmUser;
import org.orienteer.model.OMail;
import org.orienteer.resource.ICOFarmRestorePasswordResource;
import org.orienteer.util.ICOFarmUtils;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.orienteer.module.ICOFarmModule.*;

public class RestorePasswordServiceImpl implements IRestorePasswordService {

    @Inject
    private IOMailService mailService;

    @Inject
    private IDBService dbService;

    @Override
    public void restoreUserPassword(@Nonnull ICOFarmUser user) {
       updateAndSaveUser(user);
       executeScheduler(user);
       sendRestoreLink(user);
    }

    @Override
    public void clearRestoring(@Nonnull ICOFarmUser user) {
        new DBClosure<Void>() {
            @Override
            protected Void execute(ODatabaseDocument db) {
                user.setRestoreId(null);
                user.setRestoreIdCreated(null);
                db.getMetadata().getScheduler().removeEvent(getSchedulerEventName(user));
                user.save();
                return null;
            }
        }.execute();
    }

    private void updateAndSaveUser(ICOFarmUser user) {
        user.setRestoreId(UUID.randomUUID().toString())
                .setRestoreIdCreated(new Date())
                .sudoSave();
    }

    private void sendRestoreLink(ICOFarmUser user) {
        OMail mail = dbService.getMailByName("restore");
        Map<Object, Object> macros = ICOFarmUtils.getUserMacros(user);
        macros.put("link", ICOFarmRestorePasswordResource.getLinkForUser(user));
        mail.setMacros(macros);
        mailService.sendMailAsync(user.getEmail(), mail);
    }

    private void executeScheduler(ICOFarmUser user) {
        new DBClosure<Void>() {
            @Override
            protected Void execute(ODatabaseDocument db) {
                String name = getSchedulerEventName(user);
                OScheduledEvent event = createEvent(name);
                OScheduler scheduler = db.getMetadata().getScheduler();
                scheduler.removeEvent(name);
                scheduler.scheduleEvent(event);
                return null;
            }

            private OScheduledEvent createEvent(String name) {
                OProperty property = user.getDocument().getSchemaClass().getProperty(ICOFarmUser.RESTORE_ID);
                OFunction f = dbService.getFunctionByName(FUN_REMOVE_RESTORE_ID_BY_EMAIL);
                long timeout = Long.parseLong(ICOFarmApplication.REMOVE_SCHEDULE_START_TIMEOUT.getValue(property));
                Map<Object, Object> args = new HashMap<>(2);
                args.put(FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_EMAIL, user.getEmail());
                args.put(FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_EVENT_NAME, name);
                args.put(FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_TIMEOUT, timeout);
                return new OScheduledEventBuilder()
                        .setName(name)
                        .setFunction(f)
                        .setArguments(args)
                        .setRule(ICOFarmApplication.REMOVE_CRON_RULE.getValue(property))
                        .setStartTime(new Date(System.currentTimeMillis() + timeout)).build();
            }
        }.execute();
    }

    private String getSchedulerEventName(ICOFarmUser user) {
        return "removeUserRestoreId" + user.getRestoreId();
    }
}
