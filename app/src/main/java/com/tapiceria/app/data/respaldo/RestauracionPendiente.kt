
package com.tapiceria.app.data.respaldo

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.io.IOException

/**
 * Aplica un respaldo previamente validado.
 *
 * La restauración se ejecuta al iniciar la aplicación, antes de que Room
 * abra la base de datos. Esto evita reemplazar una base de datos en uso.
 */
object RestauracionPendiente {

    private const val NOMBRE_BASE_DATOS = "tapiceria_database"
    private const val NOMBRE_CARPETA_RESTAURACION = "restore_pending"
    private const val NOMBRE_CARPETA_FOTOS = "photos"
    private const val NOMBRE_BASE_DATOS_ANTERIOR = "old_database"
    private const val NOMBRE_FOTOS_ANTERIORES = "old_photos"

    /**
     * Busca una restauración preparada y la aplica si existe.
     *
     * Si ocurre un error, intenta recuperar la base de datos y las fotos
     * anteriores para evitar dejar la aplicación en un estado inconsistente.
     */
    @Synchronized
    fun aplicarSiExiste(context: Context) {
        val carpetaRestauracion = File(
            context.filesDir,
            NOMBRE_CARPETA_RESTAURACION
        )

        // Sin el marcador READY no existe una restauración validada.
        val marcadorListo = File(carpetaRestauracion, "READY")
        if (!marcadorListo.isFile) {
            return
        }

        val carpetaBaseDatosPreparada = File(
            carpetaRestauracion,
            "database"
        )

        val baseDatosPreparada = File(
            carpetaBaseDatosPreparada,
            NOMBRE_BASE_DATOS
        )

        val baseDatosActual = context.getDatabasePath(NOMBRE_BASE_DATOS)
        val carpetaBaseDatosActual = baseDatosActual.parentFile
            ?: throw IOException("No se pudo determinar la carpeta de la base de datos.")

        val respaldoBaseDatosAnterior = File(
            carpetaRestauracion,
            NOMBRE_BASE_DATOS_ANTERIOR
        )

        val carpetaFotosActuales = File(
            context.filesDir,
            NOMBRE_CARPETA_FOTOS
        )

        val carpetaFotosPreparadas = File(
            carpetaRestauracion,
            NOMBRE_CARPETA_FOTOS
        )

        val respaldoFotosAnterior = File(
            carpetaRestauracion,
            NOMBRE_FOTOS_ANTERIORES
        )

        // Se registran los cambios realizados para poder revertirlos.
        var baseDatosAnteriorMovida = false
        var nuevaBaseDatosInstalada = false
        var fotosAnterioresMovidas = false
        var nuevasFotosInstaladas = false

        try {
            // Comprueba que la base de datos preparada exista y sea válida.
            if (!baseDatosPreparada.isFile || baseDatosPreparada.length() == 0L) {
                throw IOException("La base de datos del respaldo no existe o está vacía.")
            }

            verificarBaseDatos(baseDatosPreparada)

            // Garantiza que exista la carpeta donde se encuentra SQLite.
            if (!carpetaBaseDatosActual.exists() &&
                !carpetaBaseDatosActual.mkdirs()
            ) {
                throw IOException("No se pudo crear la carpeta de la base de datos.")
            }

            // Prepara una copia temporal de la base de datos nueva.
            val baseDatosTemporal = File(
                carpetaBaseDatosActual,
                "$NOMBRE_BASE_DATOS.restore_tmp"
            )

            eliminarArchivoAuxiliar(baseDatosTemporal)

            baseDatosPreparada.copyTo(
                target = baseDatosTemporal,
                overwrite = true
            )

            // Valida la copia temporal antes de reemplazar la base actual.
            verificarBaseDatos(baseDatosTemporal)

            // Elimina respaldos de intentos anteriores, si existen.
            eliminarArchivoAuxiliar(respaldoBaseDatosAnterior)
            eliminarArchivoAuxiliar(File("${respaldoBaseDatosAnterior.path}-wal"))
            eliminarArchivoAuxiliar(File("${respaldoBaseDatosAnterior.path}-shm"))
            eliminarArchivoAuxiliar(File("${respaldoBaseDatosAnterior.path}-journal"))

            // Guarda la base de datos actual antes de instalar la nueva.
            if (baseDatosActual.exists()) {
                if (!baseDatosActual.renameTo(respaldoBaseDatosAnterior)) {
                    throw IOException("No se pudo guardar la base de datos anterior.")
                }

                baseDatosAnteriorMovida = true

                /*
                 * Conserva los archivos auxiliares junto con la base anterior.
                 * Este bloque va después de mover la base principal, para que
                 * el bloque de recuperación sepa que debe restaurarla si falla.
                 */
                moverArchivoAuxiliar(
                    File("${baseDatosActual.path}-wal"),
                    File("${respaldoBaseDatosAnterior.path}-wal")
                )

                moverArchivoAuxiliar(
                    File("${baseDatosActual.path}-shm"),
                    File("${respaldoBaseDatosAnterior.path}-shm")
                )

                moverArchivoAuxiliar(
                    File("${baseDatosActual.path}-journal"),
                    File("${respaldoBaseDatosAnterior.path}-journal")
                )
            }

            // Instala la nueva base de datos.
            if (!baseDatosTemporal.renameTo(baseDatosActual)) {
                throw IOException("No se pudo instalar la nueva base de datos.")
            }

            nuevaBaseDatosInstalada = true

            // Prepara la carpeta de fotos y conserva las fotografías anteriores.
            if (carpetaFotosActuales.exists()) {
                eliminarCarpeta(respaldoFotosAnterior)

                if (!carpetaFotosActuales.renameTo(respaldoFotosAnterior)) {
                    throw IOException("No se pudo guardar la carpeta de fotos anterior.")
                }

                fotosAnterioresMovidas = true
            }

            // Instala las fotografías incluidas en el respaldo.
            if (carpetaFotosPreparadas.exists()) {
                if (!carpetaFotosPreparadas.renameTo(carpetaFotosActuales)) {
                    throw IOException("No se pudieron instalar las fotografías del respaldo.")
                }

                nuevasFotosInstaladas = true
            } else {
                // Si el respaldo no contiene fotos, deja una carpeta vacía.
                if (!carpetaFotosActuales.exists() &&
                    !carpetaFotosActuales.mkdirs()
                ) {
                    throw IOException("No se pudo crear la carpeta de fotografías.")
                }
            }

            // La restauración terminó correctamente: limpia los archivos anteriores.
            eliminarArchivoAuxiliar(respaldoBaseDatosAnterior)
            eliminarArchivoAuxiliar(File("${respaldoBaseDatosAnterior.path}-wal"))
            eliminarArchivoAuxiliar(File("${respaldoBaseDatosAnterior.path}-shm"))
            eliminarArchivoAuxiliar(File("${respaldoBaseDatosAnterior.path}-journal"))
            eliminarCarpeta(respaldoFotosAnterior)
            eliminarArchivoAuxiliar(baseDatosTemporal)

            // Quita el marcador para no repetir la restauración en el siguiente inicio.
            eliminarCarpeta(carpetaRestauracion)

        } catch (ex: Exception) {
            /*
             * Si algo falla, intenta regresar tanto la base de datos como
             * las fotografías a su estado anterior.
             */
            try {
                // Retira la base nueva si llegó a instalarse.
                if (nuevaBaseDatosInstalada) {
                    eliminarArchivoAuxiliar(baseDatosActual)
                }

                // Recupera la base de datos anterior.
                if (baseDatosAnteriorMovida &&
                    respaldoBaseDatosAnterior.exists()
                ) {
                    if (baseDatosActual.exists()) {
                        eliminarArchivoAuxiliar(baseDatosActual)
                    }

                    if (!respaldoBaseDatosAnterior.renameTo(baseDatosActual)) {
                        throw IOException("No se pudo recuperar la base de datos anterior.")
                    }

                    // Recupera los archivos auxiliares que se conservaron.
                    moverArchivoAuxiliar(
                        File("${respaldoBaseDatosAnterior.path}-wal"),
                        File("${baseDatosActual.path}-wal")
                    )

                    moverArchivoAuxiliar(
                        File("${respaldoBaseDatosAnterior.path}-shm"),
                        File("${baseDatosActual.path}-shm")
                    )

                    moverArchivoAuxiliar(
                        File("${respaldoBaseDatosAnterior.path}-journal"),
                        File("${baseDatosActual.path}-journal")
                    )
                }

                // Retira las fotos nuevas si llegaron a instalarse.
                if (nuevasFotosInstaladas) {
                    eliminarCarpeta(carpetaFotosActuales)
                }

                // Recupera la carpeta de fotografías anterior.
                if (fotosAnterioresMovidas &&
                    respaldoFotosAnterior.exists()
                ) {
                    if (carpetaFotosActuales.exists()) {
                        eliminarCarpeta(carpetaFotosActuales)
                    }

                    if (!respaldoFotosAnterior.renameTo(carpetaFotosActuales)) {
                        throw IOException("No se pudieron recuperar las fotografías anteriores.")
                    }
                }

                // Elimina cualquier archivo temporal restante.
                eliminarArchivoAuxiliar(
                    File(
                        carpetaBaseDatosActual,
                        "$NOMBRE_BASE_DATOS.restore_tmp"
                    )
                )

            } catch (errorRecuperacion: Exception) {
                // Conserva el error original y registra el problema de recuperación.
                ex.addSuppressed(errorRecuperacion)
            }

            /*
             * No se elimina restore_pending aquí.
             * Si la restauración falla, se conserva para investigar o reintentar.
             */
            throw IOException(
                "No se pudo completar la restauración del respaldo.",
                ex
            )
        }
    }

    /**
     * Comprueba que SQLite pueda abrir la base de datos y que quick_check
     * no reporte daños.
     */
    private fun verificarBaseDatos(archivo: File) {
        var baseDatos: SQLiteDatabase? = null

        try {
            baseDatos = SQLiteDatabase.openDatabase(
                archivo.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            )

            baseDatos.rawQuery("PRAGMA quick_check", null).use { cursor ->
                if (!cursor.moveToFirst() ||
                    !cursor.getString(0).equals("ok", ignoreCase = true)
                ) {
                    throw IOException("La base de datos no pasó la comprobación de integridad.")
                }
            }

            // Comprueba que exista una tabla esencial de la aplicación.
            baseDatos.rawQuery(
                """
                SELECT name
                FROM sqlite_master
                WHERE type = 'table'
                  AND name = 'clientes'
                """.trimIndent(),
                null
            ).use { cursor ->
                if (!cursor.moveToFirst()) {
                    throw IOException("El respaldo no contiene la tabla clientes.")
                }
            }

        } finally {
            // Siempre cierra la conexión de validación.
            baseDatos?.close()
        }
    }

    /**
     * Mueve un archivo auxiliar de SQLite si existe.
     */
    private fun moverArchivoAuxiliar(origen: File, destino: File) {
        if (!origen.exists()) {
            return
        }

        if (destino.exists() && !destino.delete()) {
            throw IOException("No se pudo reemplazar ${destino.name}.")
        }

        if (!origen.renameTo(destino)) {
            throw IOException("No se pudo mover ${origen.name}.")
        }
    }

    /**
     * Elimina un archivo si existe.
     */
    private fun eliminarArchivoAuxiliar(archivo: File) {
        if (archivo.exists() && !archivo.delete()) {
            throw IOException("No se pudo eliminar ${archivo.name}.")
        }
    }

    /**
     * Elimina una carpeta y todo su contenido de forma recursiva.
     */
    private fun eliminarCarpeta(carpeta: File) {
        if (carpeta.exists() && !carpeta.deleteRecursively()) {
            throw IOException("No se pudo eliminar la carpeta ${carpeta.name}.")
        }
    }
}