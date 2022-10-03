package models

import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import java.io.File

@DataSchema
data class Residuos(
    val anioResi: String,
    val mesResi: String,
    val loteResi: String,
    val tipoResi: String,
    val codDistritoResi: String,
    val nomDistritoResi: String,
    val toneladasResi: Double
)

fun loadCsvResi(csvFile: File): List<Residuos> {
    val residuos: List<Residuos> = csvFile.readLines()
        .drop(1)
        .map { it.split(";") }
        .map {
            it.map { campo -> campo.trim() }
            Residuos(
                anioResi = it[0],
                mesResi = it[1],
                loteResi = it[2],
                tipoResi = it[3],
                codDistritoResi = it[4],
                nomDistritoResi = it[5],
                toneladasResi = punto(it[6])
            )
        }
    return residuos
}

fun punto(dato: String): Double {
    val nuevo: String = dato.replace(",", ".")
    return nuevo.toDouble()
}