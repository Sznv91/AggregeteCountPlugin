package com.tibbo.aggregate.common.sql;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class SqlTable
{
  
  private final List<SqlColumn> columns = new ArrayList<>();
  private final List<SqlColumn> primaryKeyColumns = new ArrayList<>();
  private final Map<String, SqlTableHierarchyLink> hLinks = new ConcurrentHashMap<>();
  
  private final DatabaseMetaData metaData;
  // following fields are not implied to be frequently accessed
  private final String realName;
  private final String realCatalogName;
  private final String realSchemaName;
  private boolean initialized = false;

  public SqlTable(DatabaseMetaData metaData, String dbTableName, String realCatalogName, String realSchemaName) throws SQLException
  {
    this.metaData = metaData;
    this.realName = dbTableName;
    this.realCatalogName = realCatalogName;
    this.realSchemaName = realSchemaName;
  }
  
  public String getRealName()
  {
    return realName;
  }
  
  public List<SqlColumn> getColumns() throws SQLException
  {
    initIfRequired();
    synchronized (this)
    {
      return new ArrayList<>(columns);
    }
  }
  
  public SqlColumn getColumnByName(String name) throws SQLException
  {
    initIfRequired();
    synchronized (this)
    {
      return columns.stream()
          .filter(c -> c.getRealName().equalsIgnoreCase(name)).findAny().orElse(null);
    }
  }
  
  public Map<String, SqlTableHierarchyLink> getLinks() throws SQLException {
    initIfRequired();
    return Collections.unmodifiableMap(hLinks);
  }

  public DatabaseMetaData getMetaData() throws SQLException {
    initIfRequired();
    return metaData;
  }
  
  public SqlTableHierarchyLink getLinkByTableName(String tableName) throws SQLException {
    initIfRequired();
    return hLinks.get(tableName);
  }
  
  public List<SqlColumn> getPrimaryKeyColumns() throws SQLException
  {
    initIfRequired();
    synchronized (this)
    {
      return new ArrayList<>(primaryKeyColumns);
    }
  }
  
  private synchronized void initIfRequired() throws SQLException {
    if (initialized)
    {
      return;
    }
    processColumns();
    processPrimaryKeys();
    processReferencedTables();
    processHierarchyLinks();
    initialized = true;
  }
  
  private void processColumns() throws SQLException
  {
    try (ResultSet rs = metaData.getColumns(realCatalogName,
        realSchemaName, realName, null))
    {
      synchronized (this)
      {
        while (rs.next())
        {
          columns.add(
              new SqlColumn(
                  this,
                  rs.getString("COLUMN_NAME"),
                  rs.getInt("DATA_TYPE"),
                  rs.getString("TYPE_NAME"),
                  rs.getString("IS_NULLABLE").equals("YES"),
                  rs.getString("IS_AUTOINCREMENT").equals("YES"),
                  rs.getString("IS_GENERATEDCOLUMN").equals("YES")));
        }
      }
    }
  }
  
  private void processReferencedTables() throws SQLException
  {
    try (ResultSet rs = metaData.getImportedKeys(realCatalogName,
        realSchemaName, realName))
    {
      synchronized (this)
      {
        while (rs.next())
        {
          String parentTableName = rs.getString("PKTABLE_NAME");
          String parentPrimaryColumnName = rs.getString("PKCOLUMN_NAME");
          String childForeignKeyColumnName = rs.getString("FKCOLUMN_NAME");
          
          for (SqlColumn c : columns)
          {
            if (c.getRealName().equals(childForeignKeyColumnName))
            {
              c.setReferencedTableName(parentTableName);
              c.setParentPrimaryColumnName(parentPrimaryColumnName);
            }
          }
        }
      }
    }
  }
  
  private void processHierarchyLinks()
  {
    for (SqlColumn c : columns)
    {
      if (c.isForeignKey())
      {
        if (!hLinks.containsKey(c.getReferencedTableName()))
        {
          hLinks.put(c.getReferencedTableName(), new SqlTableHierarchyLink(this.realName, c.getReferencedTableName()));
        }
        hLinks.get(c.getReferencedTableName()).addLink(c.getRealName(), c.getParentPrimaryColumnName());
      }
    }
  }
  
  private void processPrimaryKeys() throws SQLException
  {
    try (ResultSet rs = metaData
        .getPrimaryKeys(realCatalogName, realSchemaName, realName))
    {
      
      Function<String, String> getCol = name -> {
        try
        {
          return rs.getString(name);
        }
        catch (SQLException e)
        {
          return "";
        }
      };
      
      synchronized (this)
      {
        while (rs.next())
        {
          SqlColumn column = columns.stream()
              .filter(c -> c.getRealName().equals(getCol.apply("COLUMN_NAME")))
              .findAny()
              .orElse(null);
          
          if (column != null)
          {
            column.setPrimaryKey(true);
            primaryKeyColumns.add(column);
          }
        }
      }
    }
  }
}
