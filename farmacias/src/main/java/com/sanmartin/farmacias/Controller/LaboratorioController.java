package com.sanmartin.farmacias.Controller;

import com.sanmartin.farmacias.Dto.LaboratorioDto;
import com.sanmartin.farmacias.Exception.ResourceNotFoundException;
import com.sanmartin.farmacias.Services.ILaboratorioServices;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/laboratorio")
@Tag(
        name = "Laboratorio",
        description = "CRUD de laboratorios"
)
public class LaboratorioController {

    private final ILaboratorioServices laboratorioServices;

    public LaboratorioController(
            ILaboratorioServices laboratorioServices
    ) {
        this.laboratorioServices = laboratorioServices;
    }

    @GetMapping
    public ResponseEntity<List<LaboratorioDto>> listarTodo() {

        return ResponseEntity.ok(
                laboratorioServices.listarTodo()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<LaboratorioDto> obtener(
            @PathVariable Long id
    ) {

        LaboratorioDto laboratorio =
                laboratorioServices.buscarPorId(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "No se encuentra el Laboratorio con el ID: " + id
                                )
                        );

        return ResponseEntity.ok(laboratorio);
    }

    @PostMapping
    public ResponseEntity<LaboratorioDto> registrar(
            @RequestBody
            @Valid
            LaboratorioDto laboratorioDto
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        laboratorioServices.registrar(
                                laboratorioDto
                        )
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<LaboratorioDto> actualizar(
            @PathVariable Long id,
            @RequestBody
            @Valid
            LaboratorioDto laboratorioDto
    ) {

        LaboratorioDto laboratorioActualizado =
                laboratorioServices
                        .actualizar(id, laboratorioDto)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "No se pudo actualizar el Laboratorio con ID: " + id
                                )
                        );

        return ResponseEntity.ok(
                laboratorioActualizado
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id
    ) {

        if (!laboratorioServices.eliminar(id)) {

            throw new ResourceNotFoundException(
                    "No se pudo eliminar el Laboratorio con ID: " + id
            );
        }

        return ResponseEntity
                .noContent()
                .build();
    }
}