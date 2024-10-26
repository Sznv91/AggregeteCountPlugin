package com.tibbo.aggregate.common.sql;

import java.util.ArrayList;
import java.util.List;

// A simple structure to keep table reference information
public class SqlTableHierarchyLink {

    private final String childTable;
    private final String parentTable;
    private final List<String> parentPrimaryColumns;
    private final List<String> childForeignColumns;

    public SqlTableHierarchyLink(String childTable, String parentTable) {
        this(childTable, parentTable, new ArrayList<>(), new ArrayList<>());
    }

    public SqlTableHierarchyLink(String childTable, String parentTable, List<String> childForeignColumns, List<String> parentPrimaryColumns) {
        this.childTable = childTable;
        this.parentTable = parentTable;

        if (childForeignColumns.size() != parentPrimaryColumns.size()) {
            throw new IllegalArgumentException("Number of foreign columns does not match number of " +
                    "primary columns in the referenced table");
        }

        synchronized (this) {
            this.childForeignColumns = new ArrayList<>(childForeignColumns);
            this.parentPrimaryColumns = new ArrayList<>(parentPrimaryColumns);
        }
    }

    public List<String> getParentPrimaryColumns() {
        synchronized (this) {
            return new ArrayList<>(parentPrimaryColumns);
        }
    }

    public List<String> getChildForeignColumns() {
        synchronized (this) {
            return new ArrayList<>(childForeignColumns);
        }
    }

    public String getChildTable() {
        return childTable;
    }

    public String getParentTable() {
        return parentTable;
    }

    public void addLink(String childForeignColumn, String parentPrimaryColumn) {
        synchronized (this) {
            parentPrimaryColumns.add(parentPrimaryColumn);
            childForeignColumns.add(childForeignColumn);
        }
    }

    public String getPrimaryColumnByForeignColumn(String childForeignColumn) {
        synchronized (this) {
            assert childForeignColumns.size() == parentPrimaryColumns.size();
            for (int i = 0; i < childForeignColumns.size(); i++) {
                if (childForeignColumns.get(i).equals(childForeignColumn)) {
                    return parentPrimaryColumns.get(i);
                }
            }
            throw new IllegalStateException("Column: " + childForeignColumn + " is not found");
        }
    }

    public String getForeignColumnByPrimaryColumn(String parentPrimaryColumn) {
        synchronized (this) {
            assert childForeignColumns.size() == parentPrimaryColumns.size();
            for (int i = 0; i < parentPrimaryColumns.size(); i++) {
                if (parentPrimaryColumns.get(i).equals(parentPrimaryColumn)) {
                    return childForeignColumns.get(i);
                }
            }
            throw new IllegalStateException("Column: " + parentPrimaryColumn + " is not found");
        }
    }
}
