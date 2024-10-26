package com.tibbo.aggregate.common.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.common.collect.ImmutableMap;

public class SqlTablesMetadata
{
  public static final SqlTablesMetadata EMPTY = new SqlTablesMetadata();
    private final ImmutableMap<String, SqlTable> tableMap;

  public SqlTablesMetadata()
  {
    tableMap = ImmutableMap.of();
  }
  public SqlTablesMetadata(Connection connection) throws SQLException
  {
    ImmutableMap.Builder<String, SqlTable> builder = ImmutableMap.builder();
    initTablesMetadata(connection, builder);
    tableMap = builder.build();
  }
  
  private void initTablesMetadata(Connection connection, ImmutableMap.Builder<String, SqlTable> builder) throws SQLException
  {
    DatabaseMetaData metaData = connection.getMetaData();

    /*
     * Start with driver mysql-connector-java 8.0.22 metaData.getTables return all user visible tables from all databases
     */
    try (ResultSet rs = metaData.getTables(connection.getCatalog(), null, null, null))
    {
      while (rs.next())
      {
        String dbTableName = rs.getString("TABLE_NAME");
        SqlTable value = new SqlTable(metaData, dbTableName, connection.getCatalog(), connection.getSchema());
        builder.put(dbTableName.toLowerCase(), value);
      }
    }
  }
  
  public SqlTable getSqlTable(String name)
  {
    return tableMap.get(name.toLowerCase());
  }
}
