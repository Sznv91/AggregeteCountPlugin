package com.tibbo.aggregate.common.query;

import com.tibbo.aggregate.common.datatable.FieldFormat;

public class FieldDescriptor
{
  private final String fieldName;
  private final String fieldDescription;
  private final TypeMapper.Type type;
  private FieldFormat format;
  private final String columnName;
  private final String tableName;
  private final boolean isPrimaryKey;
  
  public FieldDescriptor(String columnName, String tableName, String name, String description, TypeMapper.Type type, boolean isPrimaryKey)
  {
    super();
    this.columnName = columnName;
    this.tableName = tableName;
    this.fieldName = name;
    this.fieldDescription = description;
    this.type = type;
    this.isPrimaryKey = isPrimaryKey;
  }
  
  public String getColumnName()
  {
    return columnName;
  }
  
  public String getFieldName()
  {
    return fieldName;
  }
  
  public String getFieldDescription()
  {
    return fieldDescription;
  }
  
  public TypeMapper.Type getType()
  {
    return type;
  }
  
  public FieldFormat getFormat()
  {
    return format;
  }
  
  public String getTableName()
  {
    return tableName;
  }
  
  public void setFormat(FieldFormat format)
  {
    this.format = format;
  }
  
  @Override
  public String toString()
  {
    return "FieldDescriptor [name=" + fieldName + ", description=" + fieldDescription + ", type=" + type + "]";
  }
  
  public boolean isPrimaryKey()
  {
    return isPrimaryKey;
  }
}
