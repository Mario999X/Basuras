package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import java.io.File

@DataSchema
@Serializable
@SerialName("Residuos")
data class Residuos(
    @XmlElement(true)
    val anioResi: String,
    @XmlElement(true)
    val mesResi: String,
    @XmlElement(true)
    val loteResi: String,
    @XmlElement(true)
    val tipoResi: String,
    @XmlElement(true)
    val codDistritoResi: String,
    @XmlElement(true)
    val nomDistritoResi: String,
    @XmlElement(true)
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