package com.github.danpfe.eclipsecon.controller;

import com.github.danpfe.eclipsecon.model.Sample;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.cache.Lister;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class Reconciler {
  private static final Logger LOGGER = LoggerFactory.getLogger(Reconciler.class);
  private static final String FINALIZER_NAME = "finalizer.samples.com.github.danpfe.eclipsecon";

  @Inject
  private Lister<Sample> lister;
  @Inject
  private KubernetesClient client;

  @Counted(name = "reconcile_requests",
      description = "How many times the Informer has told us to reconcole a Sample",
      absolute = true)
  public void handleEvent(@ObservesAsync ReconcileRequestHandler.ReconcileEvent event) {
    final var sample = lister.get(event.resourceKey());
    if (sample == null) {
      LOGGER.info("Received an event for reconciliation but resource {} is no longer in indexer.", event.resourceKey());
    } else {
      handleSample(sample);
    }
  }

  private void handleSample(final Sample obj) {
    LOGGER.info("Reconcile request for Sample {} in namespace {} ...", obj.getMetadata().getName(), obj.getMetadata().getNamespace());

    final var namespace = obj.getMetadata().getNamespace();
    final var name = obj.getMetadata().getName();

    if (obj.getMetadata().getDeletionTimestamp() != null) { // flagged for delete
      // TODO Implement something meaningful rather than just telling us that the finalizer is being removed!
      LOGGER.info("Removing finalizer from Sample {} in namespace {} ...", obj.getMetadata().getName(), obj.getMetadata().getNamespace());

      obj.getMetadata().getFinalizers().remove(FINALIZER_NAME);
      client.resource(obj).patch();

      return;
    }

    if (!obj.getMetadata().getFinalizers().contains(FINALIZER_NAME)) { // has no finalizer, yet
      LOGGER.info("Adding finalizer to Sample {} in namespace {} ...", obj.getMetadata().getName(), obj.getMetadata().getNamespace());

      obj.getMetadata().getFinalizers().add(FINALIZER_NAME);
      client.resource(obj).patch();

      return;
    }

    if (!obj.getMetadata().getGeneration().equals(obj.getStatus().getObservedGeneration())) { // didn't act upon latest spec yet
      // TODO Implement something meaningful rather than just printing a message and reverse it in the resource's status!
      LOGGER.info("User wrote {}", obj.getSpec().getMessage());
      obj.getStatus().setUppercaseMessage(obj.getSpec().getMessage().toUpperCase());

      // Let's record the generation of this resource we already saw.
      obj.getStatus().setObservedGeneration(obj.getMetadata().getGeneration());

      client.resource(obj).patchStatus();
    }
  }
}
