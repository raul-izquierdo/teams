# Requirements

## Inputs

### Fichero CSV con los datos de los alumnos

Esta aplicación recibe como parámetro un fichero CSV generado con la opción "download" roster the Github Classroom.

Ejemplo de fichero CSV generado por Github Classroom:

```csv
"identifier","github_username","github_id","name"
"01-Izquierdo Raúl","raul22","40261856",""
"02-Gonzalez Manolo","","",""
```

En formato de los "identifier" será siempre:
- Dos dígitos con el grupo del alumno
- Un guión
- Apellidos y nombre del alumno


### Nombre de la Organización de Github

Debe proporcionarse en la línea de comandos con la opción obligatoria "-o <organization>". Este parámetro es requerido; no existe valor por defecto.


### Token de acceso a la API de Github

Debe proporcionarse en la línea de comandos con la opción obligatoria "-t <token>".

## Output

## Crear grupos (teams)

Se usarán los dos dígitos de los "identifier" para crear los grupos en Github.
El nombre del grupo (team) será la letra "G" en mayúscula seguido de los dos dígitos del "identifier".
Por ejemplo, con el fichero csv anterior, habría que crear los grupos "G01" y "G02".
Si un grupo ya existe, no se producirá un error. Simplemente se ignorará.

## Añadir alumnos a los grupos

Se añadirán los alumnos a los grupos creados. El grupo de cada alumno será el correspondiente a sus dos primeros dígitos del "identifier".
Por ejemplo, con el ejemplo CSV anterior, el alumno "01-Izquierdo Raúl" se añadirá al grupo "G01" y el alumno "02-Gonzalez Manolo" al grupo "G02".

Si un alumno ya está asignado al grupo, simplemente se deja como está; no se da error.
Es decir, el programa debe estar preparado para poder ejecutarse varias veces si se añaden nuevos alumnos al CSV y no dar error con los ya existentes.

Puede que algunos alumnos no tengan "github_username" o "github_id" en el CSV. En ese caso, se ignorarán esos alumnos y no se añadirán a ningún grupo.

## Other Requirements

Se deberá imprimir un mensaje de ayuda si se ejecuta el programa sin los parámetros necesarios o con los parámetros incorrectos, indicando el uso correcto del programa. Todo ello en inglés.
Si el programa se ejecuta con la opción `-h` o `--help`, debe mostrar esta misma información.
