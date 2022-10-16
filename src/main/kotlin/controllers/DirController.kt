package controllers

/**
 * @author Mario Resa y Sebastián Mendoza
 */
import mu.KotlinLogging
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

/**
 * DirController clase objeto donde se maneja la función que comprueba los directorios facilitados
 *
 * @constructor Crea un DirController
 */
object DirController {

    /**
     * Init() Función que inicia la comprobación de los directorios de origen y destino facilitados por argumentos
     *
     * @param dirOrigen Directorio de origen que se ha facilitado
     * @param dirDestino Directorio de destino que se ha facilitado
     */
    fun init(dirOrigen: String, dirDestino: String) {
        val workingDir: String = System.getProperty("user.dir")
        val dirOrigenPath = Paths.get(dirOrigen)
        val dirDestinoPath = Paths.get(dirDestino)
        val fileContOrigen = Paths.get(dirOrigen + File.separator + "contenedores_varios.csv")
        val fileResiOrigen = Paths.get(dirOrigen + File.separator + "modelo_residuos_2021.csv")
        val fileBitacoraPath = Paths.get(workingDir + File.separator + "bitacora")

        if (Files.isDirectory(fileBitacoraPath) && Files.exists(fileBitacoraPath)) {
            logger.debug { "Carpeta de bitacora comprobada... OK" }
        } else {
            logger.debug { "Carpeta de bitacora no existe. Creando..." }
            Files.createDirectory(fileBitacoraPath)
            logger.debug { "Carpeta de destino creada..." }
        }
        if (Files.isDirectory(dirOrigenPath) && Files.exists(dirOrigenPath) && Files.exists(fileContOrigen) && Files.exists(
                fileResiOrigen
            )
        ) {
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
