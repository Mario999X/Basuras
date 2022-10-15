package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import java.io.File

@DataSchema
@Serializable
@SerialName("Contenedores")
data class Contenedores(
    @XmlElement(true)
    val codIntCont: String,
    @XmlElement(true)
    val tipoCont: String,
    @XmlElement(true)
    val modeloCont: String,
    @XmlElement(true)
    val descModeloCont: String,
    @XmlElement(true)
    val cantidadCont: Int,
    @XmlElement(true)
    val loteCont: String,
    @XmlElement(true)
    val distritoCont: String,
    @XmlElement(true)
    val barrioCont: String?,
    @XmlElement(true)
    val viaCont: String,
    @XmlElement(true)
    val nomViaCont: String,
    @XmlElement(true)
    val numCalleCont: String,
    @XmlElement(true)
    val coorXCont: String,
    @XmlElement(true)
    val coorYCont: String,
    @XmlElement(true)
    val longiCont: String,
    @XmlElement(true)
    val latiCont: String,
    @XmlElement(true)
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
                distritoCont = arreglarEspacios(it[6]),
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

fun arreglarEspacios(dato: String): String {
    val nuevo = dato.replace("\u00a0".toRegex(), " ")
    return nuevo.uppercase()
}