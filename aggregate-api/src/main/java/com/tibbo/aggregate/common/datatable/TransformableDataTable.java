package com.tibbo.aggregate.common.datatable;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import com.tibbo.aggregate.common.context.CallerController;
import com.tibbo.aggregate.common.context.Context;
import com.tibbo.aggregate.common.context.ContextManager;
import com.tibbo.aggregate.common.datatable.encoding.ClassicEncodingSettings;
import com.tibbo.aggregate.common.datatable.encoding.FormatCache;

public class TransformableDataTable implements DataTable
{
  
  private final DataTable origin;
  
  public TransformableDataTable(DataTable origin)
  {
    this.origin = origin;
  }
  
  @Override
  public Integer getRecordCount()
  {
    return origin.getRecordCount();
  }
  
  @Override
  public int getFieldCount()
  {
    return origin.getFieldCount();
  }
  
  @Override
  public TableFormat getFormat()
  {
    return origin.getFormat();
  }
  
  @Override
  public DataTable setFormat(TableFormat format)
  {
    return origin.setFormat(format);
  }
  
  @Override
  public FieldFormat getFormat(int field)
  {
    return origin.getFormat(field);
  }
  
  @Override
  public FieldFormat getFormat(String name)
  {
    return origin.getFormat(name);
  }
  
  @Override
  public boolean applyCachedFormat(Optional<FormatCache> formatCache)
  {
    return origin.applyCachedFormat(formatCache);
  }
  
  @Override
  public Long getId()
  {
    return origin.getId();
  }
  
  @Override
  public void setId(Long id)
  {
    origin.setId(id);
  }
  
  @Override
  public boolean hasField(String field)
  {
    return origin.hasField(field);
  }
  
  @Override
  public DataTable addRecords(List<DataRecord> records)
  {
    return origin.addRecords(records);
  }
  
  @Override
  public DataTable addRecord(DataRecord record)
  {
    return origin.addRecord(record);
  }
  
  @Override
  public DataRecord addRecord(Object... fieldValues)
  {
    return origin.addRecord(fieldValues);
  }
  
  @Override
  public DataTable addRecord(int index, DataRecord record)
  {
    return origin.addRecord(index, record);
  }
  
  @Override
  public DataRecord addRecord()
  {
    return origin.addRecord();
  }
  
  @Override
  public void validate(Context context, ContextManager contextManager, CallerController caller) throws DataTableException
  {
    origin.validate(context, contextManager, caller);
  }
  
  @Override
  public void validateRecord(DataRecord record) throws ValidationException
  {
    origin.validateRecord(record);
  }
  
  @Override
  public DataTable setRecord(int index, DataRecord record)
  {
    return origin.setRecord(index, record);
  }
  
  @Override
  public void swapRecords(int index1, int index2)
  {
    origin.swapRecords(index1, index2);
  }
  
  @Override
  @Deprecated
  public List<DataRecord> getRecords()
  {
    return origin.getRecords();
  }
  
  @Override
  public boolean isInvalid()
  {
    return origin.isInvalid();
  }
  
  @Override
  public String getInvalidationMessage()
  {
    return origin.getInvalidationMessage();
  }
  
  @Override
  public void setInvalidationMessage(String invalidationMessage)
  {
    origin.setInvalidationMessage(invalidationMessage);
  }
  
  @Override
  public Date getTimestamp()
  {
    return origin.getTimestamp();
  }
  
  @Override
  public void setTimestamp(Date timestamp)
  {
    origin.setTimestamp(timestamp);
  }
  
  @Override
  public Integer getQuality()
  {
    return origin.getQuality();
  }
  
  @Override
  public void setQuality(Integer quality)
  {
    origin.setQuality(quality);
  }
  
  @Override
  public Object getValue()
  {
    return origin.getValue();
  }
  
  @Override
  public DataRecord getRecord(int number)
  {
    return origin.getRecord(number);
  }
  
  @Override
  public DataRecord getRecordById(String id)
  {
    return origin.getRecordById(id);
  }
  
  @Override
  public DataRecord removeRecord(int index)
  {
    return origin.removeRecord(index);
  }
  
  @Override
  public void removeRecordsByIds(Collection<String> ids)
  {
    origin.removeRecordsByIds(ids);
  }
  
  @Override
  public void removeRecords(DataRecord rec)
  {
    origin.removeRecords(rec);
  }
  
  @Override
  public void reorderRecord(DataRecord record, int index)
  {
    origin.reorderRecord(record, index);
  }
  
  @Override
  public boolean equals(Object obj)
  {
    return origin.equals(obj);
  }
  
  @Override
  public int hashCode()
  {
    return origin.hashCode();
  }
  
  @Override
  public String getEncodedData(ClassicEncodingSettings settings)
  {
    return origin.getEncodedData(settings);
  }
  
  @Override
  public StringBuilder getEncodedData(StringBuilder finalSB, ClassicEncodingSettings settings, Boolean isTransferEncode, Integer encodeLevel)
  {
    return origin.getEncodedData(finalSB, settings, isTransferEncode, encodeLevel);
  }
  
  @Override
  public String encode()
  {
    return origin.encode();
  }
  
  @Override
  public String encode(boolean useVisibleSeparators)
  {
    return origin.encode(useVisibleSeparators);
  }
  
  @Override
  public String encode(ClassicEncodingSettings settings)
  {
    return origin.encode(settings);
  }
  
  @Override
  public StringBuilder encode(StringBuilder finalSB, ClassicEncodingSettings settings, Boolean isTransferEncode, Integer encodeLevel)
  {
    return origin.encode(finalSB, settings, isTransferEncode, encodeLevel);
  }
  
  @Override
  public String toString()
  {
    return origin.toString();
  }
  
  @Override
  public String getDescription()
  {
    return origin.getDescription();
  }
  
  @Override
  public String toDefaultString()
  {
    return origin.toDefaultString();
  }
  
  @Override
  public void fixRecords()
  {
    origin.fixRecords();
  }
  
  @Override
  public String dataAsString()
  {
    return origin.dataAsString();
  }
  
  @Override
  public String dataAsString(boolean showFieldNames, boolean showHiddenFields)
  {
    return origin.dataAsString(showFieldNames, showHiddenFields);
  }
  
  @Override
  public String dataAsString(boolean showFieldNames, boolean showHiddenFields, boolean showPasswords)
  {
    return origin.dataAsString(showFieldNames, showHiddenFields, showPasswords);
  }
  
  @Override
  public boolean isOneCellTable()
  {
    return origin.isOneCellTable();
  }
  
  @Override
  public boolean conform(TableFormat rf)
  {
    return origin.conform(rf);
  }
  
  @Override
  public String conformMessage(TableFormat rf)
  {
    return origin.conformMessage(rf);
  }
  
  @Override
  public List<DataRecord> selectAll(DataTableQuery query)
  {
    return origin.selectAll(query);
  }
  
  @Override
  public DataRecord select(DataTableQuery query)
  {
    return origin.select(query);
  }
  
  @Override
  public DataRecord select(String field, Object value)
  {
    return origin.select(field, value);
  }
  
  @Override
  public Integer findIndex(DataTableQuery query)
  {
    return origin.findIndex(query);
  }
  
  @Override
  public Integer findIndex(String field, Object value)
  {
    return origin.findIndex(field, value);
  }
  
  @Override
  public Integer findIndex(DataRecord record)
  {
    return origin.findIndex(record);
  }
  
  @Override
  public void sort(String field, boolean ascending)
  {
    origin.sort(field, ascending);
  }
  
  @Override
  public void sort(DataTableSorter sorter)
  {
    origin.sort(sorter);
  }
  
  @Override
  public void sort(Comparator<DataRecord> comparator)
  {
    origin.sort(comparator);
  }
  
  @Override
  public DataRecord rec()
  {
    return origin.rec();
  }
  
  @Override
  public Object get()
  {
    return origin.get();
  }
  
  @Override
  public void splitFormat()
  {
    origin.splitFormat();
  }
  
  @Override
  public void joinFormats()
  {
    origin.joinFormats();
  }
  
  @Override
  public Iterator<DataRecord> iterator()
  {
    return origin.iterator();
  }
  
  @Override
  public Iterator<DataRecord> iterator(int index)
  {
    return origin.iterator(index);
  }
  
  @Override
  public DataTable clone()
  {
    return origin.clone();
  }
  
  @Override
  public int compareTo(DataTable other)
  {
    return origin.compareTo(other);
  }
  
  @Override
  public void append(DataTable src)
  {
    origin.append(src);
  }
  
  @Override
  public DataTable makeImmutable()
  {
    return origin.makeImmutable();
  }
  
  @Override
  public DataTable cloneIfImmutable()
  {
    return origin.cloneIfImmutable();
  }
  
  @Override
  public boolean isImmutable()
  {
    return origin.isImmutable();
  }
  
  @Override
  public boolean isSimple()
  {
    return origin.isSimple();
  }
  
  @Override
  public void close()
  {
    origin.close();
  }
  
  @Override
  public Stream<DataRecord> stream()
  {
    return origin.stream();
  }
  
  @Override
  public void forEach(Consumer<? super DataRecord> action)
  {
    origin.forEach(action);
  }
  
  @Override
  public Spliterator<DataRecord> spliterator()
  {
    return origin.spliterator();
  }
  
  /**
   * Represent the table as the {@link ImmutableMap}, using on of the fields as a key, and {@link DataRecord} as the value
   * 
   * @param keyField
   *          - field of the datatable which will be the key of the {@link ImmutableMap}
   * @return
   */
  public Map<String, DataRecord> asMap(String keyField)
  {
    if (!origin.hasField(keyField))
    {
      return ImmutableMap.of();
    }
    ImmutableMap.Builder<String, DataRecord> builder = ImmutableMap.builder();
    
    for (DataRecord record : origin)
    {
      builder.put(record.getValueAsString(keyField), record);
    }
    return builder.build();
  }
}
