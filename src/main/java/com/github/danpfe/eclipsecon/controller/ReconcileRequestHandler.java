package com.github.danpfe.eclipsecon.controller;

import com.github.danpfe.eclipsecon.model.Sample;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.cache.Cache;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.NotificationOptions;
import javax.inject.Inject;

@ApplicationScoped
public class ReconcileRequestHandler implements ResourceEventHandler<Sample> {
  @Inject
  private Event<ReconcileEvent> sampleEvent;
  private NotificationOptions execOptions;

  @PostConstruct
  private void init() {
    final var executor = Executors.newSingleThreadExecutor();
    execOptions = NotificationOptions.ofExecutor(executor);
  }

  @Override
  public void onAdd(Sample obj) {
    sampleEvent.fireAsync(new ReconcileEvent(Cache.metaNamespaceKeyFunc(obj)), execOptions);
  }

  @Override
  public void onUpdate(Sample oldObj, Sample newObj) {
    if (oldObj.getMetadata().getResourceVersion().equals(newObj.getMetadata().getResourceVersion())) {
      return;
    }

    sampleEvent.fireAsync(new ReconcileEvent(Cache.metaNamespaceKeyFunc(newObj)), execOptions);
  }

  @Override
  public void onDelete(Sample obj, boolean deletedFinalStateUnknown) {
    // do nothing, we handle what we need through the finalizer
  }

  protected static record ReconcileEvent(String resourceKey) {
    // intentionally empty
  }
}
