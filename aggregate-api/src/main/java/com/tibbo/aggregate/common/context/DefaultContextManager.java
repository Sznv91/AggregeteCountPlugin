package com.tibbo.aggregate.common.context;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.tibbo.aggregate.common.Log;
import com.tibbo.aggregate.common.data.Event;
import com.tibbo.aggregate.common.event.ContextEventListener;
import com.tibbo.aggregate.common.event.ContextEventListenerSet;
import com.tibbo.aggregate.common.event.EventUtils;
import com.tibbo.aggregate.common.event.FireEventRequestController;
import com.tibbo.aggregate.common.plugin.PluginDirector;

public class DefaultContextManager<T extends Context> implements ContextManager<T>
{
  private final boolean async;
  
  private T rootContext = null;
  
  private final CallerController callerController = new UncheckedCallerController();
  
  private EventDispatcher eventDispatcher;
  private boolean eventDispatcherOwner = true;
  
  private final Map<String, Map<String, ContextEventListenerSet>> eventListeners = new ConcurrentHashMap<>();
  
  private final Map<String, Map<String, ContextEventListenerSet>> maskListeners = new ConcurrentHashMap<>();
  
  private final Map<String, Map<String, ContextEventListenerSet>> univocalListeners = new ConcurrentHashMap<>();
  
  private final ReentrantReadWriteLock maskListenersLock = new ReentrantReadWriteLock();
  private final ReentrantReadWriteLock eventListenersLock = new ReentrantReadWriteLock();
  
  private ThreadPoolExecutor executorService;
  
  private boolean started;
  
  public DefaultContextManager(boolean async)
  {
    this(async, Integer.MAX_VALUE, null);
  }
  
  public DefaultContextManager(boolean async, EventDispatcher eventDispatcher)
  {
    this(async, Integer.MAX_VALUE, null, eventDispatcher);
  }
  
  public DefaultContextManager(boolean async, int eventQueueLength, Supplier<ThreadPoolExecutor> concurrentDispatcherSupplier)
  {
    this(async, eventQueueLength, concurrentDispatcherSupplier, null);
  }
  
  public DefaultContextManager(boolean async, int eventQueueLength, Supplier<ThreadPoolExecutor> concurrentDispatcherSupplier, EventDispatcher eventDispatcher)
  {
    super();
    this.async = async;
    if (eventDispatcher != null)
    {
      this.eventDispatcher = eventDispatcher;
      this.eventDispatcherOwner = false;
    }
    
    if (async)
    {
      ensureDispatcher(eventQueueLength, concurrentDispatcherSupplier);
    }
  }
  
  public DefaultContextManager(T rootContext, boolean async, EventDispatcher eventDispatcher)
  {
    this(async, eventDispatcher);
    setRoot(rootContext);
    start();
  }
  
  public DefaultContextManager(T rootContext, boolean async)
  {
    this(rootContext, async, null);
  }
  
  @Override
  public void start()
  {
    if (async && eventDispatcherOwner)
    {
      ensureDispatcher(Integer.MAX_VALUE, null);
      eventDispatcher.start();
    }
    if (rootContext != null)
    {
      rootContext.start();
    }
    started = true;
  }
  
  @Override
  public void stop()
  {
    started = false;
    if (eventDispatcher != null && eventDispatcherOwner)
    {
      eventDispatcher.interrupt();
      eventDispatcher = null;
    }
    if (rootContext != null)
    {
      rootContext.stop();
    }
  }
  
  @Override
  public void restart()
  {
    stop();
    start();
  }
  
  private void ensureDispatcher(int eventQueueLength, Supplier<ThreadPoolExecutor> concurrentDispatcherSupplier)
  {
    if (eventDispatcher == null)
    {
      eventDispatcher = new EventDispatcher(eventQueueLength, concurrentDispatcherSupplier);
    }
  }
  
  @Override
  public T getRoot()
  {
    return rootContext;
  }
  
  public void setRoot(T newRoot)
  {
    rootContext = newRoot;
    rootContext.setup(this);
    contextAdded(newRoot);
  }
  
  @Override
  public T get(String contextName, CallerController caller)
  {
    T root = getRoot();
    return root != null ? (T) root.get(contextName, caller) : null;
  }
  
  @Override
  public T get(String contextName)
  {
    T root = getRoot();
    return root != null ? (T) root.get(contextName) : null;
  }
  
  private void addEventListener(String context, String event, ContextEventListener listener, boolean mask, boolean weak)
  {
    // Distributed: ok, because remote events will be redirected to this server
    T con = get(context, listener.getCallerController());
    
    if (con != null)
    {
      List<EventDefinition> events = EventUtils.getEvents(con, event, listener.getCallerController());
      
      for (EventDefinition ed : events)
      {
        if (Log.CONTEXT_EVENTS.isDebugEnabled())
        {
          Log.CONTEXT_EVENTS.debug("Listener: '" + listener + "' was added to the context: '" + context + "' for event: ;" + event + "'");
        }
        addListenerToContext(con, ed.getName(), listener, mask, weak);
      }
    }
    else
    {
      if (!mask)
      {
        
        eventListenersLock.writeLock().lock();
        try
        {
          ContextEventListenerSet eel = getListeners(context, event);
          if (!eel.contains(listener))
          {
            Log.CONTEXT_EVENTS.debug("Listener: '" + listener + "' was added to the context: '" + context + "' for event: ;" + event + "'");
            eel.addListener(listener, weak);
          }
        }
        finally
        {
          eventListenersLock.writeLock().unlock();
        }
      }
    }
  }
  
  protected void addListenerToContext(T con, String event, ContextEventListener listener, boolean mask, boolean weak)
  {
    EventDefinition ed = con.getEventDefinition(event, listener.getCallerController());
    if (ed != null)
    {
      con.addEventListener(event, listener, weak);
    }
  }
  
  private void removeEventListener(String context, String event, ContextEventListener listener, boolean mask)
  {
    T con = get(context, listener.getCallerController());
    
    if (con != null)
    {
      if (con.getEventDefinition(event) != null)
      {
        removeListenerFromContext(con, event, listener, mask);
      }
    }
    else
    {
      if (!mask)
      {
        eventListenersLock.writeLock().lock();
        try
        {
          ContextEventListenerSet eel = getListeners(context, event);
          if (eel != null)
          {
            eel.removeListener(listener);
          }
        }
        finally
        {
          eventListenersLock.writeLock().unlock();
        }
        
      }
    }
  }
  
  protected void removeListenerFromContext(T con, String event, ContextEventListener listener, boolean mask)
  {
    con.removeEventListener(event, listener);
  }
  
  @Override
  public void addMaskEventListener(String mask, String event, ContextEventListener listener)
  {
    addMaskEventListener(mask, event, listener, false);
  }
  
  @Override
  public void addMaskEventListener(String mask, String event, ContextEventListener listener, boolean weak)
  {
    List<String> contexts = ContextUtils.expandMaskToPaths(mask, this, listener.getCallerController());
    for (String con : contexts)
    {
      addEventListener(con, event, listener, true, weak);
    }
    processMaskListeners(mask, event, listeners -> {
      if (Log.CONTEXT_EVENTS.isDebugEnabled())
      {
        Log.CONTEXT_EVENTS.debug("Listener: '" + listener + "' was added to the mask: '" + mask + "' for event: ;" + event + "'");
      }
      listeners.addListener(listener, weak);
    });
  }
  
  @Override
  public void removeMaskEventListener(String mask, String event, ContextEventListener listener)
  {
    List<Context> contexts = ContextUtils.expandMaskToContexts(mask, this, listener.getCallerController());
    
    for (Context con : contexts)
    {
      if (!con.isInitializedEvents())
      {
        continue;
      }
      
      List<EventDefinition> events = EventUtils.getEvents(con, event, listener.getCallerController());
      
      for (EventDefinition ed : events)
      {
        removeEventListener(con.getPath(), ed.getName(), listener, true);
      }
    }
    
    processMaskListeners(mask, event, listeners -> listeners.removeListener(listener));
  }
  
  protected ContextEventListenerSet getListeners(String context, String event)
  {
    Map<String, ContextEventListenerSet> cel = getContextListeners(context);
    
    ContextEventListenerSet cels = cel.get(event);
    
    if (cels == null)
    {
      cels = new ContextEventListenerSet(this);
      cel.put(event, cels);
    }
    
    return cels;
  }
  
  private Map<String, ContextEventListenerSet> getContextListeners(String context)
  {
    
    Map<String, ContextEventListenerSet> cel = eventListeners.get(context);
    
    if (cel == null)
    {
      cel = new ConcurrentHashMap<>();
      eventListeners.put(context, cel);
    }
    
    return cel;
  }
  
  private void processMaskListeners(String mask, String event, ContextEventListenerSetProcessor processor)
  {
    processContextMaskListeners(mask, cel -> {
      ContextEventListenerSet eel = cel.get(event);
      if (eel == null)
      {
        eel = new ContextEventListenerSet(DefaultContextManager.this);
        cel.put(event, eel);
      }
      processor.process(eel);
    });
  }
  
  private void processContextMaskListeners(String mask, ContextMaskListenersProcessor contextMaskListenersProcessor)
  {
    Map<String, ContextEventListenerSet> cel;
    Map<String, Map<String, ContextEventListenerSet>> localListeners = maskListeners;
    maskListenersLock.writeLock().lock();
    try
    {
      if (!ContextUtils.isMask(mask))
      {
        localListeners = univocalListeners;
      }
      cel = localListeners.get(mask);
      
      if (cel == null)
      {
        
        cel = localListeners.get(mask);
        if (cel == null)
        {
          cel = new ConcurrentHashMap<>();
          localListeners.put(mask, cel);
        }
      }
      contextMaskListenersProcessor.process(cel);
    }
    finally
    {
      maskListenersLock.writeLock().unlock();
    }
  }
  
  @Override
  public void contextAdded(T con)
  {
    eventListenersLock.writeLock().lock();
    try
    {
      Map<String, ContextEventListenerSet> cel = eventListeners.get(con.getPath());
      if (cel != null)
      {
        for (String event : cel.keySet())
        {
          ContextEventListenerSet cels = cel.get(event);
          
          if (con.getEventData(event) != null)
          {
            cels.executeForEachListener(li -> con.addEventListener(event, li.getListener(), li.isWeak()));
          }
        }
      }
    }
    finally
    {
      eventListenersLock.writeLock().unlock();
    }
    
    processContextListeners(con, contextEventListeners -> addMaskListenerToContext(con, contextEventListeners));
  }
  
  public void addMaskListenerToContext(String mask, T context)
  {
    
    processContextMaskListeners(mask, contextEventListeners -> addMaskListenerToContext(context, contextEventListeners));
  }
  
  private void addMaskListenerToContext(T con, Map<String, ContextEventListenerSet> contextEventListeners)
  {
    for (String eventMask : contextEventListeners.keySet())
    {
      final ContextEventListenerSet listeners = contextEventListeners.get(eventMask);
      listeners.executeForEachListener(li -> {
        final List<EventDefinition> events = EventUtils.getEvents(con, eventMask, li.getListener().getCallerController());
        for (EventDefinition ed : events)
        {
          addListenerToContext(con, ed.getName(), li.getListener(), true, li.isWeak());
        }
      });
    }
  }
  
  public Set<String> getMaskListenersMasks()
  {
    maskListenersLock.readLock().lock();
    try
    {
      return Sets.union(maskListeners.keySet(), univocalListeners.keySet());
    }
    finally
    {
      maskListenersLock.readLock().unlock();
    }
  }
  
  @VisibleForTesting
  protected int getMaskListenersSize(String mask, String event)
  {
    int result = 0;
    maskListenersLock.readLock().lock();
    try
    {
      Map<String, ContextEventListenerSet> cel = maskListeners.get(mask);
      if (cel != null)
      {
        ContextEventListenerSet contextEventListenerSet = cel.get(event);
        if (contextEventListenerSet != null)
        {
          result += contextEventListenerSet.size();
        }
      }
      
      cel = univocalListeners.get(mask);
      if (cel != null)
      {
        ContextEventListenerSet contextEventListenerSet = cel.get(event);
        if (contextEventListenerSet != null)
        {
          result += contextEventListenerSet.size();
        }
      }
      return result;
    }
    finally
    {
      maskListenersLock.readLock().unlock();
    }
  }
  
  @Override
  public void contextRemoved(T con)
  {
    try
    {
      con.accept(new DefaultContextVisitor()
      {
        @Override
        public void visit(Context vc)
        {
          processContextListeners(con, contextEventListeners -> removeListeners(vc, contextEventListeners));
        }
        
        private void removeListeners(Context vc, Map<String, ContextEventListenerSet> contextMaskListeners)
        {
          for (String event : contextMaskListeners.keySet())
          {
            ContextEventListenerSet listeners = contextMaskListeners.get(event);
            listeners.executeForEachListener(li -> {
              List<EventDefinition> events = EventUtils.getEvents(vc, event, li.getListener().getCallerController());
              for (EventDefinition ed : events)
              {
                vc.removeEventListener(ed.getName(), li.getListener());
              }
            });
          }
        }
      });
      
      con.accept(new DefaultContextVisitor()
      {
        @Override
        public void visit(Context vc)
        {
          eventListenersLock.writeLock().lock();
          try
          {
            Map<String, ContextEventListenerSet> cel = getContextListeners(vc.getPath());
            final List<EventDefinition> eventDefinitions = vc.getEventDefinitions(callerController);
            for (EventDefinition ed : eventDefinitions)
            {
              EventData edata = vc.getEventData(ed.getName());
              ContextEventListenerSet listeners = cel.get(ed.getName());
              if (listeners != null)
              {
                synchronized (listeners)
                {
                  edata.addListeners(listeners);
                }
              }
            }
          }
          finally
          {
            eventListenersLock.writeLock().unlock();
          }
        }
      });
    }
    catch (ContextException ex)
    {
      throw new IllegalStateException(ex.getMessage(), ex);
    }
  }
  
  @Override
  public void contextInfoChanged(T con)
  {
    
  }
  
  @Override
  public void variableAdded(Context con, VariableDefinition vd)
  {
    
  }
  
  @Override
  public void variableRemoved(Context con, VariableDefinition vd)
  {
    
  }
  
  @Override
  public void functionAdded(Context con, FunctionDefinition fd)
  {
    
  }
  
  @Override
  public void functionRemoved(Context con, FunctionDefinition fd)
  {
    
  }
  
  @Override
  public void eventAdded(T con, EventDefinition ed)
  {
    processContextListeners(con, contextEventListeners -> addListenerToContext(con, ed, contextEventListeners));
  }
  
  private void processContextListeners(T con, ContextMaskListenersProcessor processor)
  {
    maskListenersLock.writeLock().lock();
    try
    {
      for (String mask : maskListeners.keySet())
      {
        if (ContextUtils.matchesToMask(mask, con.getPath()))
        {
          Map<String, ContextEventListenerSet> contextEventListenerSet = maskListeners.get(mask);
          processor.process(contextEventListenerSet);
        }
      }
      
      if (univocalListeners.containsKey(con.getPath()))
      {
        Map<String, ContextEventListenerSet> contextEventListenerSet = univocalListeners.get(con.getPath());
        processor.process(contextEventListenerSet);
      }
      
    }
    finally
    {
      maskListenersLock.writeLock().unlock();
    }
  }
  
  private void addListenerToContext(T con, EventDefinition ed, Map<String, ContextEventListenerSet> contextEventListeners)
  {
    for (String eventMask : contextEventListeners.keySet())
    {
      if (EventUtils.matchesToMask(eventMask, ed))
      {
        ContextEventListenerSet listeners = contextEventListeners.get(eventMask);
        synchronized (listeners)
        {
          listeners.executeForEachListener(li -> addListenerToContext(con, ed.getName(), li.getListener(), true, li.isWeak()));
        }
      }
    }
  }
  
  @Override
  public void eventRemoved(Context con, EventDefinition ed)
  {
    
  }
  
  @Override
  public void queue(EventData ed, Event ev, FireEventRequestController request)
  {
    EventDispatcher dispatcher = eventDispatcher;
    
    if (dispatcher != null)
    {
      dispatcher.registerIncomingEvent();
    }
    
    if (!async || ed.getDefinition().getConcurrency() == EventDefinition.CONCURRENCY_SYNCHRONOUS)
    {
      ed.dispatch(ev);
      if (dispatcher != null)
      {
        dispatcher.registerProcessedEvent();
      }
    }
    else
    {
      if (!haveToBeProcessed(ed, ev))
      {
        if (dispatcher != null)
        {
          dispatcher.registerProcessedEvent();
        }
        return;
      }
      QueuedEvent qe = new QueuedEvent(ed, ev);
      
      try
      {
        if (dispatcher != null)
        {
          dispatcher.queue(qe, request);
        }
      }
      catch (InterruptedException ex1)
      {
        Log.CONTEXT_EVENTS.debug("Interrupted while queueing event: " + ev);
      }
      catch (NullPointerException ex1)
      {
        Log.CONTEXT_EVENTS.debug("Cannot queue event '" + ev + "': context manager is not running");
      }
    }
  }
  
  private boolean haveToBeProcessed(EventData eventData, Event event)
  {
    // We have to put event into the queue to further processing before ContextManager is started
    if (!isStarted())
    {
      return true;
    }
    if (!eventData.hasListeners())
    {
      return false;
    }
    
    return eventData.shouldHandle(event);
  }
  
  protected void setExecutorService(ThreadPoolExecutor executorService)
  {
    this.executorService = executorService;
  }
  
  @Override
  public ThreadPoolExecutor getExecutorService()
  {
    return executorService;
  }
  
  @Override
  public CallerController getCallerController()
  {
    return callerController;
  }
  
  @Override
  public int getEventQueueLength()
  {
    return eventDispatcher != null ? eventDispatcher.getQueueLength() : 0;
  }
  
  @Override
  public long getEventsScheduled()
  {
    return eventDispatcher != null ? eventDispatcher.getEventsScheduled() : 0;
  }
  
  @Override
  public long getEventsProcessed()
  {
    return eventDispatcher != null ? eventDispatcher.getEventsProcessed() : 0;
  }
  
  @Override
  public Map<String, Long> getEventQueueStatistics()
  {
    return eventDispatcher.getEventQueueStatistics();
  }
  
  @Override
  public PluginDirector getPluginDirector()
  {
    return null;
  }
  
  @Override
  public boolean isStarted()
  {
    return started;
  }
}
