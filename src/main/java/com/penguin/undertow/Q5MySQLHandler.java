package com.penguin.undertow;

import io.undertow.server.HttpServerExchange;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Deque;

public class Q5MySQLHandler extends BaseHttpHandler {

  /**
   * Keys for http params
   */
  private static final String KEY_UID = "userlist";
  private static final String KEY_START = "start";
  private static final String KEY_END = "end";

  private static final int TRY_ONE = 0;
  private static final int TRY_TWO = 1;
  private static final int TRY_THREE = 2;

  private static final int TRY = TRY_THREE;

  @Override
  public String getResponse(HttpServerExchange exchange) {
    // Get parameters
    Deque<String> ulist = exchange.getQueryParameters().get(KEY_UID);
    Deque<String> start = exchange.getQueryParameters().get(KEY_START);
    Deque<String> end = exchange.getQueryParameters().get(KEY_END);

    if (ulist == null || ulist.isEmpty() || start == null || start.isEmpty()
        || end == null || end.isEmpty()) {
      return getDefaultResponse();
    }

    String userList = ulist.peekFirst();
    String startDate = start.peekFirst().replaceAll("-", "");
    String endDate = end.peekFirst().replaceAll("-", "");

    if (userList == null || startDate == null || endDate == null) {
      return getDefaultResponse();
    }

    StringBuilder response = new StringBuilder();
    String sql = "";

    switch (TRY) {
      case TRY_ONE:
        sql = "select userid, (sum(tweet) + max(friend)*3 + max(follower)*5) as score from twitter5 where (date between "
            + startDate
            + " and "
            + endDate
            + ") and (userid in ("
            + userList
            + ")) group by userid order by score desc";
        break;
      case TRY_TWO:
        sql = "select sq.userid, (sum(tweet) + max(friend) + max(follower)) as score from (select userid, tweet, friend, follower from twitter5 where (date >= "
            + startDate
            + " and date <= "
            + endDate
            + ") and (userid in ("
            + userList
            + ")) LIMIT 250) as sq group by sq.userid order by score desc";
        break;
      case TRY_THREE:
        sql = "select userid, sum(tweet)+max(friend)+max(follower) as score from twitter5 where userid in ("
            + userList
            + ") and date >= "
            + startDate
            + " and date <= "
            + endDate
            + " group by userid order by score DESC;";
        break;
    }

    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
      conn = ConnectionUtils.getInstance().getMySQLConnection();
      pstmt = conn.prepareStatement(sql);
      rs = pstmt.executeQuery();
      while (rs.next()) {
        response = response.append(rs.getString("userid")).append(",")
            .append(rs.getString("score")).append("\n");
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
