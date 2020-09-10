package me.schooltests.regenblocks.events.api;

import me.schooltests.regenblocks.regions.RegenData;
import me.schooltests.regenblocks.regions.RegenRegion;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RegenerateEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();

    /**
     * Bukkit Event implementation for listening to events
     * @return HandlerList for this event
     */
    public static HandlerList getHandlerList() {
        return handlerList;
    }

    /**
     * Bukkit Event implementation for listening to events
     * @return HandlerList for this event
     */
    public HandlerList getHandlers() {
        return handlerList;
    }

    private boolean cancelled;
    private RegenData regenData;
    private RegenRegion region;

    public RegenerateEvent(RegenRegion region, RegenData regenData) {
        this.region = region;
        this.regenData = regenData;
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

    public RegenData getRegenData() {
        return regenData;
    }

    public void setRegenData(RegenData regenData) {
        this.regenData = regenData;
    }

    public RegenRegion getRegion() {
        return region;
    }
}
