package org.bih.aft.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.bih.aft.controller.dao.AQLinput;
import org.bih.aft.exceptions.InvalidCountQuery;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CountVerificatorTest {

    @Test
    void validQuery()  {
        CountVerificator countVerificator = new CountVerificator();
        AQLinput aQlQuery = new AQLinput("Select COUNT(x) from EHR x");
        countVerificator.verify(aQlQuery);
        // MOCK ?!
    }

    @Test
    void invalidSelectStatementAmount()  {
        CountVerificator countVerificator = new CountVerificator();
        AQLinput aQlQuery = new AQLinput("Select COUNT(x), x from EHR x");
        Exception exception = assertThrows(InvalidCountQuery.class, () -> {
            countVerificator.verify(aQlQuery);
        });
        String expectedMessage = "Only one Select clause with one statement is allowed";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void invalidSelectFunction()  {
        CountVerificator countVerificator = new CountVerificator();
        AQLinput aQlQuery = new AQLinput("Select Max(x) from EHR x");
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
        AQLinput aQlQuery = new AQLinput("Select x from EHR x");
        Exception exception = assertThrows(InvalidCountQuery.class, () -> {
            countVerificator.verify(aQlQuery);
        });
        String expectedMessage = "No COUNT included in Select statement";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }
}

