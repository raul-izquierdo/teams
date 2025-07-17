# Requirements

## Inputs

### Fichero CSV con los datos de los alumnos

Esta aplicación recibe como parámetro un fichero CSV generado con la opción "download" roster the Github Classroom.

Ejemplo de fichero CSV generado por Github Classroom:

```csv
"identifier","github_username","github_id","name"
"01-yaagma","yaagma","40261856",""
"02-cesar acebal","","",""
```

En formato de los "identifier" será siempre:
- Dos dígitos con el grupo del alumno
- Un guión
- Apellidos y nombre del alumno

### Nombre de la Organización de Github

Se pasa en linea de comando con la opción "-o <organization>". Si no se pasa, se utiliza el valor por defecto "eii".

### Token de acceso a la API de Github

Se pasa en linea de comando con la opción requerida de "-t <token>".

## Output

## Crear grupos (teams)

Se usarán los dos dígitos de los "identifier" para crear los grupos en Github.
El nombre del grupo (team) será la letra "G" en mayúscula seguido de los dos dígitos del "identifier".
Por ejemplo, con el fichero csv anterior, habría que crear los grupos "G01" y "G02".
Si un grupo ya existe, no se producirá un error. Simplemente se ignorará.

## Añadir alumnos a los grupos

Se añadirán los alumnos a los grupos creados. El grupo de cada alumno será el correspondiente a sus dos primeros dígitos del "identifier".
Por ejemplo, con el ejemplo CSV anterior, el alumno "01-yaagma" se añadirá al grupo "G01" y el alumno "02-cesar acebal" al grupo "G02".

Si un alumno ya está asignado al grupo, simplemente se deja como está; no se da error.
Es decir, el programa debe estar preparado para poder ejecutarse varias veces si se añaden nuevos alumnos al CSV y no dar error con los ya existentes.

Puede que algunos alumnos no tengan "github_username" o "github_id" en el CSV. En ese caso, se ignorarán esos alumnos y no se añadirán a ningún grupo.

## Logging

Debería imprimir información por consola de lo que va haciendo, como por ejemplo:
- Nombres de los Grupos recién creados
- El nombre de los grupos que ya estaban creados.
- Nombres de los alumnos añadidos a los grupos
- El número de alumnos que ya estaban asignados y, por tanto, han sido ignorados.
- El número de alumnos que no se han podido añadir a ningún grupo por no tener "github_username" o "github_id".

Y, por supuesto, cualquier error que pueda ocurrir durante la ejecución del programa y que haya impedido su correcta ejecución. Si todo ha ido bien, acabar con un mensaje de éxito bien visible.

Example output:
```bash
Groups
- Created teams: G01, G02
- Already created teams: G03, G04

Students
- Added students:
   01-yaagma → G01

- Skipped students:
   Already in team: 5
   Missing GitHub username: 2
```

## Other Requirements

Se deberá imprimir un mensaje de ayuda si se ejecuta el programa sin los parámetros necesarios o con los parámetros incorrectos, indicando el uso correcto del programa. Todo ello en inglés.
If the program is executed with the `-h` or `--help` option, it should display this same info.
