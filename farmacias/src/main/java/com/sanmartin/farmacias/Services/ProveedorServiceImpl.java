package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.ProveedorDto;
import com.sanmartin.farmacias.Entity.EstadoGeneral;
import com.sanmartin.farmacias.Entity.Proveedor;
import com.sanmartin.farmacias.Repository.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProveedorServiceImpl implements IProveedorServices{

    private final ProveedorRepository proveedorRepository;

    public ProveedorServiceImpl(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    @Override
    public List<ProveedorDto> listarTodo() {
        return proveedorRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public Optional<ProveedorDto> buscarPorId(Long id) {
        return proveedorRepository.findById(id).map(this::toDto);
    }

    @Override
    @Transactional
    public ProveedorDto registrar(ProveedorDto dto) {

        if (proveedorRepository.existsByRuc(dto.ruc())) {
            throw new RuntimeException("Ya existe un proveedor con ese RUC");
        }

        Proveedor p = new Proveedor();
        p.setRazonSocial(dto.razonSocial());
        p.setRuc(dto.ruc());
        p.setTelefono(dto.telefono());
        p.setCorreo(dto.correo());
        p.setDireccion(dto.direccion());
        p.setEstado(EstadoGeneral.ACTIVO);

        return toDto(proveedorRepository.save(p));
    }

    @Override
    @Transactional
    public Optional<ProveedorDto> actualizar(Long id, ProveedorDto dto) {

        Optional<Proveedor> opt = proveedorRepository.findById(id);
        if (opt.isEmpty()) {
            return Optional.empty();
        }

        Proveedor p = opt.get();

        if (!p.getRuc().equals(dto.ruc()) && proveedorRepository.existsByRuc(dto.ruc())) {
            throw new RuntimeException("Ya existe otro proveedor con ese RUC");
        }

        p.setRazonSocial(dto.razonSocial());
        p.setRuc(dto.ruc());
        p.setTelefono(dto.telefono());
        p.setCorreo(dto.correo());
        p.setDireccion(dto.direccion());

        return Optional.of(toDto(proveedorRepository.save(p)));
    }

    @Override
    @Transactional
    public boolean eliminar(Long id) {
        if (!proveedorRepository.existsById(id)) {
            return false;
        }
        proveedorRepository.deleteById(id);
        return true;
    }

    private ProveedorDto toDto(Proveedor p) {
        return new ProveedorDto(
                p.getIdProveedor(),
                p.getRazonSocial(),
                p.getRuc(),
                p.getTelefono(),
                p.getCorreo(),
                p.getDireccion(),
                p.getEstado()
        );
    }
}
