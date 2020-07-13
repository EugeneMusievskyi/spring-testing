package com.example.demo.service;

import com.example.demo.dto.ToDoSaveRequest;
import com.example.demo.exception.ToDoNotFoundException;
import com.example.demo.model.ToDoEntity;
import com.example.demo.repository.ToDoRepository;
import com.example.demo.service.ToDoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ToDoServiceTest {

    private ToDoService toDoService;

    private ToDoRepository toDoRepository;

    @BeforeEach
    void setUp() {
        this.toDoRepository = mock(ToDoRepository.class);
        toDoService = new ToDoService(toDoRepository);
    }

    @Test
    public void whenUpsertWithIdNotFound_thenThrowNotFoundException() {
        when(toDoRepository.findById(anyLong())).thenAnswer(i -> Optional.empty());
        var toDoEntity = new ToDoEntity(0L, "New Item");
        when(toDoRepository.save(ArgumentMatchers.any(ToDoEntity.class))).thenAnswer(i -> {
            ToDoEntity arg = i.getArgument(0, ToDoEntity.class);
            Long id = arg.getId();
            if (id != null) {
                if (!id.equals(toDoEntity.getId()))
                    return new ToDoEntity(id, arg.getText());
                toDoEntity.setText(arg.getText());
                return toDoEntity; //return valid result only if we get valid id
            } else {
                return new ToDoEntity(1L, arg.getText());
            }
        });

        var toDoSaveRequest = new ToDoSaveRequest();
        toDoSaveRequest.id = 0L;
        toDoSaveRequest.text = "Some text";
        assertThrows(ToDoNotFoundException.class, () -> toDoService.upsert(toDoSaveRequest));
    }

    @Test
    public void whenCompleteToDoIdNotFound_thenThrowNotFoundException() {
        assertThrows(ToDoNotFoundException.class, () -> toDoService.completeToDo(1L));
    }
}
