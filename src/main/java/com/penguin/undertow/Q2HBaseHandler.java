package com.penguin.undertow;

import io.undertow.server.HttpServerExchange;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Deque;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * Handler for Question 2 : JDBC Query
 *
 * -------------------------
 * | userId | info:content |
 * -------------------------
 */

public class Q2HBaseHandler extends BaseHttpHandler {

  /**
   * Keys for http params
   */
  private static final String KEY_UID = "userid";
  private static final String KEY_TIME = "tweet_time";

  /**
   * HBASE column family
   */
  private static final byte[] INFO_FAMILY = Bytes.toBytes("info");
  private static final byte[] INFO_QUALIFIER = Bytes.toBytes("content");

  private static final String TABLE_NAME = "twitter";

  @Override
  public String getResponse(HttpServerExchange exchange) {
    // Get parameters
    Deque<String> uid = exchange.getQueryParameters().get(KEY_UID);
    Deque<String> time = exchange.getQueryParameters().get(KEY_TIME);

    if (uid == null || time == null || uid.isEmpty() || time.isEmpty()) {
      return getDefaultResponse();
    }

    String userId = uid.peekFirst();
    String timeStamp = time.peekFirst();

    if (userId == null || timeStamp == null) {
      return getDefaultResponse();
    }

    // Get response
    String rowKey = userId + timeStamp.replaceAll("   | |-|:|\t", "");
    Get get = new Get(Bytes.toBytes(rowKey));
    Result result = null;
    HTableInterface table = null;

    try {
      table = ConnectionUtils.getInstance().getHTable(TABLE_NAME);
      result = table.get(get);
    } catch (IOException e) {
      System.out.println("EXCEPTION " + e.toString());
    }

    byte[] value = result.getValue(INFO_FAMILY, INFO_QUALIFIER);

    if (value == null || value.length == 0) {
      return getDefaultResponse();
    }

    String res = null;
    try {
      res = new String(value, "UTF-8");

      res = res.replaceAll("pngn134", "\n");
    } catch (UnsupportedEncodingException e) {
      System.out.println("EXCEPTION " + e.toString());
    }

    return res == null ? getDefaultResponse() : (getDefaultResponse() + res);
  }
}
