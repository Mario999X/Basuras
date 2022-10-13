package controllers

import jetbrains.letsPlot.Stat
import jetbrains.letsPlot.export.ggsave
import jetbrains.letsPlot.geom.geomBar
import jetbrains.letsPlot.intern.Plot
import jetbrains.letsPlot.label.labs
import jetbrains.letsPlot.letsPlot
import models.*
import mu.KotlinLogging
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

object ResumenController {
    private val fs = File.separator

    // Filtrados
    private lateinit var numTipoContXDistrito: DataFrame<Contenedores>
    private lateinit var mediaTipoContXDistrito: DataFrame<Contenedores>
    private lateinit var mediaTonResiDistritos: DataFrame<Residuos>
    private lateinit var maxToneladasDistrito: DataFrame<Residuos>
    private lateinit var sumToneladasDistrito: DataFrame<Residuos>
    private lateinit var toneladasDistrito: DataFrame<Residuos>

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

        createHmtl(destinoPath, tiempo.toString(), fecha)
        logger.debug { "Resumen HTML realizado" }
    }

    private fun procesoFiltrados(cont: List<Contenedores>, resi: List<Residuos>) {
        logger.debug { "Inicio de filtrado general..." }
        val dfCont by lazy { cont.toDataFrame() }
        val dfResi by lazy { resi.toDataFrame() }

        // FILTRADOS
        logger.debug { "Número de contenedores de cada tipo que hay en cada distrito" }
        numTipoContXDistrito = dfCont.groupBy { it.distritoCont.rename("Distrito") }
            .aggregate {
                count { it.tipoCont == "RESTO" } into "Restos"
                count { it.tipoCont == "PAPEL-CARTON" } into "Papel-Carton"
                count { it.tipoCont == "ORGANICA" } into "Organica"
                count { it.tipoCont == "ENVASES" } into "Envases"
                count { it.tipoCont == "VIDRIO" } into "Vidrio"
            }

        logger.debug { "Media de contenedores de cada tipo por distrito" }
        mediaTipoContXDistrito = dfCont.groupBy { it.distritoCont and it.tipoCont }
            .aggregate { mean { it.cantidadCont } into "Media" }.sortBy { it.distritoCont }

        logger.debug { "Media de toneladas anuales de recogidas por cada tipo de basura agrupadas por distrito" }
        mediaTonResiDistritos =
            dfResi.groupBy { it.nomDistritoResi.rename("Distrito") and it.tipoResi.rename("Tipo") }
                .aggregate { mean { it.toneladasResi } into "Media" }.sortBy { it["Distrito"] }

        logger.debug { "Máximo, mínimo, media y desviación de toneladas anuales de recogidas por cada tipo de basura agrupadas por distrito" }
        maxToneladasDistrito =
            dfResi.groupBy { it.nomDistritoResi.rename("Distrito") and it.tipoResi.rename("Tipo") }
                .aggregate {
                    max { it.toneladasResi } into "Max"
                    min { it.toneladasResi } into "Min"
                    median { it.toneladasResi } into "Mediana"
                    std { it.toneladasResi } into "Desviación"
                }.sortBy { it["Distrito"] }

        logger.debug { "Suma de las toneladas recogidas en un año por distrito" }
        sumToneladasDistrito =
            dfResi.groupBy { it.nomDistritoResi.rename("Distrito") }
                .aggregate { sum { it.toneladasResi } into "Total Toneladas" }.sortBy { it["Distrito"] }

        logger.debug { "Por cada distrito obtener para cada tipo de residuo la cantidad recogida" }
        toneladasDistrito = dfResi.groupBy { it.nomDistritoResi.rename("Distrito") and it.tipoResi.rename("Tipo") }
            .aggregate {
                sum { it.toneladasResi } into "Total Toneladas"
            }.sortBy { it["Distrito"] }

        //--- GRAFICAS ---
        //(Total contenedores X Distrito)
        val numContenedores = dfCont.groupBy { it.distritoCont }.aggregate { count() into "contenedores" }
        val fig: Plot = letsPlot(data = numContenedores.toMap()) + geomBar(
            stat = Stat.identity, alpha = 0.8
        ) {
            x = "distritoCont"; y = "contenedores"
        } + labs(
            x = "Distrito", y = "Contenedores", title = "Total contenedores por distrito"
        )
        ggsave(fig, "ContenedoresPorDistrito.png")

        //(Grafico de media de toneladas mensuales de recogida de basura por distrito.)

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

    private fun createHmtl(dirDestino: String, tiempo: String, fecha: String) {
        val workingDir: String = System.getProperty("user.dir")
        val pathPlot = File("${workingDir}${fs}lets-plot-images${fs}ContenedoresPorDistrito.png")
        val fileHmtl = File(dirDestino + "ResumenGlobal.html")

        var data1 = ""
        for (i in numTipoContXDistrito) {
            data1 += "<li>$i</li>"
        }
        var data2 = ""
        for (i in mediaTipoContXDistrito) {
            data2 += "<li>$i</li>"
        }
        var data3 = ""
        for (i in mediaTonResiDistritos) {
            data3 += "<li>$i</li>"
        }
        var data4 = ""
        for (i in maxToneladasDistrito) {
            data4 += "<li>$i</>"
        }
        var data5 = ""
        for (i in sumToneladasDistrito) {
            data5 += "<li>$i</>"
        }
        var data6 = ""
        for (i in toneladasDistrito) {
            data6 += "<li>$i</li>"
        }


        fileHmtl.writeText(
            """
            <!DOCTYPE html>
            <html>
                <head></head>
                <body>
                  <div class="cabecera">
                    <h1>Resumen de recogidas de basura y reciclaje en Madrid</h1>
                    <h4>Fecha y Hora: $fecha</h4>
                    <h4>Autores: Mario Resa y Sebastián Mendoza</h4>
                  </div>
                  <div class="resumen">
                    <h3>Número de contenedores de cada tipo que hay en cada distrito</h3>
                    <ul>
                    $data1
                    </ul>
                    <h3>Media de contenedores de cada tipo que hay en cada distrito</h3>
                    <ul>
                    $data2
                    </ul>
                    <h3>Total de contenedores por distrito(gráfica)</h3>
                    <img src="$pathPlot" width="600">
                    <h3>Media de toneladas anuales de recogidas por cada tipo de basura agrupadas por distrito</h3>
                    <ul>
                    $data3
                    </ul>
                    <h3>Media de toneladas mensuales de recogida de basura por distrito</h3>
                    <img src="..\lets-plot-images\grafica_prueba.png" width="600">
                    <h3>Máximo, mínimo , media y desviación de toneladas anuales de recogidas por cada tipo de basura agrupadas por distrito</h3>
                    <ul>
                    $data4
                    </ul>
                    <h3>Suma de todo lo recogido en un año por distrito</h3>
                    <ul>
                    $data5
                    </ul>
                    <h3>Por cada distrito obtener para cada tipo de residuo la cantidad recogida</h3>
                    <ul>
                    $data6
                    </ul>
                    <h4>Tiempo: $tiempo ms</h4>
                  </div>
                </body>
            </html>
        """.trimIndent()
        )
    }

}
