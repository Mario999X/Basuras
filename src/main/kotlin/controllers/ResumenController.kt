package controllers

/**
 * @author Mario Resa y Sebastian Mendoza
 */
import jetbrains.letsPlot.*
import jetbrains.letsPlot.export.ggsave
import jetbrains.letsPlot.geom.geomBar
import jetbrains.letsPlot.geom.geomTile
import jetbrains.letsPlot.intern.Plot
import jetbrains.letsPlot.label.ggtitle
import jetbrains.letsPlot.label.labs
import jetbrains.letsPlot.scale.scaleFillGradient
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

/**
 * ResumenController Clase object donde se controla la filtracion de los CSV en general y
 * la creacion de del informe en formato HTML y en XML
 *
 * @constructor Crea un ResumenController
 */
object ResumenController {
    private val fs = File.separator

    // Filtrados
    private lateinit var numTipoContXDistrito: DataFrame<Contenedores>
    private lateinit var mediaTipoContXDistrito: DataFrame<Contenedores>
    private lateinit var mediaTonResiDistritos: DataFrame<Residuos>
    private lateinit var maxToneladasDistrito: DataFrame<Residuos>
    private lateinit var sumToneladasDistrito: DataFrame<Residuos>
    private lateinit var toneladasDistrito: DataFrame<Residuos>

    /**
     * init() Funcion que inicia el filtrado y el informe en general
     *
     * @param dirOrigen Directorio de origen que se ha facilitado
     * @param dirDestino Directorio de destino que se ha facilitado
     */
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

        createHtmlResumen(destinoPath, tiempo.toString(), fecha)
        logger.debug { "Resumen HTML realizado" }
    }

    /**
     * procesoFiltrados() Funcion que realiza los filtrados del CSV de forma general,
     * ademas de la creacion de las graficas y el informe en XML
     *
     * @param cont Lista de contenedores obtenida con la lectura del primer CSV ("contenedores_varios.csv")
     * @param resi Lista de residuos obtenida con la lectura del segundo CSV ("modelo residuos_2021.csv")
     */
    private fun procesoFiltrados(cont: List<Contenedores>, resi: List<Residuos>) {
        logger.debug { "Inicio de filtrado general..." }
        val dfCont by lazy { cont.toDataFrame() }
        val dfResi by lazy { resi.toDataFrame() }

        // FILTRADOS
        logger.debug { "Numero de contenedores de cada tipo que hay en cada distrito" }
        numTipoContXDistrito = dfCont.groupBy { it.distritoCont.rename("Distrito") }
            .aggregate {
                count { it.tipoCont == "RESTO" } into "Restos"
                count { it.tipoCont == "PAPEL-CARTON" } into "Papel-Carton"
                count { it.tipoCont == "ORGANICA" } into "Organica"
                count { it.tipoCont == "ENVASES" } into "Envases"
                count { it.tipoCont == "VIDRIO" } into "Vidrio"
            }.sortBy { it["Distrito"] }

        logger.debug { "Media de contenedores de cada tipo por distrito" }
        mediaTipoContXDistrito =
            dfCont.groupBy { it.distritoCont.rename("Distrito") and it.tipoCont.rename("Contenedores") }
                .aggregate { mean { it.cantidadCont } into "Media" }.sortBy { it["Distrito"] }

        logger.debug { "Media de toneladas anuales de recogidas por cada tipo de basura agrupadas por distrito" }
        mediaTonResiDistritos =
            dfResi.groupBy { it.nomDistritoResi.rename("Distrito") and it.tipoResi.rename("Tipo") }
                .aggregate { mean { it.toneladasResi } into "Media" }.sortBy { it["Distrito"] }

        logger.debug { "Maximo, minimo, media y desviacion de toneladas anuales de recogidas por cada tipo de basura agrupadas por distrito" }
        maxToneladasDistrito =
            dfResi.groupBy { it.nomDistritoResi.rename("Distrito") and it.tipoResi.rename("Tipo") }
                .aggregate {
                    max { it.toneladasResi } into "Max"
                    min { it.toneladasResi } into "Min"
                    mean { it.toneladasResi } into "Media"
                    std { it.toneladasResi } into "Desviacion"
                }.sortBy { it["Distrito"] }

        logger.debug { "Suma de las toneladas recogidas en un año por distrito" }
        sumToneladasDistrito =
            dfResi.groupBy { it.nomDistritoResi.rename("Distrito") }
                .aggregate { sum { it.toneladasResi } into "TotalToneladas" }.sortBy { it["Distrito"] }

        logger.debug { "Por cada distrito obtener para cada tipo de residuo la cantidad recogida" }
        toneladasDistrito = dfResi.groupBy { it.nomDistritoResi.rename("Distrito") and it.tipoResi.rename("Tipo") }
            .aggregate {
                sum { it.toneladasResi } into "TotalToneladas"
            }.sortBy { it["Distrito"] }

        //--- GRAFICAS ---
        //(Total contenedores X Distrito)
        val numContenedores =
            dfCont.groupBy { it.distritoCont }.aggregate { count() into "contenedores" }.sortBy { it.distritoCont }
        var fig: Plot = letsPlot(data = numContenedores.toMap()) + geomBar(
            stat = Stat.identity, alpha = 0.8
        ) {
            x = "distritoCont"; y = "contenedores"
        } + labs(
            x = "Distrito", y = "Contenedores", title = "Total contenedores por distrito"
        )
        ggsave(fig, "ContenedoresPorDistrito.png")

        //(Grafico de media de toneladas mensuales de recogida de basura por distrito.)
        val mediaToneladas =
            dfResi.groupBy { it.mesResi.rename("Mes") and it.nomDistritoResi.rename("Distrito") }
                .aggregate { mean { it.toneladasResi } into "Media" }
        fig = letsPlot(data = mediaToneladas.toMap()) +
                geomTile(height = 0.9, width = 0.9) { x = "Distrito"; y = "Mes"; fill = "Media" } +
                theme(panelBackground = elementBlank(), panelGrid = elementBlank()) + scaleFillGradient(
            low = "#00FFE5",
            high = "#006D63"
        ) + ggtitle("Media de toneladas por distrito y mes") + ggsize(900, 700)
        ggsave(fig, "ToneladasPorDistrito.png")
    }

    /**
     * createInforme() Funcion que realiza la creacion de un informe en formato XML
     * con el resultado exitoso o no de la funcion ResumenController.kt
     *
     * @param tiempo Medicion de tiempo del proceso
     */
    private fun createInforme(tiempo: String) {
        val informe = Informe(
            UUID.randomUUID().toString(),
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).toString(),
            "Resumen Global",
            "Proceso Exitoso",
            "$tiempo milisegundos"
        )
        Informe.writeToXmlFile(informe, File("bitacora${fs}bitacora.xml"))
    }

    /**
     * createHtmlResumen() Funcion que realiza el informe en formato HTML del filtrado general
     *
     * @param dirDestino Directorio de destino de los nuevos archivos
     * @param tiempo Medicion de tiempo del proceso
     * @param fecha Fecha del momento de la realizacion del proceso
     */
    private fun createHtmlResumen(dirDestino: String, tiempo: String, fecha: String) {
        val workingDir: String = System.getProperty("user.dir")
        val pathPlot1 = File("${workingDir}${fs}lets-plot-images${fs}ContenedoresPorDistrito.png")
        val pathPlot2 = File("${workingDir}${fs}lets-plot-images${fs}ToneladasPorDistrito.png")
        val fileHtml = File(dirDestino + "ResumenGlobal.html")
        val fileCss = File(dirDestino + "ResumenGlobal.css")

        var data1 = ""
        for (i in numTipoContXDistrito) {
            data1 += """<tr><td>${i["Distrito"]}</td><td>${i["Restos"]}</td><td>${i["Papel-Carton"]}</td>
            <td>${i["Organica"]}</td><td>${i["Envases"]}</td><td>${i["Vidrio"]}</td></tr>""".trimIndent()
        }
        var data2 = ""
        for (i in mediaTipoContXDistrito) {
            data2 += """<tr><td>${i["Distrito"]}</td><td>${i["Contenedores"]}</td><td>${i["Media"]}"""
        }
        var data3 = ""
        for (i in mediaTonResiDistritos) {
            data3 += """<tr><td>${i["Distrito"]}</td><td>${i["Tipo"]}</td><td>${i["Media"]}</td></tr>
            """.trimIndent()
        }
        var data4 = ""
        for (i in maxToneladasDistrito) {
            data4 += """<tr><td>${i["Distrito"]}</td><td>${i["Tipo"]}</td><td>${i["Max"]}</td><td>${i["Min"]}</td>
                <td>${i["Media"]}</td><td>${i["Desviacion"]}</td></tr>""".trimIndent()
        }
        var data5 = ""
        for (i in sumToneladasDistrito) {
            data5 += """<tr><td>${i["Distrito"]}</td><td>${i["TotalToneladas"]}</td></tr>"""
        }
        var data6 = ""
        for (i in toneladasDistrito) {
            data6 += """<tr><td>${i["Distrito"]}</td><td>${i["Tipo"]}</td><td>${i["TotalToneladas"]}</td></tr>"""
        }

        fileHtml.writeText(
            """
            <!DOCTYPE html>
            <html>
                <head><link rel="stylesheet" href="$fileCss"></head>
                <body>
                    <div class="container">
                      <div class="cabecera">
                        <h1>Resumen de recogidas de basura y reciclaje en Madrid</h1>
                        <h4>Fecha y Hora: $fecha</h4>
                        <h4>Autores: Mario Resa y Sebastian Mendoza</h4>
                      </div>
                      <div class="resumen">
                        <h3>Numero de contenedores de cada tipo que hay en cada distrito</h3>
                        <table border="1">
                        <tr><th>Distrito</th><th>Restos</th><th>Papel-Carton</th><th>Organica</th><th>Envases</th><th>Vidrio</th></tr>
                        $data1
                        </table>
                        <h3>Media de contenedores de cada tipo que hay en cada distrito</h3>
                        <table border="1">
                        <tr><th>Distrito</th><th>Contenedor</th><th>Media</th></tr>
                        $data2
                        </table>
                        <h3>Total de contenedores por distrito</h3>
                        <img src="$pathPlot1" width="700">
                        <h3>Media de toneladas anuales de recogidas por cada tipo de basura agrupadas por distrito</h3>
                        <table border="1">
                        <tr><th>Distrito</th><th>Tipo</th><th>Media</th></tr>
                        $data3
                        </table>
                        <h3>Media de toneladas mensuales de recogida de basura por distrito</h3>
                        <img src="$pathPlot2" width="700">
                        <h3>Maximo, minimo , media y desviacion de toneladas anuales de recogidas agrupadas por distrito</h3>
                        <table border="1">
                        <tr><th>Distrito</th><th>Tipo</th><th>Maximo</th><th>Minimo</th><th>Media</th><th>Desviacion</th></tr>
                        $data4
                        </table>
                        <h3>Suma de todo lo recogido en un año por distrito</h3>
                        <table border="1">
                        <tr><th>Distrito</th><th>Total Toneladas</th></tr>
                        $data5
                        </table>
                        <h3>Por cada distrito obtener para cada tipo de residuo la cantidad recogida</h3>
                        <table border="1">
                        <tr><th>Distrito</th><th>Tipo</th><th>Total Toneladas</th></tr>
                        $data6
                        </table>
                        <h3>Tiempo de generacion: $tiempo ms</h3>
                      </div>
                    </div>
                </body>
            </html>
        """.trimIndent()
        )

        fileCss.writeText(
            """ body { margin: 0;
                 background-color: #D7DBDD}
                
                .cabecera {
                background-color: #CACFD2;
                width: 100%;
                }
                
                .container, .resumen { 
                display: flex; 
                align-items: center; 
                justify-content: center; 
                flex-direction: column
                }
                
                h3,td, .cabecera { text-align: center 
                }
                
                th { background-color: #B3E6FF }
                tr:hover { background-color: #D7DBDD }
                
                table { margin: 0;
                background-color: #FBFCFC}
                
            """.trimIndent()
        )
    }

}
