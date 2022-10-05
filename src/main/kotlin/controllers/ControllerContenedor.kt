package controllers

import models.*
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.writeAsXML
import nl.adaptivity.xmlutil.xmlEncode
import org.apache.commons.csv.CSVFormat
import org.jetbrains.kotlinx.dataframe.api.*
import org.jetbrains.kotlinx.dataframe.io.toJson
import org.jetbrains.kotlinx.dataframe.io.writeCSV
import org.jetbrains.kotlinx.dataframe.io.writeJson
import java.io.File

object ControllerContenedor {

    private val fs = File.separator
    private val workingDirectory: String = System.getProperty("user.dir")
    private val pathCont = workingDirectory + fs + "data" + fs + "contenedores_varios.csv"
    private val pathResi = workingDirectory + fs + "data" + fs + "modelo_residuos_2021.csv"

    //Path destino de nuevos archivos temporal
    private val pathDestino = workingDirectory + fs + "src" + fs + "main" + fs + "resources"

    //Convertir datos de Contenedores a DataFrame
    val cont by lazy { loadCsvCont(File(pathCont)) }
    val dfCont by lazy { cont.toDataFrame() }

    //Convertir datos de Residuos a DataFrame
    val resi by lazy { loadCsvResi(File(pathResi)) }
    val dfResi by lazy { resi.toDataFrame() }

    fun parseCsv() {
        //Selección de columnas
        val nuevoCSV = dfCont.select { tipoCont and cantidadCont and distritoCont }
        //Creación CSV nuevo
        nuevoCSV.writeCSV(File(pathDestino + fs + "csvLimpio.csv"), CSVFormat.DEFAULT.withDelimiter(';'))
        //Creación de JSON
        nuevoCSV.writeJson(File(pathDestino + fs + "contenedoresJson.json"), prettyPrint = true)
        //Creación de XML
        val nuevoXml = nuevoCSV.toJson(prettyPrint = true)
        val ficheroXml = File(pathDestino + fs + "contenedoresXml.xml")
        ficheroXml.writeText(nuevoXml.xmlEncode())
    }

    fun procesoFiltrados() {
        dfCont.cast<Contenedores>()
        val numConTipo = dfCont
            //.filter { it.distritoCont == "VILLAVERDE" }
            .groupBy { it.distritoCont.rename("Distrito") }
            .aggregate {
                count { it.tipoCont == "RESTO" } into "Restos"
                count { it.tipoCont == "PAPEL-CARTON" } into "Papel-Carton"
                count { it.tipoCont == "ORGANICA" } into "Organica"
                count { it.tipoCont == "ENVASES" } into "Envases"
                count { it.tipoCont == "VIDRIO" } into "Vidrio"
            }
        println(numConTipo)
    }
}