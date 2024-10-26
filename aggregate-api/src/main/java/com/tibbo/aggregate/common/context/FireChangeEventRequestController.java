package com.tibbo.aggregate.common.context;

import com.tibbo.aggregate.common.data.*;
import com.tibbo.aggregate.common.datatable.*;
import com.tibbo.aggregate.common.datatable.encoding.*;
import com.tibbo.aggregate.common.event.*;

public class FireChangeEventRequestController extends FireEventRequestController
{
  private final VariableDefinition def;
  private final DataTable value;
  
  public FireChangeEventRequestController(Long customExpirationPeriod, VariableDefinition def, DataTable value)
  {
    super(customExpirationPeriod);
    this.def = def;
    this.value = value;
  }
  
  @Override
  public Event process(Event event)
  {
    if (event.getExpirationtime() == null)
    {
      return null;
    }

    boolean noFormat = def.getFormat() == null;

    DataTable fullValue = noFormat ? value : null;
    event.getData().rec().setValue(AbstractContext.EF_CHANGE_VALUE, fullValue);

    String dataOnly = noFormat ? null : value.getEncodedData(new ClassicEncodingSettings(false, true));
    event.getData().rec().setValue(AbstractContext.EF_CHANGE_DATA, dataOnly);

    String formatOnly = noFormat || def.getFormat().equals(value.getFormat()) ?
            null : value.getFormat().encode(new ClassicEncodingSettings(false, true));
    event.getData().rec().setValue(AbstractContext.EF_CHANGE_FORMAT, formatOnly);

    return event;
  }
}
