# Backend – Torneo de fútbol amateur (Spring Boot)

API REST para la app de React Native. Spring Boot 4 · Java 17 · JPA/Hibernate · H2 en memoria.

## Cómo correrlo

```bash
./mvnw spring-boot:run        # en Windows: mvnw.cmd spring-boot:run
./mvnw test                   # pruebas de las reglas de negocio
```

- API: `http://localhost:8080`
- Consola de la base: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:torneo`, usuario `sa`, sin contraseña)
- Al arrancar se cargan datos de ejemplo (Premier League 2026-27, fechas 6 a 8; `torneo` id 1).
  Para arrancar vacío: `app.seed=false` en `application.properties`.
- La base es en memoria: se borra al apagar el servidor.

Desde la app: `EXPO_PUBLIC_API_URL` define la dirección (emulador Android: `http://10.0.2.2:8080`;
celular real: la IP de tu PC, y el celular y la PC en la misma red).

## Capas

`controller` (HTTP, DTOs) → `service` (reglas de negocio) → `repository` (Spring Data JPA) → `entity` (tablas).
Los errores salen siempre como `{ "estado": 409, "mensaje": "..." }`, que es lo que muestra la app.

## Endpoints

| Método y ruta | Qué hace |
|---|---|
| `GET /torneos/{id}/partidos` | Partidos del torneo con el marcador calculado |
| `GET /partidos/{id}` | Un partido |
| `GET /partidos/{id}/eventos` | Goles y tarjetas, ordenados por minuto |
| `GET /partidos/{id}/jugadores` | Planteles `{ local, visitante }` |
| `POST /partidos/{id}/eventos` | Carga un evento `{ jugadorId, tipo, minuto }` |
| `DELETE /partidos/{id}/eventos/{eventoId}` | Corrige un evento mal cargado |
| `PATCH /partidos/{id}/estado` | Cambia el estado `{ estado }` |
| `GET /torneos/{id}/posiciones` | Tabla de posiciones |
| `GET /torneos/{id}/goleadores` | Goleadores del torneo |
| `GET /torneos/{id}/suspendidos?fecha=N` | Suspendidos para la fecha N (sin parámetro: la fecha en curso) |
| `POST /partidos`, `PUT /partidos/{id}` | Programar / reprogramar un partido (valida cancha y árbitro) |
| `GET/POST /torneos`, `GET /torneos/{id}/equipos`, `POST /equipos`, `GET /equipos/{id}/jugadores`, `POST /jugadores`, `GET/POST /canchas`, `GET/POST /arbitros` | Alta y consulta del resto de las entidades |

## Reglas de negocio (dónde vive cada una)

| Regla | Dónde | Cómo |
|---|---|---|
| 1. Cancha sin partidos superpuestos | Backend (`PartidoService`) | Se pisan si `inicio1 < fin2 && inicio2 < fin1`. Los SUSPENDIDOS no ocupan la cancha. 409 |
| 2. Árbitro sin dos partidos a la vez | Backend (`PartidoService`) | Mismo criterio de superposición. 409 |
| 3. 2 amarillas o 1 roja → suspendido la fecha siguiente | Backend (`SuspensionService`) | Se calcula desde los eventos y `nro_fecha`; las amarillas se acumulan en el torneo (cada par suspende una fecha). Además impide cargar eventos de un suspendido en esa fecha. 422 |
| 4. Eventos solo de jugadores de los dos equipos | Backend (`EventoService`) | El `lado` (LOCAL/VISITANTE) lo deduce el backend por el equipo del jugador. 422 |
| Estados del partido | Backend + CHECK en la base | `PROGRAMADO → EN_JUEGO → FINALIZADO / SUSPENDIDO`. Los eventos solo se cargan/borran con el partido EN_JUEGO |
| Equipo contra sí mismo, minuto 0..120, camiseta 1..99, fin > inicio | Backend **y** base (CHECK) | Doble protección: el backend da un mensaje claro, la base es la red de seguridad |
| DNI único, camiseta única por equipo, nombre de equipo único por torneo | Backend (mensaje claro) **y** base (UNIQUE) | idem |

Decisiones del modelo:
- El **resultado no se guarda**: se calcula desde los eventos (`GOL` suma al equipo del jugador, `GOL_EN_CONTRA` al rival), así nunca puede contradecirse con los eventos.
- Tabla de posiciones: 3 puntos por victoria, 1 por empate, solo partidos FINALIZADOS. Desempate: diferencia de gol, goles a favor, orden alfabético.
- Goleadores: cuentan solo los `GOL` (no los goles en contra).
- `evento_partido.partido_id` tiene `ON DELETE CASCADE`: si se borra el partido, se borran sus eventos.
- El estado se guarda como texto (`EnumType.STRING`) con un CHECK que rechaza valores fuera de la lista.
- Nombres: tablas y columnas en `snake_case` (`nro_fecha`, `equipo_local_id`); en Java, `camelCase` (`nroFecha`, `equipoLocal`).
