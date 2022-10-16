# Reciclaje y limpieza de Madrid 
Estudio de análisis de datos sobre la limpieza y la gestión de basuras de Madrid
## Autores
[Mario Resa](https://github.com/Mario999X) y [Sebastián Mendoza](https://github.com/SebsMendoza)
## Resumen
* En el directorio *controllers*, tenemos la logica principal de la app, en distintas clases objeto.
    * (**DirController**) Se encarga de comprobar la existencia y posibilidad de lectura/escritura de los directorios usados en la app.
    * Realizar las distintas operaciones segun lo introducido por parametros (Parser, resumen, resumen distrito)
* En el directorio *models*, tenemos los modelos usados en la app.
    * **Contenedores**, para el CSV de contenedores_varios
    * **Residuos**, para el CSV de modelo_residuos_2021
    * **Informe**, creado para bitacora.xml
* En **Main.kt**, hemos creado una funcion que acepte parametros de entrada, guardando esos datos en un array de String, y segun la posicion de sus datos realice las diferentes operaciones.
## Herramientas usadas
* Gradle
* Dataframe
* Serialization
* JSON
* XML
* CSV
* JUNIT
* LetsPlot
## Video
*.