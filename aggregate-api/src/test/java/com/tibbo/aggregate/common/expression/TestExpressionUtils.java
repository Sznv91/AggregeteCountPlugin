package com.tibbo.aggregate.common.expression;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.tibbo.aggregate.common.datatable.DataTable;
import com.tibbo.aggregate.common.datatable.FieldFormat;
import com.tibbo.aggregate.common.datatable.SimpleDataTable;
import com.tibbo.aggregate.common.datatable.TableFormat;
import com.tibbo.aggregate.common.tests.CommonsTestCase;

public class TestExpressionUtils extends CommonsTestCase
{
  
  public void testSyntaxValidation() throws Exception
  {
    String str = "cell({:executeQuery(\"SELECT SUM(data." + "processList" + "$hrSWRunPerfMem) as value FROM users.admin.devices.localhost:" + "processList" + " as data WHERE " + "processList"
        + "$hrSWRunName = 'svchost.exe' AND " + "processList" + "$hrSWRunPath = 'C:\\\\Windows\\\\system32\\\\'\" )}, \"value\") > 3600";

    ExpressionUtils.validateSyntax(new Expression(str), false);
  }

  public void testFunctionParameters()
  {
    String params = "\"constant\", 'expression', unquoted_expression";
    List<Object> res = ExpressionUtils.getFunctionParameters(params, true);
    assertEquals(3, res.size());
    assertEquals("constant", res.get(0));
    assertEquals(new Expression("expression"), res.get(1));
    assertEquals(new Expression("unquoted_expression"), res.get(2));
  }

  public void testEscaping()
  {
    String params = "\"SELECT COUNT(*) as value FROM users.admin.devices.lh:processList as data     WHERE       processList$hrSWRunName = 'csrss.exe' AND processList$hrSWRunPath = 'C:\\\\Windows\\\\system32\\\\' \"";
    List<Object> res = ExpressionUtils.getFunctionParameters(params, false);
    assertEquals(1, res.size());
    assertEquals(
        "SELECT COUNT(*) as value FROM users.admin.devices.lh:processList as data     WHERE       processList$hrSWRunName = 'csrss.exe' AND processList$hrSWRunPath = 'C:\\Windows\\system32\\' ",
        res.get(0));
  }

  @ParameterizedTest
  @MethodSource("referenceSourcesAndDataTableDefaultRows")
  public void testFindFieldValueByReference(String assertMessage, String source, Integer defaultRow)
  {
    // given
    TableFormat FORMAT = new TableFormat();
    FORMAT.addField(FieldFormat.<DataTable>create("folder", DataTable.class).setNullable(true).setInlineData(true));
    FORMAT.addField(FieldFormat.create("file", String.class).setNullable(true));

    SimpleDataTable innerTable = new SimpleDataTable(FORMAT);
    String innerFile1 = "innerFile1";
    String innerFile2 = "innerFile2";
    String innerFile3 = "innerFile3";
    innerTable.addRecord(null, innerFile1);
    innerTable.addRecord(null, innerFile2);
    innerTable.addRecord(null, innerFile3);

    SimpleDataTable intermediateTable = new SimpleDataTable(FORMAT);
    intermediateTable.addRecord(innerTable, null);
    intermediateTable.addRecord(innerTable, null);

    SimpleDataTable outerTable = new SimpleDataTable(FORMAT);
    outerTable.addRecord(intermediateTable, null);

    Reference reference = new Reference(source);

    // when
    Object fieldValue = ExpressionUtils.findFieldValueByReference(reference, outerTable, defaultRow);

    // then
    assertEquals(assertMessage, innerFile3, fieldValue);
  }

  private static Stream<Arguments> referenceSourcesAndDataTableDefaultRows()
  {
    return Stream.of(
            Arguments.of("all rows specified", "some_context:files$folder[0].folder[1].file[2]", null),
            Arguments.of("default row expected in intermediate table", "some_context:files$folder[0].folder.file[2]", 1)
    );
  }
}
