import controllers.ResumenController
import controllers.DirController
import controllers.DistritoController
import controllers.ParserController
import java.util.*
import javax.swing.text.html.parser.Parser

fun main(args: Array<String>) {
    when (args.size) {
        0, 1, 2 -> println("Vuelve a ejecutar el programa con una opción, una carpeta de origen de los datos y otra de destino")
        3 -> {
            when (args[0].lowercase()) {
                "parser" -> {
                    ParserController.init(args[1], args[2])
                }

                "resumen" -> {
                    DirController.init(args[1], args[2])
                    ResumenController.init(args[1], args[2])
                }

                else -> {
                    println("Vuelve a ejecutar el programa con una opción, una carpeta de origen de los datos y otra de destino")
                }
            }
        }

        4 -> {
            val opcion: String = args[0].lowercase()
        }
        //DistritoController.init()
    }
}