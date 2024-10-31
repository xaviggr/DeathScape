package com.deathscape.plugin;

import org.bukkit.plugin.java.JavaPlugin;

public class DeathScape extends JavaPlugin {

    @Override
    public void onEnable() {
        // Código a ejecutar cuando el plugin se activa
        getLogger().info("TuPlugin ha sido habilitado.");
    }

    @Override
    public void onDisable() {
        // Código a ejecutar cuando el plugin se desactiva
        getLogger().info("TuPlugin ha sido deshabilitado.");
    }
}