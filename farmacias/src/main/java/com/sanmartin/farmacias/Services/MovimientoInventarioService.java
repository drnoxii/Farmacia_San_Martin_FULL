package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.MovimientoInventarioResponseDTO;
import com.sanmartin.farmacias.Entity.Lote;
import com.sanmartin.farmacias.Entity.MovimientoInventario;
import com.sanmartin.farmacias.Entity.Producto;
import com.sanmartin.farmacias.Entity.Usuario;
import com.sanmartin.farmacias.Repository.MovimientoInventarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovimientoInventarioService {

    private final MovimientoInventarioRepository movimientoRepository;

    public MovimientoInventarioService(
            MovimientoInventarioRepository movimientoRepository) {
        this.movimientoRepository = movimientoRepository;
    }

    // ==========================================
    // CONSULTAR KARDEX CON PAGINACIÓN
    // ==========================================

    @Transactional(readOnly = true)
    public Page<MovimientoInventarioResponseDTO> listar(
            int pagina,
            int tamano) {

        Pageable pageable = PageRequest.of(pagina, tamano);

        return movimientoRepository
                .findAllByOrderByFechaDescIdMovimientoDesc(pageable)
                .map(this::toDto);
    }

    // ==========================================
    // CONVERTIR ENTIDAD A DTO
    // ==========================================

    private MovimientoInventarioResponseDTO toDto(
            MovimientoInventario movimiento) {

        Lote lote = movimiento.getLote();
        Producto producto = lote.getProducto();
        Usuario usuario = movimiento.getUsuario();

        return new MovimientoInventarioResponseDTO(

                movimiento.getIdMovimiento(),

                // Producto
                producto.getIdProducto(),
                producto.getCodigo(),
                producto.getNombreProducto(),

                // Lote
                lote.getIdLote(),
                lote.getNumeroLote(),

                // Movimiento
                movimiento.getTipoMovimiento().name(),
                movimiento.getCantidad(),
                movimiento.getStockAnterior(),
                movimiento.getStockNuevo(),

                // Referencia
                movimiento.getReferenciaTipo() != null
                        ? movimiento.getReferenciaTipo().name()
                        : null,
                movimiento.getReferenciaId(),

                // Usuario responsable
                usuario.getIdUsuario(),
                usuario.getPersona().getNombre(),

                // Auditoría
                movimiento.getFecha(),
                movimiento.getObservacion()
        );
    }
}
