package com.penguin.undertow;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
// hbase api import
// dynamodb api import

/**
 * @author mitayun
 */
public class MiniSite {
  /**
   * Database type
   */
  static final int MYSQL = 0;
  static final int HBASE = 1;
  static final int DB_TYPE = MYSQL;

  private static final String PATH_HC = "/healthcheck";
  private static final String PATH_Q1 = "/q1";
  private static final String PATH_Q2 = "/q2";
  private static final String PATH_Q3 = "/q3";
  private static final String PATH_Q4 = "/q4";
  private static final String PATH_Q5 = "/q5";
  private static final String PATH_Q6 = "/q6";

  static final int SERVER_PORT = 80;
  static final String SERVER_IP = "0.0.0.0";

  @SuppressWarnings("all")
  public static void main(String[] args) throws Exception {

    Undertow.Builder builder = Undertow.builder().addHttpListener(SERVER_PORT,
        SERVER_IP);

    HttpHandler healthCheckHandler = new HealthCheckHandler();
    HttpHandler q1Handler = new Q1HeartBeatHandler();
    HttpHandler q2Handler = null;
    HttpHandler q3Handler = null;
    HttpHandler q4Handler = null;
    HttpHandler q5Handler = new Q5MySQLHandler();
    HttpHandler q6Handler = new Q6MySQLHandler();

    switch (DB_TYPE) {
    case MYSQL:
      System.out.println("choosing mysql");
      q2Handler = new Q2MySQLHandler();
      q3Handler = new Q3MySQLHandler();
      q4Handler = new Q4MySQLHandler();
      break;
    case HBASE:
      System.out.println("choosing hbase");
      q2Handler = new Q2HBaseHandler();
      q3Handler = new Q3HBaseHandler();
      q4Handler = new Q4HBaseHandler();
      break;
    }

    builder
        .setHandler(
            Handlers.path().addPrefixPath(PATH_HC, healthCheckHandler)
                .addPrefixPath(PATH_Q1, q1Handler)
                .addPrefixPath(PATH_Q2, q2Handler)
                .addPrefixPath(PATH_Q3, q3Handler)
                .addPrefixPath(PATH_Q4, q4Handler)
                .addPrefixPath(PATH_Q5, q5Handler)
                .addPrefixPath(PATH_Q6, q6Handler)).setWorkerThreads(128)
        .setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, false);

    Undertow server = builder.build();
    server.start();
    System.out.println("Server started");
  }
}
