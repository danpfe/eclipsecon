package com.github.danpfe.eclipsecon.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Objects;

@JsonDeserialize
public class SampleSpec {
  private String message;

  public String getMessage() {
    return message;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final SampleSpec that = (SampleSpec) o;
    return Objects.equals(message, that.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(message);
  }

  @Override
  public String toString() {
    return "SampleSpec{" +
        "message='" + message + '\'' +
        '}';
  }
}
