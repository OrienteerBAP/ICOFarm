package org.orienteer.service;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.function.OFunction;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.schedule.OScheduledEvent;
import com.orientechnologies.orient.core.schedule.OScheduledEventBuilder;
import com.orientechnologies.orient.core.schedule.OScheduler;
import org.orienteer.ICOFarmApplication;
import org.orienteer.model.ICOFarmUser;
import org.orienteer.resource.ICOFarmRestorePasswordResource;
import org.orienteer.util.ICOFarmUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.orienteer.ICOFarmModule.*;

public class RestorePasswordServiceImpl implements IRestorePasswordService {

    private static final Logger LOG = LoggerFactory.getLogger(RestorePasswordServiceImpl.class);

    @Override
    public void restoreUserPassword(@Nonnull ICOFarmUser user) {
       updateAndSaveUser(user);
       executeScheduler(user);
       sendRestoreLink(user);
    }

    private void updateAndSaveUser(ICOFarmUser user) {
        user.setRestoreId(UUID.randomUUID().toString())
                .setRestoreIdCreated(new Date())
                .sudoSave();
    }

    private void sendRestoreLink(ICOFarmUser user) {
        String link = ICOFarmRestorePasswordResource.getLinkForUser(user);
        LOG.info("link for send: {}", link);
    }

    private void executeScheduler(ICOFarmUser user) {
        new DBClosure<Void>() {
            @Override
            protected Void execute(ODatabaseDocument db) {
                OScheduledEvent event = createEvent();
                OScheduler scheduler = db.getMetadata().getScheduler();
                scheduler.scheduleEvent(event);
                return null;
            }

            private OScheduledEvent createEvent() {
                OProperty property = user.getDocument().getSchemaClass().getProperty(ICOFarmUser.RESTORE_ID);
                String name = "removeUserRestoreId" + user.getId();
                OFunction f = ICOFarmUtils.getOFunctionByName(FUN_REMOVE_RESTORE_ID_BY_EMAIL);
                Map<Object, Object> args = new HashMap<>(2);
                args.put(FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_EMAIL, user.getEmail());
                args.put(FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_EVENT_NAME, name);
                args.put(FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_TIMEOUT, ICOFarmApplication.REMOVE_SCHEDULE_START_TIMEOUT.getValue(property));
                long timeout = Long.parseLong(ICOFarmApplication.REMOVE_SCHEDULE_START_TIMEOUT.getValue(property));
                return new OScheduledEventBuilder()
                        .setName(name)
                        .setFunction(f)
                        .setArguments(args)
                        .setRule(ICOFarmApplication.REMOVE_CRON_RULE.getValue(property))
                        .setStartTime(new Date(System.currentTimeMillis() + timeout)).build();
            }
        }.execute();
    }
}
