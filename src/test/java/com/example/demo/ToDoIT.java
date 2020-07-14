package com.example.demo;

import com.example.demo.dto.ToDoSaveRequest;
import com.example.demo.model.ToDoEntity;
import com.example.demo.repository.ToDoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
public class ToDoIT {

    @Autowired
    private ToDoRepository toDoRepository;

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        toDoRepository.deleteAll();
        toDoRepository.save(new ToDoEntity(1L, "First item"));
        toDoRepository.save(new ToDoEntity(2L, "Second item"));
        toDoRepository.save(new ToDoEntity(3L, "Third item"));
    }

    @Test
    public void whenGetAll_thenReturnValidResponse() throws Exception {
        this.mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].text").isString())
                .andExpect(jsonPath("$[0].completedAt").doesNotExist());
    }

    @Test
    public void whenSaveWithSaveRequestNoId_thenReturnValidResponse() throws Exception {
        String text = "Request text";
        var request = new ToDoSaveRequest();
        request.text = text;

        ObjectMapper mapper = new ObjectMapper();
        var jsonRequest = mapper.writeValueAsString(request);

        this.mockMvc
                .perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.text").value(text))
                .andExpect(jsonPath("$.completedAt").doesNotExist());

        var items = toDoRepository.findAll();
        assertEquals(4, items.size());
    }

    @Test
    public void whenSaveWithSaveRequestWithId_thenReturnValidResponse() throws Exception {
        var items = toDoRepository.findAll();
        long id = items.get(0).getId();
        String text = "Request text";
        var request = new ToDoSaveRequest();
        request.text = text;
        request.id = id;

        ObjectMapper mapper = new ObjectMapper();
        var jsonRequest = mapper.writeValueAsString(request);

        this.mockMvc
                .perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.text").value(text));

        items = toDoRepository.findAll();
        assertEquals(3, items.size());
    }

    @Test
    public void whenSaveWithId_thenReturnValidResponse() throws Exception {
        var items = toDoRepository.findAll();
        long id = items.get(0).getId();

        this.mockMvc.perform(put(String.format("/todos/%s/complete", id)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.text").isString())
                .andExpect(jsonPath("$.completedAt").exists());

        var item = toDoRepository.findById(id);
        assertTrue(item.isPresent());
        assertNotNull(item.get().getCompletedAt());
    }

    @Test
    public void whenGetOne_thenReturnValidResponse() throws Exception {
        var items = toDoRepository.findAll();
        long id = items.get(0).getId();
        this.mockMvc.perform(get("/todos/" + id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.text").isString());
    }

    @Test
    public void whenGetOneIdNotFound_thenThrow() throws Exception {
        long id = Long.MIN_VALUE;
        this.mockMvc.perform(get("/todos/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    public void whenDeleteOne_thenEntityDeleted() throws Exception {
        var items = toDoRepository.findAll();
        long id = items.get(0).getId();
        this.mockMvc.perform(delete("/todos/" + id));

        var deletedItem = this.toDoRepository.findById(id);
        assertTrue(deletedItem.isEmpty());

        items = toDoRepository.findAll();
        assertEquals(2, items.size());
    }
}
