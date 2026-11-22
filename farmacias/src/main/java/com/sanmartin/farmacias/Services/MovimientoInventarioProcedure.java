package com.sanmartin.farmacias.Services;

import com.sanmartin.farmacias.Entity.ReferenciaTipo;
import com.sanmartin.farmacias.Entity.TipoMovimientoInventario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class MovimientoInventarioProcedure {

    private final EntityManager em;

    public MovimientoInventarioProcedure(EntityManager em) {
        this.em = em;
    }


    // METODO ORIGINAL: COMPRAS Y VENTAS


    @Transactional(propagation = Propagation.MANDATORY)
    public void registrar(
            Long idLote,
            TipoMovimientoInventario tipoMovimiento,
            BigDecimal cantidad,
            BigDecimal stockAnterior,
            BigDecimal stockNuevo,
            ReferenciaTipo referenciaTipo,
            Long referenciaId,
            Long idUsuario
    ) {
        ejecutarProcedure(
                idLote, tipoMovimiento, cantidad,
                stockAnterior, stockNuevo, referenciaTipo,
                referenciaId, idUsuario, null
        );
    }

    // METODO NUEVO: CON OBSERVACIÓN


    @Transactional(propagation = Propagation.MANDATORY)
    public void registrar(
            Long idLote,
            TipoMovimientoInventario tipoMovimiento,
            BigDecimal cantidad,
            BigDecimal stockAnterior,
            BigDecimal stockNuevo,
            ReferenciaTipo referenciaTipo,
            Long referenciaId,
            Long idUsuario,
            String observacion
    ) {
        ejecutarProcedure(
                idLote, tipoMovimiento, cantidad,
                stockAnterior, stockNuevo, referenciaTipo,
                referenciaId, idUsuario, observacion
        );
    }

    // 3. EJECUTAR PROCEDURE EN ORACLE


    private void ejecutarProcedure(
            Long idLote,
            TipoMovimientoInventario tipoMovimiento,
            BigDecimal cantidad,
            BigDecimal stockAnterior,
            BigDecimal stockNuevo,
            ReferenciaTipo referenciaTipo,
            Long referenciaId,
            Long idUsuario,
            String observacion
    ) {

        StoredProcedureQuery sp =
                em.createStoredProcedureQuery(
                        "SP_REGISTRAR_MOVIMIENTO"
                );

        // Los 8 parámetros originales
        sp.registerStoredProcedureParameter(
                1, Long.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter(
                2, String.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter(
                3, BigDecimal.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter(
                4, BigDecimal.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter(
                5, BigDecimal.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter(
                6, String.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter(
                7, Long.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter(
                8, Long.class, ParameterMode.IN);

        sp.setParameter(1, idLote);
        sp.setParameter(2, tipoMovimiento.name());
        sp.setParameter(3, cantidad);
        sp.setParameter(4, stockAnterior);
        sp.setParameter(5, stockNuevo);
        sp.setParameter(6, referenciaTipo.name());
        sp.setParameter(7, referenciaId);
        sp.setParameter(8, idUsuario);

        // Noveno parámetro, solo si hay observación
        if (observacion != null) {
            sp.registerStoredProcedureParameter(
                    9, String.class, ParameterMode.IN
            );
            sp.setParameter(9, observacion);
        }

        // Ejecutar en Oracle
        sp.execute();
    }
}
