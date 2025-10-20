package com.project.demo.controller;

import com.project.demo.model.Task;
import com.project.demo.model.TaskExecution;
import com.project.demo.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // GET all tasks or a single task by ID
    @GetMapping
    public ResponseEntity<Object> getTasks(@RequestParam(name = "id", required = false) String id) {
        if (id != null && !id.isEmpty()) {
            return taskService.getTaskById(id)
                    .<ResponseEntity<Object>>map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(404).body("Task not found"));
        }
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    // Create or update a task
    @PutMapping
    public ResponseEntity<?> createTask(@RequestBody Task task) {
        try {
            return ResponseEntity.ok(taskService.createTask(task));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    // Delete a task by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable("id") String id) {
        if (taskService.getTaskById(id).isEmpty()) {
            return ResponseEntity.status(404).body("Task not found");
        }
        taskService.deleteTask(id);
        return ResponseEntity.ok("Task deleted successfully");
    }

    // Search tasks by name
    @GetMapping("/search")
    public ResponseEntity<?> findTasksByName(@RequestParam(name = "name") String name) {
        List<Task> tasks = taskService.findTasksByName(name);
        if (tasks.isEmpty()) {
            return ResponseEntity.status(404).body("No tasks found with name containing: " + name);
        }
        return ResponseEntity.ok(tasks);
    }

    // Run a task by ID
    @PutMapping("/{id}/execute")
    public ResponseEntity<?> runTask(@PathVariable("id") String id) {
        try {
            TaskExecution exec = taskService.runTask(id);
            return ResponseEntity.ok(exec);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error executing task: " + e.getMessage());
        }
    }
}
