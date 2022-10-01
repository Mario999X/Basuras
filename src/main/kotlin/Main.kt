import models.*
import org.jetbrains.kotlinx.dataframe.api.*
import java.io.File

fun main() {
    val fs = File.separator
    val pathCont = "data" + fs + "contenedores_varios.csv"
    val pathResi = "data" + fs + "modelo_residuos_2021.csv"

    val cont by lazy { loadCsvCont(File(pathCont)) }
    val dfCont by lazy { cont.toDataFrame() }
    val resi by lazy { loadCsvResi(File(pathResi)) }
    val dfResi by lazy { resi.toDataFrame() }

//    println(dfCont)
//    println(dfResi)
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