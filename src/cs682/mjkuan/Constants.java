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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Constants {
    public static final String EVENT_SERVER_1_ADDRESS = "mc04";
    public static final int EVENT_SERVER_1_PORT = 3457;

    public static final String EVENT_SERVER_2_ADDRESS = "mc05";
    public static final int EVENT_SERVER_2_PORT = 3458;

    public static final String EVENT_SERVER_3_ADDRESS = "mc06";
    public static final int EVENT_SERVER_3_PORT = 3459;

    public static final String FRONTEND_SERVER_1_ADDRESS = "mc04";
    public static final int FRONTEND_SERVER_1_PORT = 3471;

    public static final String FRONTEND_SERVER_2_ADDRESS = "mc05";
    public static final int FRONTEND_SERVER_2_PORT = 3472;

    public static final String FRONTEND_SERVER_3_ADDRESS = "mc06";
    public static final int FRONTEND_SERVER_3_PORT = 3473;

    public static final int CONNECTION_TIMEOUT = 5000;

    public static final String HEARTBEAT_LOCATION = "/heartbeat";
    public static final String ELECTION_INITIATION_LOCATION =
            "/election/intiate";
    public static final String ELECTION_VICTORY_LOCATION = "/election/victory";
    public static final String FETCH_MEMBERSHIP_LOCATION = "/members";
    public static final String GOSSIP_MEMBERSHIP_LOCATION = "/members/gossip";
    public static final String REGISTER_MEMBER_LOCATION = "/members/register";

    public static final String EVENT_CREATE_LOCATION = "/create";
    public static final String EVENT_LIST_LOCATION = "/list";
    public static final String EVENT_PURCHASE_LOCATION = "/purchase/";

    public static final String FRONTEND_EVENT_LOCATION = "/events";
    public static final String FRONTEND_EVENT_CREATE_LOCATION =
            FRONTEND_EVENT_LOCATION + "/create";

    public static final String PROTOCOL = "http://";

    public static final int USER_ID = 2995;

    public static String sendGet(String address, int port, String path,
            String[] query) {
        String destination = PROTOCOL + address + ":" + port;
        destination += path;

        if (query != null && query.length > 0) {
            destination += "?" + query[0];
            for (int i = 1; i < query.length; i++) {
                destination += "&" + query[i];
            }
        }

        URL url;
        try {
            url = new URL(destination);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }

        HttpURLConnection urlConnection;
        try {
            urlConnection =
                    (HttpURLConnection) url.openConnection();
            urlConnection.connect();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);

        StringBuilder sb = new StringBuilder();

        try (InputStream inputStream = (urlConnection.getResponseCode() == 200)
                ? urlConnection.getInputStream()
                : urlConnection.getErrorStream();
                InputStreamReader inputStreamReader =
                        new InputStreamReader(inputStream);
                BufferedReader reader =
                        new BufferedReader(inputStreamReader);) {
            for (String line = reader.readLine(); line != null;
                    line = reader.readLine()) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return sb.toString();
    }

    public static String sendPost(String address, int port, String path,
            String input) {
        String destination = PROTOCOL + address + ":" + port;
        destination += path;

        URL url;
        try {
            url = new URL(destination);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }

        HttpURLConnection urlConnection;
        try {
            urlConnection =
                    (HttpURLConnection) url.openConnection();

            urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type",
                    "application/json");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        try (OutputStream outputStream = urlConnection.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream);) {
            writer.println(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder sb = new StringBuilder();
        try (InputStream inputStream = (urlConnection.getResponseCode() == 200)
                ? urlConnection.getInputStream()
                : urlConnection.getErrorStream();
                InputStreamReader inputStreamReader =
                        new InputStreamReader(inputStream);
                BufferedReader reader =
                        new BufferedReader(inputStreamReader);) {
            for (String line = reader.readLine(); line != null;
                    line = reader.readLine()) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return sb.toString();
    }
}
