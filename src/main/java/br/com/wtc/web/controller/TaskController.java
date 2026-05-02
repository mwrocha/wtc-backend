package br.com.wtc.web.controller;

import br.com.wtc.web.dto.TaskRequest;
import br.com.wtc.domain.model.Task;
import br.com.wtc.domain.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // Criar tarefa
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody TaskRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        Task task = taskService.createTask(request, userDetails.getUsername());
        return ResponseEntity.status(201).body(task);
    }

    // Listar todas as tarefas do operador logado
    @GetMapping
    public ResponseEntity<List<Task>> getMyTasks(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.getTasksByOperator(userDetails.getUsername()));
    }

    // Listar tarefas de um cliente específico
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Task>> getTasksByClient(@PathVariable String clientId) {
        return ResponseEntity.ok(taskService.getTasksByClient(clientId));
    }

    // Atualizar status (drag & drop Kanban)
    @PatchMapping("/{taskId}/status")
    public ResponseEntity<Task> updateStatus(@PathVariable String taskId, @RequestBody Map<String, String> body, @AuthenticationPrincipal UserDetails userDetails) {
        String status = body.get("status");
        Task updated = taskService.updateStatus(taskId, status, userDetails.getUsername());
        return ResponseEntity.ok(updated);
    }

    // Atualizar tarefa completa
    @PutMapping("/{taskId}")
    public ResponseEntity<Task> updateTask(@PathVariable String taskId, @RequestBody TaskRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        Task updated = taskService.updateTask(taskId, request, userDetails.getUsername());
        return ResponseEntity.ok(updated);
    }

    // Deletar tarefa
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable String taskId, @AuthenticationPrincipal UserDetails userDetails) {
        taskService.deleteTask(taskId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}