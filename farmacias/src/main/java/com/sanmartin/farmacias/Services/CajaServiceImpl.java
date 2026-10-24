package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.CajaAperturaDTO;
import com.sanmartin.farmacias.Dto.CajaCierreDTO;
import com.sanmartin.farmacias.Dto.CajaDTO;
import com.sanmartin.farmacias.Entity.Caja;
import com.sanmartin.farmacias.Entity.EstadoCaja;
import com.sanmartin.farmacias.Entity.Usuario;
import com.sanmartin.farmacias.Exception.ResourceNotFoundException;
import com.sanmartin.farmacias.Repository.CajaRepository;
import com.sanmartin.farmacias.Repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CajaServiceImpl implements ICajaService{

    private final CajaRepository cajaRepository;
    private final UsuarioRepository usuarioRepository;

    public CajaServiceImpl(CajaRepository cajaRepository, UsuarioRepository usuarioRepository) {
        this.cajaRepository = cajaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<CajaDTO> listarTodo() {
        return cajaRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public Optional<CajaDTO> buscarPorId(Long id) {
        return cajaRepository.findById(id).map(this::toDto);
    }

    @Override
    public List<CajaDTO> listarAbiertas() {
        return cajaRepository.findByEstado(EstadoCaja.ABIERTA).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<CajaDTO> listarPorUsuario(Long idUsuario) {
        return cajaRepository.findByUsuarioIdUsuario(idUsuario).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public CajaDTO abrir(CajaAperturaDTO dto) {
        cajaRepository.findByUsuarioIdUsuarioAndEstado(dto.idUsuario(), EstadoCaja.ABIERTA)
                .ifPresent(c -> {
                    throw new RuntimeException("El usuario ya tiene una caja abierta");
                });

        Usuario usuario = usuarioRepository.findById(dto.idUsuario())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con ID: " + dto.idUsuario()));

        Caja caja = new Caja();
        caja.setUsuario(usuario);
        caja.setMontoInicial(dto.montoInicial());
        caja.setEstado(EstadoCaja.ABIERTA);
        caja.setFechaApertura(LocalDateTime.now());

        return toDto(cajaRepository.save(caja));
    }

    @Override
    public CajaDTO cerrar(Long idCaja, CajaCierreDTO dto) {
        Caja caja = cajaRepository.findById(idCaja)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Caja no encontrada con ID: " + idCaja));

        if (caja.getEstado() == EstadoCaja.CERRADA) {
            throw new RuntimeException("La caja ya está cerrada");
        }

        if (!caja.getUsuario().getIdUsuario().equals(dto.idUsuario())) {
            throw new RuntimeException("No puedes cerrar una caja que no es tuya");
        }

        BigDecimal diferencia = dto.montoContado().subtract(caja.getMontoInicial());

        caja.setMontoFinal(dto.montoContado());
        caja.setDiferencia(diferencia);
        caja.setFechaCierre(LocalDateTime.now());
        caja.setEstado(EstadoCaja.CERRADA);

        return toDto(cajaRepository.save(caja));
    }
    private CajaDTO toDto(Caja c) {
        return new CajaDTO(
                c.getIdCaja(),
                c.getUsuario().getIdUsuario(),
                c.getUsuario().getPersona().getNombre(),
                c.getMontoInicial(),
                c.getMontoFinal(),
                c.getDiferencia(),
                c.getEstado(),
                c.getFechaApertura(),
                c.getFechaCierre()
        );
    }
}
