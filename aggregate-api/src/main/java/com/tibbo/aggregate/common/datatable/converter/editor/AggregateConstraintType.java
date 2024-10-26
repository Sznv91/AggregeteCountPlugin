package com.tibbo.aggregate.common.datatable.converter.editor;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import com.tibbo.aggregate.common.Cres;
import com.tibbo.aggregate.common.datatable.SelectionValue;

public enum AggregateConstraintType implements SelectionValue
{
  
  NO_ACTION(Cres.get().getString("constraintNoAction")),
  CASCADE(Cres.get().getString("constraintCascade")),
  NULL(Cres.get().getString("constraintNull")),
  DEFAULT(Cres.get().getString("constraintDefault")),
  RESTRICT(Cres.get().getString("constraintRestrict"));
  
  private final String descr;
  
  AggregateConstraintType(String descr)
  {
    this.descr = descr;
  }
  
  public static AggregateConstraintType ofSqlRule(Integer rule) throws SQLException
  {
    switch (rule)
    {
      case DatabaseMetaData.importedKeyCascade:
        return CASCADE;
      case DatabaseMetaData.importedKeyRestrict:
        return RESTRICT;
      case DatabaseMetaData.importedKeySetNull:
        return NULL;
      case DatabaseMetaData.importedKeyNoAction:
        return NO_ACTION;
      case DatabaseMetaData.importedKeySetDefault:
        return DEFAULT;
      default:
        throw new SQLException("Unsupported constraint rule detected: " + rule);
    }
  }
  
  @Override
  public String getValue()
  {
    return name();
  }
  
  @Override
  public String getDescription()
  {
    return descr;
  }
}
