package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.LoteDTO;
import com.sanmartin.farmacias.Entity.EstadoLote;
import com.sanmartin.farmacias.Entity.Lote;
import com.sanmartin.farmacias.Repository.LoteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LoteServiceImpl implements ILoteService{

    private final LoteRepository loteRepository;

    public LoteServiceImpl(LoteRepository loteRepository) {
        this.loteRepository = loteRepository;
    }

    @Override
    public List<LoteDTO> listarTodo() {
        return loteRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public Optional<LoteDTO> buscarPorId(Long id) {
        return loteRepository.findById(id).map(this::toDTO);
    }

    @Override
    public List<LoteDTO> listarPorProducto(Long idProducto) {
        return loteRepository.findByProductoIdProducto(idProducto).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public List<LoteDTO> listarActivos() {
        return loteRepository.findByEstado(EstadoLote.ACTIVO).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public List<LoteDTO> listarPorVencer(int dias) {
        LocalDate fecha = LocalDate.now().plusDays(dias);
        return loteRepository.findPorVencerAntesDe(fecha).stream()
                .map(this::toDTO)
                .toList();
    }

    private LoteDTO toDTO(Lote l) {
        return new LoteDTO(
                l.getIdLote(),
                l.getProducto().getIdProducto(),
                l.getProducto().getNombreProducto(),
                l.getNumeroLote(),
                l.getStockLote(),
                l.getFechaVencimiento(),
                l.getEstado()
        );
    }
}
