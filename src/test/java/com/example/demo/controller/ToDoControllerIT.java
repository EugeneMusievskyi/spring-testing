package com.example.demo.controller;

import com.example.demo.dto.ToDoResponse;
import com.example.demo.dto.ToDoSaveRequest;
import com.example.demo.service.ToDoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.Charset;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ToDoController.class)
@ActiveProfiles(profiles = "test")
public class ToDoControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ToDoService toDoService;

    @Test
    public void whenSave_thenReturnValidResponse() throws Exception {
        String text = "Request text";
        var request = new ToDoSaveRequest();
        request.text = text;

        ObjectMapper mapper = new ObjectMapper();
        var requestJson = mapper.writeValueAsString(request);

        var response = new ToDoResponse();
        response.id = 0L;
        response.text = text;

        when(toDoService.upsert(ArgumentMatchers.any(ToDoSaveRequest.class)))
                .thenReturn(response);

        this.mockMvc
                .perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.text").value(text))
                .andExpect(jsonPath("$.completeAt").doesNotExist());
    }
}
