package me.schooltests.regenblocks.events.api;

import me.schooltests.regenblocks.regions.RegenData;
import me.schooltests.regenblocks.regions.RegenRegion;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RegenerateEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public HandlerList getHandlers() {
        return handlerList;
    }

    private boolean cancelled;
    private RegenData data;
    private RegenRegion region;
    private Location location;

    public RegenerateEvent(RegenRegion region, RegenData data, Location location) {
        this.region = region;
        this.data = data;
        this.location = location;
        this.cancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    public RegenData getData() {
        return data;
    }

    public void setData(RegenData data) {
        this.data = data;
    }

    public RegenRegion getRegion() {
        return region;
    }

    public Location getLocation() {
        return location;
    }
}
