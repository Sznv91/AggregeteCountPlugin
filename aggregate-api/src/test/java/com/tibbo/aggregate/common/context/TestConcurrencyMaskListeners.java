package com.tibbo.aggregate.common.context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ArrayListMultimap;
import com.tibbo.aggregate.common.data.Event;
import com.tibbo.aggregate.common.datatable.TableFormat;
import com.tibbo.aggregate.common.event.ContextEventListener;
import com.tibbo.aggregate.common.event.EventHandlingException;
import com.tibbo.aggregate.common.expression.Expression;

public class TestConcurrencyMaskListeners
{
  public static final String MASK = "mask.*";
  public static final int TOTAL_ITERATIONS = 1000;
  public static final String EVENT = "test_event";
  private final DefaultContextManager defaultContextManager = new DefaultContextManager(true);
  
  private ExecutorService pool = Executors.newFixedThreadPool(500);
  
  @Test
  public void testAddMaskListener() throws InterruptedException
  {
    runAsyncIterations(() -> defaultContextManager.addMaskEventListener(MASK, EVENT, new DefaultContextEventListener()
    {
      @Override
      public void handle(Event event)
      {
      }
    }, false));
    Assert.assertTrue(defaultContextManager.getMaskListenersMasks().contains(MASK));
    Assert.assertEquals(defaultContextManager.getMaskListenersSize(MASK, EVENT), TOTAL_ITERATIONS);
  }
  
  @Test
  public void testAddEventMaskListeners() throws InterruptedException
  {
    for (int i = 0; i < 200; i++)
    {
      addEventMaskListenersIteration(i);
    }
  }
  
  private void addEventMaskListenersIteration(int i) throws InterruptedException
  {
    EventData ed = new EventData(new EventDefinition("test" + i, new TableFormat()), (ContextManager) null);
    ArrayListMultimap<String, ContextEventListener> resultMap = ArrayListMultimap.create();
    CopyOnWriteArrayList<ContextEventListener> listenersCache = new CopyOnWriteArrayList<>();
    runAsyncIterations(() -> {
      ContextEventListener contextEventListener = addEventDataEventListener(ed);
      if (listenersCache.size() < 10)
      {
        synchronized (this)
        {
          if (listenersCache.size() < 10)
          {
            listenersCache.add(contextEventListener);
          }
        }
      }
      else
      {
        ed.addListener(listenersCache.get(ThreadLocalRandom.current().nextInt(1, 9)), false);
        ed.addListener(listenersCache.get(ThreadLocalRandom.current().nextInt(1, 9)), false);
        ed.addListener(listenersCache.get(ThreadLocalRandom.current().nextInt(1, 9)), false);
        ed.addListener(listenersCache.get(ThreadLocalRandom.current().nextInt(1, 9)), false);
        ed.addListener(listenersCache.get(ThreadLocalRandom.current().nextInt(1, 9)), false);
      }
      synchronized (this)
      {
        resultMap.put(contextEventListener.getFingerprint() == null ? "null" : contextEventListener.getFingerprint(),
            contextEventListener);
      }
    });
    
    AtomicInteger totalFilterListeners = new AtomicInteger(0);
    AtomicInteger totalFing1 = new AtomicInteger(0);
    AtomicInteger totalFing2 = new AtomicInteger(0);
    ed.doWithListeners(li -> {
      if (li.getListener() != null)
      {
        if (li.getListener().getFingerprint() == null)
        {
          totalFilterListeners.incrementAndGet();
        }
        else if (li.getListener().getFingerprint().equals("fing1"))
        {
          totalFing1.incrementAndGet();
        }
        else if (li.getListener().getFingerprint().equals("fing2"))
        {
          totalFing2.incrementAndGet();
        }
      }
    });
    Assert.assertEquals(resultMap.get("null").size(), totalFilterListeners.intValue());
    Assert.assertEquals(resultMap.get("fing1").size(), totalFing1.intValue());
    Assert.assertEquals(resultMap.get("fing2").size(), totalFing2.intValue());
  }
  
  private ContextEventListener addEventDataEventListener(final EventData eventData)
  {
    ContextEventListener listener = getRandomFingerprintListener();
    eventData.addListener(listener, false);
    return listener;
    
  }
  
  private ContextEventListener getRandomFingerprintListener()
  {
    ContextEventListener listener;
    int i = ThreadLocalRandom.current().nextInt(1, 10);
    if (i <= 2)
    {
      listener = createListener("fing1");
    }
    else if (i <= 6)
    {
      listener = createListener("fing2");
      
    }
    else
    {
      listener = createListener(null);
      
    }
    return listener;
  }
  
  private ContextEventListener createListener(String fingerPrint)
  {
    return new ContextEventListener()
    {
      
      @Override
      public String getFingerprint()
      {
        return fingerPrint;
      }
      
      @Override
      public boolean shouldHandle(Event ev) throws EventHandlingException
      {
        return true;
      }
      
      @Override
      public void handle(Event event) throws EventHandlingException
      {
      }
      
      @Override
      public void handle(Event event, EventDefinition ed) throws EventHandlingException
      {
      }
      
      @Override
      public CallerController getCallerController()
      {
        return null;
      }
      
      @Override
      public Integer getListenerCode()
      {
        return null;
      }
      
      @Override
      public Expression getFilter()
      {
        return null;
      }
      
      @Override
      public boolean isAsync()
      {
        return false;
      }
      
      @Override
      public void setListenerCode(Integer listenerCode)
      {
        
      }
    };
  }
  
  private void runAsyncIterations(Runnable runnable) throws InterruptedException
  {
    
    List<Callable<Void>> tasks = new ArrayList<>();
    for (int i = 0; i < TOTAL_ITERATIONS; i++)
    {
      tasks.add(() -> {
        try
        {
          runnable.run();
        }
        catch (Exception e)
        {
        }
        return null;
      });
    }
    pool.invokeAll(tasks);
  }
  
}
