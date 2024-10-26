package com.tibbo.aggregate.common.filter;

import java.io.CharArrayReader;
import java.util.function.Supplier;

import com.tibbo.aggregate.common.datatable.DataRecord;
import com.tibbo.aggregate.common.datatable.DataTable;
import com.tibbo.aggregate.common.datatable.SimpleDataTable;
import com.tibbo.aggregate.common.datatable.TableFormat;
import com.tibbo.aggregate.common.expression.parser.ASTStart;
import com.tibbo.aggregate.common.expression.parser.ExpressionParser;
import com.tibbo.aggregate.common.expression.parser.ParseException;

public class FilterEvaluator {

    private final TableFormat tableFormat;
    private final Expression rootExpression;
    private final DataSupplier dataSupplier = new DataSupplierImpl();
    private DataTable dataTable;
    private int cursor = 0;

    public FilterEvaluator(TableFormat tableFormat, String filterExpression) throws ParseException {
        Expression visitorExpression = null;
        
        if(!filterExpression.isEmpty()) {
            ExpressionParser parser = new ExpressionParser(new CharArrayReader(filterExpression.toCharArray()));
            ASTStart root = parser.Start();

            FilterPredicateExpressionParserVisitor visitor = new FilterPredicateExpressionParserVisitor();
            root.jjtAccept(visitor, null);
            visitorExpression = visitor.getRootExpression();
        }

        this.tableFormat = tableFormat;
        this.rootExpression = visitorExpression;
        bindReferences(rootExpression);
    }

    public FilterEvaluator(String filterExpression) throws ParseException {
        ExpressionParser parser = new ExpressionParser(new CharArrayReader(filterExpression.toCharArray()));
        ASTStart root = parser.Start();
        FilterPredicateExpressionParserVisitor visitor = new FilterPredicateExpressionParserVisitor();
        root.jjtAccept(visitor, null);
        this.rootExpression = visitor.getRootExpression();
        this.tableFormat = null;
    }
    public static void validateExpression(TableFormat format, String expression) {
        if (expression == null || expression.isEmpty() || format == null || format.getFields().isEmpty()) {
            return;
        }
        try {
            new FilterEvaluator(format, expression);
        } catch (Throwable e) {
            if (e instanceof SmartFilterEvaluationException) {
                return;
            }
            throw new SmartFilterRuntimeException("Invalid Expression: " + expression);
        }
    }
    public Expression getRootExpression() {
        return rootExpression;
    }

    public void setDataTable(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    public DataTable getDataTable() {
        return dataTable;
    }

    public DataTable filterTable() {
        DataTable result = new SimpleDataTable(tableFormat);
        for (cursor = 0; cursor < dataTable.getRecordCount(); cursor++) {
            if (rootExpression != null) {
                Boolean predicate = (Boolean) rootExpression.evaluate();
                if (predicate != null && predicate) {
                    result.addRecord(dataTable.getRecord(cursor));
                }
            } else {
                result.addRecord(dataTable.getRecord(cursor));
            }
        }
        return result;
    }

    private void bindReferences(Expression expression) {
        if (expression != null) {
            if (expression instanceof ColumnName) {
                ColumnName cn = (ColumnName) expression;
                cn.setDataSupplier(dataSupplier);
                cn.bind(tableFormat);
            }

            for (int i = 0; i < expression.getChildren().length; i++) {
                bindReferences(expression.getChildren()[i]);
            }
        }
    }

    private class DataSupplierImpl implements DataSupplier {

        private final Supplier<DataRecord> dataRecordSupplier = new DataRecordSupplier();

        @Override
        public Supplier<DataRecord> getDataRecordSupplier() {
            return dataRecordSupplier;
        }

        @Override
        public TableFormat getTableFormat() {
            return tableFormat;
        }
    }

    private class DataRecordSupplier implements Supplier<DataRecord> {
        @Override
        public DataRecord get() {
            return dataTable.getRecord(cursor);
        }
    }

}
