# Videoclub – Proyecto Final Programación II

Sistema en **Java + Swing** para controlar películas, clientes y préstamos, conectado a una base de datos compatible con MySQL.
La base de datos viene incluida: no hay que instalar ni configurar nada más que Java.

## Requisito único: Java 17 o superior

Comprueba la versión abriendo una terminal (o CMD / PowerShell) y escribiendo:

```
java -version
```

Si no aparece o es menor que 17, instala **Temurin 17+** desde https://adoptium.net

## Cómo ejecutar

Descomprime `ProyectoFinal.zip` en una carpeta (de preferencia sin tildes ni espacios en la ruta).

### 1. Windows

Para que `ejecutar.bat` funcione, la carpeta debe contener **`app.jar`** junto al `.bat`. El JAR no viene en el repositorio (es un archivo generado), así que tienes dos opciones:

**Opción A: descargar el zip ya armado (recomendada)**
1. Ve a la sección *Releases* del repositorio en GitHub y descarga `ProyectoFinal.zip`.
2. Descomprímelo. Dentro debe verse `app.jar` al lado de `ejecutar.bat`.

**Opción B: compilar tú mismo**
1. Instala **JDK 17+** (https://adoptium.net) y **Maven** (https://maven.apache.org/download.cgi, descomprime y agrega su carpeta `bin` al PATH).
2. Abre PowerShell en la carpeta del proyecto y ejecuta:
   ```
   mvn package
   copy target\app.jar .
   ```
3. Ahora `app.jar` está junto a `ejecutar.bat`.

**Ejecutar**
1. Haz doble clic en **`ejecutar.bat`**.
2. Si Windows muestra "Windows protegió su PC", pulsa *Más información* → *Ejecutar de todas formas*.
3. Alternativa en CMD o PowerShell, dentro de la carpeta: `java -jar app.jar`

Si ves `Error: Unable to access jarfile app.jar`, significa que `app.jar` no está en esa carpeta. Revisa el paso anterior.

### 2. Linux (Fedora, Ubuntu, etc.)
```bash
chmod +x ejecutar.sh
./ejecutar.sh
```
- Alternativa: `java -jar app.jar`

### 3. macOS
1. Abre Terminal en la carpeta y ejecuta una sola vez: `chmod +x ejecutar.command ejecutar.sh`
2. Haz doble clic en **`ejecutar.command`**.
3. Si macOS lo bloquea: clic derecho sobre el archivo → *Abrir* → *Abrir*.
4. Alternativa en Terminal: `java -jar app.jar`

## Base de datos

El motor en uso aparece en la **barra inferior** de la ventana. La aplicación usa:

1. **MySQL externo**, si `mysql.url` está escrito en `config.properties`. Si no conecta, cae al paso 2.
2. **H2 en modo MySQL**, la base embebida que viene incluida. No necesita instalar nada.

Los datos se guardan en la carpeta `data/`, junto al programa.

### Usar un MySQL real
1. Edita `config.properties`:
   ```properties
   modo=mysql
   mysql.url=jdbc:mysql://localhost:3306/videoclub?createDatabaseIfNotExist=true
   mysql.usuario=root
   mysql.clave=TU_CLAVE
   ```
2. Vuelve a ejecutar la aplicación. Con `modo=mysql`, si no conecta se muestra el error en vez de usar H2.
3. Para crear las tablas a mano, usa `sql/schema.sql`. La app también las crea sola al iniciar.

## Funcionalidad
- **Películas:** código, título, año, duración, clasificación, actores, director y ejemplares. Insertar, modificar y consultar.
- **Clientes:** insertar, modificar y consultar, con estado Activo/Inactivo.
- **Préstamos:** se registran con estado `A`. Si no quedan ejemplares disponibles se lanza una excepción con el aviso correspondiente.
- **Devoluciones:** se consulta el código de préstamo y se registra la fecha de devolución y el estado `I`.

## Compilar desde el código fuente
Requiere JDK 17+ y Maven:
```bash
mvn package        # genera target/app.jar
./empaquetar.sh    # genera ProyectoFinal.zip
```

## Solución de problemas
| Problema | Solución |
|---|---|
| `java` no se reconoce como comando | Instala Java 17+ y reabre la terminal. |
| Error al iniciar la base de datos | Mueve la carpeta a una ruta sin tildes ni espacios y borra la carpeta `data/`. |
| Los datos desaparecen entre ejecuciones | Ejecuta desde la carpeta del programa: los datos se guardan en `data/` junto a `app.jar`. |
