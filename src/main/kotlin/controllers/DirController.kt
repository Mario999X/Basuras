package controllers

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

object DirController {

    fun init() {
        val fs = File.separator
        val workingDirectory: String = System.getProperty("user.dir")
        val pathResources = Paths.get(workingDirectory + fs + "src" + fs + "main" + fs + "resources")

        val isDirResources = Files.isDirectory(pathResources) && Files.isWritable(pathResources)

        if (!isDirResources) Files.createDirectory(pathResources) + println("Directorio Creado $pathResources")
    }
}
