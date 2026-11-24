# Cómo usar Git en este proyecto

Git guarda versiones de los archivos para consultar el historial, comparar cambios y recuperar versiones anteriores. Cada proyecto tiene su propio historial dentro de `.git/`.

## Trabajo diario

Abre la terminal en esta carpeta y ejecuta:

```bash
git status
git diff
git add .
git diff --cached
git commit -m "Describe lo que cambiaste"
```

`status` muestra pendientes; `diff` compara cambios de texto; `add` prepara la próxima versión; `diff --cached` permite revisar lo preparado; `commit` guarda esa versión. Guarda primero los archivos en tu editor. Los cambios posteriores necesitan otro commit.

## Consultar versiones

```bash
git log --oneline
git show HEAD
```

## Probar una idea en otra rama

```bash
git switch -c prueba-mejora
```

Haz los cambios y un commit. Para volver a la rama principal:

```bash
git switch main
```

Para incorporar una prueba terminada, desde main:

```bash
git merge prueba-mejora
```

## Recuperar un archivo

Primero consulta la versión guardada sin sobrescribir tu trabajo:

```bash
git show HEAD:README.md
```

`git restore README.md` descarta los cambios no preparados de ese archivo. Úsalo solo si deseas perder esas modificaciones. Git no recupera automáticamente cambios que nunca guardaste en un commit.

## Identidad y respaldo

Se configuró una identidad local: Jeremy <jeremy@localhost>. Es válida para trabajar en este equipo. Para usar otra identidad en este proyecto, sustituye los valores siguientes por tus datos reales:

```bash
git config user.name "Tu nombre"
git config user.email "Tu correo o correo privado de GitHub"
```

Git funciona sin Internet. GitHub es un servicio aparte para alojar repositorios y colaborar. Este repositorio todavía no tiene un remoto: los commits solo están en este equipo. Para tener respaldo externo, conserva una copia en otro dispositivo o conecta un remoto y sube tus commits. No borres `.git/`, porque contiene el historial.

Los archivos excluidos mediante `.gitignore` no se guardan con `git add .`. Conserva aparte los datos y configuraciones locales que necesites respaldar.

En Visual Studio Code puedes abrir esta carpeta y usar Control de código fuente (Ctrl+Shift+G) para revisar cambios y crear commits.
