package com.example.demo.dto.mapper;

import com.example.demo.dto.ToDoResponse;
import com.example.demo.model.ToDoEntity;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;

public class ToDoEntityToResponseMapperTest {
    @Test
    public void whenMapWithEntity_thenReturnResponse() {
        var time = ZonedDateTime.now(ZoneOffset.UTC);
        var toDoEntity = new ToDoEntity(1L, "New item", time);

        var expectedResponse = new ToDoResponse();
        expectedResponse.id = toDoEntity.getId();
        expectedResponse.text = toDoEntity.getText();
        expectedResponse.completedAt = toDoEntity.getCompletedAt();

        var result = ToDoEntityToResponseMapper.map(toDoEntity);

        assertThat(result, samePropertyValuesAs(expectedResponse));
    }
}
