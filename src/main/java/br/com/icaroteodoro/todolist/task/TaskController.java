package br.com.icaroteodoro.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.icaroteodoro.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository repository;


    @PostMapping
    public ResponseEntity create(@RequestBody TaskModel task, HttpServletRequest request){
        var idUser = request.getAttribute("idUser");
        task.setIdUser((UUID) idUser);


        //data atual
        var curentDate = LocalDateTime.now();
        if(curentDate.isAfter(task.getStartAt()) || curentDate.isAfter(task.getEndAt())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de inicio / data de termino devem ser maiores que a data atual!");
        }
        if(task.getStartAt().isAfter(task.getEndAt())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de inicio deve ser menor que a data de termino!");
        }
        
        TaskModel taskCreated = this.repository.save(task);
        return ResponseEntity.status(HttpStatus.OK).body(taskCreated);
    }

    @GetMapping
    public List<TaskModel> findByIdUser(HttpServletRequest request){
        var idUser = request.getAttribute("idUser");
        var tasks = this.repository.findByIdUser((UUID) idUser);
        return tasks;
    }


    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, @PathVariable UUID id, HttpServletRequest request) {
        var task = this.repository.findById(id).orElse(null);

        if(task == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tarefa nao encontrada");
        }

        var idUser = request.getAttribute("idUser");

        if(!task.getIdUser().equals(idUser)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("O usuario nao tem permissao para alterar essa task");
        }   

        Utils.copyNonNullProperties(taskModel, task);
        var taskUpdated = this.repository.save(task);
        return  ResponseEntity.status(HttpStatus.OK).body(taskUpdated);
    }

}
