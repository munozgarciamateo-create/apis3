-- Edge Function: favoritos-guardar  (Language: SQL, Database: apis3.sqlite)
-- Los valores del cuerpo JSON se usan con el prefijo $: clave, titulo, subtitulo, imagen, datosJson, agregadoEn
INSERT OR REPLACE INTO favoritos (clave, titulo, subtitulo, imagen, datosJson, agregadoEn)
VALUES ($clave, $titulo, $subtitulo, NULLIF($imagen, ''), $datosJson, $agregadoEn);
