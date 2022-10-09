package controllers

import java.nio.file.Files
import java.nio.file.Paths

object DirController {

    fun init(dirOrigen: String, dirDestino: String) {
        val dirOrigenPath = Paths.get(dirOrigen)
        val dirDestinoPath = Paths.get(dirDestino)
        if (Files.isDirectory(dirOrigenPath) && Files.exists(dirOrigenPath)) {
            println("Carpeta de origen comprobada...")
        } else {
            println("Proporciona una carpeta de origen válida...")
            System.exit(0)
        }
        if (Files.isDirectory(dirDestinoPath) && Files.exists(dirDestinoPath)) {
            println("Carpeta de destino comprobada...")
        } else {
            println("No existe la carpeta. Creando...")
            Files.createDirectory(dirDestinoPath)
            Files.isWritable(dirDestinoPath)
            println("Carpeta de destino creada...")
        }
//        val fs = File.separator
//        val workingDirectory: String = System.getProperty("user.dir")
//        val pathResources = Paths.get(workingDirectory + fs + "src" + fs + "main" + fs + "resources")
//        val isDirResources = Files.isDirectory(pathResources) && Files.isWritable(pathResources)
//        if (!isDirResources) Files.createDirectory(pathResources) + println("Directorio Creado $pathResources")
    }
}
