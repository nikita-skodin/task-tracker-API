package com.skodin.controllers;

import com.skodin.DTO.TaskDTO;
import com.skodin.exceptions.BadRequestException;
import com.skodin.exceptions.NotFoundException;
import com.skodin.models.ProjectEntity;
import com.skodin.models.TaskEntity;
import com.skodin.models.TaskStateEntity;
import com.skodin.services.ProjectService;
import com.skodin.services.TaskService;
import com.skodin.services.TaskStateService;
import com.skodin.util.ModelMapper;
import com.skodin.util.ProjectTaskStateTuple;
import com.skodin.validators.TaskValidator;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Validated
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/projects/{project_id}/task-states/{task-states_id}/tasks")
public class TaskController extends MainController {

    TaskService taskService;
    ProjectService projectService;
    TaskStateService taskStateService;
    ModelMapper modelMapper;

    TaskValidator taskValidator;

    public static final String GET_ALL_TASKS = "/get";
    public static final String ADD_NEW_TASK = "/create";
    public static final String GET_TASK_BY_ID = "/get/{task_id}";
    public static final String UPDATE_TASK_BY_ID = "/update/{task_id}";
    public static final String DELETE_TASK_BY_ID = "/delete/{task_id}";

    @GetMapping(GET_ALL_TASKS)
    @PreAuthorize("@projectSecurityExpression.checkUserProjectAccess(#projectId)")
    public ResponseEntity<List<TaskDTO>> getTasks(
            @PathVariable("project_id") Long projectId,
            @PathVariable("task-states_id") Long taskStateId) {

        ProjectTaskStateTuple tuple = checkTaskStateInProjectOrThrowEx(projectId, taskStateId, null);

        List<TaskEntity> taskEntities = tuple.getTaskState().getTaskEntities();

        return ResponseEntity
                .ok()
                .body(taskEntities.stream()
                        .map(modelMapper::getTaskDTO).collect(Collectors.toList()));

    }

    @GetMapping(GET_TASK_BY_ID)
    @PreAuthorize("@projectSecurityExpression.checkUserProjectAccess(#projectId)")
    public ResponseEntity<TaskDTO> getTaskById(
            @PathVariable("project_id") Long projectId,
            @PathVariable("task-states_id") Long taskStateId,
            @PathVariable("task_id") Long taskId) {
        checkTaskStateInProjectOrThrowEx(projectId, taskStateId, taskId);

        TaskEntity taskEntity = taskService.findById(taskId);

        return ResponseEntity
                .ok()
                .body(modelMapper.getTaskDTO(taskEntity));
    }

    @SneakyThrows
    @PostMapping(ADD_NEW_TASK)
    @PreAuthorize("@projectSecurityExpression.checkUserProjectAccess(#projectId)")
    public ResponseEntity<TaskDTO> createTask(
            @PathVariable("project_id") Long projectId,
            @PathVariable("task-states_id") Long taskStateId,
            @Valid @RequestBody TaskDTO taskDTO,
            BindingResult bindingResult) {
        checkTaskStateInProjectOrThrowEx(projectId, taskStateId, null);

        taskDTO.setTaskStateId(taskStateId);
        TaskEntity task = modelMapper.getTask(taskDTO);

        taskValidator.validate(task, bindingResult);
        checkBindingResult(bindingResult);

        TaskEntity taskEntity = taskService.saveAndFlush(task);

        return ResponseEntity
                .created(new URI(String.format("/api/projects/%d/task-states/%d/tasks/%d",
                        projectId, taskStateId, taskEntity.getId())))
                .body(modelMapper.getTaskDTO(taskEntity));

    }

    @PatchMapping(UPDATE_TASK_BY_ID)
    @PreAuthorize("@projectSecurityExpression.checkUserProjectAccess(#projectId)")
    public ResponseEntity<TaskDTO> updateTaskById(
            @PathVariable("project_id") Long projectId,
            @PathVariable("task-states_id") Long taskStateId,
            @PathVariable("task_id") Long taskId,
            @Valid@RequestBody TaskDTO taskDTO,
            BindingResult bindingResult) {
        if (taskDTO.getId() != null && !taskDTO.getId().equals(taskId)) {
            throw new BadRequestException("Id in DTO and in url must be the same");
        }

        taskDTO.setId(taskId);
        taskDTO.setTaskStateId(taskStateId);
        TaskEntity task = modelMapper.getTask(taskDTO);

        checkTaskStateInProjectOrThrowEx(projectId, taskStateId, taskId);
        taskValidator.validate(task, bindingResult);
        checkBindingResult(bindingResult);

        TaskEntity updated = taskService.update(taskId, task);

        return ResponseEntity
                .ok()
                .body(modelMapper.getTaskDTO(updated));
    }

    @DeleteMapping(DELETE_TASK_BY_ID)
    @PreAuthorize("@projectSecurityExpression.checkUserProjectAccess(#projectId)")
    public ResponseEntity<HttpStatus> deleteTaskById(
            @PathVariable("project_id") Long projectId,
            @PathVariable("task-states_id") Long taskStateId,
            @PathVariable("task_id") Long taskId) {
        checkTaskStateInProjectOrThrowEx(projectId, taskStateId, taskId);

        taskService.deleteById(taskId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private ProjectTaskStateTuple checkTaskStateInProjectOrThrowEx(Long projectId, Long taskStateId, Long taskId) {
        ProjectEntity project = projectService.findById(projectId);
        TaskStateEntity taskState = taskStateService.findById(taskStateId);

        if (!project.getTaskStateEntities().contains(taskState)) {
            throw new NotFoundException(
                    String.format("There is no Task State with id %d in Project with id %d",
                            taskStateId, projectId));
        }

        if (taskId != null) {

            TaskEntity taskEntity = taskService.findById(taskId);

            if (!taskState.getTaskEntities().contains(taskEntity)) {
                throw new NotFoundException(
                        String.format("There is no Task with id %d in Task State with id %d",
                                taskId, taskStateId));
            }
        }

        return new ProjectTaskStateTuple(project, taskState);
    }

}
