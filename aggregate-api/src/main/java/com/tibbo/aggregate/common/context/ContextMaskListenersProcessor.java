package com.tibbo.aggregate.common.context;

import java.util.Map;

import com.tibbo.aggregate.common.event.ContextEventListenerSet;

public interface ContextMaskListenersProcessor
{
  void process(Map<String, ContextEventListenerSet> contextEventListeners);
}
