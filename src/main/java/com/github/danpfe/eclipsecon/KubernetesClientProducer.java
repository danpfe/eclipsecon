package com.github.danpfe.eclipsecon;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;


/**
 * CDI Producer so we dont have to create a KubernetesClient all the time.
 */
public class KubernetesClientProducer {
  @Produces
  @ApplicationScoped
  public KubernetesClient produceClient() {
    final var builder = new KubernetesClientBuilder();
    return builder.build();
  }

  public void dispose(@Disposes KubernetesClient client) {
    client.close();
  }
}
