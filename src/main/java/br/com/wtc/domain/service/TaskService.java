package br.com.wtc.domain.service;

import br.com.wtc.web.dto.TaskRequest;
import br.com.wtc.domain.model.Task;
import br.com.wtc.domain.repository.TaskRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task createTask(TaskRequest request, String operatorId) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setClientId(request.getClientId());
        task.setClientName(request.getClientName());
        task.setOperatorId(operatorId);
        task.setCategory(request.getCategory() != null ? request.getCategory() : "OTHER");
        task.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        task.setStatus(request.getStatus() != null ? request.getStatus() : "PENDING");
        task.setMessageRef(request.getMessageRef());
        task.setDueDate(request.getDueDate());
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    public List<Task> getTasksByOperator(String operatorId) {
        return taskRepository.findByOperatorIdOrderByCreatedAtDesc(operatorId);
    }

    public List<Task> getTasksByClient(String clientId) {
        return taskRepository.findByClientIdOrderByCreatedAtDesc(clientId);
    }

    public Task updateStatus(String taskId, String status, String operatorId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));
        if (!task.getOperatorId().equals(operatorId)) {
            throw new RuntimeException("Sem permissão para atualizar esta tarefa");
        }
        task.setStatus(status);
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    public Task updateTask(String taskId, TaskRequest request, String operatorId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));
        if (!task.getOperatorId().equals(operatorId)) {
            throw new RuntimeException("Sem permissão");
        }
        if (request.getTitle()       != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getCategory()    != null) task.setCategory(request.getCategory());
        if (request.getPriority()    != null) task.setPriority(request.getPriority());
        if (request.getStatus()      != null) task.setStatus(request.getStatus());
        if (request.getDueDate()     != null) task.setDueDate(request.getDueDate());
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    public void deleteTask(String taskId, String operatorId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));
        if (!task.getOperatorId().equals(operatorId)) {
            throw new RuntimeException("Sem permissão");
        }
        taskRepository.delete(task);
    }
}