package com.tibbo.aggregate.common.filter;

import static com.tibbo.aggregate.common.filter.FilterApiConstants.COMPONENT_SMART_FILTER_COLUMN;
import static com.tibbo.aggregate.common.filter.FilterApiConstants.COMPONENT_SMART_FILTER_STATE;
import static com.tibbo.aggregate.common.filter.FilterApiConstants.COMPONENT_SMART_FILTER_TYPE;
import static com.tibbo.aggregate.common.filter.FilterApiConstants.CONTROL_PANE_DATA_TABLE_VALUE;
import static com.tibbo.aggregate.common.filter.FilterApiConstants.CONTROL_PANE_DATE_VALUE;
import static com.tibbo.aggregate.common.filter.FilterApiConstants.CONTROL_PANE_NUMERIC_VALUE;
import static com.tibbo.aggregate.common.filter.FilterApiConstants.PANE_FIELD_CONTAINS;
import static com.tibbo.aggregate.common.filter.FilterApiConstants.PANE_FIELD_EQUALS;
import static com.tibbo.aggregate.common.filter.FilterApiConstants.PANE_FIELD_LAST_INTERVAL;
import static com.tibbo.aggregate.common.filter.FilterApiConstants.PANE_FIELD_RECORD_COUNT;
import static com.tibbo.aggregate.common.filter.FilterApiConstants.PANE_FIELD_VALUE_PRESENCE;
import static com.tibbo.aggregate.common.filter.FilterApiConstants.TF_CONTROL_PANE_DATATABLE_VALUE;
import static com.tibbo.aggregate.common.filter.FilterApiConstants.TF_CONTROL_PANE_DATE_VALUE;
import static com.tibbo.aggregate.common.filter.FilterApiConstants.TF_TIME_INTERVAL;
import static com.tibbo.aggregate.common.filter.FilterApiConstants.TIME_INTERVAL_AMOUNT;
import static com.tibbo.aggregate.common.filter.FilterApiConstants.VALUE_PRESENCE_SET;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.tibbo.aggregate.common.datatable.DataRecord;
import com.tibbo.aggregate.common.datatable.DataTable;
import com.tibbo.aggregate.common.datatable.FieldFormat;
import com.tibbo.aggregate.common.datatable.SimpleDataTable;
import com.tibbo.aggregate.common.datatable.TableFormat;
import com.tibbo.aggregate.common.filter.converter.ControlStatesFilterConverter;
import com.tibbo.aggregate.common.filter.converter.ControlStatesFilterUtil;


public class ControlStatesFilterConverterTest {

    public static final long WEEK_AS_MS = 604800000L;

    @Test
    public void testConvertSingleColumnFilter() {
        SimpleDataTable table = new SimpleDataTable(FilterApiConstants.TF_COMPONENT_SMART_FILTER);
        DataRecord dr = table.addRecord();
        dr.setValue(COMPONENT_SMART_FILTER_COLUMN, "column1");
        dr.setValue(COMPONENT_SMART_FILTER_TYPE, CONTROL_PANE_NUMERIC_VALUE);

        SimpleDataTable pane = new SimpleDataTable(FilterApiConstants.TF_CONTROL_PANE_NUMERIC_VALUE);
        DataRecord drf = pane.addRecord();
        drf.setValue(PANE_FIELD_VALUE_PRESENCE, VALUE_PRESENCE_SET);
        dr.setValue(COMPONENT_SMART_FILTER_STATE, pane);

        String text = ControlStatesFilterConverter.buildFilerTextExpression(table);
        Assertions.assertEquals("isNotNull({column1})", text);
    }

    @Test
    public void testConvertDataTableFilter() {
        SimpleDataTable table = new SimpleDataTable(FilterApiConstants.TF_COMPONENT_SMART_FILTER);
        DataRecord dr = table.addRecord();
        dr.setValue(COMPONENT_SMART_FILTER_COLUMN, "column1");
        dr.setValue(COMPONENT_SMART_FILTER_TYPE, CONTROL_PANE_DATA_TABLE_VALUE);

        SimpleDataTable pane = new SimpleDataTable(TF_CONTROL_PANE_DATATABLE_VALUE);
        DataRecord drf = pane.addRecord();
        drf.setValue(PANE_FIELD_RECORD_COUNT, 42);
        drf.setValue(PANE_FIELD_CONTAINS, "findme");

        dr.setValue(COMPONENT_SMART_FILTER_STATE, pane);

        String text = ControlStatesFilterConverter.buildFilerTextExpression(table);

        Assertions.assertEquals("simpleContains(encode({column1}, true), \"findme\") && records({column1}) == 42", text);
    }

    @Test
    public void testConvertComplexFilter() {
        SimpleDataTable table = new SimpleDataTable(FilterApiConstants.TF_COMPONENT_SMART_FILTER);
        DataRecord dr = table.addRecord();
        dr.setValue(COMPONENT_SMART_FILTER_COLUMN, "column1");
        dr.setValue(COMPONENT_SMART_FILTER_TYPE, CONTROL_PANE_NUMERIC_VALUE);

        SimpleDataTable paneNumeric = new SimpleDataTable(FilterApiConstants.TF_CONTROL_PANE_NUMERIC_VALUE);
        DataRecord drf = paneNumeric.addRecord();
        drf.setValue(PANE_FIELD_VALUE_PRESENCE, VALUE_PRESENCE_SET);
        DataTable doubleVariant = ControlStatesFilterUtil.makeEmptyDouble();
        doubleVariant.rec().setValue(0, 3.1415);
        drf.setValue(PANE_FIELD_EQUALS, doubleVariant);

        dr.setValue(COMPONENT_SMART_FILTER_STATE, paneNumeric);

        dr = table.addRecord();
        dr.setValue(COMPONENT_SMART_FILTER_COLUMN, "column2");
        dr.setValue(COMPONENT_SMART_FILTER_TYPE, CONTROL_PANE_DATE_VALUE);

        SimpleDataTable paneDate = new SimpleDataTable(TF_CONTROL_PANE_DATE_VALUE);
        DataRecord drf1 = paneDate.addRecord();
        drf1.setValue(PANE_FIELD_VALUE_PRESENCE, VALUE_PRESENCE_SET);
        SimpleDataTable interval = new SimpleDataTable(TF_TIME_INTERVAL);
        interval.addRecord()
                .setValue(TIME_INTERVAL_AMOUNT, WEEK_AS_MS * 3);

        drf1.setValue(PANE_FIELD_LAST_INTERVAL, interval);
        dr.setValue(COMPONENT_SMART_FILTER_STATE, paneDate);

        String text = ControlStatesFilterConverter.buildFilerTextExpression(table);
        Assertions.assertEquals("isNotNull({column1}) && {column1} == 3.1415 && isNotNull({column2}) && lastWeeks({column2}, 3)", text);
    }

    @Test
    public void testCreateEmptyPaneFilter() {
        TableFormat tf = new TableFormat();

        FieldFormat<?> ff = FieldFormat.create("name", FieldFormat.STRING_FIELD);
        tf.addField(ff);

        ff = FieldFormat.create("age", FieldFormat.INTEGER_FIELD);
        tf.addField(ff);

        ff = FieldFormat.create("speechDate", FieldFormat.DATA_FIELD);
        tf.addField(ff);

        DataTable table = new SimpleDataTable(tf);

        DataTable filter = ControlStatesFilterConverter.buildEmptyStateTableFromMetadata(table);
        Assertions.assertEquals(3, filter.getRecordCount());
    }
}
