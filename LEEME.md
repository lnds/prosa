# Esto es Prosa

Release 0.1

Prosa es un servidor de blogs que utiliza un estilo minimalista de edición de textos.

Para entender la motivación de este proyecto recomiendo leer el siguiente post: http://www.prosa.io/blog/lnds/2014/7/16/razones

Prosa está escrito en Scala y es posible gracias, principalmente, a los siguientes proyectos opensource:

- Play Framework: http://www.playframework.com/
- Slick 2.0: http://slick.typesafe.com/
- Medium Editor: https://github.com/daviferreira/medium-editor
- Medium Editor Insert Plugin: https://github.com/orthes/medium-editor-insert-plugin
- AWScala https://github.com/seratch/AWScala

El archivo INSTALAR tiene las instrucciones para instalar.


# Sobre el versionamiento

Uso el siguiente esquema

    Major.Minor.Ticket.Build
    
Major: es el numero de versión, 0 significa que está en etapa de pre-release (etapas alpha y beta), sólo cambia cuando se produce un cambio que es incompatible hacia atrás (o pasamos a la versión 1).
Minor: Actualmente uso el numero de sprint (ver más abajo).
Ticket: número del ticket resuelto en essa versión.
Build: un número incremental que vuelve a cero cuando se incrementa Minor. Normalmente un incremento en este numero implica un bugfix.

# Metodología

A partir de octubre estoy usando como metodología de trabajo controlada a través de mi Jira personal (más adelante lo publicaré).
El sprint tiene duración de 2 semanas e incluye varios tickets (mejoras, nuevas características y bugs).
Al empezar un Sprint incremento el valor Minor.

Trabajaré 1 ticket a la vez.
Cada ticket se trabaja en su propio branch en un repositorio stash interno (usando la metodología propuesta por Atlassian).

Al empezar a trabajar en un ticket cambio el valor de Ticket.

Al cerrar el ticket se integra a la rama master y además se hace el push a GitHub.



