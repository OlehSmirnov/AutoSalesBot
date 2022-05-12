package com.olegsmirnov;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.json.*;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;

import static org.apache.http.protocol.HTTP.USER_AGENT;

public class Controller extends Thread {

    private final double SELL_PERCENT = 0.997;
    private final double BUY_PERCENT = 0.943;
    private final int MINIMUM_POPULARITY = 150;
    private final int MINIMUM_PRICE = 200;
    private final int POST_PARAMS_LIMIT = 100;

    private volatile List<String> autotradableItemsPUBG;
    private volatile List<String> autotradableItemsDOTA;

    @Override
    public void run() {
        System.setProperty("http.agent", "Chrome");
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+3:00"));
        deleteBuyOrders();
        updateDatabase(Game.DOTA1);
        new Logger().start();
        new OnlineUpdaterPUBG().start();
    //    new OnlineUpdaterPUBG2().start();
        new OnlineUpdaterDOTA().start();
        new InventoryUpdaterPUBG().start();
    //    new InventoryUpdaterPUBG2().start();
        new InventoryUpdaterDOTA().start();
        new OrdersUpdaterPUBG().start();
    //    new OrdersUpdaterPUBG2().start();
        new OrdersUpdaterDOTA().start();
        new PricesUpdaterPUBG().start();
    //    new PricesUpdaterPUBG2().start();
        new PricesUpdaterDOTA().start();
        new TraderPUBG().start();
    //    new TraderPUBG2().start();
        new TraderDOTA().start();
    }

    private void processOrdersPUBG() {
        try {
            List<String> postParams = new ArrayList<>();
            int counter = 0;
            for (String item : autotradableItemsDOTA) {
                counter++;
                if (counter == autotradableItemsDOTA.size() || counter % POST_PARAMS_LIMIT == 0)
                    postParams.add(item.substring(0, item.length() - 1).replaceFirst("/", "_"));
                else
                    postParams.add(item.substring(0, item.length() - 1).replaceFirst("/", "_") + ",");
            }
            processAllOrders(Game.DOTA1, postParams);
        } catch (Exception e) {
            System.out.println("Exception in processOrdersPUBG: " + e.getMessage());
        }
    }

    private void processOrdersPUBG2() {
        try {
            List<String> postParams = new ArrayList<>();
            int counter = 0;
            for (String item : autotradableItemsDOTA) {
                counter++;
                if (counter == autotradableItemsDOTA.size() || counter % POST_PARAMS_LIMIT == 0)
                    postParams.add(item.substring(0, item.length() - 1).replaceFirst("/", "_"));
                else
                    postParams.add(item.substring(0, item.length() - 1).replaceFirst("/", "_") + ",");
            }
            processAllOrders(Game.DOTA2, postParams);
        } catch (Exception e) {
            System.out.println("Exception in processOrdersPUBG2: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processOrdersDOTA() {
        try {
            List<String> postParams = new ArrayList<>();
            int counter = 0;
            for (String item : autotradableItemsDOTA) {
                counter++;
                if (counter == autotradableItemsDOTA.size() || counter % POST_PARAMS_LIMIT == 0)
                    postParams.add(item.substring(0, item.length() - 1).replaceFirst("/", "_"));
                else
                    postParams.add(item.substring(0, item.length() - 1).replaceFirst("/", "_") + ",");
            }
            processAllOrders(Game.DOTA3, postParams);
        } catch (Exception e) {
            System.out.println("Exception in processOrdersDOTA: " + e.getMessage());
        }
    }

    private void setPricesPUBG() {
        try {
            JSONArray inventory = getInventory(Game.DOTA1);
            if (inventory != null) {
                for (int i = 0; i < inventory.length(); i++) {
                    JSONObject item = inventory.optJSONObject(i);
                    if (item != null) {
                        String classId = item.optString("i_classid") + "/";
                        String instanceId = item.optString("i_instanceid") + "/";
                        if (!classId.equals("2451526758/")) {
                            JSONArray itemSellOrders = getItemSellOrders(classId + instanceId, Game.DOTA1);
                            double avgPrice = getItemAveragePrice(classId + instanceId, Game.DOTA1);
                            if (avgPrice != 0 && itemSellOrders != null) {
                                for (int j = 0; j < itemSellOrders.length(); j++) {
                                    JSONObject order = itemSellOrders.optJSONObject(j);
                                    if (order != null) {
                                        long sellOrderPrice = Long.parseLong(order.optString("price"));
                                        int myCount = Integer.parseInt(order.optString("my_count"));
                                        if (sellOrderPrice >= avgPrice * SELL_PERCENT && myCount != 0 && sellOrderPrice >= avgPrice) {
                                            sendGetRequest("https://market.dota2.net/api/SetPrice/" +
                                                    item.optString("ui_id") + "/" + String.valueOf(sellOrderPrice) + "/?key=" + API_KEY_DOTA1);
                                            break;
                                        } else if (sellOrderPrice >= avgPrice * SELL_PERCENT && myCount == 0) {
                                            sendGetRequest("https://market.dota2.net/api/SetPrice/" +
                                                    item.optString("ui_id") + "/" + String.valueOf(sellOrderPrice - 1) + "/?key=" + API_KEY_DOTA1);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in setPricesPUBG: " + e.getMessage());
        }
    }

    private void setPricesPUBG2() {
        try {
            JSONArray inventory = getInventory(Game.DOTA2);
            if (inventory != null) {
                for (int i = 0; i < inventory.length(); i++) {
                    JSONObject item = inventory.optJSONObject(i);
                    if (item != null) {
                        String classId = item.optString("i_classid") + "/";
                        if (!classId.equals("2451526758/")) {
                            String instanceId = item.optString("i_instanceid") + "/";
                            JSONArray itemSellOrders = getItemSellOrders(classId + instanceId, Game.DOTA2);
                            double avgPrice = getItemAveragePrice(classId + instanceId, Game.DOTA2);
                            if (avgPrice != 0 && itemSellOrders != null) {
                                for (int j = 0; j < itemSellOrders.length(); j++) {
                                    JSONObject order = itemSellOrders.optJSONObject(j);
                                    if (order != null) {
                                        long sellOrderPrice = Long.parseLong(order.optString("price"));
                                        int myCount = Integer.parseInt(order.optString("my_count"));
                                        if (sellOrderPrice >= avgPrice * SELL_PERCENT && myCount != 0) {
                                            sendGetRequest("https://market.dota2.net/api/SetPrice/" +
                                                    item.optString("ui_id") + "/" + String.valueOf(sellOrderPrice) + "/?key=" + API_KEY_DOTA2);
                                            break;
                                        } else if (sellOrderPrice >= avgPrice * SELL_PERCENT && myCount == 0) {
                                            sendGetRequest("https://market.dota2.net/api/SetPrice/" +
                                                    item.optString("ui_id") + "/" + String.valueOf(sellOrderPrice - 1) + "/?key=" + API_KEY_DOTA2);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in setPricesPUBG2: " + e.getMessage());
        }
    }

    private void setPricesDOTA() {
        try {
            JSONArray inventory = getInventory(Game.DOTA3);
            if (inventory != null) {
                for (int i = 0; i < inventory.length(); i++) {
                    JSONObject item = inventory.optJSONObject(i);
                    if (item != null) {
                        String classId = item.optString("i_classid") + "/";
                        String instanceId = item.optString("i_instanceid") + "/";
                        JSONArray itemSellOrders = getItemSellOrders(classId + instanceId, Game.DOTA3);
                        double avgPrice = getItemAveragePrice(classId + instanceId, Game.DOTA3);
                        if (itemSellOrders == null) {
                            instanceId = "0/";
                            itemSellOrders = getItemSellOrders(classId + instanceId, Game.DOTA3);
                            avgPrice = getItemAveragePrice(classId + instanceId, Game.DOTA3);
                        }
                        if (avgPrice != 0 && itemSellOrders != null) {
                            for (int j = 0; j < itemSellOrders.length(); j++) {
                                JSONObject order = itemSellOrders.optJSONObject(j);
                                if (order != null) {
                                    long sellOrderPrice = Long.parseLong(order.optString("price"));
                                    int myCount = Integer.parseInt(order.optString("my_count"));
                                    if (sellOrderPrice >= avgPrice * SELL_PERCENT && myCount != 0) {
                                        sendGetRequest("https://market.dota2.net/api/SetPrice/" +
                                                item.optString("ui_id") + "/" + String.valueOf(sellOrderPrice) + "/?key=" + API_KEY_DOTA3);
                                        break;
                                    } else if (sellOrderPrice >= avgPrice * SELL_PERCENT && myCount == 0) {
                                        sendGetRequest("https://market.dota2.net/api/SetPrice/" +
                                                item.optString("ui_id") + "/" + String.valueOf(sellOrderPrice - 1) + "/?key=" + API_KEY_DOTA3);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in setPricesDOTA: " + e.getMessage());
        }
    }

    private void updatePricesPUBG() {
        try {
            JSONArray trades = getTrades(Game.DOTA1);
            if (trades != null) {
                Set<String> uniqueItems = new HashSet<>();
                for (int i = 0; i < trades.length(); i++) {
                    JSONObject item = trades.optJSONObject(i);
                    String uiStatus = item.optString("ui_status");
                    if (uiStatus.equals("1")) {
                        String classId = item.optString("i_classid") + "_";
                        String instanceId = item.optString("i_instanceid");
                        uniqueItems.add(classId + instanceId);
                    }
                }
                List<String> postParams = new ArrayList<>();
                Iterator<String> iterator = uniqueItems.iterator();
                while (iterator.hasNext()) {
                    String item = iterator.next();
                    if (iterator.hasNext())
                        postParams.add(item + ",");
                    else
                        postParams.add(item);
                }
                String responseStr = sendPostRequest("https://market.dota2.net/api/MassInfo/1/0/1/0/?key=" + API_KEY_DOTA1, postParams);
                if (responseStr != null) {
                    JSONObject response = new JSONObject(responseStr);
                    JSONArray results = response.optJSONArray("results");
                    if (results != null) {
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject item = results.optJSONObject(i);
                            String classId = item.optString("classid") + "_";
                            String instanceId = item.optString("instanceid");
                            JSONObject sellOffers = item.optJSONObject("sell_offers");
                            if (sellOffers != null) {
                                JSONArray sellOffersArray = sellOffers.optJSONArray("offers");
                                JSONArray mySellOffersArray = sellOffers.optJSONArray("my_offers");
                                long myOffersPrice = 0;
                                if (mySellOffersArray != null)
                                    myOffersPrice = mySellOffersArray.optLong(0);
                                JSONObject history = item.optJSONObject("history");
                                JSONArray historyArray = history.optJSONArray("history");
                                double avgPrice = getItemAveragePrice(historyArray);
                                if (avgPrice != 0 && sellOffersArray != null) {
                                    for (int j = 0; j < sellOffersArray.length(); j++) {
                                        long sellOfferPrice = sellOffersArray.optJSONArray(j).optLong(0);
                                        if (sellOfferPrice >= avgPrice * SELL_PERCENT && myOffersPrice > sellOfferPrice) {
                                            if (uniqueItems.contains(classId + instanceId))
                                                sendGetRequest("https://market.dota2.net/api/MassSetPrice/" + (classId + instanceId + "/") +
                                                        String.valueOf(sellOfferPrice - 1) + "/?key=" + API_KEY_DOTA1);
                                            break;
                                        } else if (sellOfferPrice >= avgPrice * SELL_PERCENT && myOffersPrice < sellOfferPrice) {
                                            if (uniqueItems.contains(classId + instanceId) && (sellOfferPrice > myOffersPrice + 1))
                                                sendGetRequest("https://market.dota2.net/api/MassSetPrice/" + (classId + instanceId + "/") +
                                                        String.valueOf(sellOfferPrice - 1) + "/?key=" + API_KEY_DOTA1);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in updatePricesPUBG: " + e.getMessage());
        }
    }

    private void updatePricesPUBG2() {
        try {
            JSONArray trades = getTrades(Game.DOTA2);
            if (trades != null) {
                Set<String> uniqueItems = new HashSet<>();
                for (int i = 0; i < trades.length(); i++) {
                    JSONObject item = trades.optJSONObject(i);
                    String uiStatus = item.optString("ui_status");
                    if (uiStatus.equals("1")) {
                        String classId = item.optString("i_classid") + "_";
                        String instanceId = item.optString("i_instanceid");
                        uniqueItems.add(classId + instanceId);
                    }
                }
                List<String> postParams = new ArrayList<>();
                Iterator<String> iterator = uniqueItems.iterator();
                while (iterator.hasNext()) {
                    String item = iterator.next();
                    if (iterator.hasNext())
                        postParams.add(item + ",");
                    else
                        postParams.add(item);
                }
                String responseStr = sendPostRequest("https://market.dota2.net/api/MassInfo/1/0/1/0/?key=" + API_KEY_DOTA2, postParams);
                if (responseStr != null) {
                    JSONObject response = new JSONObject(responseStr);
                    JSONArray results = response.optJSONArray("results");
                    if (results != null) {
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject item = results.optJSONObject(i);
                            String classId = item.optString("classid") + "_";
                            String instanceId = item.optString("instanceid");
                            JSONObject sellOffers = item.optJSONObject("sell_offers");
                            if (sellOffers != null) {
                                JSONArray sellOffersArray = sellOffers.optJSONArray("offers");
                                JSONArray mySellOffersArray = sellOffers.optJSONArray("my_offers");
                                long myOffersPrice = 0;
                                if (mySellOffersArray != null)
                                    myOffersPrice = mySellOffersArray.optLong(0);
                                JSONObject history = item.optJSONObject("history");
                                JSONArray historyArray = history.optJSONArray("history");
                                double avgPrice = getItemAveragePrice(historyArray);
                                if (avgPrice != 0 && sellOffersArray != null) {
                                    for (int j = 0; j < sellOffersArray.length(); j++) {
                                        long sellOfferPrice = sellOffersArray.optJSONArray(j).optLong(0);
                                        if (sellOfferPrice >= avgPrice * SELL_PERCENT && myOffersPrice > sellOfferPrice) {
                                            if (uniqueItems.contains(classId + instanceId))
                                                sendGetRequest("https://market.dota2.net/api/MassSetPrice/" + (classId + instanceId + "/") +
                                                        String.valueOf(sellOfferPrice - 1) + "/?key=" + API_KEY_DOTA2);
                                            break;
                                        } else if (sellOfferPrice >= avgPrice * SELL_PERCENT && myOffersPrice < sellOfferPrice) {
                                            if (uniqueItems.contains(classId + instanceId) && (sellOfferPrice > myOffersPrice + 1))
                                                sendGetRequest("https://market.dota2.net/api/MassSetPrice/" + (classId + instanceId + "/") +
                                                        String.valueOf(sellOfferPrice - 1) + "/?key=" + API_KEY_DOTA2);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in updatePricesPUBG: " + e.getMessage());
        }
    }

    private void updatePricesDOTA() {
        try {
            JSONArray trades = getTrades(Game.DOTA3);
            if (trades != null) {
                Set<String> uniqueItems = new HashSet<>();
                for (int i = 0; i < trades.length(); i++) {
                    JSONObject item = trades.optJSONObject(i);
                    String uiStatus = item.optString("ui_status");
                    if (uiStatus.equals("1")) {
                        String classId = item.optString("i_classid") + "_";
                        String instanceId = item.optString("i_instanceid");
                        uniqueItems.add(classId + instanceId);
                    }
                }
                List<String> postParams = new ArrayList<>();
                Iterator<String> iterator = uniqueItems.iterator();
                while (iterator.hasNext()) {
                    String item = iterator.next();
                    if (iterator.hasNext())
                        postParams.add(item + ",");
                    else
                        postParams.add(item);
                }
                String responseStr = sendPostRequest("https://market.dota2.net/api/MassInfo/1/0/1/0/?key=" + API_KEY_DOTA3, postParams);
                if (responseStr != null) {
                    JSONObject response = new JSONObject(responseStr);
                    JSONArray results = response.optJSONArray("results");
                    if (results != null) {
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject item = results.optJSONObject(i);
                            String classId = item.optString("classid") + "_";
                            String instanceId = item.optString("instanceid");
                            JSONObject sellOffers = item.optJSONObject("sell_offers");
                            if (sellOffers != null) {
                                JSONArray sellOffersArray = sellOffers.optJSONArray("offers");
                                JSONArray mySellOffersArray = sellOffers.optJSONArray("my_offers");
                                long myOffersPrice = 0;
                                if (mySellOffersArray != null)
                                    myOffersPrice = mySellOffersArray.optLong(0);
                                JSONObject history = item.optJSONObject("history");
                                JSONArray historyArray = history.optJSONArray("history");
                                double avgPrice = getItemAveragePrice(historyArray);
                                if (avgPrice != 0 && sellOffersArray != null) {
                                    for (int j = 0; j < sellOffersArray.length(); j++) {
                                        long sellOfferPrice = sellOffersArray.optJSONArray(j).optLong(0);
                                        if (sellOfferPrice >= avgPrice * SELL_PERCENT && myOffersPrice > sellOfferPrice) {
                                            if (uniqueItems.contains(classId + instanceId))
                                                sendGetRequest("https://market.dota2.net/api/MassSetPrice/" + (classId + instanceId + "/") +
                                                        String.valueOf(sellOfferPrice - 1) + "/?key=" + API_KEY_DOTA3);
                                            break;
                                        } else if (sellOfferPrice >= avgPrice * SELL_PERCENT && myOffersPrice < sellOfferPrice) {
                                            if (uniqueItems.contains(classId + instanceId) && (sellOfferPrice > myOffersPrice + 1))
                                                sendGetRequest("https://market.dota2.net/api/MassSetPrice/" + (classId + instanceId + "/") +
                                                        String.valueOf(sellOfferPrice - 1) + "/?key=" + API_KEY_DOTA3);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in updatePricesDOTA: " + e.getMessage());
        }
    }

    private void processTradesPUBG() {
        try {
            Set<String> botIds = new HashSet<>();
            JSONArray trades = getTrades(Game.DOTA1);
            if (trades != null && trades.length() > 0) {
                for (int i = 0; i < trades.length(); i++) {
                    JSONObject trade = trades.optJSONObject(i);
                    if (trade != null) {
                        String uiStatus = trade.optString("ui_status");
                        String uiBid = trade.optString("ui_bid");
                        if (uiStatus.equals("2")) {
                            sendGetRequest("https://market.dota2.net/api/ItemRequest/in/1/?key=" + API_KEY_DOTA1);
                        } else if (uiStatus.equals("4")) {
                            if (uiBid != null)
                                botIds.add(uiBid);
                        }
                    }
                }
            }
            if (!botIds.isEmpty()) {
                for (String botId : botIds) {
                    sendGetRequest("https://market.dota2.net/api/ItemRequest/out/" + botId + "/?key=" + API_KEY_DOTA1);
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in TraderPUBG: " + e.getMessage());
        }
    }

    private void processTradesPUBG2() {
        try {
            Set<String> botIds = new HashSet<>();
            JSONArray trades = getTrades(Game.DOTA2);
            if (trades != null && trades.length() > 0) {
                for (int i = 0; i < trades.length(); i++) {
                    JSONObject trade = trades.optJSONObject(i);
                    if (trade != null) {
                        String uiStatus = trade.optString("ui_status");
                        String uiBid = trade.optString("ui_bid");
                        if (uiStatus.equals("2")) {
                            sendGetRequest("https://market.dota2.net/api/ItemRequest/in/1/?key=" + API_KEY_DOTA2);
                        } else if (uiStatus.equals("4")) {
                            if (uiBid != null)
                                botIds.add(uiBid);
                        }

                    }
                }
            }
            if (!botIds.isEmpty()) {
                for (String botId : botIds) {
                    sendGetRequest("https://market.dota2.net/api/ItemRequest/out/" + botId + "/?key=" + API_KEY_DOTA2);
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in TraderPUBG2: " + e.getMessage());
        }
    }

    private void processTradesDOTA() {
        try {
            Set<String> botIds = new HashSet<>();
            JSONArray trades = getTrades(Game.DOTA3);
            if (trades != null && trades.length() > 0) {
                for (int i = 0; i < trades.length(); i++) {
                    JSONObject trade = trades.optJSONObject(i);
                    if (trade != null) {
                        String uiStatus = trade.optString("ui_status");
                        String uiBid = trade.optString("ui_bid");
                        if (uiStatus.equals("2")) {
                            sendGetRequest("https://market.dota2.net/api/ItemRequest/in/1/?key=" + API_KEY_DOTA3);
                        } else if (uiStatus.equals("4")) {
                            if (uiBid != null)
                                botIds.add(uiBid);
                        }
                    }
                }
            }
            if (!botIds.isEmpty()) {
                for (String botId : botIds) {
                    sendGetRequest("https://market.dota2.net/api/ItemRequest/out/" + botId + "/?key=" + API_KEY_DOTA3);
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in TraderDOTA: " + e.getMessage());
        }
    }

    private void processAllOrders(Game gameName, List<String> postParams) {
        try {
            String response;
            if (gameName == Game.DOTA1) {
                int remainingParams = postParams.size();
                for (int i = 0; i < postParams.size(); i += POST_PARAMS_LIMIT) {
                    if (remainingParams > POST_PARAMS_LIMIT) {
                        response = sendPostRequest
                                ("https://market.dota2.net/api/MassInfo/1/1/1/0/?key=" + API_KEY_DOTA1, postParams.subList(i, i + POST_PARAMS_LIMIT));
                        if (response != null)
                            processHundredOrders(Game.DOTA1, response);
                        remainingParams -= POST_PARAMS_LIMIT;
                    } else {
                        response = sendPostRequest
                                ("https://market.dota2.net/api/MassInfo/1/1/1/0/?key=" + API_KEY_DOTA1, postParams.subList(i, i + remainingParams));
                        if (response != null)
                            processHundredOrders(Game.DOTA1, response);
                        remainingParams = 0;
                    }
                }
            } else if (gameName == Game.DOTA2) {
                int remainingParams = postParams.size();
                for (int i = 0; i < postParams.size(); i += POST_PARAMS_LIMIT) {
                    if (remainingParams > POST_PARAMS_LIMIT) {
                        response = sendPostRequest
                                ("https://market.dota2.net/api/MassInfo/1/1/1/0/?key=" + API_KEY_DOTA2, postParams.subList(i, i + POST_PARAMS_LIMIT));
                        if (response != null)
                            processHundredOrders(Game.DOTA2, response);
                        remainingParams -= POST_PARAMS_LIMIT;
                    } else {
                        response = sendPostRequest
                                ("https://market.dota2.net/api/MassInfo/1/1/1/0/?key=" + API_KEY_DOTA2, postParams.subList(i, i + remainingParams));
                        if (response != null)
                            processHundredOrders(Game.DOTA2, response);
                        remainingParams = 0;
                    }
                }
            } else if (gameName == Game.DOTA3) {
                int remainingParams = postParams.size();
                for (int i = 0; i < postParams.size(); i += POST_PARAMS_LIMIT) {
                    if (remainingParams > POST_PARAMS_LIMIT) {
                        response = sendPostRequest
                                ("https://market.dota2.net/api/MassInfo/1/1/1/0/?key=" + API_KEY_DOTA3, postParams.subList(i, i + POST_PARAMS_LIMIT));
                        if (response != null)
                            processHundredOrders(Game.DOTA3, response);
                        remainingParams -= POST_PARAMS_LIMIT;
                    } else {
                        response = sendPostRequest
                                ("https://market.dota2.net/api/MassInfo/1/1/1/0/?key=" + API_KEY_DOTA3, postParams.subList(i, i + remainingParams));
                        if (response != null)
                            processHundredOrders(Game.DOTA3, response);
                        remainingParams = 0;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in processAllOrders: " + e.getMessage());
        }
    }

    private void processHundredOrders(Game gameName, String responseStr) {
        try {
            JSONObject response = new JSONObject(responseStr);
            JSONArray results = response.optJSONArray("results");
            if (results != null) {
                for (int i = 0; i < results.length(); i++) {
                    JSONObject result = results.optJSONObject(i);
                    JSONObject buyOrders = result.optJSONObject("buy_offers");
                    JSONObject sellOffers = result.optJSONObject("sell_offers");
                    if (buyOrders != null && sellOffers != null) {
                        JSONArray buyOrdersArray = buyOrders.optJSONArray("offers");
                        long myOrderPrice = buyOrders.optLong("my_offer");
                        JSONObject history = result.optJSONObject("history");
                        JSONArray historyArray = history.optJSONArray("history");
                        double historyAvgPrice = history.optLong("average");
                        String classId = result.optString("classid") + "/";
                        String instanceId = result.optString("instanceid") + "/";
                        double avgPrice = getItemAveragePrice(historyArray);
                        if (avgPrice >= MINIMUM_PRICE && historyAvgPrice >= MINIMUM_PRICE) {
                            for (int j = 0; j < 2; j++) {
                                JSONArray order = buyOrdersArray.optJSONArray(j);
                                if (order != null) {
                                    long orderPrice = order.optLong(0);
                                    if (gameName == Game.DOTA1) {
                                        if (myOrderPrice < orderPrice && orderPrice >= MINIMUM_PRICE && orderPrice <= 1000 && orderPrice <= avgPrice * BUY_PERCENT ) {
                                            sendGetRequest("https://market.dota2.net/api/ProcessOrder/" +
                                                    classId + instanceId + String.valueOf(orderPrice + 2) + "/?key=" + API_KEY_DOTA1);
                                        } else if (myOrderPrice > orderPrice + 2 && orderPrice >= MINIMUM_PRICE && orderPrice <= 1000 && orderPrice <= avgPrice * BUY_PERCENT)
                                        break;
                                    } else if (gameName == Game.DOTA2) {
                                        if (myOrderPrice < orderPrice && orderPrice > MINIMUM_PRICE && orderPrice <= 1000 && orderPrice <= avgPrice * 0.92) {
                                            sendGetRequest("https://market.dota2.net/api/ProcessOrder/" +
                                                    classId + instanceId + String.valueOf(orderPrice + 2) + "/?key=" + API_KEY_DOTA2);
                                        } else if (myOrderPrice > orderPrice + 2 && orderPrice >= MINIMUM_PRICE && orderPrice <= 1000 && orderPrice <= avgPrice * 0.92)
                                        break;
                                    } else if (gameName == Game.DOTA3) {
                                        if (myOrderPrice < orderPrice && orderPrice > 1000 && orderPrice <= avgPrice * BUY_PERCENT) {
                                            sendGetRequest("https://market.dota2.net/api/ProcessOrder/" +
                                                    classId + instanceId + String.valueOf(orderPrice + 2) + "/?key=" + API_KEY_DOTA3);
                                        } else if (myOrderPrice > orderPrice + 2 && orderPrice >= MINIMUM_PRICE && orderPrice > 1000 && orderPrice <= avgPrice * BUY_PERCENT)
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in processHundredOrders: ");
            e.printStackTrace();
        }
    }

    private double getItemAveragePrice(String itemInfo, Game gameName) {
        try {
            String response = null;
            if (gameName == Game.DOTA1) {
                response = sendGetRequest("https://market.dota2.net/api/ItemHistory/" +
                        itemInfo.replaceFirst("/", "_") + "?key=" + API_KEY_DOTA1);
            } else if (gameName == Game.DOTA2) {
                response = sendGetRequest("https://market.dota2.net/api/ItemHistory/" +
                        itemInfo.replaceFirst("/", "_") + "?key=" + API_KEY_DOTA2);
            } else if (gameName == Game.DOTA3) {
                response = sendGetRequest("https://market.dota2.net/api/ItemHistory/" +
                        itemInfo.replaceFirst("/", "_") + "?key=" + API_KEY_DOTA3);
            }
            if (response != null) {
                JSONArray history = new JSONObject(response).optJSONArray("history");
                if (history != null) {
                    List<Long> prices = new ArrayList<>();
                    long lastTriggeredTime = Long.parseLong(history.optJSONObject(0).optString("l_time"));
                    JSONObject firstTrigger = history.optJSONObject(29);
                    if (firstTrigger != null) {
                        long firstTriggeredTime = Long.parseLong(history.optJSONObject(29).optString("l_time"));
                        if (lastTriggeredTime != 0 && firstTriggeredTime != 0) {
                            if (lastTriggeredTime - firstTriggeredTime >= 43200 * 3) {
                                for (int i = 0; i < 15; i++) {
                                    long price = Long.parseLong(history.optJSONObject(i).optString("l_price"));
                                    if (price != 0)
                                        prices.add(price);
                                }
                            } else if (lastTriggeredTime - firstTriggeredTime >= 43200 && lastTriggeredTime - firstTriggeredTime < 43200 * 3) {
                                for (int i = 0; i < 20; i++) {
                                    long price = Long.parseLong(history.optJSONObject(i).optString("l_price"));
                                    if (price != 0)
                                        prices.add(price);
                                }
                            } else if (lastTriggeredTime - firstTriggeredTime >= 60 * 300 && lastTriggeredTime - firstTriggeredTime < 43200) {
                                for (int i = 0; i < 30; i++) {
                                    long price = Long.parseLong(history.optJSONObject(i).optString("l_price"));
                                    if (price != 0)
                                        prices.add(price);
                                }
                            } else if (lastTriggeredTime - firstTriggeredTime >= 60 * 60 && lastTriggeredTime - firstTriggeredTime < 60 * 300) {
                                for (int i = 0; i < 50; i++) {
                                    long price = Long.parseLong(history.optJSONObject(i).optString("l_price"));
                                    if (price != 0)
                                        prices.add(price);
                                }
                            } else if (lastTriggeredTime - firstTriggeredTime >= 60 * 20 && lastTriggeredTime - firstTriggeredTime < 60 * 60) {
                                for (int i = 0; i < 70; i++) {
                                    long price = Long.parseLong(history.optJSONObject(i).optString("l_price"));
                                    if (price != 0)
                                        prices.add(price);
                                }
                            } else if (lastTriggeredTime - firstTriggeredTime < 60 * 20) {
                                for (int i = 0; i < 100; i++) {
                                    long price = Long.parseLong(history.optJSONObject(i).optString("l_price"));
                                    if (price != 0)
                                        prices.add(price);
                                }
                            }
                            Collections.sort(prices);
                            if (!prices.isEmpty()) {
                                if (prices.size() <= 35) {
                                    for (int i = 0; i < 3; i++) {
                                        prices.remove(0);
                                        prices.remove(prices.size() - 1);
                                    }
                                } else if (prices.size() <= 50) {
                                    for (int i = 0; i < 5; i++) {
                                        prices.remove(0);
                                        prices.remove(prices.size() - 1);
                                    }
                                } else if (prices.size() <= 100) {
                                    for (int i = 0; i < 10; i++) {
                                        prices.remove(0);
                                        prices.remove(prices.size() - 1);
                                    }
                                }
                                long sumOfPrices = 0;
                                for (long price : prices) {
                                    sumOfPrices += price;
                                }
                                double avgPrice = sumOfPrices / prices.size();
                                int counter = 0;
                                long resultSumOfPrices = 0;
                                for (long price : prices) {
                                    if (!(price > avgPrice * 1.3) && !(price < avgPrice / 1.3)) {
                                        counter++;
                                        resultSumOfPrices += price;
                                    }
                                }
                                if (counter == 0)
                                    return 0;
                                else
                                    return resultSumOfPrices / counter;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in getItemAveragePrice, game " + gameName + ": " + e.getMessage());
        }
        return 0;
    }

    private double getItemAveragePrice(JSONArray historyArray) {
        try {
            if (historyArray != null) {
                List<Long> prices = new ArrayList<>();
                long lastTriggeredTime = historyArray.optJSONArray(0).optLong(0);
                JSONArray firstTrigger = historyArray.optJSONArray(29);
                long firstTriggeredTime = 0;
                if (firstTrigger != null)
                    firstTriggeredTime = firstTrigger.optLong(0);
                if (firstTriggeredTime != 0) {
                    if (lastTriggeredTime - firstTriggeredTime >= 43200 * 3) {
                        for (int i = 0; i < 15; i++) {
                            long price = historyArray.optJSONArray(i).optLong(1);
                            if (price != 0)
                                prices.add(price);
                        }
                    } else if (lastTriggeredTime - firstTriggeredTime >= 43200) {
                        for (int i = 0; i < 20; i++) {
                            long price = historyArray.optJSONArray(i).optLong(1);
                            if (price != 0)
                                prices.add(price);
                        }
                    } else if (lastTriggeredTime - firstTriggeredTime >= 60 * 300) {
                        for (int i = 0; i < 30; i++) {
                            long price = historyArray.optJSONArray(i).optLong(1);
                            if (price != 0)
                                prices.add(price);
                        }
                    } else if (lastTriggeredTime - firstTriggeredTime >= 60 * 60) {
                        for (int i = 0; i < 50; i++) {
                            long price = historyArray.optJSONArray(i).optLong(1);
                            if (price != 0)
                                prices.add(price);
                        }
                    } else if (lastTriggeredTime - firstTriggeredTime >= 60 * 20) {
                        for (int i = 0; i < 70; i++) {
                            long price = historyArray.optJSONArray(i).optLong(1);
                            if (price != 0)
                                prices.add(price);
                        }
                    } else if (lastTriggeredTime - firstTriggeredTime < 60 * 20) {
                        for (int i = 0; i < 100; i++) {
                            long price = historyArray.optJSONArray(i).optLong(1);
                            if (price != 0)
                                prices.add(price);
                        }
                    }
                    Collections.sort(prices);
                    if (!prices.isEmpty()) {
                        if (prices.size() <= 35) {
                            for (int i = 0; i < 3; i++) {
                                prices.remove(0);
                                prices.remove(prices.size() - 1);
                            }
                        } else if (prices.size() <= 50) {
                            for (int i = 0; i < 5; i++) {
                                prices.remove(0);
                                prices.remove(prices.size() - 1);
                            }
                        } else if (prices.size() <= 100) {
                            for (int i = 0; i < 10; i++) {
                                prices.remove(0);
                                prices.remove(prices.size() - 1);
                            }
                        }
                        long sumOfPrices = 0;
                        for (long price : prices) {
                            sumOfPrices += price;
                        }
                        double avgPrice = sumOfPrices / prices.size();
                        int counter = 0;
                        long resultSumOfPrices = 0;
                        for (long price : prices) {
                            if (!(price > avgPrice * 1.3) && !(price < avgPrice / 1.3)) {
                                counter++;
                                resultSumOfPrices += price;
                            }
                        }
                        if (counter == 0)
                            return 0;
                        else
                            return resultSumOfPrices / counter;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception in getItemAveragePrice (array): " + e.getMessage());
        }
        return 0;
    }

    private double getBestBuyOfferPrice(String itemInfo, Game gameName) {
        try {
            JSONObject response = null;
            if (gameName == Game.DOTA1)
                response = new JSONObject(sendGetRequest("https://market.dota2.net/api/BestBuyOffer/" + itemInfo + "?key=" + API_KEY_DOTA1));
            else if (gameName == Game.DOTA2)
                response = new JSONObject(sendGetRequest("https://market.dota2.net/api/BestBuyOffer/" + itemInfo + "?key=" + API_KEY_DOTA2));
            else if (gameName == Game.DOTA3)
                response = new JSONObject(sendGetRequest("https://market.dota2.net/api/BestBuyOffer/" + itemInfo + "?key=" + API_KEY_DOTA3));
            return response != null ? response.optDouble("best_offer") : 0;
        } catch (Exception ignored) { }
        return 0;
    }

    private double getBestSellOfferPrice(String itemInfo, Game gameName) {
        try {
            JSONObject response = null;
            if (gameName == Game.DOTA1)
                response = new JSONObject(sendGetRequest("https://market.dota2.net/api/BestSellOffer/" + itemInfo + "?key=" + API_KEY_DOTA1));
            else if (gameName == Game.DOTA2)
                response = new JSONObject(sendGetRequest("https://market.dota2.net/api/BestSellOffer/" + itemInfo + "?key=" + API_KEY_DOTA2));
            else if (gameName == Game.DOTA3)
                response = new JSONObject(sendGetRequest("https://market.dota2.net/api/BestSellOffer/" + itemInfo + "?key=" + API_KEY_DOTA3));
            return response != null ? response.optDouble("best_offer") : 0;
        } catch (Exception e) {
        }
        return 0;
    }

    private String sendGetRequest(String urlString) {
        try {
            if (urlString.contains(API_KEY_DOTA1))
                Thread.sleep(230);
            else if (urlString.contains(API_KEY_DOTA2))
                Thread.sleep(30);
            else if (urlString.contains(API_KEY_DOTA3))
                Thread.sleep(230);
            URL url = new URL(urlString);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream == null) {
                System.out.println("inputStream is null");
                return null;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                stringBuilder.append((char) c);
            }
            inputStream.close();
            reader.close();
            urlConnection.disconnect();
            return stringBuilder.toString();
        } catch (Exception ignored) {
        }
        return null;
    }

    private String sendPostRequest(String urlString, List<String> postParams) {
        try {
            if (urlString.contains(API_KEY_DOTA1))
                Thread.sleep(10);
            else if (urlString.contains(API_KEY_DOTA2))
                Thread.sleep(30);
            else if (urlString.contains(API_KEY_DOTA3))
                Thread.sleep(170);
            URL url = new URL(urlString);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("User-Agent", USER_AGENT);
            // Send post request
            urlConnection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
            if (postParams != null && postParams.size() != 0) {
                if (!postParams.get(0).contains("list"))
                    wr.writeBytes("list=");
                for (String param : postParams)
                    wr.writeBytes(param);
                wr.flush();
                wr.close();
                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream == null) {
                    System.out.println("inputStream is null");
                    return null;
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                int c;
                while ((c = reader.read()) != -1) {
                    stringBuilder.append((char) c);
                }
                inputStream.close();
                reader.close();
                urlConnection.disconnect();
                return stringBuilder.toString();

            }
        } catch (Exception e) {
            /*System.out.println(e.getMessage());
            if (urlString.contains("pubg"))
                System.out.println("Exception in sendPostRequest, game PUBG for url: " + urlString + " " + new Date());
            else if (urlString.contains("csgo"))
                System.out.println("Exception in sendPostRequest, game CSGO for url: " + urlString + " " + new Date());
            else if (urlString.contains("dota"))
                System.out.println("Exception in sendPostRequest, game DOTA for url: " + urlString + " " + new Date());*/
        }
        return null;
    }

    private void goOnline(Game gameName) {
        try {
            if (gameName == Game.DOTA1) {
                String response = sendGetRequest("https://market.dota2.net/api/PingPong/?key=" + API_KEY_DOTA1);
                if (response != null && response.toLowerCase().contains("bad key")) {
                    System.out.println("bad key PUBG");
                    for (int i = 0; i < 3; i++) {
                        Toolkit.getDefaultToolkit().beep();
                        Thread.sleep(4000);
                    }
                }
            } else if (gameName == Game.DOTA2) {
                String response = sendGetRequest("https://market.dota2.net/api/PingPong/?key=" + API_KEY_DOTA2);
                if (response != null && response.toLowerCase().contains("bad key")) {
                    System.out.println("bad key PUBG2");
                    for (int i = 0; i < 3; i++) {
                        Toolkit.getDefaultToolkit().beep();
                        Thread.sleep(4000);
                    }
                }
            } else if (gameName == Game.DOTA3) {
                String response = sendGetRequest("https://market.dota2.net/api/PingPong/?key=" + API_KEY_DOTA3);
                if (response != null && response.toLowerCase().contains("bad key")) {
                    System.out.println("bad key DOTA");
                    for (int i = 0; i < 3; i++) {
                        Toolkit.getDefaultToolkit().beep();
                        Thread.sleep(4000);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in goOnline: " + e.getMessage());
        }
    }

    private JSONArray getItemSellOrders(String itemInfo, Game gameName) {
        String response = null;
        try {
            if (gameName == Game.DOTA1) {
                response = sendGetRequest("https://market.dota2.net/api/SellOffers/" +
                        itemInfo.replaceFirst("/", "_") + "?key=" + API_KEY_DOTA1);
            } else if (gameName == Game.DOTA2) {
                response = sendGetRequest("https://market.dota2.net/api/SellOffers/" +
                        itemInfo.replaceFirst("/", "_") + "?key=" + API_KEY_DOTA2);
            } else if (gameName == Game.DOTA3) {
                response = sendGetRequest("https://market.dota2.net/api/SellOffers/" +
                        itemInfo.replaceFirst("/", "_") + "?key=" + API_KEY_DOTA3);
            }
            JSONObject jsonObject = null;
            if (response != null) {
                jsonObject = new JSONObject(response);
            }
            if (jsonObject != null) {
                return jsonObject.optJSONArray("offers");
            }
        } catch (Exception e) {
            System.out.println("Exception in getItemSellOrders, game " + gameName + ": " + e.getMessage());
        }
        return null;
    }

    private void updateDatabase(Game gameName) {
        System.out.println("Beginning of database update, game " + gameName);
        Reader reader = null;
        File db = null;
        try {
            String dbName = getDatabaseName(gameName);
            db = new File("F:\\Bot\\csv_files\\" + dbName);
            reader = new FileReader(db);
            Iterable<CSVRecord> records = CSVFormat.newFormat(';').parse(reader);
            if (gameName == Game.DOTA1) {
                autotradableItemsDOTA = new ArrayList<>();
            }
            records.iterator().next();
            for (CSVRecord record : records) {
                String classId = record.get(0);
                String instanceId = record.get(1);
                // if there is any letter
                if (!classId.matches(".*\\d+.*") || !instanceId.matches(".*\\d+.*"))
                    continue;
                long price = Long.parseLong(record.get(2));
                int popularity = Integer.parseInt(record.get(4));
                if (price != 0) {
                    String marketName = record.get(8).toLowerCase();
//                    if (marketName.contains("crate")) {
//
//                    }
                    if (price >= 10000 && popularity >= MINIMUM_POPULARITY || price >= 1000 && popularity >= 300 || price >= MINIMUM_PRICE && popularity >= 450)
                        autotradableItemsDOTA.add(classId + "/" + instanceId + "/");
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in updateDatabase, game " + gameName + ": " + e.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (db != null && db.delete())
                    System.out.println(gameName + " database was updated at " + Calendar.getInstance().getTime().toString().substring(11, 19));
            } catch (IOException e) {
                System.out.println("Exception in reader close (updateDatabase): " + e.getMessage());
            }
        }
    }

    private boolean updateInventory(Game gameName) {
        try {
            JSONObject response = null;
            if (gameName == Game.DOTA1)
                response = new JSONObject(sendGetRequest("https://market.dota2.net/api/UpdateInventory/?key=" + API_KEY_DOTA1));
            if (gameName == Game.DOTA2)
                response = new JSONObject(sendGetRequest("https://market.dota2.net/api/UpdateInventory/?key=" + API_KEY_DOTA2));
            if (gameName == Game.DOTA3)
                response = new JSONObject(sendGetRequest("https://market.dota2.net/api/UpdateInventory/?key=" + API_KEY_DOTA3));
            return response != null && response.optBoolean("success");
        } catch (Exception ignored) {
        }
        return false;
    }

    private void deleteSellOrders() {
        try {
            System.out.println("deleteSellOrdersPUBG: " + sendGetRequest("https://market.dota2.net/api/RemoveAll/?key=" + API_KEY_DOTA1));
            System.out.println("deleteSellOrdersCSGO: " + sendGetRequest("https://market.dota2.net/api/RemoveAll/?key=" + API_KEY_DOTA2));
            System.out.println("deleteSellOrdersDOTA: " + sendGetRequest("https://market.dota2.net/api/RemoveAll/?key=" + API_KEY_DOTA3));
        } catch (Exception e) {
            System.out.println("Exception in deleteSellOrders: " + e.getMessage());
        }
    }

    private void deleteBuyOrders() {
        try {
            System.out.println(sendGetRequest("https://market.dota2.net/api/DeleteOrders/?key=" + API_KEY_DOTA1));
            System.out.println(sendGetRequest("https://market.dota2.net/api/DeleteOrders/?key=" + API_KEY_DOTA2));
            System.out.println(sendGetRequest("https://market.dota2.net/api/DeleteOrders/?key=" + API_KEY_DOTA3));
        } catch (Exception e) {
            System.out.println("Exception in deleteButOrders: " + e.getMessage());
        }
    }

    private void deleteAllOrders() {
        deleteSellOrders();
        deleteBuyOrders();
    }

    private String getDatabaseName(Game gameName) {
        try {
            String response = null;
            if (gameName == Game.DOTA1)
                response = sendGetRequest("https://market.dota2.net/itemdb/current_570.json");
            if (response != null) {
                JSONObject dbInfo = new JSONObject(response);
                String dbName = dbInfo.optString("db");
                if (dbName != null) {
                    URL downloadLink = null;
                    if (gameName == Game.DOTA1)
                        downloadLink = new URL("https://market.dota2.net/itemdb/" + dbName);
                    FileUtils.copyURLToFile(downloadLink, new File("F:\\Bot\\csv_files\\" + dbName));
                    return dbName;
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in getDatabaseName, game " + gameName + ": " + e.getMessage());
        }
        return null;
    }

    private JSONArray getInventory(Game gameName) {
        String response = null;
        try {
            if (gameName == Game.DOTA1)
                response = sendGetRequest("https://market.dota2.net/api/GetInv/?key=" + API_KEY_DOTA1);
            else if (gameName == Game.DOTA2)
                response = sendGetRequest("https://market.dota2.net/api/GetInv/?key=" + API_KEY_DOTA2);
            else if (gameName == Game.DOTA3)
                response = sendGetRequest("https://market.dota2.net/api/GetInv/?key=" + API_KEY_DOTA3);
            if (response != null && response.charAt(0) == '{') {
                JSONObject inventory = new JSONObject(response);
                if (inventory.optString("ok").equals("true"))
                    return new JSONArray(inventory.optString("data"));
            }
        } catch (Exception e) {
            System.out.println("Exception in getInventory, game " + gameName + ": " + e.getMessage());
        }
        return null;
    }

    private JSONArray getTrades(Game gameName) {
        try {
            String response = null;
            if (gameName == Game.DOTA1)
                response = sendGetRequest("https://market.dota2.net/api/Trades/?key=" + API_KEY_DOTA1);
            else if (gameName == Game.DOTA2)
                response = sendGetRequest("https://market.dota2.net/api/Trades/?key=" + API_KEY_DOTA2);
            else if (gameName == Game.DOTA3)
                response = sendGetRequest("https://market.dota2.net/api/Trades/?key=" + API_KEY_DOTA3);
            if (response != null) {
                if (response.contains("[") && response.contains("]") && response.length() > 3)
                    return new JSONArray(response);
            }
        } catch (Exception e) {
            System.out.println("Exception in getTrades, game " + gameName + ": " + e.getMessage());
        }
        return null;
    }

    private double getTradesMoney() {
        try {
            JSONArray tradesPUBG = new JSONArray(sendGetRequest("https://market.dota2.net/api/Trades/?key=" + API_KEY_DOTA1));
            double sumOfTradesPUBG = 0;
            for (int i = 0; i < tradesPUBG.length(); i++) {
                JSONObject item = tradesPUBG.optJSONObject(i);
                if (item.optString("ui_status").equals("2")) {
                    double price = Double.parseDouble(item.optString("ui_price")) * 1.07;
                    sumOfTradesPUBG += price;
                } else {
                    double price = Double.parseDouble(item.optString("ui_price"));
                    sumOfTradesPUBG += price;
                }
            }
            System.out.println("-----------------TRADES-----------------");
            System.out.printf("PUBG: %.2f ", sumOfTradesPUBG * 0.93);
            JSONArray tradesCSGO = new JSONArray(sendGetRequest("https://market.dota2.net/api/Trades/?key=" + API_KEY_DOTA2));
            double sumOfTradesCSGO = 0;
            for (int i = 0; i < tradesCSGO.length(); i++) {
                JSONObject item = tradesCSGO.optJSONObject(i);
                if (item.optString("ui_status").equals("2")) {
                    double price = Double.parseDouble(item.optString("ui_price")) * 1.07;
                    sumOfTradesCSGO += price;
                } else {
                    double price = Double.parseDouble(item.optString("ui_price"));
                    sumOfTradesCSGO += price;
                }
            }
            System.out.printf("CSGO: %.2f ", sumOfTradesCSGO * 0.93);
            JSONArray tradesDOTA = new JSONArray(sendGetRequest("https://market.dota2.net/api/Trades/?key=" + API_KEY_DOTA3));
            double sumOfTradesDOTA = 0;
            for (int i = 0; i < tradesDOTA.length(); i++) {
                JSONObject item = tradesDOTA.optJSONObject(i);
                if (item.optString("ui_status").equals("2")) {
                    double price = Double.parseDouble(item.optString("ui_price")) * 1.07;
                    sumOfTradesDOTA += price;
                } else {
                    double price = Double.parseDouble(item.optString("ui_price"));
                    sumOfTradesDOTA += price;
                }

            }
            System.out.printf("DOTA: %.2f\n", sumOfTradesDOTA * 0.93);
            return (sumOfTradesPUBG + sumOfTradesCSGO + sumOfTradesDOTA) * 0.93;
        } catch (Exception e) {
            System.out.println("Exception in getTradesPrices: " + e.getMessage());
        }
        return 0;
    }

    private double getMoney() {
        try {
            JSONObject moneyPUBG = new JSONObject(sendGetRequest("https://market.dota2.net/api/GetMoney/?key=" + API_KEY_DOTA1));
            JSONObject moneyPUBG2 = new JSONObject(sendGetRequest("https://market.dota2.net/api/GetMoney/?key=" + API_KEY_DOTA2));
            JSONObject moneyDOTA = new JSONObject(sendGetRequest("https://market.dota2.net/api/GetMoney/?key=" + API_KEY_DOTA3));
            double dMoneyPUBG = Double.parseDouble(moneyPUBG.optString("money"));
            double dMoneyPUBG2 = Double.parseDouble(moneyPUBG2.optString("money"));
            double dMoneyDOTA = Double.parseDouble(moneyDOTA.optString("money"));
            ;
            System.out.println("-----------------MONEY------------------");
            System.out.println("PUBG: " + dMoneyPUBG / 100 + " CSGO: " + dMoneyPUBG2 / 100 + " DOTA: " + dMoneyDOTA / 100);
            return (dMoneyPUBG + dMoneyPUBG2 + dMoneyDOTA) / 100;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception in getMoney: " + e.getMessage());
        }
        return 0;
    }

    private String getWSAuth() {
        try {
            JSONObject wsKeyObject = new JSONObject(sendGetRequest("https://market.dota2.net/api/GetWSAuth" + "/?key=" + API_KEY_DOTA2));
            boolean isSuccess = wsKeyObject.optBoolean("success");
            String wsKey = wsKeyObject.optString("wsAuth");
            if (isSuccess)
                return wsKey;
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showLastTrades() {
        try {
            Map<String, List<Double>> itemsPUBG = new HashMap<>();
            Map<String, List<Double>> itemsCSGO = new HashMap<>();
            Map<String, List<Double>> itemsDOTA = new HashMap<>();
            long beginTime = System.currentTimeMillis() / 1000 - 21600;
            long endTime = System.currentTimeMillis() / 1000;
            JSONObject responsePUBG = new JSONObject
                    (sendGetRequest("https://market.dota2.net/api/OperationHistory/" + beginTime + "/" + endTime + "/?key=" + API_KEY_DOTA1));
            JSONObject responseCSGO = new JSONObject
                    (sendGetRequest("https://market.dota2.net/api/OperationHistory/" + beginTime + "/" + endTime + "/?key=" + API_KEY_DOTA2));
            JSONObject responseDOTA = new JSONObject
                    (sendGetRequest("https://market.dota2.net/api/OperationHistory/" + beginTime + "/" + endTime + "/?key=" + API_KEY_DOTA3));
            JSONArray historyPUBG = responsePUBG.optJSONArray("history");
            for (int i = 0; i < historyPUBG.length(); i++) {
                JSONObject operation = historyPUBG.optJSONObject(i);
                if (!operation.optString("stage").equals("5")) {
                    if (operation.optString("h_event").equals("buy_pb")) {
                        String marketName = operation.optString("market_name");
                        if (itemsPUBG.get(marketName) == null) {
                            itemsPUBG.put(marketName, new ArrayList<>());
                            itemsPUBG.get(marketName).add(Double.valueOf(operation.optString("recieved")) / 100);
                            itemsPUBG.get(marketName).add(0D);
                        } else {
                            double buySummary = itemsPUBG.get(marketName).get(0);
                            itemsPUBG.get(marketName).set(0, Double.valueOf(operation.optString("recieved")) / 100 + buySummary);
                        }
                    } else if (operation.optString("h_event").equals("sell_pb")) {
                        String marketName = operation.optString("market_name");
                        if (itemsPUBG.get(marketName) == null) {
                            itemsPUBG.put(marketName, new ArrayList<>());
                            itemsPUBG.get(marketName).add(0D);
                            itemsPUBG.get(marketName).add(Double.valueOf(operation.optString("recieved")) / 100);
                        } else {
                            double sellSummary = itemsPUBG.get(marketName).get(1);
                            itemsPUBG.get(marketName).set(1, Double.valueOf(operation.optString("recieved")) / 100 + sellSummary);
                        }
                    }
                }
            }
            JSONArray historyCSGO = responseCSGO.optJSONArray("history");
            for (int i = 0; i < historyCSGO.length(); i++) {
                JSONObject operation = historyCSGO.optJSONObject(i);
                if (!operation.optString("stage").equals("5")) {
                    if (operation.optString("h_event").equals("buy_go")) {
                        String marketName = operation.optString("market_name");
                        if (itemsCSGO.get(marketName) == null) {
                            itemsCSGO.put(marketName, new ArrayList<>());
                            itemsCSGO.get(marketName).add(Double.valueOf(operation.optString("recieved")) / 100);
                            itemsCSGO.get(marketName).add(0D);
                        } else {
                            double buySummary = itemsCSGO.get(marketName).get(0);
                            itemsCSGO.get(marketName).set(0, Double.valueOf(operation.optString("recieved")) / 100 + buySummary);
                        }
                    } else if (operation.optString("h_event").equals("sell_go")) {
                        String marketName = operation.optString("market_name");
                        if (itemsCSGO.get(marketName) == null) {
                            itemsCSGO.put(marketName, new ArrayList<>());
                            itemsCSGO.get(marketName).add(0D);
                            itemsCSGO.get(marketName).add(Double.valueOf(operation.optString("recieved")) / 100);
                        } else {
                            double sellSummary = itemsCSGO.get(marketName).get(1);
                            itemsCSGO.get(marketName).set(1, Double.valueOf(operation.optString("recieved")) / 100 + sellSummary);
                        }
                    }
                }
            }
            JSONArray historyDOTA = responseDOTA.optJSONArray("history");
            for (int i = 0; i < historyDOTA.length(); i++) {
                JSONObject operation = historyDOTA.optJSONObject(i);
                if (!operation.optString("stage").equals("5")) {
                    if (operation.optString("h_event").equals("buy_dota")) {
                        String marketName = operation.optString("market_name");
                        if (itemsDOTA.get(marketName) == null) {
                            itemsDOTA.put(marketName, new ArrayList<>());
                            itemsDOTA.get(marketName).add(Double.valueOf(operation.optString("recieved")) / 100);
                            itemsDOTA.get(marketName).add(0D);
                        } else {
                            double buySummary = itemsDOTA.get(marketName).get(0);
                            itemsDOTA.get(marketName).set(0, Double.valueOf(operation.optString("recieved")) / 100 + buySummary);
                        }
                    } else if (operation.optString("h_event").equals("sell_dota")) {
                        String marketName = operation.optString("market_name");
                        if (itemsDOTA.get(marketName) == null) {
                            itemsDOTA.put(marketName, new ArrayList<>());
                            itemsDOTA.get(marketName).add(0D);
                            itemsDOTA.get(marketName).add(Double.valueOf(operation.optString("recieved")) / 100);
                        } else {
                            double sellSummary = itemsDOTA.get(marketName).get(1);
                            itemsDOTA.get(marketName).set(1, Double.valueOf(operation.optString("recieved")) / 100 + sellSummary);
                        }
                    }
                }
            }
            long profitPUBG = 0;
            long profitCSGO = 0;
            long profitDOTA = 0;
            for (Map.Entry<String, List<Double>> entry : itemsPUBG.entrySet()) {
                double bought = entry.getValue().get(0);
                double sold = entry.getValue().get(1);
                //System.out.println(entry.getKey() + ":    " + (sold - bought));
                profitPUBG += sold - bought;
            }
            for (Map.Entry<String, List<Double>> entry : itemsCSGO.entrySet()) {
                double bought = entry.getValue().get(0);
                double sold = entry.getValue().get(1);
                //System.out.println(entry.getKey() + ":    " + (sold - bought));
                profitCSGO += sold - bought;
            }
            for (Map.Entry<String, List<Double>> entry : itemsDOTA.entrySet()) {
                double bought = entry.getValue().get(0);
                double sold = entry.getValue().get(1);
                //System.out.println(entry.getKey() + ":    " + (sold - bought));
                profitDOTA += sold - bought;
            }
            System.out.println("profit PUBG: " + profitPUBG + " profit CSGO: " + profitCSGO + " profit DOTA: " + profitDOTA);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getTime(String msg) {
        System.out.println(msg + " " + Calendar.getInstance().get(Calendar.MINUTE) + ":" +
                Calendar.getInstance().get(Calendar.SECOND) + ":" +Calendar.getInstance().get(Calendar.MILLISECOND));
    }

    private class OrdersUpdaterPUBG extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    processOrdersPUBG();
                } catch (Exception e) {
                    System.out.println("Exception in OrdersUpdaterPUBG: " + e.getMessage());
                }
            }
        }

    }

    private class OrdersUpdaterPUBG2 extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    processOrdersPUBG2();
                } catch (Exception e) {
                    System.out.println("Exception in OrdersUpdaterPUBG2: " + e.getMessage());
                }
            }
        }
    }

    private class OrdersUpdaterDOTA extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    processOrdersDOTA();
                } catch (Exception e) {
                    System.out.println("Exception in OrdersUpdaterDOTA: " + e.getMessage());
                }
            }
        }
    }

    private class PricesUpdaterPUBG extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    updatePricesPUBG();
                } catch (Exception e) {
                    System.out.println("Exception in PricesUpdaterPUBG: " + e.getMessage());
                }
            }
        }
    }

    private class PricesUpdaterPUBG2 extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    updatePricesPUBG2();
                } catch (Exception e) {
                    System.out.println("Exception in PricesUpdaterPUBG2: " + e.getMessage());
                }
            }
        }
    }

    private class PricesUpdaterDOTA extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    updatePricesDOTA();
                } catch (Exception e) {
                    System.out.println("Exception in PricesUpdaterDOTA: " + e.getMessage());
                }
            }
        }
    }

    private class TraderPUBG extends Thread {

        @Override
        public void run() {
            try {
                while (true) {
                    processTradesPUBG();
                    Thread.sleep(10000);
                }
            } catch (Exception e) {
                System.out.println("Exception in Trader: " + e.getMessage());
            }
        }
    }

    private class TraderPUBG2 extends Thread {

        @Override
        public void run() {
            try {
                while (true) {
                    processTradesPUBG2();
                    Thread.sleep(10000);
                }
            } catch (Exception e) {
                System.out.println("Exception in Trader: " + e.getMessage());
            }
        }
    }

    private class TraderDOTA extends Thread {

        @Override
        public void run() {
            try {
                while (true) {
                    processTradesDOTA();
                    Thread.sleep(10000);
                }
            } catch (Exception e) {
                System.out.println("Exception in Trader: " + e.getMessage());
            }
        }
    }

    private class InventoryUpdaterPUBG extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    if (updateInventory(Game.DOTA1))
                        setPricesPUBG();
                    Thread.sleep(20000);
                } catch (Exception e) {
                    System.out.println("Exception in InventoryUpdater: " + e.getMessage());
                }
            }
        }
    }

    private class InventoryUpdaterPUBG2 extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    if (updateInventory(Game.DOTA2))
                        setPricesPUBG2();
                    Thread.sleep(20000);
                } catch (Exception e) {
                    System.out.println("Exception in InventoryUpdater: " + e.getMessage());
                }
            }
        }
    }

    private class InventoryUpdaterDOTA extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    if (updateInventory(Game.DOTA3))
                        setPricesDOTA();
                    Thread.sleep(20000);
                } catch (Exception e) {
                    System.out.println("Exception in InventoryUpdater: " + e.getMessage());
                }
            }
        }
    }

    private class OnlineUpdaterPUBG extends Thread {

        @Override
        public void run() {
            try {
                while (true) {
                    goOnline(Game.DOTA1);
                    Thread.sleep(50000);
                }
            } catch (Exception e) {
                System.out.println("Exception in OnlineUpdater: " + e.getMessage());
            }
        }
    }

    private class OnlineUpdaterPUBG2 extends Thread {

        @Override
        public void run() {
            try {
                while (true) {
                    goOnline(Game.DOTA2);
                    Thread.sleep(50000);
                }
            } catch (Exception e) {
                System.out.println("Exception in OnlineUpdater: " + e.getMessage());
            }
        }
    }

    private class OnlineUpdaterDOTA extends Thread {

        @Override
        public void run() {
            try {
                while (true) {
                    goOnline(Game.DOTA3);
                    Thread.sleep(50000);
                }
            } catch (Exception e) {
                System.out.println("Exception in OnlineUpdater: " + e.getMessage());
            }
        }
    }

    private class Logger extends Thread {

        @Override
        public void run() {
            try {
                while (true) {
                    double tradesMoney = getTradesMoney();
                    double myMoney = getMoney();
                    Writer writer = new FileWriter("log_bot.txt", true);
                    writer.write("----------------SUMMARY-----------------\n");
                    writer.flush();
                    System.out.println("----------------SUMMARY-----------------");
                    String date = Calendar.getInstance().getTime().toString().substring(11, 19);
                    System.out.printf("          %.2f at %s\n", tradesMoney + myMoney, date);
                    String summary = String.format("          %.2f at %s\n\n", tradesMoney + myMoney, date);
                    Thread.sleep(60000 * 60);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // unusable classes, methods and variables section
    /*

    private class DatabaseUpdater extends Task<Void> {
            @Override
            protected Void call() {
                try {
                    while (true) {
                        Thread.sleep(3600000);
                        double tradesMoney = getTradesMoney();
                        double myMoney = getMoney() / 100;
                        System.out.println("Trades money: " + tradesMoney + ", my money: " + myMoney + ", sum: " + (tradesMoney + myMoney));
                        deleteBuyOrders();
                        for (int i = 0; i < 3; i++) {
                            updateDatabase(Game.values()[i]);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Exception in DatabaseUpdater");
                    e.printStackTrace();
                }
                return null;
            }
    }

    try {
            WebsocketClientEndpoint clientEndPoint = new WebsocketClientEndpoint(new URI("wss://wsn.dota2.net/wsn/"));
            clientEndPoint.addMessageHandler(System.out::println);
            //clientEndPoint.sendMessage(getWSAuth());
            clientEndPoint.sendMessage("newitems_pb");
            while (true) {
                Thread.sleep(45000);
                clientEndPoint.sendMessage("ping");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    private int getNumberOfInventoryItem(String itemClassId, String itemInstanceId, Game gameName) {
        try {
            JSONArray inventory = getInventory(gameName);
            if (inventory != null) {
                int counter = 0;
                for (int i = 0; i < inventory.length(); i++) {
                    JSONObject item = inventory.optJSONObject(i);
                    if (item != null) {
                        String classId = item.optString("i_classid");
                        if (classId.equals(itemClassId)) {
                            String instanceId = item.optString("i_instanceid");
                            if (instanceId.equals(itemInstanceId))
                                counter++;
                        }
                    }
                }
                return counter;
            }
        } catch (Exception e) {
            System.out.println("Exception in getNumberOfInventoryItem, game: " + gameName);
            e.printStackTrace();
        }
        return 0;
    }

    private void testOnline(Game gameName) {
        HttpsURLConnection urlConnection = null;
        try {
            if (gameName == Game.DOTA1)
                urlConnection = sendGetRequest("https://market.dota2.net/api/Test/?key=" + API_KEY);
            else if (gameName == Game.DOTA2)
                urlConnection = sendGetRequest("https://market.dota2.net/api/Test/?key=" + API_KEY);
            else if (gameName == Game.DOTA3)
                urlConnection = sendGetRequest("https://market.dota2.net/api/Test/?key=" + API_KEY);
            if (urlConnection != null) {
                JSONObject response = new JSONObject(readServerResponse(urlConnection));
                if (response.optString("success").equals("true")) {
                    JSONObject status = response.optJSONObject("status");
                    if (status.optString("site_online").equals("false"))
                        goOnline(gameName);
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in testOnline, game: " + gameName);
            e.printStackTrace();
        }
    }
}
*/
}
