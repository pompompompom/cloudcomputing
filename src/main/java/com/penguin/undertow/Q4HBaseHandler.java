package com.penguin.undertow;

import io.undertow.server.HttpServerExchange;

import java.io.IOException;
import java.util.Deque;
import java.util.List;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * Handler for Question 2 : JDBC Query
 *
 * -------------------------------------------------
 * | hashTag | info:uid | info:yeardate | info:tid |
 * -------------------------------------------------
 */

public class Q4HBaseHandler extends BaseHttpHandler {

  /**
   * Keys for http params
   */
  private static final String KEY_HASHTAG = "hashtag";
  private static final String KEY_START = "start";
  private static final String KEY_END = "end";

  /**
   * HBASE column family
   */
  private static final byte[] INFO_FAMILY = Bytes.toBytes("info");
  private static final byte[] QUALIFIER_UID = Bytes.toBytes("uid");
  private static final byte[] QUALIFIER_TIME = Bytes.toBytes("yeardate");
  private static final byte[] QUALIFIER_TID = Bytes.toBytes("tid");

  private static final String TABLE_NAME = "twitter4";

  @Override
  public String getResponse(HttpServerExchange exchange) {
    // Get parameters
    Deque<String> hashtag = exchange.getQueryParameters().get(KEY_HASHTAG);
    Deque<String> start = exchange.getQueryParameters().get(KEY_START);
    Deque<String> end = exchange.getQueryParameters().get(KEY_END);

    if (hashtag == null || hashtag.isEmpty() || start == null
        || start.isEmpty() || end == null || end.isEmpty()) {
      return getDefaultResponse();
    }

    String tag = hashtag.peekFirst();
    String starttime = start.peekFirst();
    String endtime = end.peekFirst();

    if (tag == null || starttime == null || endtime == null) {
      return getDefaultResponse();
    }
    starttime = starttime.replaceAll("-", "") + "000000";
    endtime = endtime.replaceAll("-", "") + "235959";

    // Get response
    Scan scan = new Scan();
    FilterList list = new FilterList(FilterList.Operator.MUST_PASS_ALL);
    RowFilter rowFilter = new RowFilter(CompareOp.EQUAL, new BinaryComparator(
        Bytes.toBytes(tag)));
    list.addFilter(rowFilter);
    SingleColumnValueFilter startFilter = new SingleColumnValueFilter(
        INFO_FAMILY, QUALIFIER_TIME, CompareOp.GREATER_OR_EQUAL,
        Bytes.toBytes(starttime));
    startFilter.setFilterIfMissing(true);
    list.addFilter(startFilter);
    SingleColumnValueFilter endFilter = new SingleColumnValueFilter(
        INFO_FAMILY, QUALIFIER_TIME, CompareOp.LESS_OR_EQUAL,
        Bytes.toBytes(endtime));
    endFilter.setFilterIfMissing(true);
    list.addFilter(endFilter);
    scan.setFilter(list);

    ResultScanner scanner = null;
    HTableInterface table = null;

    try {
      table = ConnectionUtils.getInstance().getHTable(TABLE_NAME);

      scanner = table.getScanner(scan);

    } catch (IOException e) {
      System.out.println("EXCEPTION " + e.toString());
    }

    StringBuilder response = new StringBuilder();
    for (Result result : scanner) {
      List<KeyValue> keyvalues = result.list();

      String time = Bytes.toString(keyvalues.get(0).getValue());
      String userId = Bytes.toString(keyvalues.get(1).getValue());
      String tweetId = Bytes.toString(keyvalues.get(2).getValue());

      response = response.append(tweetId).append(",").append(userId)
          .append(",").append(time).append("\n");
    }

    return response.toString();
  }

  public String getTime(String str) {
    StringBuilder sb = new StringBuilder();
    sb = sb.append(str.substring(0, 4)).append("-").append(str.substring(4, 6))
        .append("-").append(str.substring(6, 8)).append("+")
        .append(str.substring(8, 10)).append(":").append(str.substring(10, 12))
        .append(":").append(str.substring(12, 14));
    return sb.toString();
  }
}
