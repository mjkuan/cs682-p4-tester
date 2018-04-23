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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class PurchaseTest {
    public static final Logger logger =
            Logger.getLogger(PurchaseTest.class.getName());

    public static void main(String[] args) {
        /*
         * Important note! This test relies on client test!
         */
        logger.info("Testing on event servers directly.");
        purchaseWithBadCredentials(false);
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n");
        purchaseFifteenEvents(false);
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n");
        purchaseConcurrentlyFifteenEvents(false);
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n");
        System.out.println("------------------------");
        logger.info("Testing on frontends...");
        purchaseWithBadCredentials(true);
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n");
        purchaseFifteenEvents(true);
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n");
        purchaseConcurrentlyFifteenEvents(true);
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n");
        System.out.println("------------------------");
    }

    public static void purchaseWithBadCredentials(boolean frontend) {
        logger.info("Purchasing tickets with bad credentials...");

        JsonObject inputJsonObject = new JsonObject();
        inputJsonObject.addProperty("userid", -1);
        inputJsonObject.addProperty("eventid", 0);
        inputJsonObject.addProperty("tickets", 10);

        String response = frontend
                ? Constants.sendPost(Constants.FRONTEND_SERVER_2_ADDRESS,
                        Constants.FRONTEND_SERVER_2_PORT,
                        Constants.FRONTEND_EVENT_LOCATION + "/0/purchase/-1",
                        inputJsonObject.toString())
                : Constants.sendPost(Constants.EVENT_SERVER_1_ADDRESS,
                        Constants.EVENT_SERVER_1_PORT,
                        Constants.EVENT_PURCHASE_LOCATION + "/0",
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

    public static void purchaseFifteenEvents(boolean frontend) {
        logger.info("Purchasing tickets for 15 events...");

        Set<Integer> eventIds = new HashSet<>();

        for (int i = 0; i < 15; i++) {
            JsonObject inputJsonObject = new JsonObject();
            inputJsonObject.addProperty("userid", Constants.USER_ID);
            inputJsonObject.addProperty("eventid", i);
            inputJsonObject.addProperty("tickets", i * 5);

            String response = frontend
                    ? Constants.sendPost(Constants.FRONTEND_SERVER_3_ADDRESS,
                            Constants.FRONTEND_SERVER_3_PORT,
                            Constants.FRONTEND_EVENT_LOCATION + "/" + i
                                    + "/purchase/" + Constants.USER_ID,
                            inputJsonObject.toString())
                    : Constants.sendPost(Constants.EVENT_SERVER_1_ADDRESS,
                            Constants.EVENT_SERVER_1_PORT,
                            Constants.EVENT_PURCHASE_LOCATION + "/" + i,
                            inputJsonObject.toString());

            eventIds.add(i);
        }

        logger.info(
                "Checking the integrity of the 15 events in another server...");

        for (int i : eventIds) {
            String response = frontend
                    ? Constants.sendGet(Constants.FRONTEND_SERVER_1_ADDRESS,
                            Constants.FRONTEND_SERVER_1_PORT,
                            Constants.FRONTEND_EVENT_LOCATION + "/"
                                    + i,
                            null)
                    : Constants.sendGet(Constants.EVENT_SERVER_2_ADDRESS,
                            Constants.EVENT_SERVER_2_PORT,
                            "/" + i,
                            null);

            JsonParser jsonParser = new JsonParser();
            JsonObject outputJsonObject =
                    jsonParser.parse(response).getAsJsonObject();

            int numTickets = outputJsonObject.getAsJsonPrimitive("purchased")
                    .getAsInt();

            if (numTickets != (i * 5)) {
                logger.severe("Test failed for the numTickets check!");
                logger.severe(numTickets + " != " + (i * 5));
                return;
            }
        }

        logger.info("Test passed for 15 purchased events.");
        logger.info("As a reference, here is the events list:");
        logger.info(Constants.sendGet(Constants.EVENT_SERVER_2_ADDRESS,
                Constants.EVENT_SERVER_2_PORT,
                Constants.EVENT_LIST_LOCATION,
                null));
    }

    public static void purchaseConcurrentlyFifteenEvents(boolean frontend) {
        logger.info("Purchasing tickts for 15 events...");

        Set<Integer> eventIds = new HashSet<>();
        Thread[] threads = new Thread[15];

        for (int i = 15; i < 30; i++) {
            final int j = i;
            threads[i - 15] = new Thread(() -> {
                JsonObject inputJsonObject = new JsonObject();
                inputJsonObject.addProperty("userid", Constants.USER_ID);
                inputJsonObject.addProperty("eventid", j);
                inputJsonObject.addProperty("tickets", j * 5);

                String response = frontend
                        ? Constants.sendPost(
                                Constants.FRONTEND_SERVER_3_ADDRESS,
                                Constants.FRONTEND_SERVER_3_PORT,
                                Constants.FRONTEND_EVENT_LOCATION + "/" + j
                                        + "/purchase/" + Constants.USER_ID,
                                inputJsonObject.toString())
                        : Constants.sendPost(Constants.EVENT_SERVER_1_ADDRESS,
                                Constants.EVENT_SERVER_1_PORT,
                                Constants.EVENT_PURCHASE_LOCATION + "/" + j,
                                inputJsonObject.toString());

                eventIds.add(j);
            });

            threads[i - 15].start();
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

        for (int i : eventIds) {
            String response = frontend
                    ? Constants.sendGet(Constants.FRONTEND_SERVER_1_ADDRESS,
                            Constants.FRONTEND_SERVER_1_PORT,
                            Constants.FRONTEND_EVENT_LOCATION + "/"
                                    + i,
                            null)
                    : Constants.sendGet(Constants.EVENT_SERVER_2_ADDRESS,
                            Constants.EVENT_SERVER_2_PORT,
                            "/" + i,
                            null);

            JsonParser jsonParser = new JsonParser();
            JsonObject outputJsonObject =
                    jsonParser.parse(response).getAsJsonObject();

            int numTickets = outputJsonObject.getAsJsonPrimitive("purchased")
                    .getAsInt();

            if (numTickets != (i * 5)) {
                logger.severe("Test failed for the numTickets check!");
                return;
            }
        }

        logger.info("Test passed for 15 purchased events.");
        logger.info("As a reference, here is the events list:");
        logger.info(Constants.sendGet(Constants.EVENT_SERVER_2_ADDRESS,
                Constants.EVENT_SERVER_2_PORT,
                Constants.EVENT_LIST_LOCATION,
                null));
    }
}
