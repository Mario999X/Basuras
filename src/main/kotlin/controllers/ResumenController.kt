package controllers

import jetbrains.letsPlot.Stat
import jetbrains.letsPlot.export.ggsave
import jetbrains.letsPlot.geom.geomBar
import jetbrains.letsPlot.intern.Plot
import jetbrains.letsPlot.label.labs
import jetbrains.letsPlot.letsPlot
import models.*
import org.jetbrains.kotlinx.dataframe.api.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.system.measureTimeMillis

object ResumenController {

    private val fs = File.separator
    private val workingDirectory: String = System.getProperty("user.dir")
    private val pathCont = workingDirectory + fs + "data" + fs + "contenedores_varios.csv"
    private val pathResi = workingDirectory + fs + "data" + fs + "modelo_residuos_2021.csv"

    //Convertir datos de Contenedores a DataFrame
    private val cont by lazy { loadCsvCont(File(pathCont)) }
    private val dfCont by lazy { cont.toDataFrame() }

    //Convertir datos de Residuos a DataFrame
    private val resi by lazy { loadCsvResi(File(pathResi)) }
    private val dfResi by lazy { resi.toDataFrame() }

    fun init() {
        val tiempo = measureTimeMillis {
            procesoFiltrados()
        }
        println("Tiempo: $tiempo ms")

        val fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss"))
        println(fecha)
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
        // Grafico barras (Total contenedores X Distrito)
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
