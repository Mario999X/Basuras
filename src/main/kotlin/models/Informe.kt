package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlElement
import java.io.File
import java.util.*

@Serializable
@SerialName("Informe")
data class Informe(
    @XmlElement(true)
    var id: String,
    @XmlElement(true)
    var createdAt: String,
    @XmlElement(true)
    var opcion: String,
    @XmlElement(true)
    var exito: String,
    @XmlElement(true)
    var tiempo: String
) {
    companion object
}

fun Informe.Companion.writeToXmlFile(informe: Informe, xmlFile: File) {
    println("writing Informe")
    val xml = XML { indentString = "  " }
    xmlFile.writeText(xml.encodeToString(informe))
}