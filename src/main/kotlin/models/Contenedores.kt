package models

import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import java.io.File

@DataSchema
data class Contenedores(
    val codIntCont: String,
    val tipoCont: String,
    val modeloCont: String,
    val descModeloCont: String,
    val cantidadCont: Int,
    val loteCont: String,
    val distritoCont: String,
    val barrioCont: String?,
    val viaCont: String,
    val nomViaCont: String,
    val numCalleCont: String,
    val coorXCont: String,
    val coorYCont: String,
    val longiCont: String,
    val latiCont: String,
    val dirCompletaCont: String
)

fun loadCsvCont(csvFile: File): List<Contenedores> {
    val contenedores: List<Contenedores> = csvFile.readLines()
        .drop(1)
        .map { it.split(";") }
        .map {
            it.map { campo -> campo.trim() }
            Contenedores(
                codIntCont = it[0],
                tipoCont = it[1],
                modeloCont = it[2],
                descModeloCont = it[3],
                cantidadCont = it[4].toInt(),
                loteCont = it[5],
                distritoCont = it[6],
                barrioCont = it[7],
                viaCont = it[8],
                nomViaCont = it[9],
                numCalleCont = it[10],
                coorXCont = it[11],
                coorYCont = it[12],
                longiCont = it[13],
                latiCont = it[14],
                dirCompletaCont = it[15]
            )
        }
    return contenedores
}