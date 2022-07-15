package com.github.danpfe.eclipsecon.validation;

import com.github.danpfe.eclipsecon.model.Sample;
import io.fabric8.kubernetes.api.model.Status;
import io.fabric8.kubernetes.api.model.admission.v1.AdmissionResponse;
import io.fabric8.kubernetes.api.model.admission.v1.AdmissionReview;
import java.util.ArrayList;
import java.util.Set;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
@Path("/validation")
public class ValidationResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(ValidationResource.class);
  @Inject
  @ConfigProperty(name = "fauxoperator.forbidden.words", defaultValue = "poop,damn")
  private Set<String> forbiddenWords;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Counted(name = "validation_requests",
      description = "How many times the a user submitted a create/update for validation of Sample",
      absolute = true)
  public AdmissionReview doPost(final AdmissionReview review) {

    review.setResponse(new AdmissionResponse());
    review.getResponse().setStatus(new Status());
    review.getResponse().setUid(review.getRequest().getUid()); // copy request UID to response.

    final var resource = review.getRequest().getObject();

    if (resource instanceof Sample sample) {

      final var spec = sample.getSpec();
      final var message = spec.getMessage();

      final var warnings = new ArrayList<String>();
      forbiddenWords.forEach(it -> {
        if (message.toLowerCase().contains(it.toLowerCase())) {
          warnings.add(String.format("The message '%s' contains the word '%s', that is not allowed!", message, it));
        }
      });

      review.getResponse().setAllowed(warnings.isEmpty());
      if (!warnings.isEmpty()) {
        review.getResponse().getWarnings().addAll(warnings);
        review.getResponse().getStatus().setCode(403);
        review.getResponse().getStatus().setMessage("Contains invalid words.");
      }

    } else {

      review.getResponse().setAllowed(false);
      review.getResponse().getStatus().setCode(400);
      review.getResponse().getStatus().setMessage("Something magical happened, we got another resource than Sample!");

    }

    return review;
  }
}
