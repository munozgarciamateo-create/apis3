-- Ejecutar UNA sola vez en Studio > Console (base de datos: apis3.sqlite)
CREATE TABLE IF NOT EXISTS favoritos (
    clave      TEXT PRIMARY KEY NOT NULL,
    titulo     TEXT NOT NULL,
    subtitulo  TEXT NOT NULL,
    imagen     TEXT,
    datosJson  TEXT NOT NULL,
    agregadoEn INTEGER NOT NULL
);
