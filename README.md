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
1. Haz doble clic en **`ejecutar.bat`**.
2. Si Windows muestra "Windows protegió su PC", pulsa *Más información* → *Ejecutar de todas formas*.
3. Alternativa en CMD, dentro de la carpeta: `java -jar app.jar`

### 2. Linux (Fedora, Ubuntu, etc.)
```bash
chmod +x ejecutar.sh
./ejecutar.sh
```
- Alternativa: `java -jar app.jar`
- **Fedora:** si el motor MariaDB embebido no arranca por la librería `libcrypt.so.1`, instala:
  ```bash
  sudo dnf install libxcrypt-compat
  ```
  Sin ella la aplicación sigue funcionando con el motor de respaldo (H2).

### 3. macOS
1. Abre Terminal en la carpeta y ejecuta una sola vez: `chmod +x ejecutar.command ejecutar.sh`
2. Haz doble clic en **`ejecutar.command`**.
3. Si macOS lo bloquea: clic derecho sobre el archivo → *Abrir* → *Abrir*.
4. Alternativa en Terminal: `java -jar app.jar`

## Base de datos

El motor en uso aparece en la **barra inferior** de la ventana. La aplicación prueba, en orden:

1. **MySQL externo**, si se escribe `mysql.url` en `config.properties`.
2. **MariaDB embebida** (compatible con MySQL).
3. **H2 en modo MySQL** (respaldo).

Los datos se guardan en la carpeta `data/`, junto al programa.

### Usar un MySQL real
1. Crea las tablas con `sql/schema.sql` (o deja que la app las cree).
2. Edita `config.properties`:
   ```properties
   modo=mysql
   mysql.url=jdbc:mysql://localhost:3306/videoclub?createDatabaseIfNotExist=true
   mysql.usuario=root
   mysql.clave=TU_CLAVE
   ```
3. Vuelve a ejecutar la aplicación.

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
| La ventana tarda en abrir la primera vez | Normal: extrae y arranca la base de datos embebida. |
| Error al iniciar la base de datos | Mueve la carpeta a una ruta sin tildes ni espacios y borra la carpeta `data/`. |
