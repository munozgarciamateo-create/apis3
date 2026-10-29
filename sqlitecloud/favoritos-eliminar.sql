-- Edge Function: favoritos-eliminar  (Language: SQL, Database: apis3.sqlite)
-- Valor del cuerpo JSON: clave (se usa como $clave)
DELETE FROM favoritos WHERE clave = $clave;
