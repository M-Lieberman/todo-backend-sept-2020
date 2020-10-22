package com.techreturners.service;

import com.techreturners.model.Task;
import java.util.List;

public interface TasksService {

    List<Task> getTasks(String userId);

    void saveTask(String userId, String description, boolean completed);
}
