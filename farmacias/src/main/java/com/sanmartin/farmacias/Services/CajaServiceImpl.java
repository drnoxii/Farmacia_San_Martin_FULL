package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Dto.CajaAperturaDTO;
import com.sanmartin.farmacias.Dto.CajaCierreDTO;
import com.sanmartin.farmacias.Dto.CajaDTO;
import com.sanmartin.farmacias.Dto.CajaResumenDTO;
import com.sanmartin.farmacias.Dto.CajaVentaDTO;
import com.sanmartin.farmacias.Dto.CajaVentaDetalleDTO;
import com.sanmartin.farmacias.Entity.Caja;
import com.sanmartin.farmacias.Entity.DetalleVenta;
import com.sanmartin.farmacias.Entity.EstadoCaja;
import com.sanmartin.farmacias.Entity.EstadoVenta;
import com.sanmartin.farmacias.Entity.MetodoPago;
import com.sanmartin.farmacias.Entity.Usuario;
import com.sanmartin.farmacias.Entity.Venta;
import com.sanmartin.farmacias.Exception.ResourceNotFoundException;
import com.sanmartin.farmacias.Repository.CajaRepository;
import com.sanmartin.farmacias.Repository.UsuarioRepository;
import com.sanmartin.farmacias.Repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class CajaServiceImpl implements ICajaService {

    private final CajaRepository cajaRepository;
    private final UsuarioRepository usuarioRepository;
    private final VentaRepository ventaRepository;   // NUEVO: para leer las ventas de cada caja

    // Spring inyecta automáticamente los 3 repositorios al crear el servicio.
    public CajaServiceImpl(CajaRepository cajaRepository,
                           UsuarioRepository usuarioRepository,
                           VentaRepository ventaRepository) {
        this.cajaRepository = cajaRepository;
        this.usuarioRepository = usuarioRepository;
        this.ventaRepository = ventaRepository;
    }

    // =====================================================================
    //  CONSULTAS BÁSICAS
    // =====================================================================

    /**
     * QUÉ HACE: devuelve todas las cajas registradas (abiertas y cerradas).
     * POR QUÉ: sirve para reportes o administración. Se convierten a DTO para
     *          no exponer las entidades JPA directamente.
     */
    @Override
    public List<CajaDTO> listarTodo() {
        return cajaRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * QUÉ HACE: busca una caja por su ID.
     * POR QUÉ: devuelve Optional para que el controlador decida qué hacer
     *          (responder 404) cuando no exista.
     */
    @Override
    public Optional<CajaDTO> buscarPorId(Long id) {
        return cajaRepository.findById(id).map(this::toDto);
    }

    /**
     * QUÉ HACE: lista solo las cajas con estado ABIERTA.
     * POR QUÉ: el administrador puede ver qué cajeros tienen caja activa.
     */
    @Override
    public List<CajaDTO> listarAbiertas() {
        return cajaRepository.findByEstado(EstadoCaja.ABIERTA).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * QUÉ HACE: lista todas las cajas de un usuario (su historial).
     * POR QUÉ: alimenta la tabla "Historial de cajas" y permite al frontend
     *          encontrar cuál de ellas está abierta.
     */
    @Override
    public List<CajaDTO> listarPorUsuario(Long idUsuario) {
        return cajaRepository.findByUsuarioIdUsuario(idUsuario).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * QUÉ HACE: busca la caja ABIERTA de un usuario, si la tiene.
     * POR QUÉ: un usuario solo puede tener una caja abierta a la vez; este
     *          método permite saber si ya tiene una.
     */
    @Override
    public Optional<CajaDTO> buscarCajaAbiertaPorUsuario(Long idUsuario) {
        return cajaRepository.findByUsuarioIdUsuarioAndEstado(idUsuario, EstadoCaja.ABIERTA)
                .map(this::toDto);
    }

    // =====================================================================
    //  APERTURA Y CIERRE
    // =====================================================================

    /**
     * QUÉ HACE: abre una caja nueva para un usuario con un monto inicial.
     * POR QUÉ: sin caja abierta no se pueden registrar ventas. Se valida que
     *          no tenga otra abierta para evitar dinero repartido en dos cajas.
     */
    @Override
    public CajaDTO abrir(CajaAperturaDTO dto) {

        // 1. Regla de negocio: una sola caja abierta por usuario.
        cajaRepository.findByUsuarioIdUsuarioAndEstado(dto.idUsuario(), EstadoCaja.ABIERTA)
                .ifPresent(c -> {
                    throw new RuntimeException("El usuario ya tiene una caja abierta");
                });

        // 2. El usuario debe existir; si no, responde 404.
        Usuario usuario = usuarioRepository.findById(dto.idUsuario())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con ID: " + dto.idUsuario()));

        // 3. Crear la caja con estado ABIERTA y la hora actual.
        Caja caja = new Caja();
        caja.setUsuario(usuario);
        caja.setMontoInicial(dto.montoInicial());   // sencillo con el que arranca el cajón
        caja.setEstado(EstadoCaja.ABIERTA);
        caja.setFechaApertura(LocalDateTime.now());

        return toDto(cajaRepository.save(caja));
    }

    /**
     * QUÉ HACE: cierra la caja guardando lo que el cajero contó y la diferencia.
     * POR QUÉ (corrección): antes la diferencia era "contado - montoInicial" e
     *          ignoraba las ventas, así que una caja con ventas siempre
     *          "sobraba". Ahora se compara contra el efectivo ESPERADO
     *          (monto inicial + ventas en efectivo).
     */
    @Override
    public CajaDTO cerrar(Long idCaja, CajaCierreDTO dto) {

        // 1. La caja debe existir.
        Caja caja = cajaRepository.findById(idCaja)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Caja no encontrada con ID: " + idCaja));

        // 2. No se puede cerrar dos veces la misma caja.
        if (caja.getEstado() == EstadoCaja.CERRADA) {
            throw new RuntimeException("La caja ya está cerrada");
        }

        // 3. Seguridad: solo el dueño de la caja puede cerrarla.
        if (!caja.getUsuario().getIdUsuario().equals(dto.idUsuario())) {
            throw new RuntimeException("No puedes cerrar una caja que no es tuya");
        }

        // 4. Efectivo que DEBERÍA haber en el cajón:
        //    monto inicial + ventas pagadas en efectivo.
        //    Yape/Plin/Tarjeta no cuentan porque ese dinero no está en el cajón.
        BigDecimal ventasEfectivo = sumarPorMetodo(
                ventaRepository.findByCajaIdCaja(idCaja), MetodoPago.EFECTIVO);
        BigDecimal esperado = caja.getMontoInicial().add(ventasEfectivo);

        // 5. Diferencia: positivo = sobrante, negativo = faltante, cero = cuadró.
        BigDecimal diferencia = dto.montoContado().subtract(esperado);

        // 6. Guardar el cierre.
        caja.setMontoFinal(dto.montoContado());
        caja.setDiferencia(diferencia);
        caja.setFechaCierre(LocalDateTime.now());
        caja.setEstado(EstadoCaja.CERRADA);

        return toDto(cajaRepository.save(caja));
    }

    // =====================================================================
    //  RESUMEN (lo que muestra la pantalla "Gestión de Caja")
    // =====================================================================

    /**
     * QUÉ HACE: arma el resumen completo de una caja: totales por método de
     *           pago, efectivo esperado y la lista de ventas.
     * POR QUÉ: la pantalla necesita todo junto; calcularlo en el backend evita
     *          errores de redondeo y mantiene una sola fuente de verdad del dinero.
     * @Transactional: las ventas cargan cliente, usuario y detalles de forma LAZY;
     *          sin una transacción abierta daría LazyInitializationException.
     */
    @Override
    @Transactional(readOnly = true)
    public CajaResumenDTO resumen(Long idCaja) {

        // 1. Buscar la caja; si no existe devolvemos 404.
        Caja caja = cajaRepository.findById(idCaja)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Caja no encontrada con ID: " + idCaja));

        // 2. Traer todas las ventas hechas en ESTA caja.
        List<Venta> ventas = ventaRepository.findByCajaIdCaja(idCaja);

        // 3. Sumar por método de pago (solo ventas EMITIDA).
        BigDecimal efectivo = sumarPorMetodo(ventas, MetodoPago.EFECTIVO);
        BigDecimal yape     = sumarPorMetodo(ventas, MetodoPago.YAPE);
        BigDecimal plin     = sumarPorMetodo(ventas, MetodoPago.PLIN);
        BigDecimal tarjeta  = sumarPorMetodo(ventas, MetodoPago.TARJETA);

        // 4. Total vendido = efectivo + todo lo digital.
        BigDecimal totalVendido = efectivo.add(yape).add(plin).add(tarjeta);

        // 5. Efectivo esperado = lo que debería haber físicamente en el cajón.
        BigDecimal esperado = caja.getMontoInicial().add(efectivo);

        // 6. Cantidad de ventas válidas (las anuladas no cuentan).
        long cantidad = ventas.stream()
                .filter(v -> v.getEstado() == EstadoVenta.EMITIDA)
                .count();

        // 7. Convertir las ventas a DTO, de la más reciente a la más antigua.
        List<CajaVentaDTO> ventasDto = ventas.stream()
                .sorted(Comparator.comparing(Venta::getFechaVenta).reversed())
                .map(this::toVentaDto)
                .toList();

        // 8. Armar la respuesta. El orden debe coincidir con el record CajaResumenDTO.
        return new CajaResumenDTO(
                caja.getIdCaja(),
                caja.getUsuario().getPersona().getNombre(),
                caja.getEstado(),
                caja.getFechaApertura(),
                caja.getFechaCierre(),
                caja.getMontoInicial(),
                efectivo, yape, plin, tarjeta,
                totalVendido,
                cantidad,
                esperado,
                caja.getMontoFinal(),
                caja.getDiferencia(),
                ventasDto
        );
    }

    // =====================================================================
    //  MÉTODOS AUXILIARES PRIVADOS
    // =====================================================================

    /**
     * QUÉ HACE: suma el total de las ventas EMITIDA que usaron un método de pago.
     * POR QUÉ: se reutiliza en resumen() y en cerrar() para que ambos calculen
     *          el dinero con la MISMA regla. Las ANULADAS se excluyen porque ese
     *          dinero no ingresó realmente.
     */
    private BigDecimal sumarPorMetodo(List<Venta> ventas, MetodoPago metodo) {
        return ventas.stream()
                .filter(v -> v.getEstado() == EstadoVenta.EMITIDA)   // ignora anuladas
                .filter(v -> v.getMetodoPago() == metodo)            // solo el método pedido
                .map(Venta::getTotal)                                // toma el total de cada venta
                .reduce(BigDecimal.ZERO, BigDecimal::add);           // empieza en 0 y va sumando
    }

    /**
     * QUÉ HACE: transforma la entidad Venta en el DTO que consume Angular.
     * POR QUÉ: no se exponen las entidades JPA directamente (tienen relaciones
     *          LAZY y ciclos que romperían el JSON).
     */
    private CajaVentaDTO toVentaDto(Venta v) {

        // Si la venta no tiene cliente (venta rápida), mostramos "Cliente general".
        String cliente = (v.getCliente() != null)
                ? v.getCliente().getPersona().getNombre()
                : "Cliente general";

        List<CajaVentaDetalleDTO> detalles = v.getDetalles().stream()
                .map(this::toDetalleDto)
                .toList();

        return new CajaVentaDTO(
                v.getNumeroVenta(),
                v.getFechaVenta(),
                cliente,
                v.getUsuario().getPersona().getNombre(),                                   // vendedor
                v.getTipoComprobante() != null ? v.getTipoComprobante().name() : "-",      // BOLETA / FACTURA
                v.getMetodoPago().name(),                                                  // EFECTIVO / YAPE / ...
                v.getTotal(),
                v.getEstado().name(),                                                      // EMITIDA / ANULADA
                detalles
        );
    }

    /**
     * QUÉ HACE: transforma un DetalleVenta (una línea de producto) en DTO.
     * POR QUÉ: Angular muestra producto, lote, cantidad y precio al expandir
     *          una venta.
     */
    private CajaVentaDetalleDTO toDetalleDto(DetalleVenta d) {
        return new CajaVentaDetalleDTO(
                d.getLote().getProducto().getNombreProducto(),
                d.getLote().getNumeroLote(),
                d.getCantidad(),
                d.getPrecioUnitario(),
                // Subtotal calculado (precio x cantidad) por si la columna de BD viene nula.
                d.getPrecioUnitario().multiply(d.getCantidad()),
                d.getEstado() != null ? d.getEstado().name() : "V"   // V = vendido, D = devuelto
        );
    }

    /**
     * QUÉ HACE: convierte la entidad Caja en el DTO simple CajaDTO.
     * POR QUÉ: es la forma en que se envían las cajas al frontend en las
     *          listas y en abrir/cerrar, sin exponer la entidad.
     */
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