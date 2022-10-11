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

object DistritoController {
    private val fs = File.separator

    fun init(distritoMain: String, dirOrigen: String, dirDestino: String) {
        val csvContenedores = dirOrigen + fs + "contenedores_varios.csv"
        val csvResiduos = dirOrigen + fs + "modelo_residuos_2021.csv"
        val destinoPath = dirDestino + fs

        //Lectura de csv
        val cont by lazy { loadCsvCont(File(csvContenedores)) }
        val resi by lazy { loadCsvResi(File(csvResiduos)) }

        val distrito = distritoMain.uppercase()
        val tiempo = measureTimeMillis {
            println("Distrito elegido: $distrito")
            procesoFiltrados(distrito, cont, resi)
        }
        println("Tiempo de filtrados: $tiempo ms")

        val fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss"))
        println(fecha)
    }

    private fun procesoFiltrados(distrito: String, cont: List<Contenedores>, resi: List<Residuos>) {
        val dfCont by lazy { cont.toDataFrame() }
        val dfResi by lazy { resi.toDataFrame() }
        dfCont.cast<Contenedores>()

        // Numero de contenedores de cada tipo, distrito especifico
        val numTipoContXDistrito =
            dfCont.filter { it.distritoCont == distrito }
                .aggregate {
                    count { it.tipoCont == "RESTO" } into "Restos"
                    count { it.tipoCont == "PAPEL-CARTON" } into "Papel-Carton"
                    count { it.tipoCont == "ORGANICA" } into "Organica"
                    count { it.tipoCont == "ENVASES" } into "Envases"
                    count { it.tipoCont == "VIDRIO" } into "Vidrio"
                }
        println(numTipoContXDistrito)

        // Total toneladas recogidas en x Distrito por residuo
        val totalTonResiDistrito =
            dfResi.filter { it.nomDistritoResi == distrito }.groupBy { it.tipoResi.rename("Tipo") }
                .aggregate {
                    sum { it.toneladasResi } into "TotalToneladas"
                }.sortBy { it["TotalToneladas"].desc() }
        println(totalTonResiDistrito)

        // GRAFICAS
        //Grafico barras (Total Toneladas X Residuo) en x Distrito
        val fig: Plot = letsPlot(data = totalTonResiDistrito.toMap()) + geomBar(
            stat = Stat.identity, alpha = 0.8
        ) {
            x = "Tipo"; y = "TotalToneladas"
        } + labs(
            x = "Residuos", y = "Toneladas", title = "Total Toneladas por Residuo en $distrito"
        )
        ggsave(fig, "ToneladasPorResiduo.png")
    }
}