package models

/**
 * @author Mario Resa y Sebastián Mendoza
 */
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import mu.KotlinLogging
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlElement
import java.io.File
import java.util.*

private val logger = KotlinLogging.logger {}

@Serializable
@SerialName("Informe")
data class Informe(
    var id: String,
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

/**
 * WriteToXmlFile Función que crea el informe en formato XML
 *
 * @param informe Parámetro de tipo Informe que almacena la información a guardar
 * @param xmlFile Parámetro de tipo File donde se guardará el informe
 */
fun Informe.Companion.writeToXmlFile(informe: Informe, xmlFile: File) {
    logger.debug { "Escribiendo informe..." }
    val xml = XML { indentString = " " }
    xmlFile.appendText(xml.encodeToString(informe))
    xmlFile.appendText("\n")
    logger.debug { "Informe realizado con éxito" }
}