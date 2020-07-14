package com.example.demo.controller;

import com.example.demo.dto.ToDoResponse;
import com.example.demo.dto.ToDoSaveRequest;
import com.example.demo.dto.mapper.ToDoEntityToResponseMapper;
import com.example.demo.exception.ToDoNotFoundException;
import com.example.demo.model.ToDoEntity;
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

import java.time.Clock;
import java.time.ZonedDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ToDoController.class)
@ActiveProfiles(profiles = "test")
public class ToDoControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ToDoService toDoService;

    @Test
    public void whenSaveWithSaveRequest_thenReturnValidResponse() throws Exception {
        String text = "Request text";
        var request = new ToDoSaveRequest();
        request.text = text;

        ObjectMapper mapper = new ObjectMapper();
        var jsonRequest = mapper.writeValueAsString(request);

        var response = new ToDoResponse();
        response.id = 4553L;
        response.text = text;

        when(toDoService.upsert(ArgumentMatchers.any(ToDoSaveRequest.class)))
                .thenReturn(response);

        this.mockMvc
                .perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.id").value(response.id))
                .andExpect(jsonPath("$.text").value(text))
                .andExpect(jsonPath("$.completedAt").doesNotExist());
    }

    @Test
    public void whenSaveWithId_thenReturnValidResponse() throws Exception {
        long id = 5325L;
        String text = "Request text";
        ZonedDateTime time = ZonedDateTime.now(Clock.systemUTC());
        var toDoEntity = new ToDoEntity(id, text, time);

        when(toDoService.completeToDo(id)).then(i -> ToDoEntityToResponseMapper.map(toDoEntity));

        this.mockMvc
                .perform(put(String.format("/todos/%s/complete", id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.text").value(text))
                .andExpect(jsonPath("$.completedAt").exists());
    }

    @Test
    public void whenSaveWithIdNotFound_throwException() throws Exception {
        long id = 412412L;
        String text = "Request text";
        ObjectMapper mapper = new ObjectMapper();
        var jsonRequest = mapper.writeValueAsString(id);

        when(toDoService.completeToDo(id)).thenThrow(ToDoNotFoundException.class);

        this.mockMvc
                .perform(put(String.format("/todos/%s/complete", id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(jsonRequest))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    public void whenGetOne_thenReturnValidResponse() throws Exception {
        long id = 4124L;
        String text = "Request text";
        var toDoEntity = new ToDoEntity(id, text);

        when(toDoService.getOne(id)).then(a -> ToDoEntityToResponseMapper.map(toDoEntity));

        this.mockMvc.perform(get(String.format("/todos/%s", id)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.text").value(text));
    }

    @Test
    public void whenGetOneWithIdNotFound() throws Exception {
        long id = 4124L;

        when(toDoService.getOne(id)).thenThrow(ToDoNotFoundException.class);

        this.mockMvc.perform(get(String.format("/todos/%s", id)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").doesNotExist());

    }
}
