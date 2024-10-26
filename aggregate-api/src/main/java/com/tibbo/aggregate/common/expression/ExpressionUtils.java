package com.tibbo.aggregate.common.expression;

import java.io.CharArrayReader;
import java.text.CharacterIterator;
import java.text.MessageFormat;
import java.text.StringCharacterIterator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import com.tibbo.aggregate.common.Cres;
import com.tibbo.aggregate.common.datatable.DataRecord;
import com.tibbo.aggregate.common.datatable.DataTable;
import com.tibbo.aggregate.common.datatable.DataTableUtils;
import com.tibbo.aggregate.common.datatable.TableFormat;
import com.tibbo.aggregate.common.datatable.encoding.ClassicEncodingSettings;
import com.tibbo.aggregate.common.expression.parser.ASTStart;
import com.tibbo.aggregate.common.expression.parser.ExpressionParser;
import com.tibbo.aggregate.common.expression.parser.ExpressionParserVisitor;
import com.tibbo.aggregate.common.expression.util.DumpingVisitor;
import com.tibbo.aggregate.common.expression.util.ReferencesFinderVisitor;
import com.tibbo.aggregate.common.util.Pair;
import com.tibbo.aggregate.common.util.SyntaxErrorException;

public class ExpressionUtils
{
  public static final char PARAM_ESCAPE_SINGLE = '\'';
  public static final char PARAM_ESCAPE_DOUBLE = '\"';
  private static final char PARAMS_DELIM = ',';
  private static final char PARAMS_ESCAPE = '\\';
  
  public static final String NULL_PARAM = "null";
  
  public static Object getValue(AttributedObject ao)
  {
    return ao != null ? ao.getValue() : null;
  }
  
  public static AttributedObject toAttributed(Object value)
  {
    if (value instanceof AttributedObject)
    {
      return (AttributedObject) value;
    }
    
    return new DefaultAttributedObject(value);
  }
  
  public static AttributedObject toAttributed(Object value, AttributedObject source)
  {
    if (value instanceof AttributedObject)
    {
      return (AttributedObject) value;
    }
    
    return new DefaultAttributedObject(value, source != null ? source.getTimestamp() : null, source != null ? source.getQuality() : null);
  }
  
  public static AttributedObject toAttributed(Object value, AttributedObject first, AttributedObject second)
  {
    if (value instanceof AttributedObject)
    {
      return (AttributedObject) value;
    }
    
    Date timestamp = null;
    Integer quality = null;
    
    if (first != null && first.getTimestamp() != null && second != null && second.getTimestamp() != null)
    {
      timestamp = null;
      
      // TODO: mix quality properly
      quality = null;
    }
    else if (first != null && first.getTimestamp() != null)
    {
      timestamp = first.getTimestamp();
      quality = first.getQuality();
    }
    else if (second != null && second.getTimestamp() != null)
    {
      timestamp = second.getTimestamp();
      quality = second.getQuality();
    }
    
    return new DefaultAttributedObject(value, timestamp, quality);
  }
  
  public static void copyAttributes(AttributedObject source, AttributedObject destination)
  {
    destination.setTimestamp(source.getTimestamp());
    destination.setQuality(source.getQuality());
  }

  public static void validateSyntax(Expression expression, boolean showExpressionInErrorText) throws SyntaxErrorException
  {
    ExpressionUtils.parse(expression, showExpressionInErrorText);
  }

  public static List<Object> getFunctionParameters(String paramsString, boolean allowExpressions)
  {
    LinkedList<Object> params = new LinkedList();
    boolean insideSingleQuotedLiteral = false;
    boolean insideDoubleQuotedLiteral = false;
    boolean escaped = false;
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < paramsString.length(); i++)
    {
      char c = paramsString.charAt(i);
      if (c == PARAMS_ESCAPE)
      {
        if (escaped)
        {
          escaped = false;
          buf.append(c);
          continue;
        }
        else
        {
          escaped = true;
          continue;
        }
      }
      else if (insideSingleQuotedLiteral)
      {
        if (c == PARAM_ESCAPE_SINGLE)
        {
          if (!escaped)
          {
            insideSingleQuotedLiteral = false;
            String param = buf.toString();
            if (allowExpressions)
            {
              params.add(new Expression(prepareParameter(param)));
            }
            else
            {
              params.add(prepareParameter(param));
            }
            buf = null;
          }
        }
      }
      else if (insideDoubleQuotedLiteral)
      {
        if (c == PARAM_ESCAPE_DOUBLE)
        {
          if (!escaped)
          {
            insideDoubleQuotedLiteral = false;
            String param = buf.toString();
            params.add(prepareParameter(param));
            buf = null;
          }
        }
      }
      else if (c == PARAMS_DELIM)
      {
        if (!insideSingleQuotedLiteral && !insideDoubleQuotedLiteral)
        {
          if (buf != null)
          {
            String param = buf.toString().trim();
            if (param.length() > 0)
            {
              params.add(new Expression(prepareParameter(param)));
            }
          }
          
          buf = new StringBuilder();
          continue;
        }
      }
      else if (c == PARAM_ESCAPE_SINGLE && !insideDoubleQuotedLiteral)
      {
        insideSingleQuotedLiteral = true;
        buf = new StringBuilder();
        continue;
      }
      else if (c == PARAM_ESCAPE_DOUBLE && !insideSingleQuotedLiteral)
      {
        insideDoubleQuotedLiteral = true;
        buf = new StringBuilder();
        continue;
      }
      
      if (c != PARAMS_ESCAPE)
      {
        escaped = false;
      }
      
      if (buf != null)
      {
        buf.append(c);
      }
    }
    
    if (buf != null)
    {
      String param = buf.toString().trim();
      if (param.length() > 0)
      {
        params.add(new Expression(prepareParameter(param)));
      }
    }
    
    if (insideSingleQuotedLiteral)
    {
      throw new IllegalArgumentException("Illegal function parameters: " + params);
    }
    
    if (insideDoubleQuotedLiteral)
    {
      throw new IllegalArgumentException("Illegal function parameters: " + params);
    }
    
    return params;
  }
  
  private static String prepareParameter(String parameter)
  {
    return parameter;
  }
  
  public static String getFunctionParameters(List<Object> params)
  {
    StringBuilder sb = new StringBuilder();
    
    int i = 0;
    for (Object param : params)
    {
      if (param == null)
      {
        sb.append(NULL_PARAM);
      }
      else
      {
        if (param instanceof Expression)
        {
          String value = param.toString();
          
          if (value.indexOf(PARAMS_DELIM) != -1)
          {
            sb.append(PARAM_ESCAPE_SINGLE);
            sb.append(value);
            sb.append(PARAM_ESCAPE_SINGLE);
          }
          else
          {
            sb.append(value);
          }
          
        }
        else
        {
          sb.append(PARAM_ESCAPE_DOUBLE);
          sb.append(param.toString());
          sb.append(PARAM_ESCAPE_DOUBLE);
        }
      }
      if (i < params.size() - 1)
      {
        sb.append(PARAMS_DELIM);
      }
      i++;
    }
    
    return sb.toString();
  }
  
  public static void dump(String expression) throws SyntaxErrorException
  {
    ASTStart rootNode = parse(new Expression(expression), true);
    ExpressionParserVisitor visitor = new DumpingVisitor();
    rootNode.jjtAccept(visitor, null);
  }
  
  public static ASTStart parse(Expression expression, boolean showExpressionInErrorText) throws SyntaxErrorException
  {
    return parse(expression.getText(), showExpressionInErrorText);
  }
  
  public static ASTStart parse(String expressionText, boolean showExpressionInErrorText) throws SyntaxErrorException
  {
    try
    {
      ExpressionParser parser = new ExpressionParser(new CharArrayReader(expressionText.toCharArray()));
      return parser.Start();
    }
    catch (Throwable ex)
    {
      throw new SyntaxErrorException(Cres.get().getString("exprParseErr") + (showExpressionInErrorText ? " '" + expressionText + "': " : ": ") + ex.getMessage(), ex);
    }
  }
  
  public static List<Reference> findReferences(Expression expression) throws SyntaxErrorException
  {
    ASTStart rootNode = ParsedExpressionCache.getCachedAstRoot(expression.getText());
    ReferencesFinderVisitor visitor = new ReferencesFinderVisitor();
    rootNode.jjtAccept(visitor, null);
    return visitor.getIdentifiers();
  }
  
  public static LinkedHashSet<Reference> deduplicateExpressionsReferences(List<String> expressions) throws SyntaxErrorException
  {
    LinkedHashSet<Reference> depdupeIdentifiers = new LinkedHashSet<>();
    for (String expressionText : expressions)
    {
      if (expressionText == null)
        continue;
      Expression expression = new Expression(expressionText);
      List<Reference> identifiers = findReferences(expression);
      depdupeIdentifiers.addAll(identifiers);
    }
    return depdupeIdentifiers;
  }
  
  public static String escapeStringLiteral(String text)
  {
    if (text == null)
    {
      return null;
    }
    
    final StringBuilder result = new StringBuilder();
    final StringCharacterIterator iterator = new StringCharacterIterator(text);
    char character = iterator.current();
    while (character != CharacterIterator.DONE)
    {
      if (character == '\"')
      {
        result.append("\\\"");
      }
      else if (character == '\'')
      {
        result.append("\\\'");
      }
      else if (character == '\\')
      {
        result.append("\\\\");
      }
      else
      {
        result.append(character);
      }
      
      character = iterator.next();
    }
    return result.toString();
  }
  
  public static long generateBindingId()
  {
    long id = Math.round(Math.random() * Long.MAX_VALUE);
    return id;
  }
  
  public static TableFormat readFormat(String format) throws SyntaxErrorException
  {
    return new TableFormat(format, new ClassicEncodingSettings(useVisibleSeparators(format)), true);
  }
  
  public static boolean useVisibleSeparators(String formatString) throws SyntaxErrorException
  {
    if (formatString == null)
    {
      throw new SyntaxErrorException("The given string is null");
    }
    
    boolean useVisibleSeparators = true;
    
    for (int i = 0; i < formatString.length(); i++)
    {
      char c = formatString.charAt(i);
      
      if (c == DataTableUtils.ELEMENT_START)
      {
        useVisibleSeparators = false;
        break;
      }
      
      if (c == DataTableUtils.ELEMENT_VISIBLE_START)
      {
        useVisibleSeparators = true;
        break;
      }
    }
    
    return useVisibleSeparators;
  }

  @Nullable
  public static Object findFieldValueByReference(Reference ref, DataTable table, Integer defaultRowIndex)
  {
    if (ref.getFields().isEmpty())
    {
      throw new IllegalArgumentException("No fields in reference: " + ref.getImage());
    }

    Iterator<Pair<String, Integer>> it = ref.getFields().iterator();
    Pair<String, Integer> fieldRef = null;
    DataTable nestedTable = table;
    DataRecord nestedRecord = null;
    Integer rowIndex;

    while (it.hasNext())
    {
      if (nestedRecord != null)
      {
        Object nestedValue = nestedRecord.getValue(fieldRef.getFirst());
        if (nestedValue instanceof DataTable)
        {
          nestedTable = (DataTable) nestedValue;
        }
        else
        {
          throw new IllegalArgumentException("Type of field '" + fieldRef.getFirst() + "' is '" +
                                                nestedValue.getClass().getSimpleName() + "', but expects only data table");
        }
      }

      fieldRef = it.next();
      rowIndex = fieldRef.getSecond();
      if (rowIndex == null)
      {
        rowIndex = defaultRowIndex != null ? defaultRowIndex : 0;
      }

      if (nestedTable.getRecordCount() != null && rowIndex >= nestedTable.getRecordCount())
      {
        throw new IllegalStateException(MessageFormat.format(Cres.get().getString("exprNonExistentRow"),
                                              rowIndex, nestedTable.getRecordCount()) + ": " + nestedTable.dataAsString());
      }

      nestedRecord = nestedTable.getRecord(rowIndex);
    }

    return nestedRecord.getValue(fieldRef.getFirst());
  }
}
