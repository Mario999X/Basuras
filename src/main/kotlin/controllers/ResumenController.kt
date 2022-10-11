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
    fun init(dirOrigen: String, dirDestino: String) {
        val csvContenedores = dirOrigen + fs + "contenedores_varios.csv"
        val csvResiduos = dirOrigen + fs + "modelo_residuos_2021.csv"
        val destinoPath = dirDestino + fs

        //Lectura de csv
        val cont by lazy { loadCsvCont(File(csvContenedores)) }
        val resi by lazy { loadCsvResi(File(csvResiduos)) }

        val tiempo = measureTimeMillis {
            procesoFiltrados(cont, resi)
        }
        println("Tiempo: $tiempo ms")

        val fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss"))
        println(fecha)
    }

    private fun procesoFiltrados(cont: List<Contenedores>, resi: List<Residuos>) {
        val dfCont by lazy { cont.toDataFrame() }
        val dfResi by lazy { resi.toDataFrame() }
        dfCont.cast<Contenedores>()

        //Numero de contenedores de cada tipo que hay en cada distrito
        val numTipoContXDistrito = dfCont.groupBy { it.distritoCont.rename("Distrito") }
            .aggregate {
                count { it.tipoCont == "RESTO" } into "Restos"
                count { it.tipoCont == "PAPEL-CARTON" } into "Papel-Carton"
                count { it.tipoCont == "ORGANICA" } into "Organica"
                count { it.tipoCont == "ENVASES" } into "Envases"
                count { it.tipoCont == "VIDRIO" } into "Vidrio"
            }
        println(numTipoContXDistrito)

        // Media de contenedores de cada tipo por distrito
        val mediaTipoContXDistrito = dfCont.groupBy { it.distritoCont and it.tipoCont }
            .aggregate { mean { it.cantidadCont } into "Media" }.sortBy { it.distritoCont }
        println(mediaTipoContXDistrito)

        //Media de toneladas anuales de recogidas por cada tipo de basura agrupadas por distrito
        val mediaTonResiDistritos =
            dfResi.groupBy { it.nomDistritoResi.rename("Distrito") and it.tipoResi.rename("Tipo") }
                .aggregate { mean { it.toneladasResi } into "Media" }.sortBy { it["Distrito"] }
        println(mediaTonResiDistritos)

        //--- GRAFICAS ---
        //Grafico barras (Total contenedores X Distrito)
        val numContenedores = dfCont.groupBy { it.distritoCont }.aggregate { count() into "contenedores" }
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
