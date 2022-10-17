package controllers

import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.io.File

internal class ParserControllerTest {
    private val dirOrigen = "data"
    private val dirDestino = "src${File.separator}test${File.separator}resources"

    @Test
    fun initFail() {
        val pathFail = "src${File.separator}test"
        val resultado = ParserController.init(pathFail, dirDestino)
        assertFalse(resultado)
    }
    @Test
    fun initOK() {
        val resultado = ParserController.init(dirOrigen, dirDestino)
        assertTrue(resultado)
    }
}