package controllers

import jetbrains.letsPlot.Stat
import jetbrains.letsPlot.export.ggsave
import jetbrains.letsPlot.geom.geomBar
import jetbrains.letsPlot.intern.Plot
import jetbrains.letsPlot.label.labs
import jetbrains.letsPlot.letsPlot
import kotlinx.serialization.encodeToString
import models.*
import nl.adaptivity.xmlutil.serialization.XML
import org.apache.commons.csv.CSVFormat
import org.jetbrains.kotlinx.dataframe.api.*
import org.jetbrains.kotlinx.dataframe.io.writeCSV
import org.jetbrains.kotlinx.dataframe.io.writeJson
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.system.measureTimeMillis

object ResumenController {

    private val fs = File.separator
    private val workingDirectory: String = System.getProperty("user.dir")
    private val pathCont = workingDirectory + fs + "data" + fs + "contenedores_varios.csv"
    private val pathResi = workingDirectory + fs + "data" + fs + "modelo_residuos_2021.csv"

    //Path destino de nuevos archivos temporal
    private val pathDestino = workingDirectory + fs + "src" + fs + "main" + fs + "resources"

    //Convertir datos de Contenedores a DataFrame
    private val cont by lazy { loadCsvCont(File(pathCont)) }
    private val dfCont by lazy { cont.toDataFrame() }

    //Convertir datos de Residuos a DataFrame
    private val resi by lazy { loadCsvResi(File(pathResi)) }
    private val dfResi by lazy { resi.toDataFrame() }

    fun init() {
        val tiempo = measureTimeMillis {
            parseCsv()
            procesoFiltrados()
        }
        println("Tiempo: $tiempo ms")

        val fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss"))
        println(fecha)
    }

    fun parseCsv() {
        //Selección de columnas
        val contNuevoCsv = dfCont.select { it.tipoCont and it.cantidadCont and it.distritoCont }
        val resiNuevoCsv = dfResi.select { it.mesResi and it.tipoResi and it.nomDistritoResi and it.toneladasResi }
        //Creación CSV nuevo
        contNuevoCsv.writeCSV(File(pathDestino + fs + "contenedoresCsv.csv"), CSVFormat.DEFAULT.withDelimiter(';'))
        resiNuevoCsv.writeCSV(File(pathDestino + fs + "residuosCsv.csv"), CSVFormat.DEFAULT.withDelimiter(';'))
        //Creación de JSON
        contNuevoCsv.writeJson(File(pathDestino + fs + "contenedoresJson.json"), prettyPrint = true)
        resiNuevoCsv.writeJson(File(pathDestino + fs + "residuosJson.json"), prettyPrint = true)
        //Creación de XML
        val xml = XML { indentString = " " }
        val contenedoresXml = File(pathDestino + fs + "contenedoresXML.xml")
        contenedoresXml.writeText(xml.encodeToString(cont))
        val residuosXml = File(pathDestino + fs + "residuosXml.xml")
        residuosXml.writeText(xml.encodeToString(resi))
    }

    fun procesoFiltrados() {
        dfCont.cast<Contenedores>()
        val numTipoContXDistrito = dfCont.groupBy { it.distritoCont.rename("Distrito") }
            .aggregate {
                count { it.tipoCont == "RESTO" } into "Restos"
                count { it.tipoCont == "PAPEL-CARTON" } into "Papel-Carton"
                count { it.tipoCont == "ORGANICA" } into "Organica"
                count { it.tipoCont == "ENVASES" } into "Envases"
                count { it.tipoCont == "VIDRIO" } into "Vidrio"
            }
        println(numTipoContXDistrito)

        // Media de contenedores de cada tipo
        val mediaTipoContXDistrito = numTipoContXDistrito.mean()
        println(mediaTipoContXDistrito)

        val numContenedores = dfCont.groupBy { it.distritoCont }.aggregate { count() into "contenedores" }
        // Gráfico barras (Total contenedores X Distrito)
        val fig: Plot = letsPlot(data = numContenedores.toMap()) + geomBar(
            stat = Stat.identity, alpha = 0.8
        ) {
            x = "distritoCont"; y = "contenedores"
        } + labs(
            x = "Distrito", y = "Contenedores", title = "Total contenedores por distrito"
        )
        ggsave(fig, "ContenedoresPorDistrito.png")

    }


}
