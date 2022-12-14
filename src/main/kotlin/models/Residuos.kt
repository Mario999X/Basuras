package models

/**
 * @author Mario Resa y Sebastian Mendoza
 */
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import java.io.File
import java.text.Normalizer

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

/**
 * loadCsvResi() Funcion que lee el CSV facilitado y lo guarda en una lista de tipo Residuos
 *
 * @param csvFile Parametro de tipo File donde se indicara el CSV que se va a leer
 * @return Variable de tipo Lista de Residuos obtenida en la lectura del CSV
 */
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
                nomDistritoResi = igualarString(it[5]),
                toneladasResi = punto(it[6])
            )
        }
    return residuos
}

/**
 * punto() Funcion que cambia las comas en puntos para poder realizar los calculos de las filtraciones
 *
 * @param dato Variable de tipo String que se le aplica el cambio
 * @return Variable de tipo Double con el que se puede hacer calculos
 */
fun punto(dato: String): Double {
    val nuevo: String = dato.replace(",", ".")
    return nuevo.toDouble()
}

/**
 * igualarString() Funcion que "normaliza" el String de la lectura del CSV para poder trabajar en el listado
 *
 * @param dato Variable de tip String al que se le hara la normalizacion
 * @return Variable normalizada y transformada en mayusculas para su uso en el filtrado
 */
fun igualarString(dato: String): String {
    var nuevo: String = Normalizer.normalize(dato, Normalizer.Form.NFD).replace("[^\\p{ASCII}]".toRegex(), "")
    if (dato.contains(" - ")){
        nuevo = Normalizer.normalize(dato, Normalizer.Form.NFD).replace(" - ", "-")
    }

    return nuevo.uppercase()
}