package hu.jgj52.arenaCats.Types;

import org.bukkit.Location;

public record Waypoint(String name, Location location) {
    public double x() {
        return location.x();
    }

    public double y() {
        return location.y();
    }

    public double z() {
        return location.z();
    }

    public float yaw() {
        return location.getYaw();
    }

    public float pitch() {
        return location.getPitch();
    }
}
