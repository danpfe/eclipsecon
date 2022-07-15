package com.github.danpfe.eclipsecon;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Event;
import javax.enterprise.event.NotificationOptions;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class AsyncStartup {
  @Inject
  private Event<AsyncStartupEvent> asyncStartupEvent;

  public static class AsyncStartupEvent {
    // intentionally left empty
  }

  public void onStart(@Observes @Initialized(ApplicationScoped.class) Object unused) {
    asyncStartupEvent.fireAsync(new AsyncStartupEvent(), NotificationOptions.ofExecutor(runnable -> {
      final var thread = new Thread(runnable);
      thread.setName("Custom-Startup-Event-Thread");
      thread.setDaemon(true);
      thread.start();
    }));
  }
}
