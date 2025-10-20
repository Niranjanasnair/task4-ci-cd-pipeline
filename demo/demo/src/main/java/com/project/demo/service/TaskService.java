package com.project.demo.service;

import com.project.demo.model.Task;
import com.project.demo.model.TaskExecution;
import com.project.demo.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Optional<Task> getTaskById(String id) {
        return taskRepository.findById(id);
    }

    public Task createTask(Task task) {
        if (isUnsafeCommand(task.getCommand())) {
            throw new RuntimeException("Unsafe command detected!");
        }
        return taskRepository.save(task);
    }

    public void deleteTask(String id) {
        taskRepository.deleteById(id);
    }

    public List<Task> findTasksByName(String name) {
        return taskRepository.findByNameContainingIgnoreCase(name);
    }

    public TaskExecution runTask(String id) throws Exception {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        TaskExecution exec = new TaskExecution();
        exec.setStartTime(new Date());

        ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", task.getCommand());
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        process.waitFor();
        exec.setEndTime(new Date());
        exec.setOutput(output.toString());

        task.getTaskExecutions().add(exec);
        taskRepository.save(task);
        return exec;
    }

    private boolean isUnsafeCommand(String cmd) {
        String lower = cmd.toLowerCase();
        return lower.contains("rm ") || lower.contains("sudo") || lower.contains("shutdown") || lower.contains("reboot");
    }
}
