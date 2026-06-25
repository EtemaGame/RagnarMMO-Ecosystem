# Ground Cells

Estado al 2026-06-24.

## Regla base

- `1 RO cell = 1 bloque de Minecraft` en plano horizontal X/Z.
- Una ground cell no es solo un punto: es un volumen.
- El volumen base cubre el bloque donde se coloca la skill y hasta 2 bloques de altura sobre esa base.

Modelo:

```text
cell = BlockPos(x, y, z)

area horizontal:
  x .. x + 1
  z .. z + 1

altura efectiva:
  y .. y + 2
```

En codigo, la deteccion de entidades deberia usar una `AABB` equivalente a:

```text
AABB(x, y, z, x + 1, y + 2, z + 1)
```

## Areas

- `3x3` = 3 bloques por 3 bloques.
- `5x5` = 5 bloques por 5 bloques.
- `Range 9` = hasta 9 bloques.
- `Knockback 2 cells` = empujar 2 bloques.

## Skills afectadas

- Safety Wall: 1 celda/bloque protegido.
- Fire Wall: linea de celdas/bloques persistentes.
- Pneuma: area de celdas/bloques que bloquea ataques ranged.
- Thunder Storm: area de impacto en celdas/bloques.
- Traps futuras: celdas persistentes con trigger.
- Sanctuary/Land Protector futuras: areas persistentes.

## Implementado

- Target ground por raycast de bloque para skills activas.
- Celdas persistentes por dimension con owner, duracion y hits restantes.
- Regla activa de solapamiento: una sola ground cell por bloque; colocar otra reemplaza la anterior en ese bloque.
- Pneuma: area 3x3 que bloquea ranged physical del pipeline propio y proyectiles vanilla.
- Safety Wall: 1 celda que bloquea melee physical y consume hits por golpe real.
- Fire Wall: linea de 3 celdas con dano Fire, budget de hits y knockback aproximado.
- Arrow Shower: ground target 3x3 con knockback desde la celda objetivo.
- Thunder Storm: ground target 5x5 con dano Wind separado por `hit_spacing_ticks`.
- La ruta de combate fisica informa `hitCount`, por lo que las barreras pueden consumir golpes multiples de forma proporcional.

## Pendiente de implementacion

- Definir como escoger la Y base cuando el target esta en pendiente, agua, slabs o bloques no completos.
- Reemplazar la politica temporal de reemplazo por reglas RO finales de solapamiento entre Safety Wall, Pneuma, Fire Wall y futuros ground effects.
- Reglas exactas de diagonales y max active para Fire Wall.
- Reglas finas de Thunder Storm si se necesita retarget por golpe, limites por objetivo o interacciones especiales por mapa.
