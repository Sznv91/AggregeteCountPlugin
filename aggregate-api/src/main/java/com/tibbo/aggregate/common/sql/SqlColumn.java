package com.tibbo.aggregate.common.sql;

import java.sql.Types;
import java.util.Objects;

public final class SqlColumn {

    private final SqlTable table;
    private final String realName;
    private final String name;
    private final int dataType;
    private final String typeName;
    private volatile String referencedTableName;
    private volatile String parentPrimaryColumnName;
    private volatile boolean isPrimaryKey;
    private volatile boolean isNullable;
    private volatile boolean isAutoIncrement;
    private volatile boolean isGenerated;

    // Not public. Is called by corresponding SqlTable object. Does not contain consistency
    // validation logic.
    SqlColumn(SqlTable table,
              String realName,
              int dataType,
              String typeName,
              boolean isNullable,
              boolean isAutoIncrement,
              boolean isGenerated) {
        this.table = table;
        this.realName = realName;
        this.name = realName.toLowerCase();
        this.dataType = dataType;
        this.typeName = typeName;
        this.isNullable = isNullable;
        this.isAutoIncrement = isAutoIncrement;
        this.setGenerated(isGenerated);
    }

    public String getName() {
        return name;
    }

    public SqlTable getTable() {
        return table;
    }

    public String getRealName() {
        return realName;
    }

    public int getDataType() {
        return dataType;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getReferencedTableName() {
        return referencedTableName;
    }

    public void setReferencedTableName(String referencedTableName) {
        this.referencedTableName = referencedTableName;
    }

    public boolean isForeignKey() {
        return referencedTableName != null;
    }

    public String getParentPrimaryColumnName() {
        return parentPrimaryColumnName;
    }

    public void setParentPrimaryColumnName(String parentPrimaryColumnName) {
        this.parentPrimaryColumnName = parentPrimaryColumnName;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        isPrimaryKey = primaryKey;
    }

    public boolean isNullable() {
        return isNullable;
    }

    public void setNullable(boolean nullable) {
        isNullable = nullable;
    }

    public boolean isAutoIncrement() {
        return isAutoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        isAutoIncrement = autoIncrement;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof SqlColumn)
                && ((SqlColumn) other).realName.equals(this.realName)
                && ((SqlColumn) other).dataType == this.dataType
                && ((SqlColumn) other).typeName.equals(this.typeName)
                && ((SqlColumn) other).getTable().equals(this.getTable());
    }

    @Override
    public int hashCode() {
        return Objects.hash(table,
                dataType,
                realName
        );
    }

    public boolean isGenerated() {
        return isGenerated;
    }

    public void setGenerated(boolean generated) {
        isGenerated = generated;
    }

    public boolean isString() {
        switch (dataType) {
            case Types.CHAR:
            case Types.LONGNVARCHAR:
            case Types.VARCHAR:
                return true;
            default:
                return false;
        }
    }

    public boolean isNumber() {
        switch (dataType) {
            case Types.BIGINT:
            case Types.INTEGER:
            case Types.TINYINT:
                return true;
            default:
                return false;
        }
    }

    public boolean isDataBlock() {
        switch (dataType)
        {
            case Types.BLOB:
            case Types.CLOB:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return true;
            default:
                return false;
        }
    }
}
