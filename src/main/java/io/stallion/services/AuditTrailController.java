/*
 * Stallion Core: A Modern Web Framework
 *
 * Copyright (C) 2015 - 2016 Stallion Software LLC.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 2 of
 * the License, or (at your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public
 * License for more details. You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/gpl-2.0.html>.
 *
 *
 *
 */

package io.stallion.services;

import io.stallion.Context;
import io.stallion.dataAccess.*;
import io.stallion.dataAccess.db.DB;
import io.stallion.utils.DateUtils;
import io.stallion.utils.json.JSON;

import static io.stallion.utils.Literals.truncate;


public class AuditTrailController extends StandardModelController<AuditTrail> {
    public static AuditTrailController instance() {
        return (AuditTrailController) DataAccessRegistry.instance().get("stallion_audit_trail");
    }

    public static void register() {
        DataAccessRegistration reg = new DataAccessRegistration()
                .setBucket("stallion_audit_trail")
                .setModelClass(AuditTrail.class)
                .setControllerClass(AuditTrailController.class);
        if (DB.available()) {
            reg.setDatabaseBacked(true);
            reg.setStashClass(NoStash.class);
        }
        DataAccessRegistry.instance().register(reg);
    }


    public void logUpdate(Model obj) {
        AuditTrail at = new AuditTrail()
                .setCreatedAt(DateUtils.utcNow())
                .setOrgId(Context.getUser().getOrgId())
                .setKeepLongTerm(false)
                .setObjectData(JSON.stringify(obj))
                .setRemoteIp(Context.getRequest().getActualIp())
                .setTable(obj.getBucket())
                .setObjectId(obj.getId())
                .setUserAgent(truncate(Context.getRequest().getHeader("User-agent"), 200))
                .setUserEmail(Context.getUser().getEmail())
                .setValetEmail(Context.getValetEmail())
                .setValetId(Context.getValetUserId())
                ;
        AuditTrailController.instance().save(at);
    }
}
