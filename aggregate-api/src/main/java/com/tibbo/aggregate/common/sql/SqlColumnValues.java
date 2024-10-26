package com.tibbo.aggregate.common.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SqlColumnValues {

    private final SqlTable table;
    private final List<SqlColumn> columns = new ArrayList<>();
    private final Map<String, Object> columnValues = new HashMap<>();

    public SqlColumnValues(SqlTable table) {
        this.table = table;
    }

    public void addColumn(SqlColumn column) {
        columns.add(column);
    }

    public void addValue(SqlColumn column, Object value) {
        columns.add(column);
        columnValues.put(column.getRealName(), value);
    }

    public void fetch(ResultSet rs) throws SQLException {
        for (SqlColumn column : columns) {
            Object value = rs.getObject(column.getRealName());
            columnValues.put(column.getRealName(), value);
        }
    }

    public List<String> getFullColumnNames() {
        return columns.stream().map(c -> table.getRealName() + "." + c.getRealName()).collect(Collectors.toList());
    }

    public String getFullColumnNamesList() {
        StringBuilder sb = new StringBuilder();
        sb.append(" ");
        boolean first = true;
        for(String s: getFullColumnNames()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(s);
        }
        sb.append(" ");
        return sb.toString();
    }

    public List<String> getColumnNames() {
        return columns.stream().map(c -> c.getRealName()).collect(Collectors.toList());
    }

    public List<Object> getColumnValues() {
        return columns.stream().map(c -> columnValues.get(c.getRealName())).collect(Collectors.toList());
    }

    public SqlTable getTable() {
        return table;
    }

    public Map<String, Object> getColumnValuesMap() {
      return columnValues;
    }
}
