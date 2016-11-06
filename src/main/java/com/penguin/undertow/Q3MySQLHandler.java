package com.penguin.undertow;

import io.undertow.server.HttpServerExchange;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Deque;

/**
 * Question 3 MySQL Handler.
 *
 * Table schema: create table twitter33(sourceid bigint NOT NULL, content text)
 * engine=myisam;
 *
 * ----------------------
 * | sourceid | content |
 * ----------------------
 *
 * Table index:
 * create index sourceid_index on twitter33(sourceid);
 */

public class Q3MySQLHandler extends BaseHttpHandler {

  /**
   * Keys for http params
   */
  private static final String KEY_UID = "userid";

  @Override
  public String getResponse(HttpServerExchange exchange) {
    // Get parameters
    Deque<String> uid = exchange.getQueryParameters().get(KEY_UID);

    if (uid == null || uid.isEmpty()) {
      return getDefaultResponse();
    }

    String userId = uid.peekFirst();

    if (userId == null) {
      return getDefaultResponse();
    }

    StringBuilder response = new StringBuilder();
    String sql = "SELECT content FROM twitter33 WHERE sourceid=" + userId + ";";
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
      conn = ConnectionUtils.getInstance().getMySQLConnection();
      pstmt = conn.prepareStatement(sql);
      rs = pstmt.executeQuery();
      while (rs.next()) {
        response = response.append(rs.getString("content"));
      }
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
