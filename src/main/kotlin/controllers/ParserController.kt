package controllers

import kotlinx.serialization.encodeToString
import models.*
import nl.adaptivity.xmlutil.serialization.XML
import org.apache.commons.csv.CSVFormat
import org.jetbrains.kotlinx.dataframe.api.select
import org.jetbrains.kotlinx.dataframe.api.toDataFrame
import org.jetbrains.kotlinx.dataframe.io.writeCSV
import org.jetbrains.kotlinx.dataframe.io.writeJson
import java.io.File

object ParserController {

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

    fun init(){
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
}