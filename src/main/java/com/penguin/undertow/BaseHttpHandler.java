package com.penguin.undertow;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author mitayun
 */
public abstract class BaseHttpHandler implements HttpHandler {

  /**
   * Team ID
   */
  private static final String ID_TEAM = "Penguins";

  /**
   * AWS IDs
   */
  private static final String ID1 = "4281-8781-3308"; // Mita
  private static final String ID2 = "8247-8059-6172"; // Zheng
  private static final String ID3 = "3818-9577-0956"; // Kaifu

  /**
   * MySQL
   */
  private static final String ADDRESS_MYSQL = "jdbc:mysql://localhost:3306/CC_Final";
  private static final String USERNAME = "root";
  private static final String PW = "";

  private static String sName = null;
  private static String sDefaultResponse = null;

  protected long startTime = 0;

  /**
   * Main function for handling HTTP request
   */
  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {

    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE,
        "text/plain; charset=utf-8");

    exchange.getResponseSender().send(getResponse(exchange));
  }

  /**
   * Get response for HTTP request
   *
   * @param exchange
   * @return
   */
  public abstract String getResponse(HttpServerExchange exchange);

  /**
   * @return the default response for invalid requests
   */
  protected static String getDefaultResponse() {
    if (sDefaultResponse == null) {
      sDefaultResponse = getName() + "\n";
    }
    return sDefaultResponse;
  }

  /**
   * @return team information
   */
  protected static String getName() {
    if (sName == null) {
      sName = ID_TEAM + "," + ID1 + "," + ID2 + "," + ID3;
    }
    return sName;
  }

  protected Connection getMySQLConnection() throws SQLException {
    return DriverManager.getConnection(ADDRESS_MYSQL, USERNAME, PW);
  }

  protected void setStartTime() {
    startTime = System.currentTimeMillis();
  }

  protected void logTime(String key) {
    long endTime = System.currentTimeMillis();
    System.out.println(key + ": " + (endTime - startTime) + "ms");
  }

  protected void print(String print) {
    System.out.println(print);
  }
}
