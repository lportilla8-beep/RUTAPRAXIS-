import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Locale;

public class ConexionBD {
    private static final String MOTOR = System.getProperty("db.motor", "oracle").toLowerCase(Locale.ROOT);
    private static final String HOST = System.getProperty("db.host", "192.168.254.215");
    private static final String PUERTO = System.getProperty("db.port", "1521");
    private static final String BASE_DATOS = System.getProperty("db.name", "ORCL");
    private static final String USER = System.getProperty("db.user", "rutapraxis");
    private static final String PASSWORD = System.getProperty("db.password", "rutapraxis123");
    private static final String TABLA_ADMINISTRADOR = "administrador";
    private static final String TABLA_USUARIOS_ANTERIOR = "usuarios";

    private static final String URL = System.getProperty("db.url", construirUrl());
    private static boolean usandoBD = false;

    private String motor;
    private String host;
    private String puerto;
    private String baseDatos;
    private String usuario;
    private String password;
    private Connection conexion;

    public ConexionBD() {
        this(MOTOR, HOST, PUERTO, BASE_DATOS, USER, PASSWORD);
    }

    public ConexionBD(String motor, String host, String puerto, String baseDatos, String usuario, String password) {
        this.motor = motor.toLowerCase(Locale.ROOT);
        this.host = host;
        this.puerto = puerto;
        this.baseDatos = baseDatos;
        this.usuario = usuario;
        this.password = password;
    }

    public static boolean isUsandoBD() {
        return usandoBD;
    }

    public static Connection obtenerConexion() {
        try {
            cargarDriver();
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            usandoBD = true;
            return conn;
        } catch (Exception e) {
            usandoBD = false;
            System.out.println("No se pudo conectar a la base de datos " + MOTOR + ". Trabajando en modo local.");
            System.out.println("Detalle: " + e.getMessage());
            return null;
        }
    }

    public Connection conectar() {
        try {
            cargarDriver(motor);
            conexion = DriverManager.getConnection(construirUrl(motor, host, puerto, baseDatos), usuario, password);
            usandoBD = true;
            System.out.println("Conexion exitosa a " + motor);
            return conexion;
        } catch (Exception e) {
            usandoBD = false;
            System.out.println("No se pudo conectar a la base de datos " + motor + ".");
            System.out.println("Detalle: " + e.getMessage());
            return null;
        }
    }

    public void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("Conexion cerrada");
            }
        } catch (SQLException e) {
            System.out.println("Error al cerrar conexion: " + e.getMessage());
        }
    }

    public static void inicializarTablas() {
        try (Connection conn = obtenerConexion()) {
            if (conn == null) return;

            if (esOracle()) {
                inicializarTablasOracle(conn);
            } else if (esSqlServer()) {
                inicializarTablasSqlServer(conn);
            } else {
                throw new SQLException("Motor de base de datos no soportado: " + MOTOR);
            }

            asegurarPasswordAdministrador(conn);
            asegurarIdAutoincrementalAdministrador(conn);
            actualizarRolUsuariosAAdministrador(conn);
            System.out.println("Conexion con " + MOTOR + " establecida y tablas inicializadas correctamente.");
        } catch (SQLException e) {
            System.err.println("Error al inicializar tablas en la base de datos: " + e.getMessage());
        }
    }

    private static String construirUrl() {
        return construirUrl(MOTOR, HOST, PUERTO, BASE_DATOS);
    }

    private static String construirUrl(String motor, String host, String puerto, String baseDatos) {
        if ("sqlserver".equals(motor)) {
            return "jdbc:sqlserver://" + host + ":" + puerto
                    + ";databaseName=" + baseDatos
                    + ";encrypt=true;trustServerCertificate=true";
        }

        String estiloOracle = System.getProperty("db.oracle.style", "sid").toLowerCase(Locale.ROOT);
        if ("service".equals(estiloOracle)) {
            return "jdbc:oracle:thin:@//" + host + ":" + puerto + "/" + baseDatos;
        }
        return "jdbc:oracle:thin:@" + host + ":" + puerto + ":" + baseDatos;
    }

    private static void cargarDriver() throws ClassNotFoundException {
        cargarDriver(MOTOR);
    }

    private static void cargarDriver(String motor) throws ClassNotFoundException {
        if ("oracle".equals(motor)) {
            Class.forName("oracle.jdbc.OracleDriver");
        } else if ("sqlserver".equals(motor)) {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        }
    }

    private static boolean esOracle() {
        return "oracle".equals(MOTOR);
    }

    private static boolean esSqlServer() {
        return "sqlserver".equals(MOTOR);
    }

    private static void inicializarTablasOracle(Connection conn) throws SQLException {
        prepararTablaAdministrador(conn);

        crearTablaSiNoExiste(conn, TABLA_ADMINISTRADOR,
                "CREATE TABLE " + TABLA_ADMINISTRADOR + " ("
                        + "id VARCHAR2(50) PRIMARY KEY, "
                        + "nombre_completo VARCHAR2(255) NOT NULL, "
                        + "correo VARCHAR2(255) UNIQUE NOT NULL, "
                        + "rol VARCHAR2(100) NOT NULL, "
                        + "ultimo_acceso VARCHAR2(100), "
                        + "estado VARCHAR2(50) NOT NULL, "
                        + "password VARCHAR2(255) NOT NULL"
                        + ")");

        crearTablaSiNoExiste(conn, "tareas_asignadas",
                "CREATE TABLE tareas_asignadas ("
                        + "clase VARCHAR2(50) PRIMARY KEY, "
                        + "titulo VARCHAR2(255) NOT NULL, "
                        + "archivo_tutor CLOB, "
                        + "fecha_limite VARCHAR2(50) NOT NULL, "
                        + "hora_limite VARCHAR2(50) NOT NULL, "
                        + "descripcion CLOB NOT NULL"
                        + ")");

        crearTablaSiNoExiste(conn, "tareas_entregadas",
                "CREATE TABLE tareas_entregadas ("
                        + "estudiante VARCHAR2(255), "
                        + "clase VARCHAR2(50), "
                        + "titulo VARCHAR2(255) NOT NULL, "
                        + "archivo CLOB NOT NULL, "
                        + "fecha_entrega VARCHAR2(50) NOT NULL, "
                        + "hora_entrega VARCHAR2(50) NOT NULL, "
                        + "estado VARCHAR2(50) NOT NULL, "
                        + "comentario CLOB, "
                        + "comentario_asesor CLOB, "
                        + "calificacion NUMBER DEFAULT 0, "
                        + "PRIMARY KEY (estudiante, clase)"
                        + ")");

        agregarColumnaSiNoExiste(conn, "tareas_entregadas", "comentario_asesor", "comentario_asesor CLOB");
        agregarColumnaSiNoExiste(conn, "tareas_entregadas", "calificacion", "calificacion NUMBER DEFAULT 0");
    }

    private static void inicializarTablasSqlServer(Connection conn) throws SQLException {
        prepararTablaAdministrador(conn);

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("IF OBJECT_ID('" + TABLA_ADMINISTRADOR + "', 'U') IS NULL CREATE TABLE " + TABLA_ADMINISTRADOR + " ("
                    + "id VARCHAR(50) PRIMARY KEY, "
                    + "nombre_completo VARCHAR(255) NOT NULL, "
                    + "correo VARCHAR(255) UNIQUE NOT NULL, "
                    + "rol VARCHAR(100) NOT NULL, "
                    + "ultimo_acceso VARCHAR(100), "
                    + "estado VARCHAR(50) NOT NULL, "
                    + "password VARCHAR(255) NOT NULL"
                    + ")");

            stmt.executeUpdate("IF OBJECT_ID('tareas_asignadas', 'U') IS NULL CREATE TABLE tareas_asignadas ("
                    + "clase VARCHAR(50) PRIMARY KEY, "
                    + "titulo VARCHAR(255) NOT NULL, "
                    + "archivo_tutor VARCHAR(MAX), "
                    + "fecha_limite VARCHAR(50) NOT NULL, "
                    + "hora_limite VARCHAR(50) NOT NULL, "
                    + "descripcion VARCHAR(MAX) NOT NULL"
                    + ")");

            stmt.executeUpdate("IF OBJECT_ID('tareas_entregadas', 'U') IS NULL CREATE TABLE tareas_entregadas ("
                    + "estudiante VARCHAR(255), "
                    + "clase VARCHAR(50), "
                    + "titulo VARCHAR(255) NOT NULL, "
                    + "archivo VARCHAR(MAX) NOT NULL, "
                    + "fecha_entrega VARCHAR(50) NOT NULL, "
                    + "hora_entrega VARCHAR(50) NOT NULL, "
                    + "estado VARCHAR(50) NOT NULL, "
                    + "comentario VARCHAR(MAX), "
                    + "comentario_asesor VARCHAR(MAX), "
                    + "calificacion FLOAT DEFAULT 0, "
                    + "PRIMARY KEY (estudiante, clase)"
                    + ")");
        }
    }

    private static void crearTablaSiNoExiste(Connection conn, String tabla, String sql) throws SQLException {
        if (existeTabla(conn, tabla)) return;

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private static void agregarColumnaSiNoExiste(Connection conn, String tabla, String columna, String definicion) throws SQLException {
        if (existeColumna(conn, tabla, columna)) return;

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER TABLE " + tabla + " ADD " + definicion);
        }
    }

    private static void renombrarTablaUsuariosAAdministrador(Connection conn) throws SQLException {
        if (!tablaExiste(conn, TABLA_USUARIOS_ANTERIOR) || tablaExiste(conn, TABLA_ADMINISTRADOR)) return;

        try (Statement stmt = conn.createStatement()) {
            if (esSqlServer()) {
                stmt.executeUpdate("EXEC sp_rename '" + TABLA_USUARIOS_ANTERIOR + "', '" + TABLA_ADMINISTRADOR + "'");
            } else {
                stmt.executeUpdate("ALTER TABLE " + TABLA_USUARIOS_ANTERIOR + " RENAME TO " + TABLA_ADMINISTRADOR);
            }
        }
    }

    private static void prepararTablaAdministrador(Connection conn) throws SQLException {
        if (!tablaExiste(conn, TABLA_ADMINISTRADOR) && tablaExiste(conn, TABLA_USUARIOS_ANTERIOR)) {
            renombrarTablaUsuariosAAdministrador(conn);
        }
    }

    private static boolean tablaExiste(Connection conn, String tabla) throws SQLException {
        if (esOracle()) {
            String query = "SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, tabla.toUpperCase(Locale.ROOT));
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0;
                }
            }
        }
        return existeTabla(conn, tabla);
    }

    private static boolean existeTabla(Connection conn, String tabla) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        String nombreTabla = tabla.toUpperCase(Locale.ROOT);
        try (ResultSet rs = metaData.getTables(null, USER.toUpperCase(Locale.ROOT), nombreTabla, new String[] {"TABLE"})) {
            if (rs.next()) return true;
        }

        try (ResultSet rs = metaData.getTables(null, null, nombreTabla, new String[] {"TABLE"})) {
            return rs.next();
        }
    }

    private static boolean existeColumna(Connection conn, String tabla, String columna) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        String nombreTabla = tabla.toUpperCase(Locale.ROOT);
        String nombreColumna = columna.toUpperCase(Locale.ROOT);
        try (ResultSet rs = metaData.getColumns(null, USER.toUpperCase(Locale.ROOT), nombreTabla, nombreColumna)) {
            if (rs.next()) return true;
        }

        try (ResultSet rs = metaData.getColumns(null, null, nombreTabla, nombreColumna)) {
            return rs.next();
        }
    }

    private static String columnaPasswordUsuarios(Connection conn) throws SQLException {
        if (existeColumna(conn, TABLA_ADMINISTRADOR, "password")) return "password";
        if (existeColumna(conn, TABLA_ADMINISTRADOR, "contrasena")) return "contrasena";
        return null;
    }

    private static String expresionPasswordUsuarios(Connection conn) throws SQLException {
        String columnaPassword = columnaPasswordUsuarios(conn);
        if (columnaPassword != null) return columnaPassword;
        if (existeColumna(conn, TABLA_ADMINISTRADOR, "usuario")) return "usuario";
        return "''";
    }

    private static boolean esquemaSimpleUsuarios(Connection conn) throws SQLException {
        return existeColumna(conn, TABLA_ADMINISTRADOR, "usuario")
                && existeColumna(conn, TABLA_ADMINISTRADOR, "rol")
                && !existeColumna(conn, TABLA_ADMINISTRADOR, "id");
    }

    private static void actualizarRolUsuariosAAdministrador(Connection conn) throws SQLException {
        if (!existeTabla(conn, TABLA_ADMINISTRADOR) || !existeColumna(conn, TABLA_ADMINISTRADOR, "rol")) return;

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("UPDATE " + TABLA_ADMINISTRADOR + " SET rol = 'ADMINISTRADOR' WHERE UPPER(rol) = 'USUARIOS'");
        }
    }

    private static void asegurarPasswordAdministrador(Connection conn) throws SQLException {
        if (!tablaExiste(conn, TABLA_ADMINISTRADOR)) return;

        if (!existeColumna(conn, TABLA_ADMINISTRADOR, "password") && !existeColumna(conn, TABLA_ADMINISTRADOR, "contrasena")) {
            String tipo = esOracle() ? "VARCHAR2(255)" : "VARCHAR(255)";
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE " + TABLA_ADMINISTRADOR + " ADD password " + tipo);
            }
        }

        String columnaPassword = columnaPasswordUsuarios(conn);
        if (columnaPassword == null) return;

        String updateAdmin = "UPDATE " + TABLA_ADMINISTRADOR + " SET " + columnaPassword + " = ? "
                + "WHERE LOWER(id) = ? OR LOWER(correo) = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateAdmin)) {
            stmt.setString(1, "rutapraxis123");
            stmt.setString(2, "rutapraxis");
            stmt.setString(3, "rutapraxis");
            stmt.executeUpdate();
        }
    }

    private static void asegurarIdAutoincrementalAdministrador(Connection conn) throws SQLException {
        if (!esOracle() || !tablaExiste(conn, TABLA_ADMINISTRADOR) || !existeColumna(conn, TABLA_ADMINISTRADOR, "id")) return;

        if (!existeSecuenciaOracle(conn, "ADMINISTRADOR_ID_SEQ")) {
            int siguienteId = obtenerSiguienteIdAdministrador(conn);
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE SEQUENCE administrador_id_seq START WITH " + siguienteId + " INCREMENT BY 1 NOCACHE");
            }
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    "CREATE OR REPLACE TRIGGER administrador_bi "
                            + "BEFORE INSERT ON " + TABLA_ADMINISTRADOR + " "
                            + "FOR EACH ROW "
                            + "BEGIN "
                            + "IF :NEW.id IS NULL THEN "
                            + "SELECT administrador_id_seq.NEXTVAL INTO :NEW.id FROM dual; "
                            + "END IF; "
                            + "END;"
            );
        }
    }

    private static boolean existeSecuenciaOracle(Connection conn, String secuencia) throws SQLException {
        String query = "SELECT COUNT(*) FROM USER_SEQUENCES WHERE SEQUENCE_NAME = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, secuencia.toUpperCase(Locale.ROOT));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private static int obtenerSiguienteIdAdministrador(Connection conn) throws SQLException {
        int maxId = 0;
        String query = "SELECT id FROM " + TABLA_ADMINISTRADOR;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String id = rs.getString(1);
                if (id == null || !id.matches("\\d+")) continue;
                maxId = Math.max(maxId, Integer.parseInt(id));
            }
        }
        return maxId + 1;
    }

    public static ArrayList<Usuario> cargarUsuarios() {
        ArrayList<Usuario> lista = new ArrayList<>();
        try (Connection conn = obtenerConexion()) {
            if (conn == null) return lista;

            prepararTablaAdministrador(conn);
            asegurarPasswordAdministrador(conn);
            String columnaPassword = expresionPasswordUsuarios(conn);
            if (esquemaSimpleUsuarios(conn)) {
                cargarUsuariosEsquemaSimple(conn, lista, columnaPassword);
                return lista;
            }

            String query = "SELECT id, nombre_completo, correo, rol, ultimo_acceso, estado, " + columnaPassword + " FROM " + TABLA_ADMINISTRADOR;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    lista.add(new Usuario(
                            rs.getString(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4),
                            rs.getString(5),
                            rs.getString(6),
                            rs.getString(7)
                    ));
                }
            } catch (SQLException e) {
                if (e.getMessage() != null && e.getMessage().contains("ORA-00904")) {
                    lista.clear();
                    cargarUsuariosEsquemaSimple(conn, lista, columnaPassword);
                    return lista;
                }
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar usuarios: " + e.getMessage());
        }
        return lista;
    }

    private static void cargarUsuariosEsquemaSimple(Connection conn, ArrayList<Usuario> lista, String columnaPassword) throws SQLException {
        String query = "SELECT usuario, " + columnaPassword + ", rol FROM " + TABLA_ADMINISTRADOR;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String usuario = rs.getString(1);
                lista.add(new Usuario(
                        usuario,
                        usuario,
                        usuario,
                        rs.getString(3),
                        "",
                        "Activo",
                        rs.getString(2)
                ));
            }
        }
    }

    public static void guardarUsuario(Usuario u) {
        guardarUsuario(u, u.id);
    }

    public static void guardarUsuario(Usuario u, String idAnterior) {
        try (Connection conn = obtenerConexion()) {
            if (conn == null) return;

            prepararTablaAdministrador(conn);
            asegurarPasswordAdministrador(conn);
            asegurarIdAutoincrementalAdministrador(conn);
            u.rol = Usuario.normalizarRolAdministrador(u.rol);
            u.actualizarEstadoPorUltimoAcceso();
            if (u.id == null || u.id.trim().isEmpty()) {
                u.id = String.valueOf(obtenerSiguienteIdAdministrador(conn));
            }
            String columnaPassword = columnaPasswordUsuarios(conn);
            if (esquemaSimpleUsuarios(conn)) {
                guardarUsuarioEsquemaSimple(conn, u, columnaPassword != null ? columnaPassword : "usuario");
                return;
            }

            boolean tieneColumnaUsuario = existeColumna(conn, TABLA_ADMINISTRADOR, "usuario");
            String update = columnaPassword != null
                    ? "UPDATE " + TABLA_ADMINISTRADOR + " SET id=?, nombre_completo=?, correo=?, rol=?, ultimo_acceso=?, estado=?, " + columnaPassword + "=?"
                            + (tieneColumnaUsuario ? ", usuario=?" : "") + " WHERE id=?"
                    : "UPDATE " + TABLA_ADMINISTRADOR + " SET id=?, nombre_completo=?, correo=?, rol=?, ultimo_acceso=?, estado=?"
                            + (tieneColumnaUsuario ? ", usuario=?" : "") + " WHERE id=?";
            String insert = columnaPassword != null
                    ? "INSERT INTO " + TABLA_ADMINISTRADOR + " (id, nombre_completo, correo, rol, ultimo_acceso, estado, " + columnaPassword
                            + (tieneColumnaUsuario ? ", usuario" : "") + ") VALUES (?, ?, ?, ?, ?, ?, ?" + (tieneColumnaUsuario ? ", ?" : "") + ")"
                    : "INSERT INTO " + TABLA_ADMINISTRADOR + " (id, nombre_completo, correo, rol, ultimo_acceso, estado"
                            + (tieneColumnaUsuario ? ", usuario" : "") + ") VALUES (?, ?, ?, ?, ?, ?" + (tieneColumnaUsuario ? ", ?" : "") + ")";
            try (PreparedStatement updateStmt = conn.prepareStatement(update)) {
                int idx = 1;
                updateStmt.setString(idx++, u.id);
                updateStmt.setString(idx++, u.nombreCompleto);
                updateStmt.setString(idx++, u.correo);
                updateStmt.setString(idx++, u.rol);
                updateStmt.setString(idx++, u.ultimoAcceso);
                updateStmt.setString(idx++, u.estado);
                if (columnaPassword != null) {
                    updateStmt.setString(idx++, u.contrasena);
                }
                if (tieneColumnaUsuario) updateStmt.setString(idx++, u.id);
                updateStmt.setString(idx, idAnterior);

                if (updateStmt.executeUpdate() == 0) {
                    try (PreparedStatement insertStmt = conn.prepareStatement(insert)) {
                        idx = 1;
                        insertStmt.setString(idx++, u.id);
                        insertStmt.setString(idx++, u.nombreCompleto);
                        insertStmt.setString(idx++, u.correo);
                        insertStmt.setString(idx++, u.rol);
                        insertStmt.setString(idx++, u.ultimoAcceso);
                        insertStmt.setString(idx++, u.estado);
                        if (columnaPassword != null) {
                            insertStmt.setString(idx++, u.contrasena);
                        }
                        if (tieneColumnaUsuario) insertStmt.setString(idx, u.id);
                        insertStmt.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                if (e.getMessage() != null && e.getMessage().contains("ORA-00904")) {
                    guardarUsuarioEsquemaSimple(conn, u, columnaPassword);
                    return;
                }
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error al guardar usuario: " + e.getMessage());
        }
    }

    private static void guardarUsuarioEsquemaSimple(Connection conn, Usuario u, String columnaPassword) throws SQLException {
        String update = "UPDATE " + TABLA_ADMINISTRADOR + " SET " + columnaPassword + "=?, rol=? WHERE usuario=?";
        String insert = "INSERT INTO " + TABLA_ADMINISTRADOR + " (usuario, " + columnaPassword + ", rol) VALUES (?, ?, ?)";
        try (PreparedStatement updateStmt = conn.prepareStatement(update)) {
            updateStmt.setString(1, u.contrasena);
            updateStmt.setString(2, u.rol);
            updateStmt.setString(3, u.id);

            if (updateStmt.executeUpdate() == 0) {
                try (PreparedStatement insertStmt = conn.prepareStatement(insert)) {
                    insertStmt.setString(1, u.id);
                    insertStmt.setString(2, u.contrasena);
                    insertStmt.setString(3, u.rol);
                    insertStmt.executeUpdate();
                }
            }
        }
    }

    public static ArrayList<TareaAsignada> cargarTareasAsignadas() {
        ArrayList<TareaAsignada> lista = new ArrayList<>();
        try (Connection conn = obtenerConexion()) {
            if (conn == null) return lista;

            String query = "SELECT * FROM tareas_asignadas";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    lista.add(new TareaAsignada(
                            rs.getString("clase"),
                            rs.getString("titulo"),
                            rs.getString("archivo_tutor"),
                            rs.getString("fecha_limite"),
                            rs.getString("hora_limite"),
                            rs.getString("descripcion")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar tareas asignadas: " + e.getMessage());
        }
        return lista;
    }

    public static void guardarTareaAsignada(TareaAsignada ta) {
        try (Connection conn = obtenerConexion()) {
            if (conn == null) return;

            String update = "UPDATE tareas_asignadas SET titulo=?, archivo_tutor=?, fecha_limite=?, hora_limite=?, descripcion=? WHERE clase=?";
            String insert = "INSERT INTO tareas_asignadas (clase, titulo, archivo_tutor, fecha_limite, hora_limite, descripcion) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement updateStmt = conn.prepareStatement(update)) {
                updateStmt.setString(1, ta.titulo);
                updateStmt.setString(2, ta.archivoTutor);
                updateStmt.setString(3, ta.fechaLimite);
                updateStmt.setString(4, ta.horaLimite);
                updateStmt.setString(5, ta.descripcion);
                updateStmt.setString(6, ta.clase);

                if (updateStmt.executeUpdate() == 0) {
                    try (PreparedStatement insertStmt = conn.prepareStatement(insert)) {
                        insertStmt.setString(1, ta.clase);
                        insertStmt.setString(2, ta.titulo);
                        insertStmt.setString(3, ta.archivoTutor);
                        insertStmt.setString(4, ta.fechaLimite);
                        insertStmt.setString(5, ta.horaLimite);
                        insertStmt.setString(6, ta.descripcion);
                        insertStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al guardar tarea asignada: " + e.getMessage());
        }
    }

    public static void eliminarTareaAsignada(String clase) {
        try (Connection conn = obtenerConexion()) {
            if (conn == null) return;

            String query = "DELETE FROM tareas_asignadas WHERE clase = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, clase);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error al eliminar tarea asignada: " + e.getMessage());
        }
    }

    public static ArrayList<TareaEntregada> cargarTareasEntregadas() {
        ArrayList<TareaEntregada> lista = new ArrayList<>();
        try (Connection conn = obtenerConexion()) {
            if (conn == null) return lista;

            String query = "SELECT * FROM tareas_entregadas";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    TareaEntregada te = new TareaEntregada(
                            rs.getString("estudiante"),
                            rs.getString("clase"),
                            rs.getString("titulo"),
                            rs.getString("archivo"),
                            rs.getString("fecha_entrega"),
                            rs.getString("hora_entrega")
                    );
                    te.estado = rs.getString("estado");
                    te.comentario = rs.getString("comentario");
                    te.comentarioAsesor = rs.getString("comentario_asesor");
                    te.calificacion = rs.getDouble("calificacion");
                    lista.add(te);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar tareas entregadas: " + e.getMessage());
        }
        return lista;
    }

    public static void guardarTareaEntregada(TareaEntregada te) {
        try (Connection conn = obtenerConexion()) {
            if (conn == null) return;

            String update = "UPDATE tareas_entregadas SET titulo=?, archivo=?, fecha_entrega=?, hora_entrega=?, estado=?, comentario=?, comentario_asesor=?, calificacion=? WHERE estudiante=? AND clase=?";
            String insert = "INSERT INTO tareas_entregadas (estudiante, clase, titulo, archivo, fecha_entrega, hora_entrega, estado, comentario, comentario_asesor, calificacion) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement updateStmt = conn.prepareStatement(update)) {
                updateStmt.setString(1, te.titulo);
                updateStmt.setString(2, te.archivo);
                updateStmt.setString(3, te.fechaEntrega);
                updateStmt.setString(4, te.horaEntrega);
                updateStmt.setString(5, te.estado);
                updateStmt.setString(6, te.comentario);
                updateStmt.setString(7, te.comentarioAsesor);
                updateStmt.setDouble(8, te.calificacion);
                updateStmt.setString(9, te.estudiante);
                updateStmt.setString(10, te.clase);

                if (updateStmt.executeUpdate() == 0) {
                    try (PreparedStatement insertStmt = conn.prepareStatement(insert)) {
                        insertStmt.setString(1, te.estudiante);
                        insertStmt.setString(2, te.clase);
                        insertStmt.setString(3, te.titulo);
                        insertStmt.setString(4, te.archivo);
                        insertStmt.setString(5, te.fechaEntrega);
                        insertStmt.setString(6, te.horaEntrega);
                        insertStmt.setString(7, te.estado);
                        insertStmt.setString(8, te.comentario);
                        insertStmt.setString(9, te.comentarioAsesor);
                        insertStmt.setDouble(10, te.calificacion);
                        insertStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al guardar tarea entregada: " + e.getMessage());
        }
    }
}
