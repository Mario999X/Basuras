package controllers

import jetbrains.letsPlot.Stat
import jetbrains.letsPlot.export.ggsave
import jetbrains.letsPlot.geom.geomBar
import jetbrains.letsPlot.intern.Plot
import jetbrains.letsPlot.label.labs
import jetbrains.letsPlot.letsPlot
import models.*
import mu.KotlinLogging
import org.jetbrains.kotlinx.dataframe.api.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

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
        createInforme(tiempo.toString())
        logger.debug { "Tiempo: $tiempo ms" }

        val fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss"))
        logger.debug { fecha }
    }


    private fun procesoFiltrados(cont: List<Contenedores>, resi: List<Residuos>) {
        logger.debug { "Inicio de filtrado general..." }
        val dfCont by lazy { cont.toDataFrame() }
        val dfResi by lazy { resi.toDataFrame() }
        dfCont.cast<Contenedores>()

        logger.debug { "Número de contenedores de cada tipo que hay en cada distrito" }
        val numTipoContXDistrito = dfCont.groupBy { it.distritoCont.rename("Distrito") }
            .aggregate {
                count { it.tipoCont == "RESTO" } into "Restos"
                count { it.tipoCont == "PAPEL-CARTON" } into "Papel-Carton"
                count { it.tipoCont == "ORGANICA" } into "Organica"
                count { it.tipoCont == "ENVASES" } into "Envases"
                count { it.tipoCont == "VIDRIO" } into "Vidrio"
            }
        println(numTipoContXDistrito)

        logger.debug { "Media de contenedores de cada tipo por distrito" }
        val mediaTipoContXDistrito = dfCont.groupBy { it.distritoCont and it.tipoCont }
            .aggregate { mean { it.cantidadCont } into "Media" }.sortBy { it.distritoCont }
        println(mediaTipoContXDistrito)

        logger.debug { "Media de toneladas anuales de recogidas por cada tipo de basura agrupadas por distrito" }
        val mediaTonResiDistritos =
            dfResi.groupBy { it.nomDistritoResi.rename("Distrito") and it.tipoResi.rename("Tipo") }
                .aggregate { mean { it.toneladasResi } into "Media" }.sortBy { it["Distrito"] }
        println(mediaTonResiDistritos)

        logger.debug { "Máximo, mínimo, media y desviación de toneladas anuales de recogidas por cada tipo de basura agrupadas por distrito" }
        val maxToneladasDistrito =
            dfResi.groupBy { it.nomDistritoResi.rename("Distrito") and it.tipoResi.rename("Tipo") }
                .aggregate {
                    max { it.toneladasResi } into "Max"
                    min { it.toneladasResi } into "Min"
                    median { it.toneladasResi } into "Mediana"
                    std { it.toneladasResi } into "Desviación"
                }.sortBy { it["Distrito"] }
        println(maxToneladasDistrito)

        logger.debug { "Suma de las toneladas recogidas en un año por distrito" }
        val sumToneladasDistrito =
            dfResi.groupBy { it.nomDistritoResi.rename("Distrito") }
                .aggregate { sum { it.toneladasResi } into "Total" }.sortBy { it["Distrito"] }
        println(sumToneladasDistrito)

        logger.debug { "Por cada distrito obtener para cada tipo de residuo la cantidad recogida" }
        val toneladasDistrito = dfResi.groupBy { it.nomDistritoResi.rename("Distrito") and it.tipoResi.rename("Tipo") }
            .aggregate {
                sum { it.toneladasResi } into "Total"
            }.sortBy { it["Distrito"] }
        println(toneladasDistrito)

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

    private fun createInforme(tiempo: String) {
        val informe = Informe(
            UUID.randomUUID().toString(),
            LocalDateTime.now().toString(),
            "Resumen Global",
            "Proceso Exitoso",
            "$tiempo milisegundos"
        )
        Informe.writeToXmlFile(informe, File("bitacora${fs}bitacora.xml"))
    }
}
