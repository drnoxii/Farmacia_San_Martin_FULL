package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.LaboratorioDto;
import com.sanmartin.farmacias.Entity.Laboratorio;
import com.sanmartin.farmacias.Repository.LaboratorioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class LaboratorioServicesImpl implements ILaboratorioServices {

    private final LaboratorioRepository laboratorioRepository;

    public LaboratorioServicesImpl(
            LaboratorioRepository laboratorioRepository
    ) {
        this.laboratorioRepository = laboratorioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LaboratorioDto> listarTodo() {

        return laboratorioRepository
                .findAll()
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LaboratorioDto> buscarPorId(Long id) {

        return laboratorioRepository
                .findById(id)
                .map(this::convertToDto);
    }

    @Override
    @Transactional
    public LaboratorioDto registrar(LaboratorioDto laboratorioDto) {

        Laboratorio laboratorio = new Laboratorio();

        laboratorio.setNombreLaboratorio(
                laboratorioDto.nombreLaboratorio()
        );

        laboratorio.setPaisOrigenLab(
                laboratorioDto.paisOrigenLab()
        );

        return convertToDto(
                laboratorioRepository.save(laboratorio)
        );
    }

    @Override
    @Transactional
    public Optional<LaboratorioDto> actualizar(
            Long id,
            LaboratorioDto laboratorioDto
    ) {

        return laboratorioRepository.findById(id)
                .map(laboratorio -> {

                    laboratorio.setNombreLaboratorio(
                            laboratorioDto.nombreLaboratorio()
                    );

                    laboratorio.setPaisOrigenLab(
                            laboratorioDto.paisOrigenLab()
                    );

                    return convertToDto(
                            laboratorioRepository.save(laboratorio)
                    );
                });
    }

    @Override
    @Transactional
    public boolean eliminar(Long id) {

        if (!laboratorioRepository.existsById(id)) {
            return false;
        }

        laboratorioRepository.deleteById(id);

        return true;
    }

    private LaboratorioDto convertToDto(
            Laboratorio laboratorio
    ) {

        return new LaboratorioDto(
                laboratorio.getIdLaboratorio(),
                laboratorio.getNombreLaboratorio(),
                laboratorio.getPaisOrigenLab()
        );
    }
}