package com.tibbo.aggregate.common.query;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tibbo.aggregate.common.Log;
import com.tibbo.aggregate.common.data.Data;
import com.tibbo.aggregate.common.datatable.DataTableException;
import com.tibbo.aggregate.common.datatable.DataTableUtils;
import com.tibbo.aggregate.common.datatable.FieldFormat;
import com.tibbo.aggregate.common.datatable.SimpleDataTable;
import com.tibbo.aggregate.common.datatable.encoding.ClassicEncodingSettings;
import com.tibbo.aggregate.common.sql.SqlTable;
import com.tibbo.aggregate.common.sql.SqlTablesMetadata;
import com.tibbo.aggregate.common.util.Util;

public class DataTableWrappingUtils
{
  
  private final static Cache<String, String> ESCAPED_COLUMNS_CACHE = CacheBuilder.newBuilder().weakKeys().build();

  public static Map<String, FieldDescriptor> extractResultSetFields(ResultSet rs, boolean uppercase) throws SQLException
  {
    return extractResultSetFields(rs, SqlTablesMetadata.EMPTY, uppercase);
  }

  public static Map<String, FieldDescriptor> extractResultSetFields(ResultSet rs, SqlTablesMetadata metadata, boolean uppercase) throws SQLException
  {
    Map<String, FieldDescriptor> result = new LinkedHashMap<>();
    ResultSetMetaData rsmd = rs.getMetaData();
    
    int columnCount = rsmd.getColumnCount();
    
    for (int i = 1; i <= columnCount; i++)
    {
      String colName = rsmd.getColumnLabel(i);

      String tableName = Optional.ofNullable(rsmd.getTableName(i)).orElse("");
      
      if (colName == null || colName.isEmpty())
      {
        // Some databases (including hsql) create empty column names for function calls etc
        colName = generateUniqueName(result.values(), i, colName);
      }
      int sqlType = rsmd.getColumnType(i);
      TypeMapper.Type type = TypeMapper.getTypeForSqlType(sqlType);
      
      String columnName = uppercase ? colName.toUpperCase(Locale.ENGLISH) : colName.toLowerCase(Locale.ENGLISH);
      String fieldName = DataTableWrappingUtils.escapeColumnName(colName);

      SqlTable sqlTable = metadata.getSqlTable(tableName);
      
      boolean isPk = false;
      if (sqlTable != null)
      {
        isPk = sqlTable.getPrimaryKeyColumns().stream().anyMatch(sqlColumn -> sqlColumn.getName().equals(columnName));
      }
      
      FieldDescriptor fd = new FieldDescriptor(columnName, tableName, fieldName, rsmd.getColumnLabel(i), type, isPk);
      
      result.put(fieldName, fd);
    }
    
    return result;
  }
  
  /**
   * SQL column names are case insensitive and their parts are separated with the separator sign So we'll escape column name by removing separator and capitalizing the character that follows the
   * dollar sign. All other chars are to be small letters.
   */
  public static String escapeColumnName(String columnName)
  {
    if (columnName == null)
    {
      return null;
    }
    String result = ESCAPED_COLUMNS_CACHE.getIfPresent(columnName);
    if (result != null)
    {
      return result;
    }

    StringBuffer r = new StringBuffer(columnName);
    for (int i = 0; i < r.length(); i++)
    {
      char c = r.charAt(i);
      if (c == QueryConstants.SEPARATOR.charAt(0))
      {
        r.deleteCharAt(i);
        r.setCharAt(i, Character.toUpperCase(r.charAt(i)));
      }
      else
      {
        if (!Character.isLetter(c) && !Character.isDigit(c))
        {
          c = '_';
        }
        r.setCharAt(i, Character.toLowerCase(c));
      }
    }

    result = r.toString();

    ESCAPED_COLUMNS_CACHE.put(columnName, result);
    return result;
  }
  
  public static String generateUniqueName(Collection<FieldDescriptor> result, int i, String colName) throws IllegalStateException
  {
    final String COLUMN_PREFIX = "COLUMN_";
    String resultName = colName;
    resultName = COLUMN_PREFIX + i;
    
    for (FieldDescriptor fd : result)
    {
      if (Util.equals(fd.getColumnName(), resultName))
      {
        throw new IllegalStateException("Can't generate unique column name");
      }
    }
    
    return resultName;
  }
  
  public static Object getFieldValue(ResultSet rs, int columnIndex, FieldFormat ff) throws SQLException
  {
    char ffType = ff.getType();
    Object value = null;
    
    switch (ffType)
    {
      case FieldFormat.INTEGER_FIELD:
        Number n = Util.convertToNumber(rs.getObject(columnIndex), false, true);
        value = n != null ? n.intValue() : null;
        break;
      case FieldFormat.STRING_FIELD:
        Object obj = rs.getObject(columnIndex);
        if (obj == null)
        {
          value = null;
        }
        else if (obj instanceof Blob)
        {
          Blob blob = (Blob) obj;
          value = new String(blob.getBytes(1, (int) blob.length()));
        }
        else if (obj instanceof Clob)
        {
          Clob clob = (Clob) obj;
          value = clob.getSubString(1, (int) clob.length());
        }
        else if (obj instanceof byte[])
        {
          value = new String((byte[]) obj);
        }
        else
        {
          value = obj.toString();
        }
        break;
      case FieldFormat.BOOLEAN_FIELD:
        value = Util.convertToBoolean(rs.getObject(columnIndex), false, true);
        break;
      case FieldFormat.LONG_FIELD:
        n = Util.convertToNumber(rs.getObject(columnIndex), false, true);
        value = n != null ? n.longValue() : null;
        break;
      case FieldFormat.FLOAT_FIELD:
        n = Util.convertToNumber(rs.getObject(columnIndex), false, true);
        value = n != null ? n.floatValue() : null;
        break;
      case FieldFormat.DOUBLE_FIELD:
        n = Util.convertToNumber(rs.getObject(columnIndex), false, true);
        value = n != null ? n.doubleValue() : null;
        break;
      case FieldFormat.DATE_FIELD:
        value = rs.getTimestamp(columnIndex);
        break;
      case FieldFormat.DATATABLE_FIELD:
        String s = rs.getString(columnIndex);
        try
        {
          value = s != null ? (s.startsWith(String.valueOf(DataTableUtils.ELEMENT_START)) ? new SimpleDataTable(s) : s) : null;
        }
        catch (DataTableException ex)
        {
          throw new IllegalStateException(ex);
        }
        break;
      case FieldFormat.DATA_FIELD:
        value = getDataValue(rs, columnIndex, ff);
        break;
      default:
        value = rs.getString(columnIndex);
        if (value != null)
        {
          value = ff.valueFromString((String) value);
        }
        break;
    }
    if (rs.wasNull())
    {
      // ResultSet getters return primitive types that can't be nulls...
      // Fix it
      value = null;
    }
    return value;
  }
  
  public static Data getDataValue(ResultSet rs, int columnIndex, FieldFormat<Data> ff) throws SQLException
  {
    String raw = null;
    Object dataObj = rs.getObject(columnIndex);
    if (dataObj != null)
    {
      byte[] bytes = null;
      if (dataObj instanceof byte[])
      {
        bytes = (byte[]) dataObj;
        raw = new String(bytes);
      }
      else if (dataObj instanceof Blob)
      {
        Blob blob = (Blob) dataObj;
        bytes = blob.getBytes(1, (int) blob.length());
        raw = new String(bytes);
      }
      else
      {
        Log.DATATABLE.error("The data object is neither an array of bytes nor a Blob; the data field of the Data object will be set to null");
      }
      
      try
      {
        return ff.valueFromEncodedString(raw, new ClassicEncodingSettings(false), true);
      }
      catch (Exception ex)
      {
        return new Data(bytes);
      }
    }
    return null;
  }
}
