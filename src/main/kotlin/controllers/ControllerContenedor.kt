package controllers

import models.*
import org.jetbrains.kotlinx.dataframe.api.cast
import org.jetbrains.kotlinx.dataframe.api.count
import org.jetbrains.kotlinx.dataframe.api.groupBy
import org.jetbrains.kotlinx.dataframe.api.toDataFrame
import java.io.File

object ControllerContenedor {

    private val fs = File.separator
    private val workingDirectory: String = System.getProperty("user.dir")
    private val pathCont = workingDirectory + fs + "data" + fs + "contenedores_varios.csv"
    private val pathResi = workingDirectory + fs + "data" + fs + "modelo_residuos_2021.csv"

    fun procesoFiltrados() {
        val cont by lazy { loadCsvCont(File(pathCont)) }
        val dfCont by lazy { cont.toDataFrame() }
        val resi by lazy { loadCsvResi(File(pathResi)) }
        val dfResi by lazy { resi.toDataFrame() }

        println(dfCont)
        println(dfResi)
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