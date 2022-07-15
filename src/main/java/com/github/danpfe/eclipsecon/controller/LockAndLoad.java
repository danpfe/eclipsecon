package com.github.danpfe.eclipsecon.controller;

import com.github.danpfe.eclipsecon.AsyncStartup;
import com.github.danpfe.eclipsecon.model.Sample;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.extended.leaderelection.LeaderCallbacks;
import io.fabric8.kubernetes.client.extended.leaderelection.LeaderElectionConfigBuilder;
import io.fabric8.kubernetes.client.extended.leaderelection.resourcelock.ConfigMapLock;
import io.fabric8.kubernetes.client.extended.leaderelection.resourcelock.LeaseLock;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import io.fabric8.kubernetes.client.informers.cache.Lister;
import java.time.Duration;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class LockAndLoad {
  private static final Logger LOGGER = LoggerFactory.getLogger(LockAndLoad.class);

  @Inject
  @ConfigProperty(name = "pod.name")
  private String lockIdentity;
  @Inject
  private KubernetesClient kc;
  @Inject
  private ReconcileRequestHandler handler;
  private SharedInformerFactory informers;
  private Lister<Sample> sampleLister;

  @PostConstruct
  private void prepare() {
    informers = kc.informers();
    final var sampleInformer =
        informers.sharedIndexInformerFor(Sample.class, 60 * 1000L);
    sampleInformer.addEventHandler(handler);
    sampleLister = new Lister<>(sampleInformer.getIndexer());
  }

  @Produces
  Lister<Sample> getLister() {
    return sampleLister;
  }

  void onStart(@ObservesAsync AsyncStartup.AsyncStartupEvent unused) {
    LOGGER.info("My lease lock ID is {}", lockIdentity);

    final var leader = kc.leaderElector()
        .withConfig(
            new LeaderElectionConfigBuilder()
                .withName("faux-operator-leader-elector")
                .withLeaseDuration(Duration.ofSeconds(15L))
                .withLock(new LeaseLock(kc.getNamespace(), "faux-operator-leader", lockIdentity))
                .withRenewDeadline(Duration.ofSeconds(10L))
                .withRetryPeriod(Duration.ofSeconds(2L))
                .withLeaderCallbacks(new LeaderCallbacks(
                    this::lead,
                    this::yield,
                    newLeader -> LOGGER.info("Elected leader with lease lock ID {}", newLeader)))
                .build())
        .build();

    leader.run();
  }

  private void lead() {
    LOGGER.info("Yeah! My turn to reconcile Samples!");
    informers.startAllRegisteredInformers();
  }

  private void yield() {
    LOGGER.info("Awww, stopping to reconcile Samples!");
    informers.stopAllRegisteredInformers();
  }
}
