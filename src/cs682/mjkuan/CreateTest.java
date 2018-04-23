/**
 * CS 682 Project 4 Tester
 * Copyright (C) 2018  Martino Kuan
 *
 * CS 682 Project 4 Tester.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cs682.mjkuan;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class CreateTest {
    public static final Logger logger =
            Logger.getLogger(CreateTest.class.getName());

    public static void main(String[] args) {
        /*
         * Important note! This program assumes that the are no other clients
         * attempting to modify this server.
         */
        logger.info("Testing on event servers directly.");
        createWithBadCredentials(false);
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n");
        createFifteenEvents(false);
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n");
        createConcurrentlyFifteenEvents(false);
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n");
        System.out.println("------------------------");
        logger.info("Testing on frontends...");
        createWithBadCredentials(true);
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n");
        createFifteenEvents(true);
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n");
        createConcurrentlyFifteenEvents(true);
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n");
        System.out.println("------------------------");
    }

    public static void createWithBadCredentials(boolean frontend) {
        logger.info("Creating events with bad credentials...");

        JsonObject inputJsonObject = new JsonObject();
        inputJsonObject.addProperty("userid", -1);
        inputJsonObject.addProperty("eventname",
                2000 + " celebration");
        inputJsonObject.addProperty("numtickets", 10);

        String response = frontend
                ? Constants.sendPost(Constants.FRONTEND_SERVER_2_ADDRESS,
                        Constants.FRONTEND_SERVER_2_PORT,
                        Constants.FRONTEND_EVENT_CREATE_LOCATION,
                        inputJsonObject.toString())
                : Constants.sendPost(Constants.EVENT_SERVER_1_ADDRESS,
                        Constants.EVENT_SERVER_1_PORT,
                        Constants.EVENT_CREATE_LOCATION,
                        inputJsonObject.toString());

        try {
            JsonParser jsonParser = new JsonParser();
            jsonParser.parse(response);
        } catch (JsonSyntaxException e) {
            logger.info("Bad credential test passed.");
            return;
        }

        logger.info("Bad credential test failed.");
    }

    public static void createFifteenEvents(boolean frontend) {
        logger.info("Creating 15 events...");

        Map<Integer, Integer> eventIds = new HashMap<>();

        for (int i = 0; i < 15; i++) {
            JsonObject inputJsonObject = new JsonObject();
            inputJsonObject.addProperty("userid", 2995);
            inputJsonObject.addProperty("eventname",
                    (i + 2000) + " celebration");
            inputJsonObject.addProperty("numtickets", (i * 10));

            String response = frontend
                    ? Constants.sendPost(Constants.FRONTEND_SERVER_3_ADDRESS,
                            Constants.FRONTEND_SERVER_3_PORT,
                            Constants.FRONTEND_EVENT_CREATE_LOCATION,
                            inputJsonObject.toString())
                    : Constants.sendPost(Constants.EVENT_SERVER_1_ADDRESS,
                            Constants.EVENT_SERVER_1_PORT,
                            Constants.EVENT_CREATE_LOCATION,
                            inputJsonObject.toString());

            JsonParser jsonParser = new JsonParser();
            JsonObject outputJsonObject =
                    jsonParser.parse(response).getAsJsonObject();

            int eventId =
                    outputJsonObject.getAsJsonPrimitive("eventid").getAsInt();
            eventIds.put(i, eventId);
        }

        logger.info(
                "Checking the integrity of the 15 events in another server...");

        for (int i : eventIds.keySet()) {
            String response = frontend
                    ? Constants.sendGet(Constants.FRONTEND_SERVER_1_ADDRESS,
                            Constants.FRONTEND_SERVER_1_PORT,
                            Constants.FRONTEND_EVENT_LOCATION + "/"
                                    + eventIds.get(i),
                            null)
                    : Constants.sendGet(Constants.EVENT_SERVER_2_ADDRESS,
                            Constants.EVENT_SERVER_2_PORT,
                            "/" + eventIds.get(i),
                            null);

            JsonParser jsonParser = new JsonParser();
            JsonObject outputJsonObject =
                    jsonParser.parse(response).getAsJsonObject();

            int userId =
                    outputJsonObject.getAsJsonPrimitive("userid").getAsInt();
            String eventName = outputJsonObject.getAsJsonPrimitive("eventname")
                    .getAsString();
            int numTickets = outputJsonObject.getAsJsonPrimitive("avail")
                    .getAsInt();

            if (userId != 2995) {
                logger.severe("Test failed for the user ID check!");
                return;
            } else if (!eventName
                    .equalsIgnoreCase((i + 2000) + " celebration")) {
                logger.severe("Test failed for the event name check!");
                return;
            } else if (numTickets != (i * 10)) {
                logger.severe("Test failed for the numTickets check!");
                return;
            }
        }

        logger.info("Test passed for 15 created events.");
        logger.info("As a reference, here is the events list:");
        logger.info(Constants.sendGet(Constants.EVENT_SERVER_2_ADDRESS,
                Constants.EVENT_SERVER_2_PORT,
                Constants.EVENT_LIST_LOCATION,
                null));
    }

    public static void createConcurrentlyFifteenEvents(boolean frontend) {
        logger.info("Creating 15 events...");

        Map<Integer, Integer> eventIds = new ConcurrentHashMap<>();
        Thread[] threads = new Thread[15];

        for (int i = 0; i < 15; i++) {
            final int j = i;
            threads[i] = new Thread(() -> {
                JsonObject inputJsonObject = new JsonObject();
                inputJsonObject.addProperty("userid", 2995);
                inputJsonObject.addProperty("eventname",
                        (j + 2000) + " celebration");
                inputJsonObject.addProperty("numtickets", (j * 10));

                String response = frontend
                        ? Constants.sendPost(
                                Constants.FRONTEND_SERVER_2_ADDRESS,
                                Constants.FRONTEND_SERVER_2_PORT,
                                Constants.FRONTEND_EVENT_CREATE_LOCATION,
                                inputJsonObject.toString())
                        : Constants.sendPost(Constants.EVENT_SERVER_1_ADDRESS,
                                Constants.EVENT_SERVER_1_PORT,
                                Constants.EVENT_CREATE_LOCATION,
                                inputJsonObject.toString());

                JsonParser jsonParser = new JsonParser();
                JsonObject outputJsonObject =
                        jsonParser.parse(response).getAsJsonObject();

                int eventId =
                        outputJsonObject.getAsJsonPrimitive("eventid")
                                .getAsInt();
                eventIds.put(j, eventId);
            });

            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }

        logger.info(
                "Checking the integrity of the 15 events using another server...");

        for (int i : eventIds.keySet()) {
            String response = frontend
                    ? Constants.sendGet(Constants.FRONTEND_SERVER_1_ADDRESS,
                            Constants.FRONTEND_SERVER_1_PORT,
                            Constants.FRONTEND_EVENT_LOCATION + "/"
                                    + eventIds.get(i),
                            null)
                    : Constants.sendGet(Constants.EVENT_SERVER_3_ADDRESS,
                            Constants.EVENT_SERVER_3_PORT,
                            "/" + eventIds.get(i),
                            null);

            JsonParser jsonParser = new JsonParser();
            JsonObject outputJsonObject =
                    jsonParser.parse(response).getAsJsonObject();

            int userId =
                    outputJsonObject.getAsJsonPrimitive("userid").getAsInt();
            String eventName = outputJsonObject.getAsJsonPrimitive("eventname")
                    .getAsString();
            int numTickets = outputJsonObject.getAsJsonPrimitive("avail")
                    .getAsInt();

            if (userId != 2995) {
                logger.severe("Test failed for the user ID check!");
                logger.severe(userId + " != " + 2995 + " celebration");
                return;
            } else if (!eventName
                    .equalsIgnoreCase((i + 2000) + " celebration")) {
                logger.severe("Test failed for the event name check!");
                logger.severe(i + ": " + eventIds.get(i) + ": " + eventName);
                logger.severe(eventName + " != " + (i + 2000) + " celebration");
                return;
            } else if (numTickets != (i * 10)) {
                logger.severe("Test failed for the numTickets check!");
                logger.severe(numTickets + " != " + (i * 10));
                return;
            }
        }

        logger.info("Test passed for 15 created events.");
        logger.info("As a reference, here is the events list:");
        logger.info(Constants.sendGet(Constants.EVENT_SERVER_2_ADDRESS,
                Constants.EVENT_SERVER_2_PORT,
                Constants.EVENT_LIST_LOCATION,
                null));
    }
}
