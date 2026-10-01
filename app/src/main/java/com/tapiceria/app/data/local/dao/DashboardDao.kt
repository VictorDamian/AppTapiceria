
package com.tapiceria.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.tapiceria.app.domain.model.CotizacionPendienteDashboard
import com.tapiceria.app.domain.model.DashboardResumen
import com.tapiceria.app.domain.model.EntregaProximaDashboard
import com.tapiceria.app.domain.model.SaldoPendienteDashboard
import com.tapiceria.app.domain.model.TrabajoDashboard
import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardDao {

    /**
     * Obtiene los indicadores generales.
     */
    @Query("""
        SELECT
            (
                SELECT COUNT(*)
                FROM trabajos
                WHERE estado IN ('PENDIENTE', 'EN_PROCESO', 'TERMINADO')
            ) AS trabajosActivos,

            (
                SELECT COUNT(*)
                FROM cotizaciones
                WHERE estado = 'PENDIENTE'
                  AND (fechaVigencia IS NULL OR fechaVigencia >= :ahora)
            ) AS cotizacionesPendientes,

            (
                SELECT COUNT(*)
                FROM trabajos
                WHERE fechaEntregaEstimada >= :ahora
                  AND fechaEntregaEstimada <= :limiteEntrega
                  AND estado NOT IN ('ENTREGADO', 'CANCELADO')
            ) AS entregasProximas,

            (
                SELECT COUNT(*)
                FROM trabajos t
                WHERE t.estado NOT IN ('ENTREGADO', 'CANCELADO')
                  AND t.importeCentavos > (
                      SELECT COALESCE(SUM(p.importeCentavos), 0)
                      FROM pagos p
                      WHERE p.trabajoId = t.id
                  )
            ) AS trabajosConSaldo
    """)
    fun observarResumen(
        ahora: Long,
        limiteEntrega: Long
    ): Flow<DashboardResumen>

    /**
     * Obtiene los diez trabajos más recientes.
     */
    @Query("""
        SELECT
            t.id AS id,
            t.folio AS folio,
            c.nombre AS nombreCliente,
            t.descripcion AS descripcion,
            t.importeCentavos AS importeCentavos,
            t.estado AS estado,
            t.fechaRecepcion AS fechaRecepcion,
            t.fechaEntregaEstimada AS fechaEntregaEstimada
        FROM trabajos t
        INNER JOIN clientes c ON c.id = t.clienteId
        ORDER BY t.fechaRecepcion DESC
        LIMIT 10
    """)
    fun observarTrabajosRecientes(): Flow<List<TrabajoDashboard>>

    /**
     * Cotizaciones pendientes, sin incluir las que ya vencieron.
     */
    @Query("""
        SELECT
            co.id AS id,
            co.folio AS folio,
            cl.nombre AS nombreCliente,
            co.descripcion AS descripcion,
            co.importeCentavos AS importeCentavos,
            co.fechaCreacion AS fechaCreacion,
            co.fechaVigencia AS fechaVigencia
        FROM cotizaciones co
        INNER JOIN atenciones a ON a.id = co.atencionId
        INNER JOIN clientes cl ON cl.id = a.clienteId
        WHERE co.estado = 'PENDIENTE'
          AND (co.fechaVigencia IS NULL OR co.fechaVigencia >= :ahora)
        ORDER BY co.fechaCreacion DESC
        LIMIT 10
    """)
    fun observarCotizacionesPendientes(
        ahora: Long
    ): Flow<List<CotizacionPendienteDashboard>>

    /**
     * Entregas estimadas dentro de los próximos siete días.
     */
    @Query("""
        SELECT
            t.id AS id,
            t.folio AS folio,
            c.nombre AS nombreCliente,
            t.fechaEntregaEstimada AS fechaEntregaEstimada,
            t.estado AS estado
        FROM trabajos t
        INNER JOIN clientes c ON c.id = t.clienteId
        WHERE t.fechaEntregaEstimada >= :ahora
          AND t.fechaEntregaEstimada <= :limiteEntrega
          AND t.estado NOT IN ('ENTREGADO', 'CANCELADO')
        ORDER BY t.fechaEntregaEstimada ASC
        LIMIT 10
    """)
    fun observarEntregasProximas(
        ahora: Long,
        limiteEntrega: Long
    ): Flow<List<EntregaProximaDashboard>>

    /**
     * Lista los trabajos con saldo pendiente, de mayor a menor saldo.
     */
    @Query("""
        SELECT
            t.id AS id,
            t.folio AS folio,
            c.nombre AS nombreCliente,
            t.importeCentavos AS importeCentavos,
            COALESCE(SUM(p.importeCentavos), 0) AS totalPagadoCentavos,
            t.importeCentavos -
                COALESCE(SUM(p.importeCentavos), 0) AS saldoPendienteCentavos
        FROM trabajos t
        INNER JOIN clientes c ON c.id = t.clienteId
        LEFT JOIN pagos p ON p.trabajoId = t.id
        WHERE t.estado NOT IN ('ENTREGADO', 'CANCELADO')
        GROUP BY
            t.id, t.folio, c.nombre, t.importeCentavos
        HAVING saldoPendienteCentavos > 0
        ORDER BY saldoPendienteCentavos DESC
        LIMIT 10
    """)
    fun observarSaldosPendientes(): Flow<List<SaldoPendienteDashboard>>
}