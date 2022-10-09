import controllers.ResumenController
import controllers.DirController
import controllers.DistritoController
import java.util.*

fun main(args: Array<String>) {
    when (args.size) {
        0, 1, 2 -> println("Vuelve a ejecutar el programa con una opción, una carpeta de origen de los datos y otra de destino")
        3 -> {
            val opcion: String = args[0].lowercase()
            if (opcion == "resumen"){
                DirController.init(args[1], args[2])
            }
        }
    }
    //DirController.init()
    //ResumenController.init()
    //DistritoController.init()

}