package controllers

import mu.KotlinLogging
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess
private val logger = KotlinLogging.logger {}
object DirController {
    fun init(dirOrigen: String, dirDestino: String) {
        val dirOrigenPath = Paths.get(dirOrigen)
        val dirDestinoPath = Paths.get(dirDestino)
        val fileContOrigen = Paths.get(dirOrigen + File.separator + "contenedores_varios.csv")
        val fileResiOrigen = Paths.get(dirOrigen + File.separator + "modelo_residuos_2021.csv")
        if (Files.isDirectory(dirOrigenPath) && Files.exists(dirOrigenPath) && Files.exists(fileContOrigen) && Files.exists(fileResiOrigen)) {
            logger.debug { "Carpeta de origen comprobada... OK" }
        } else {
            logger.debug { "Proporciona una carpeta de origen válida... Fin de la aplicación" }
            exitProcess(0)
        }
        if (Files.isDirectory(dirDestinoPath) && Files.exists(dirDestinoPath)) {
            logger.debug { "Carpeta de destino comprobada... OK" }
        } else {
            logger.debug { "No existe la carpeta. Creando..." }
            Files.createDirectory(dirDestinoPath)
            Files.isWritable(dirDestinoPath)
            logger.debug { "Carpeta de destino creada..." }
        }
    }
}
