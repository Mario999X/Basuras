package controllers

import kotlinx.serialization.encodeToString
import models.*
import mu.KotlinLogging
import nl.adaptivity.xmlutil.serialization.XML
import org.apache.commons.csv.CSVFormat
import org.jetbrains.kotlinx.dataframe.api.select
import org.jetbrains.kotlinx.dataframe.api.toDataFrame
import org.jetbrains.kotlinx.dataframe.io.writeCSV
import org.jetbrains.kotlinx.dataframe.io.writeJson
import java.io.File
import java.time.LocalDateTime
import java.util.*
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

object ParserController {
    private val fs = File.separator
    fun init(dirOrigen: String, dirDestino: String) {
        logger.debug { "Creando archivos..." }
        val csvContenedores = dirOrigen + fs + "contenedores_varios.csv"
        val csvResiduos = dirOrigen + fs + "modelo_residuos_2021.csv"
        val destinoPath = dirDestino + fs

        //Lectura de csv
        val cont by lazy { loadCsvCont(File(csvContenedores)) }
        val resi by lazy { loadCsvResi(File(csvResiduos)) }

        val tiempo = measureTimeMillis {
            parserCsv(cont, resi, destinoPath)
        }
        createInforme(tiempo.toString())
        logger.debug { "Tiempo: $tiempo ms" }
    }

    private fun parserCsv(cont: List<Contenedores>, resi: List<Residuos>, destino: String) {
        val dfCont by lazy { cont.toDataFrame() }
        val dfResi by lazy { resi.toDataFrame() }
        //Selección de columnas
        val contNuevoCsv = dfCont.select { it.tipoCont and it.cantidadCont and it.distritoCont }
        val resiNuevoCsv = dfResi.select { it.mesResi and it.tipoResi and it.nomDistritoResi and it.toneladasResi }
        //Creación CSV nuevo
        contNuevoCsv.writeCSV(File(destino + "contenedoresCsv.csv"), CSVFormat.DEFAULT.withDelimiter(';'))
        resiNuevoCsv.writeCSV(File(destino + "residuosCsv.csv"), CSVFormat.DEFAULT.withDelimiter(';'))
        //Creación de JSON
        contNuevoCsv.writeJson(File(destino + "contenedoresJson.json"), prettyPrint = true)
        resiNuevoCsv.writeJson(File(destino + "residuosJson.json"), prettyPrint = true)
        //Creación de XML
        val xml = XML { indentString = " " }
        val contenedoresXml = File(destino + "contenedoresXML.xml")
        contenedoresXml.writeText(xml.encodeToString(cont))
        val residuosXml = File(destino + "residuosXml.xml")
        residuosXml.writeText(xml.encodeToString(resi))
    }

    private fun createInforme(tiempo: String) {
        val informe = Informe(
            UUID.randomUUID().toString(),
            LocalDateTime.now().toString(),
            "Parser de archivos CSV",
            "Proceso Exitoso",
            "$tiempo milisegundos"
        )
        Informe.writeToXmlFile(informe, File("bitacora${fs}bitacora.xml"))
    }
}