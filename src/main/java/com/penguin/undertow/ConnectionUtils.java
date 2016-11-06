package com.penguin.undertow;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * @author mitayun
 */
public class ConnectionUtils {

  private static ConnectionUtils instance = null;

  /**
   * MySQL
   */

  private static final String ADDRESS_MYSQL = "jdbc:mysql://localhost:3306/CC_Final";
  private static final String USERNAME = "root";
  private static final String PW = "";
  private static HikariDataSource mySqlDataSource = null;

  /**
   * HBase
   */
  private Configuration conf = null;
  private HConnection hConnection = null;

  @SuppressWarnings("all")
  protected ConnectionUtils() {
    // MySQL
    if (MiniSite.DB_TYPE == MiniSite.MYSQL) {
      HikariConfig config = new HikariConfig();
      config.setMaximumPoolSize(150);
      config
          .setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
      config.addDataSourceProperty("serverName", "localhost");
      config.addDataSourceProperty("url", ADDRESS_MYSQL);
      config.addDataSourceProperty("user", USERNAME);
      config.addDataSourceProperty("password", PW);
      config.addDataSourceProperty("port", 3306);
      config.addDataSourceProperty("autoReconnect", false);
      mySqlDataSource = new HikariDataSource(config);
    } else if (MiniSite.DB_TYPE == MiniSite.HBASE) {

      conf = HBaseConfiguration.create();
      try {
        hConnection = HConnectionManager.createConnection(conf);
      } catch (ZooKeeperConnectionException e) {
        e.printStackTrace();
      }
    }
  }

  public static ConnectionUtils getInstance() {
    if (instance == null) {
      instance = new ConnectionUtils();
    }
    return instance;
  }

  public Connection getMySQLConnection() throws SQLException {
    return mySqlDataSource.getConnection();
  }

  public HTableInterface getHTable(String tableName) throws IOException {
    return hConnection.getTable(tableName);
  }
}
