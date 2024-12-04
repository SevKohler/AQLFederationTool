package org.bih.aft.controller.dao;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import java.util.Map;

public record AqlWithParams(
        String aql,
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        Map<String, String> parameters
) {
}
