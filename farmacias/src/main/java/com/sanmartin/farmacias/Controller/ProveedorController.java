package com.sanmartin.farmacias.Controller;

import com.sanmartin.farmacias.Dto.ProveedorDto;
import com.sanmartin.farmacias.Exception.ResourceNotFoundException;
import com.sanmartin.farmacias.Services.IProveedorServices;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/proveedor"})
@Tag(name = "Proveedor", description = "CRUD de los proveedores")
public class ProveedorController {
    private final IProveedorServices proveedorServices;

    public ProveedorController(IProveedorServices proveedorServices) {
        this.proveedorServices = proveedorServices;
    }

    @GetMapping
    public ResponseEntity<List<ProveedorDto>> listarTodo(){
        return ResponseEntity.ok(this.proveedorServices.listarTodo());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProveedorDto> obtener(@PathVariable Long id){
        return (ResponseEntity)this.proveedorServices.buscarPorId(id).map(ResponseEntity::ok).orElseThrow(() -> new ResourceNotFoundException("No se encuentra el Proveedor con el ID: "+id));
    }

    @PostMapping
    public ResponseEntity<ProveedorDto> registrar(@RequestBody @Valid ProveedorDto dto){
        return new ResponseEntity(this.proveedorServices.registrar(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProveedorDto> actualizar(@PathVariable Long id, @Valid @RequestBody ProveedorDto dto){
        return (ResponseEntity)this.proveedorServices.actualizar(id, dto).map(ResponseEntity::ok).orElseThrow(() -> new ResourceNotFoundException("No se pudo actualiza el registro del Proveedor con ID: "+id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        if (this.proveedorServices.eliminar(id)){
            return ResponseEntity.noContent().build();
        }else {
            throw new ResourceNotFoundException("No se pudo eliminar el registro con ID: "+id);
        }
    }
}
