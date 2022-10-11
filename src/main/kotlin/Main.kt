import controllers.ResumenController
import controllers.DirController
import controllers.DistritoController
import controllers.ParserController

fun main(args: Array<String>) {
    println("Ejecutando App \n")
    when (args.size) {
        0, 1, 2 -> println("Vuelve a ejecutar el programa con una opción, una carpeta de origen de los datos y otra de destino")
        3 -> {
            when (args[0].lowercase()) {
                "parser" -> {
                    DirController.init(args[1], args[2])
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
            when (args[0].lowercase()) {
                "resumen" -> {
                    DirController.init(args[2], args[3])
                    DistritoController.init(args[1], args[2], args[3])
                }
            }
        }
    }
    println("\nCerrando App")
}