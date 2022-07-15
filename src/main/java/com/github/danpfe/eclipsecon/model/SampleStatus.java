package com.github.danpfe.eclipsecon.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import java.util.Objects;

@JsonDeserialize
public class SampleStatus implements KubernetesResource {
  private Long observedGeneration;
  private String uppercaseMessage;

  public Long getObservedGeneration() {
    return observedGeneration;
  }

  public void setObservedGeneration(final Long observedGeneration) {
    this.observedGeneration = observedGeneration;
  }

  public String getUppercaseMessage() {
    return uppercaseMessage;
  }

  public void setUppercaseMessage(final String uppercaseMessage) {
    this.uppercaseMessage = uppercaseMessage;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final SampleStatus that = (SampleStatus) o;
    return Objects.equals(observedGeneration, that.observedGeneration) && Objects.equals(uppercaseMessage, that.uppercaseMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(observedGeneration, uppercaseMessage);
  }

  @Override
  public String toString() {
    return "SampleStatus{" +
        "observedGeneration=" + observedGeneration +
        ", uppercaseMessage='" + uppercaseMessage + '\'' +
        '}';
  }
}
