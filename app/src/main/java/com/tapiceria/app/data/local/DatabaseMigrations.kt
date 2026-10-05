package com.tapiceria.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migraciones de la base de datos local.
 *
 * La migración 1 -> 2 modifica atenciones para permitir
 * que una atención exista sin cliente asociado.
 */
object DatabaseMigrations {

    /**
     * Room Database versión 1 -> 2.
     *
     * Se reconstruye la tabla porque SQLite no permite cambiar
     * directamente una columna NOT NULL a NULL mediante
     * ALTER COLUMN.
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {

        override fun migrate(
            database: SupportSQLiteDatabase
        ) {

            // Creamos temporalmente la nueva estructura.
            database.execSQL(
                """
                CREATE TABLE atenciones_new (
                    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    clienteId INTEGER,
                    tipo TEXT NOT NULL,
                    descripcion TEXT NOT NULL,
                    fechaAtencion INTEGER NOT NULL,
                    notas TEXT NOT NULL,
                    FOREIGN KEY(clienteId)
                        REFERENCES clientes(id)
                        ON DELETE RESTRICT
                )
                """.trimIndent()
            )

            // Copiamos todos los registros existentes.
            //
            // Los registros anteriores tenían clienteId obligatorio,
            // por lo que todos podrán copiarse directamente.
            database.execSQL(
                """
                INSERT INTO atenciones_new (
                    id,
                    clienteId,
                    tipo,
                    descripcion,
                    fechaAtencion,
                    notas
                )
                SELECT
                    id,
                    clienteId,
                    tipo,
                    descripcion,
                    fechaAtencion,
                    notas
                FROM atenciones
                """.trimIndent()
            )

            // Eliminamos la tabla anterior.
            database.execSQL(
                "DROP TABLE atenciones"
            )

            // Renombramos la nueva tabla.
            database.execSQL(
                "ALTER TABLE atenciones_new RENAME TO atenciones"
            )

            // Recreamos el índice utilizado por Room.
            database.execSQL(
                """
                CREATE INDEX IF NOT EXISTS
                index_atenciones_clienteId
                ON atenciones(clienteId)
                """.trimIndent()
            )
        }
    }
}