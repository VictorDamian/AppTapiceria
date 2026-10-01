
package com.tapiceria.app.data.exportacion

import android.content.Context
import android.net.Uri
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

/**
 * Exporta las tablas de la base de datos local a archivos CSV.
 *
 * No modifica los datos almacenados en Room.
 */
class ExportacionService(
    private val context: Context
) {

    /**
     * Tablas permitidas para exportación.
     * Evitamos exponer tablas internas de SQLite o de Room.
     */
    val tablasExportables: List<String> = listOf(
        "clientes",
        "atenciones",
        "cotizaciones",
        "trabajos",
        "pagos",
        "fotos_trabajo",
        "servicios"
    )

    /**
     * Exporta una tabla a la ubicación elegida por el usuario.
     *
     * El usuario selecciona la ubicación mediante el selector de documentos
     * de Android; la aplicación no necesita permisos de almacenamiento general.
     */
    suspend fun exportarTablaCsv(
        nombreTabla: String,
        destino: Uri
    ) = withContext(Dispatchers.IO) {

        require(nombreTabla in tablasExportables) {
            "La tabla seleccionada no está permitida."
        }

        val archivoBd = context.getDatabasePath("tapiceria_database")

        require(archivoBd.exists()) {
            "No se encontró la base de datos de TapiceriaApp."
        }

        // SQLite puede leer los cambios confirmados que permanezcan en el WAL.
        val baseDatos = SQLiteDatabase.openDatabase(
            archivoBd.absolutePath,
            null,
            SQLiteDatabase.OPEN_READONLY
        )

        try {
            val cursor = baseDatos.rawQuery(
                "SELECT * FROM \"$nombreTabla\"",
                null
            )

            try {
                context.contentResolver.openOutputStream(destino)?.use { salida ->
                    // BOM para facilitar la lectura de acentos en Excel.
                    salida.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

                    BufferedWriter(
                        OutputStreamWriter(salida, StandardCharsets.UTF_8)
                    ).use { escritor ->
                        escribirEncabezados(cursor, escritor)
                        escribirFilas(cursor, escritor)
                    }
                } ?: throw IllegalStateException(
                    "No se pudo abrir el archivo de destino."
                )
            } finally {
                cursor.close()
            }
        } finally {
            baseDatos.close()
        }
    }

    /**
     * Escribe los nombres de las columnas.
     */
    private fun escribirEncabezados(
        cursor: Cursor,
        escritor: BufferedWriter
    ) {
        val columnas = (0 until cursor.columnCount).map { indice ->
            cursor.getColumnName(indice)
        }

        escritor.appendLine(columnas.joinToString(";") { escaparCsv(it) })
    }

    /**
     * Escribe los registros de la tabla.
     */
    private fun escribirFilas(
        cursor: Cursor,
        escritor: BufferedWriter
    ) {
        while (cursor.moveToNext()) {
            val valores = (0 until cursor.columnCount).map { indice ->
                if (cursor.isNull(indice)) {
                    ""
                } else {
                    cursor.getString(indice) ?: ""
                }
            }

            escritor.appendLine(valores.joinToString(";") { escaparCsv(it) })
        }
    }

    /**
     * Escapa campos CSV que contienen separadores, comillas o saltos de línea.
     */
    private fun escaparCsv(valor: String): String {
        val necesitaComillas =
            valor.contains(";") ||
                    valor.contains("\"") ||
                    valor.contains("\n") ||
                    valor.contains("\r")

        val valorSeguro = valor.replace("\"", "\"\"")

        return if (necesitaComillas) {
            "\"$valorSeguro\""
        } else {
            valorSeguro
        }
    }
}