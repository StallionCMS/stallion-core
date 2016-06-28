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

package io.stallion.dataAccess.db.mysql;

import io.stallion.dataAccess.Tickets;
import io.stallion.dataAccess.db.DB;
import io.stallion.services.Log;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.SQLException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


public class MySqlTickets implements Tickets {
    private Queue<Long> loadedIds;
    private DB db;

    public MySqlTickets(DB db) {
        loadedIds = new ConcurrentLinkedQueue<Long>();
        this.db = db;
        createSequence();
        refillQueue();
    }

    @Override
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
        List items = db.findRecords("SHOW TABLES LIKE 'stallion_tickets'");
        if (items.size() > 0) {
            return;
        }
        QueryRunner q = db.newQuery();
        try {
            q.update("CREATE TABLE `stallion_tickets` (\n" +
                    "  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,\n" +
                    "  `ticket_name` varchar(10) DEFAULT NULL,\n" +
                    "  PRIMARY KEY (`id`),\n" +
                    "  UNIQUE KEY `ticket_name_unique` (`ticket_name`)\n" +
                    ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
            db.execute("INSERT INTO stallion_tickets (`ticket_name`) VALUES('a')");
        } catch(Exception e) {
            Log.exception(e, "Error creating tickets table");
        }
    }

    public void refillQueue() {
        QueryRunner q = db.newQuery();
        //String sql = "insert into users (username) values (?)";
        String sql = "REPLACE INTO stallion_tickets (ticket_name) VALUES('a')";
        long nextId = 0;
        try {
            nextId = q.insert(sql, new ScalarHandler<Long>());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Log.fine("Next ticket id {0}", nextId);
        nextId = nextId * 1000;

        //ScalarHandler<Long> scalar = new ScalarHandler<Long>();
        //Long nextId = q.query("SELECT nextval('stallion_tickets_seq')", scalar);
        for (int x= 0; x < 1000; x++ ) {
            loadedIds.add(nextId + x);
        }
    }
}
