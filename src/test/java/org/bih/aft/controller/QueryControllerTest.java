package org.bih.aft.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bih.aft.exceptions.InvalidCountQuery;
import org.bih.aft.ports.QueryUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = QueryController.class)
class QueryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QueryUseCase queryUseCase;

    @Test
    void whenValidInput_thenReturns200() throws Exception {
        String body = "{\"aql\": \"Select e FROM EHR e\" }";
        mockMvc.perform(post("/query/local").contentType("application/json").content(body))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void whenServiceThrowsInvalidCountQuery_thenReturn400AndSetMessage() throws Exception {
        String body = "{\"aql\": \"Select e FROM EHR e\" }";

        when(queryUseCase.local(any())).thenThrow(new InvalidCountQuery("asdasd"));

        mockMvc.perform(post("/query/local").contentType("application/json").content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("asdasd"));
    }
}
