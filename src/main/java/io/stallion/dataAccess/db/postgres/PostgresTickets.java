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

package io.stallion.dataAccess.db.postgres;

import io.stallion.dataAccess.db.DB;
import io.stallion.dataAccess.Tickets;
import io.stallion.services.Log;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


public class PostgresTickets implements Tickets {
    private Queue<Long> loadedIds;
    private DB db;


    public PostgresTickets(DB db) {
        this.db = db;
        loadedIds = new ConcurrentLinkedQueue<Long>();
        createSequence();
        try {
            refillQueue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Long nextId() {
        for (int x=0; x<20; x++) {
            Long id = loadedIds.poll();
            if (id != null) {
                return id;
            } else {
                try {
                    refillQueue();
                } catch (Exception e) {
                    Log.exception(e, "Error refilling queue");
                    try {
                        Thread.sleep(500);
                    } catch (Exception ex) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        throw new RuntimeException("Could not load the next ticket");
    }

    public void createSequence() {
        QueryRunner q = db.newQuery();
        try {
            q.update("CREATE SEQUENCE stallion_tickets_seq INCREMENT BY 300 MINVALUE 100000;");
        } catch(Exception e) {
            Log.info("Sequence already exists");
        }
    }

    public void refillQueue() throws SQLException {
        QueryRunner q = db.newQuery();
        ScalarHandler<Long> scalar = new ScalarHandler<Long>();
        Long nextId = q.query("SELECT nextval('stallion_tickets_seq')", scalar);
        for (int x= 0; x < 300; x++ ) {
            loadedIds.add(nextId + x);
        }
    }
}
