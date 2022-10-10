package controllers

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

object DirController {

    fun init(dirOrigen: String, dirDestino: String) {
        val dirOrigenPath = Paths.get(dirOrigen)
        val dirDestinoPath = Paths.get(dirDestino)
        val fileContOrigen = Paths.get(dirOrigen + File.separator + "contenedores_varios.csv")
        val fileResiOrigen = Paths.get(dirOrigen + File.separator + "modelo_residuos_2021.csv")
        if (Files.isDirectory(dirOrigenPath) && Files.exists(dirOrigenPath) && Files.exists(fileContOrigen) && Files.exists(fileResiOrigen)) {
            println("Carpeta de origen comprobada...")
        } else {
            println("Proporciona una carpeta de origen válida...")
            exitProcess(0)
        }
        if (Files.isDirectory(dirDestinoPath) && Files.exists(dirDestinoPath)) {
            println("Carpeta de destino comprobada...")
        } else {
            println("No existe la carpeta. Creando...")
            Files.createDirectory(dirDestinoPath)
            Files.isWritable(dirDestinoPath)
            println("Carpeta de destino creada...")
        }
    }
}
