package models

import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import java.io.File

@DataSchema
data class Residuos(
    val mesResi: String,
    val tipoResi: String,
    val distritoResi: String, //Habrá que decidir si elegir el código del residuo o el nombre. De momento uso el nombre
    val toneladasResi: Double
)

fun loadCsvResi(csvFile: File): List<Residuos> {
    val residuos: List<Residuos> = csvFile.readLines()
        .drop(1)
        .map { it.split(";") }
        .map {
            it.map { campo -> campo.trim() }
            Residuos(
                mesResi = it[1],
                tipoResi = it[3],
                distritoResi = it[5],
                toneladasResi = punto(it[6])
            )
        }
    return residuos
}

fun punto(dato: String): Double {
    val nuevo: String = dato.replace(",", ".")
    return nuevo.toDouble()
}