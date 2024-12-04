package org.bih.aft.service.query;

import org.bih.aft.controller.dao.AqlWithParams;
import org.bih.aft.exceptions.InvalidCountQuery;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CountVerificatorTest {

    @Test
    void validQuery() {
        CountVerificator countVerificator = new CountVerificator();
        AqlWithParams aQlQuery = new AqlWithParams("Select COUNT(x) from EHR x", Map.of());
        countVerificator.verify(aQlQuery);
        // MOCK ?!
    }

    @Test
    void invalidSelectStatementAmount() {
        CountVerificator countVerificator = new CountVerificator();
        AqlWithParams aQlQuery = new AqlWithParams("Select COUNT(x), x from EHR x", Map.of());
        Exception exception = assertThrows(InvalidCountQuery.class, () -> {
            countVerificator.verify(aQlQuery);
        });
        String expectedMessage = "Only one Select clause with one statement is allowed";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void invalidSelectFunction() {
        CountVerificator countVerificator = new CountVerificator();
        AqlWithParams aQlQuery = new AqlWithParams("Select Max(x) from EHR x", Map.of());
        Exception exception = assertThrows(InvalidCountQuery.class, () -> {
            countVerificator.verify(aQlQuery);
        });
        String expectedMessage = "Function has to be COUNT";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void invalidSelectWithoutCount() {
        CountVerificator countVerificator = new CountVerificator();
        AqlWithParams aQlQuery = new AqlWithParams("Select x from EHR x", Map.of());
        Exception exception = assertThrows(InvalidCountQuery.class, () -> {
            countVerificator.verify(aQlQuery);
        });
        String expectedMessage = "No COUNT included in Select statement";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }
}

