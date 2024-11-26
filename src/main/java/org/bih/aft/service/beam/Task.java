package org.bih.aft.service.beam;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

record Task(
    String id,
    String from,
    List<String> to,
    String body,
    @JsonDeserialize(using = FailureStrategyDeserializer.class)
    FailureStrategy failure_strategy,
    String ttl,
    Map metadata
) { }

sealed interface FailureStrategy { }

enum Discard implements FailureStrategy {
    discard,
}

record Retry(
        RetryValues retry
) implements FailureStrategy { }

record RetryValues(
        int backoff_millisecs,
        int max_tries
) { }

class FailureStrategyDeserializer extends StdDeserializer<FailureStrategy> {
    public FailureStrategyDeserializer() {
        this(null);
    }

    protected FailureStrategyDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public FailureStrategy deserialize(JsonParser p, DeserializationContext ctx) throws IOException, JsonProcessingException {
        try {
            return Discard.valueOf(p.readValueAs(String.class));
        } catch (IOException|IllegalArgumentException ignored) {
            JsonNode f = p.readValueAsTree();
            return new Retry(new RetryValues(
                    f.get("retry").get("backoff_millisecs").asInt(),
                    f.get("retry").get("max_tries").asInt()
            ));
        }
    }
}
