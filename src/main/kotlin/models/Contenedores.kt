package models

import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import java.io.File

@DataSchema
data class Contenedores(
    val tipoCont: String,
    val modeloCont: String,
    val cantidadCont: Int,
    val distritoCont: String
)

fun loadCsvCont(csvFile: File): List<Contenedores> {
    val contenedores: List<Contenedores> = csvFile.readLines()
        .drop(1)
        .map { it.split(";") }
        .map {
            it.map { campo -> campo.trim() }
            Contenedores(
                tipoCont = it[1],
                modeloCont = it[2],
                cantidadCont = it[4].toInt(),
                distritoCont = it[6]
            )
        }
    return contenedores
}