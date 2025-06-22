package dc.Business.controllers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Controller that handles temporary player states such as buffs, debuffs, or custom effects.
 */
public class MetaDataManagerController {

    // Jugadores con doble daño (Beleño negro)
    private final Set<UUID> doubleDamagePlayers = new HashSet<>();

    // Jugadores con inmunidad al daño por caída (Néctar del Guardián)
    private final Set<UUID> noFallDamagePlayers = new HashSet<>();

    // ----- MÉTODOS PARA BELEÑO -----

    public void markPlayerDoubleDamage(UUID uuid, boolean active) {
        if (active) doubleDamagePlayers.add(uuid);
        else doubleDamagePlayers.remove(uuid);
    }

    public boolean isPlayerInDoubleDamage(UUID uuid) {
        return doubleDamagePlayers.contains(uuid);
    }

    // ----- MÉTODOS PARA INMUNIDAD CAÍDA -----

    public void markPlayerNoFallDamage(UUID uuid, boolean active) {
        if (active) noFallDamagePlayers.add(uuid);
        else noFallDamagePlayers.remove(uuid);
    }

    public boolean hasNoFallDamage(UUID uuid) {
        return noFallDamagePlayers.contains(uuid);
    }

    // ----- Limpieza o reinicio (opcional) -----

    public void clearAll() {
        doubleDamagePlayers.clear();
        noFallDamagePlayers.clear();
    }
}
