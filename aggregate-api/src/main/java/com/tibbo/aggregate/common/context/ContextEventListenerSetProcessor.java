package com.tibbo.aggregate.common.context;

import com.tibbo.aggregate.common.event.ContextEventListenerSet;

public interface ContextEventListenerSetProcessor
{
  void process(ContextEventListenerSet listeners);
}
