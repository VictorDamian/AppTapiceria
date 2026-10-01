
package com.tapiceria.app.data.respaldo

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Crea respaldos ZIP y prepara restauraciones de la base de datos
 * y las fotografías almacenadas por la aplicación.
 */
class RespaldoService(
    private val context: Context
) {
    private val nombreBaseDatos = "tapiceria_database"
    private val nombreCarpetaFotos = "photos"
    private val nombreCarpetaPendiente = "restore_pending"

    private val archivoBaseDatos: File
        get() = context.getDatabasePath(nombreBaseDatos)

    private val carpetaFotos: File
        get() = File(context.filesDir, nombreCarpetaFotos)

    private val carpetaPendiente: File
        get() = File(context.filesDir, nombreCarpetaPendiente)

    /**
     * Genera un ZIP en la ubicación elegida por el usuario.
     *
     * Es importante no registrar información durante esta operación.
     */
    suspend fun crearRespaldo(destino: Uri) = withContext(Dispatchers.IO) {
        require(archivoBaseDatos.exists()) {
            "No se encontró la base de datos."
        }

        // Intenta escribir los cambios pendientes de SQLite en el archivo principal.
        // La aplicación debe estar inactiva respecto a escrituras mientras se respalda.
        ejecutarCheckpoint()

        context.contentResolver.openOutputStream(destino)?.use { salida ->
            ZipOutputStream(salida).use { zip ->
                // Se incluye una marca de versión para validar futuros respaldos.
                agregarTexto(
                    zip,
                    "manifest.txt",
                    "TAPICERIA_BACKUP=1\n" +
                            "DATABASE=$nombreBaseDatos\n" +
                            "CREATED_AT=${System.currentTimeMillis()}\n"
                )

                agregarArchivo(
                    zip = zip,
                    archivo = archivoBaseDatos,
                    rutaZip = "database/$nombreBaseDatos"
                )

                // Las fotografías se almacenan como archivos, no dentro de Room.
                if (carpetaFotos.exists()) {
                    agregarCarpeta(
                        zip = zip,
                        carpetaRaiz = carpetaFotos,
                        carpetaActual = carpetaFotos,
                        prefijoZip = "photos"
                    )
                }
            }
        } ?: throw IOException("No se pudo crear el archivo ZIP.")
    }

    /**
     * Extrae y valida el ZIP en una carpeta temporal.
     *
     * La base de datos activa no se modifica en este paso.
     * La aplicación del respaldo se realiza al iniciar la app.
     */
    suspend fun prepararRestauracion(origen: Uri) =
        withContext(Dispatchers.IO) {
            limpiarCarpeta(carpetaPendiente)

            if (!carpetaPendiente.mkdirs()) {
                throw IOException("No se pudo crear la carpeta temporal.")
            }

            try {
                val entrada = context.contentResolver.openInputStream(origen)
                    ?: throw IOException("No se pudo abrir el archivo seleccionado.")

                entrada.use { flujo ->
                    ZipInputStream(flujo).use { zip ->
                        var entradaZip = zip.nextEntry

                        while (entradaZip != null) {
                            extraerEntradaSegura(zip, entradaZip.name)
                            zip.closeEntry()
                            entradaZip = zip.nextEntry
                        }
                    }
                }

                val manifiesto = File(carpetaPendiente, "manifest.txt")
                val baseDatosPreparada = File(
                    carpetaPendiente,
                    "database/$nombreBaseDatos"
                )

                require(manifiesto.exists()) {
                    "El ZIP no contiene el manifiesto del respaldo."
                }

                val contenidoManifiesto = manifiesto.readText(Charsets.UTF_8)

                require(contenidoManifiesto.lines().contains("TAPICERIA_BACKUP=1")) {
                    "El archivo no es un respaldo válido de TapiceriaApp."
                }

                require(baseDatosPreparada.exists() && baseDatosPreparada.length() > 0L) {
                    "El respaldo no contiene una base de datos válida."
                }

                // Verifica que SQLite pueda abrir la base de datos restaurada.
                verificarBaseDatos(baseDatosPreparada)

                // La marca se escribe únicamente cuando todo se ha validado.
                File(carpetaPendiente, "READY").writeText("OK")
            } catch (ex: Exception) {
                limpiarCarpeta(carpetaPendiente)
                throw ex
            }
        }

    /**
     * Ejecuta un checkpoint para reducir datos pendientes en el archivo WAL.
     */
    private fun ejecutarCheckpoint() {
        val baseDatos = SQLiteDatabase.openDatabase(
            archivoBaseDatos.absolutePath,
            null,
            SQLiteDatabase.OPEN_READWRITE
        )

        try {
            baseDatos.rawQuery("PRAGMA wal_checkpoint(TRUNCATE)", null).use {
                if (it.moveToFirst() && it.getInt(0) != 0) {
                    throw IOException(
                        "No se pudo completar el checkpoint de SQLite. " +
                                "Intenta nuevamente cuando no haya operaciones activas."
                    )
                }
            }
        } finally {
            baseDatos.close()
        }
    }

    /**
     * Comprueba que el archivo restaurado sea una base de datos SQLite legible.
     */
    private fun verificarBaseDatos(archivo: File) {
        val db = SQLiteDatabase.openDatabase(
            archivo.absolutePath,
            null,
            SQLiteDatabase.OPEN_READONLY
        )

        try {
            db.rawQuery("PRAGMA quick_check", null).use { cursor ->
                if (!cursor.moveToFirst() || cursor.getString(0) != "ok") {
                    throw IOException("La base de datos del respaldo está dañada.")
                }
            }

            // Evita restaurar un SQLite ajeno o un archivo incompleto.
            db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type = 'table' " +
                        "AND name = 'clientes'",
                null
            ).use { cursor ->
                if (!cursor.moveToFirst()) {
                    throw IOException(
                        "La base de datos no corresponde a TapiceriaApp."
                    )
                }
            }
        } finally {
            db.close()
        }
    }

    /**
     * Agrega un archivo al ZIP.
     */
    private fun agregarArchivo(
        zip: ZipOutputStream,
        archivo: File,
        rutaZip: String
    ) {
        FileInputStream(archivo).use { entrada ->
            zip.putNextEntry(ZipEntry(rutaZip))
            entrada.copyTo(zip)
            zip.closeEntry()
        }
    }

    /**
     * Agrega un texto al ZIP.
     */
    private fun agregarTexto(
        zip: ZipOutputStream,
        rutaZip: String,
        contenido: String
    ) {
        zip.putNextEntry(ZipEntry(rutaZip))
        zip.write(contenido.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    /**
     * Agrega recursivamente las fotografías.
     */
    private fun agregarCarpeta(
        zip: ZipOutputStream,
        carpetaRaiz: File,
        carpetaActual: File,
        prefijoZip: String
    ) {
        carpetaActual.listFiles()?.forEach { archivo ->
            val rutaRelativa = archivo.relativeTo(carpetaRaiz)
                .invariantSeparatorsPath

            val rutaZip = "$prefijoZip/$rutaRelativa"

            if (archivo.isDirectory) {
                agregarCarpeta(zip, carpetaRaiz, archivo, prefijoZip)
            } else {
                agregarArchivo(zip, archivo, rutaZip)
            }
        }
    }

    /**
     * Extrae solo las rutas permitidas y bloquea rutas como ../../archivo.
     */
    private fun extraerEntradaSegura(
        zip: ZipInputStream,
        nombreEntrada: String
    ) {
        val nombreNormalizado = nombreEntrada.replace('\\', '/')

        val rutaPermitida =
            nombreNormalizado == "manifest.txt" ||
                    nombreNormalizado == "database/$nombreBaseDatos" ||
                    nombreNormalizado.startsWith("photos/")

        require(rutaPermitida) {
            "El ZIP contiene una ruta no permitida: $nombreEntrada"
        }

        val destino = File(carpetaPendiente, nombreNormalizado)
        val raizCanonica = carpetaPendiente.canonicalFile
        val destinoCanonico = destino.canonicalFile

        require(
            destinoCanonico.path.startsWith(raizCanonica.path + File.separator) ||
                    destinoCanonico == raizCanonica
        ) {
            "El ZIP contiene una ruta insegura."
        }

        if (nombreNormalizado.endsWith("/")) {
            if (!destino.exists() && !destino.mkdirs()) {
                throw IOException("No se pudo crear una carpeta del respaldo.")
            }
            return
        }

        destino.parentFile?.let { carpeta ->
            if (!carpeta.exists() && !carpeta.mkdirs()) {
                throw IOException("No se pudo crear una carpeta temporal.")
            }
        }

        FileOutputStream(destino).use { salida ->
            zip.copyTo(salida)
        }
    }

    /**
     * Elimina recursivamente un directorio y su contenido.
     */
    private fun limpiarCarpeta(carpeta: File) {
        if (!carpeta.exists()) return

        carpeta.walkBottomUp().forEach { archivo ->
            if (!archivo.delete()) {
                throw IOException(
                    "No se pudo limpiar un archivo temporal: ${archivo.name}"
                )
            }
        }
    }
}