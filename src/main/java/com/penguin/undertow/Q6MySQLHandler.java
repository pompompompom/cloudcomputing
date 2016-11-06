package com.penguin.undertow;

import io.undertow.server.HttpServerExchange;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Deque;

public class Q6MySQLHandler extends BaseHttpHandler {

  /**
   * Keys for http params
   */
  private static final String KEY_START = "m";
  private static final String KEY_END = "n";

  @Override
  public String getResponse(HttpServerExchange exchange) {
    // Get parameters
    Deque<String> start = exchange.getQueryParameters().get(KEY_START);
    Deque<String> end = exchange.getQueryParameters().get(KEY_END);

    if (start == null || start.isEmpty() || end == null || end.isEmpty()) {
      return getDefaultResponse();
    }

    String startId = start.peekFirst().replaceAll("=", "");
    String endId = end.peekFirst().replaceAll("=", "");

    if (startId == null || endId == null) {
      return getDefaultResponse();
    }

    StringBuilder response = new StringBuilder();
    String sql1 = "SELECT row FROM twitter6 WHERE userid >=" + startId
        + " limit 1;";
    String sql2 = "SELECT row FROM twitter6 WHERE userid <=" + endId
        + " order by userid desc limit 1;";
    // System.out.println(sql1);
    // System.out.println(sql2);

    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
      conn = ConnectionUtils.getInstance().getMySQLConnection();
      long startRow = 0;
      long endRow = 0;
      pstmt = conn.prepareStatement(sql1);
      rs = pstmt.executeQuery();
      while (rs.next()) {
        startRow = Long.parseLong(rs.getString("row"));
      }

      pstmt = conn.prepareStatement(sql2);
      rs = pstmt.executeQuery();
      while (rs.next()) {
        endRow = Long.parseLong(rs.getString("row"));
      }

      // System.out.println("start: " + startRow);
      // System.out.println("end: " + endRow);

      response = response.append(endRow - startRow + 1 + "\n");
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (pstmt != null) {
          pstmt.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (Exception e) {
        System.out.println("exception when closing");
      }
    }

    String result = response.toString();
    return getDefaultResponse() + result;
  }

}
