package com.penguin.undertow;

import io.undertow.server.HttpServerExchange;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Deque;
import java.util.HashMap;

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

public class Q3HBaseHandler extends BaseHttpHandler {

  /**
   * Keys for http params
   */
  private static final String KEY_UID = "userid";

  /**
   * HBASE column family
   */
  private static final byte[] INFO_FAMILY = Bytes.toBytes("info");
  private static final byte[] INFO_QUALIFIER = Bytes.toBytes("content");

  private static final String TABLE_NAME = "twitter3";

  private static final HashMap<Integer, String> FLAG_MAP = new HashMap<Integer, String>();
  static {
    FLAG_MAP.put(1, "-");
    FLAG_MAP.put(2, "+");
    FLAG_MAP.put(3, "*");
  }

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

    Get get = new Get(Bytes.toBytes(userId));

    // Get response
    HTableInterface table = null;
    Result result = null;

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
