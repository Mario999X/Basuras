import controllers.ResumenController
import controllers.DirController
import controllers.DistritoController
import controllers.ParserController
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * main() Funcion principal donde se trabaja la logica de los argumentos introducidos en la ejecucion del programa
 *
 * @param args Array de tipo String donde se almacenan los argumentos introducidos
 */
fun main(args: Array<String>) {
    logger.debug { "Ejecutando aplicación" }
    when (args.size) {
        0, 1, 2 ->
            logger.debug { "Sin parámetros o datos erroneos: Vuelve a ejecutar el programa con una opción, una carpeta de origen de los datos y otra de destino" }

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
                    logger.debug { "Opción errónea: Vuelve a ejecutar el programa con una opción válida, una carpeta de origen de los datos y otra de destino" }
                }
            }
        }

        4 -> {
            when (args[0].lowercase()) {
                "resumen" -> {
                    DirController.init(args[2], args[3])
                    DistritoController.init(args[1], args[2], args[3])
                }

                else -> {
                    logger.debug { "Parámetros erróneos: Vuelve a ejecutar el programa con una opción válida, una carpeta de origen de los datos y otra de destino" }
                }
            }
        }

        else -> {
            logger.debug { "Parámetros erróneos: Vuelve a ejecutar el programa con una opción válida, una carpeta de origen de los datos y otra de destino" }
        }
    }
    logger.debug { "Fin de la aplicación" }
}