package controllers

/**
 * @author Mario Resa y Sebastián Mendoza
 */
import jetbrains.datalore.base.values.Color
import jetbrains.letsPlot.*
import jetbrains.letsPlot.export.ggsave
import jetbrains.letsPlot.geom.geomBar
import jetbrains.letsPlot.intern.Plot
import jetbrains.letsPlot.label.labs
import models.*
import mu.KotlinLogging
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.api.*
import java.io.File
import java.text.Normalizer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

/**
 * DistritoController Clase objeto donde se controla la filtración de los CSV por distrito y la creación de del informe en formato HTML y en XML
 *
 * @constructor Crea un DistritoController
 */
object DistritoController {
    private var exito: Boolean = true
    private val fs = File.separator

    private lateinit var numTipoContXDistrito: DataRow<Contenedores>
    private lateinit var totalTonResiDistrito: DataFrame<Residuos>
    private lateinit var operacionesToneladas: DataFrame<Residuos>

    /**
     * Init() Función que inicia el filtrado y el informe por distrito
     *
     * @param distritoMain Parámetro donde se especifica el distrito que se desea filtrar
     * @param dirOrigen Directorio de origen que se ha facilitado
     * @param dirDestino Directorio de destino que se ha facilitado
     */
    fun init(distritoMain: String, dirOrigen: String, dirDestino: String) {
        val csvContenedores = dirOrigen + fs + "contenedores_varios.csv"
        val csvResiduos = dirOrigen + fs + "modelo_residuos_2021.csv"
        val destinoPath = dirDestino + fs

        //Lectura de csv
        val cont by lazy { loadCsvCont(File(csvContenedores)) }
        val resi by lazy { loadCsvResi(File(csvResiduos)) }

        val distrito =
            Normalizer.normalize(distritoMain.uppercase(), Normalizer.Form.NFD).replace("[^\\p{ASCII}]".toRegex(), "")

        val tiempo = measureTimeMillis {
            logger.debug { "Distrito elegido: $distrito" }
            procesoFiltrados(distrito, cont, resi)
        }
        createInforme(distrito, tiempo.toString())
        logger.debug { "Tiempo de filtrados: $tiempo ms" }

        val fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss"))
        logger.debug { fecha }

        createHtmlDistrito(distrito, destinoPath, tiempo.toString(), fecha)
        logger.debug { "Resumen $distrito HTML realizado" }
    }

    /**
     * ProcesoFiltrados Función que realiza los filtrados del CSV por distrito,
     * además de la creación de las gráficas y el informe en XML
     *
     * @param distrito Parámetro don de se especifica el distrito a filtrar
     * @param cont Lista de contenedores obtenida con la lectura del primer CSV ("contenedores_varios.csv")
     * @param resi Lista de residuos obtenida con la lectura del segundo CSV ("modelo residuos_2021.csv")
     */
    private fun procesoFiltrados(distrito: String, cont: List<Contenedores>, resi: List<Residuos>) {
        logger.debug { "Inicio de filtrado por distrito: $distrito" }
        val dfCont by lazy { cont.toDataFrame() }
        val dfResi by lazy { resi.toDataFrame() }

        logger.debug { "Comprobando nombre del distrito..." }
        val dfDistritos = dfCont.count { it.distritoCont == distrito }
        if (dfDistritos == 0) {
            logger.debug { "No existe el distrito" }
            exito = false
            createInforme(distrito, "0")
            exitProcess(0)
        }

        logger.debug { "Número de contenedores de cada tipo, distrito específico" }
        numTipoContXDistrito =
            dfCont.filter { it.distritoCont == distrito }
                .aggregate {
                    count { it.tipoCont == "RESTO" } into "Restos"
                    count { it.tipoCont == "PAPEL-CARTON" } into "Papel-Carton"
                    count { it.tipoCont == "ORGANICA" } into "Organica"
                    count { it.tipoCont == "ENVASES" } into "Envases"
                    count { it.tipoCont == "VIDRIO" } into "Vidrio"
                }

        logger.debug { "Total de toneladas recogidas en este distrito por residuos" }
        totalTonResiDistrito =
            dfResi.filter { it.nomDistritoResi == distrito }.groupBy { it.tipoResi.rename("Tipo") }
                .aggregate {
                    sum { it.toneladasResi } into "Total"
                }.sortBy { it["Total"].desc() }

        logger.debug { "Máximo, mínimo, media y desviación" }
        operacionesToneladas = dfResi.filter { it.nomDistritoResi == distrito }
            .groupBy { it.tipoResi.rename("Tipo") }
            .aggregate {
                max(it.toneladasResi) into "Max"
                min(it.toneladasResi) into "Min"
                mean(it.toneladasResi) into "Media"
                std(it.toneladasResi) into "Desviacion"
            }
        println(operacionesToneladas)

        // GRÁFICAS
        //Barras (Total Toneladas X Residuo) en determinado Distrito
        var fig: Plot = letsPlot(data = totalTonResiDistrito.toMap()) + geomBar(
            stat = Stat.identity, alpha = 0.8
        ) {
            x = "Tipo"; y = "Total"
        } + labs(
            x = "Residuos", y = "Toneladas", title = "Total Toneladas por Residuo en $distrito"
        )
        ggsave(fig, "ToneladasPorResiduo${distrito}.png")

        // Gráfico (Máximo, mínimo y media por meses en dicho distrito)
        fig = letsPlot(data = operacionesToneladas.toMap()) + geomBar(
            stat = Stat.identity,
            alpha = 0.8,
            fill = Color.BLACK,
        ) {
            x = "Tipo"; y = "Max"
        } + geomBar(
            stat = Stat.identity,
            alpha = 0.8,
            fill = Color.RED
        ) {
            x = "Tipo"; y = "Media"
        } + geomBar(
            stat = Stat.identity,
            alpha = 0.8,
            fill = Color.PACIFIC_BLUE
        ) {
            x = "Tipo"; y = "Min"
        } + labs(
            x = "Tipo",
            y = "Operaciones",
            title = "Máximo, media y minimo para $distrito"
        )
        ggsave(fig, "Operaciones${distrito}.png")

    }

    /**
     * CreateInforme Función que realiza la creación de un informe en formato XML con el resultado exitoso o no de la función DistritoController.kt
     *
     * @param distritoMain
     * @param tiempo
     */
    private fun createInforme(distritoMain: String, tiempo: String) {
        var exitoString = "Proceso exitoso"
        if (!exito) {
            exitoString = "Proceso fallido"
        }
        val informe = Informe(
            UUID.randomUUID().toString(),
            LocalDateTime.now().toString(),
            "Resumen $distritoMain",
            exitoString,
            "$tiempo milisegundos"
        )
        Informe.writeToXmlFile(informe, File("bitacora${fs}bitacora.xml"))
    }

    /**
     * CreateHtmlDistrito Función que realiza el informe en formato HTML del filtrado por distrito
     *
     * @param distrito Parámetro don de se especifica el distrito a filtrar
     * @param dirDestino Directorio de destino facilitado
     * @param tiempo Medición de tiempo del proceso
     * @param fecha Fecha del momento de la realización del proceso
     */
    private fun createHtmlDistrito(distrito: String, dirDestino: String, tiempo: String, fecha: String) {
        val workingDir: String = System.getProperty("user.dir")
        val pathPlot1 = File("${workingDir}${fs}lets-plot-images${fs}ToneladasPorResiduo${distrito}.png")
        val pathPlot2 = File("${workingDir}${fs}lets-plot-images${fs}Operaciones${distrito}.png")
        val fileHtml = File(dirDestino + "resumen_${distrito}.html")
        val fileCss = File(dirDestino + "resumen_distrito.css")

        val data1 =
            """<tr><td>${numTipoContXDistrito[0]}</td><td>${numTipoContXDistrito[1]}</td>
                <td>${numTipoContXDistrito[2]}</td><td>${numTipoContXDistrito[3]}</td>
                <td>${numTipoContXDistrito[4]}</td></tr>""".trimIndent()
        var data2 = ""
        for (i in totalTonResiDistrito) {
            data2 += """<tr><td>${i["Tipo"]}</td><td>${i["Total"]}</td></tr>"""
        }
        var data3 = ""
        for (i in operacionesToneladas) {
            data3 += """<tr><td>${i["Tipo"]}</td><td>${i["Max"]}</td><td>${i["Min"]}</td><td>${i["Media"]}</td>
                <td>${i["Desviacion"]}</td></tr>""".trimIndent()
        }

        fileHtml.writeText(
            """
                <!DOCTYPE html>
                <html>
                    <head><link rel="stylesheet" href="$fileCss"></head>
                    <body>
                        <div class="container">
                            <div class="cabecera">
                                <h1>Resumen de recogidas de basura y reciclaje en $distrito</h1>
                                <h4>Fecha y Hora: $fecha</h4>
                                <h4>Autores: Mario Resa y Sebastian Mendoza</h4>
                            </div>
                            <div class="resumen">
                                <h3>Numero de contenedores de cada tipo que hay en este distrito</h3>
                                <table border="1">
                                <tr><th>Restos</th><th>Papel-Carton</th><th>Organica</th><th>Envases</th><th>Vidrio</th></tr>
                                $data1
                                </table>
                                <h3>Total de toneladas recogidas en este distrito por residuo</h3>
                                <table border="1">
                                <tr><th>Tipo</th><th>Total</th></tr>
                                $data2
                                </table>
                                <h3>Total de toneladas por residuo en este distrito</h3>
                                <img src="$pathPlot1" width="700">
                                <h3>Maximo, minimo, media y desviacion por mes por residuo en este distrito</h3>
                                <table border="1">
                                <tr><th>Tipo</th><th>Max</th><th>Min</th><th>Media</th><th>Desviacion</th></tr>
                                $data3
                                </table>
                                <h3>maximo, minimo y media por meses en dicho distrito</h3>
                                <img src="$pathPlot2" width="700">
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