package com.denizenscript.denizen2sponge.events.player;

import com.denizenscript.denizen2core.events.ScriptEvent;
import com.denizenscript.denizen2core.tags.AbstractTagObject;
import com.denizenscript.denizen2core.tags.objects.DurationTag;
import com.denizenscript.denizen2sponge.Denizen2Sponge;
import com.denizenscript.denizen2sponge.events.D2SpongeEventHelper;
import com.denizenscript.denizen2sponge.tags.objects.ItemTag;
import com.denizenscript.denizen2sponge.tags.objects.LocationTag;
import com.denizenscript.denizen2sponge.tags.objects.PlayerTag;
import com.denizenscript.denizen2sponge.utilities.Utilities;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.world.World;

import java.util.HashMap;

public class PlayerStopsUsingItemScriptEvent extends ScriptEvent {

    // <--[event]
    // @Since 0.3.0
    // @Events
    // player stops using item
    //
    // @Updated 2017/10/14
    //
    // @Cancellable true
    //
    // @Group Player
    //
    // @Triggers when a player intentionally stops using an item.
    //
    // @Switch item (ItemTag) checks the item used.
    // @Switch world (WorldTag) checks the world.
    // @Switch cuboid (CuboidTag) checks the cuboid area.
    // @Switch weather (TextTag) checks the weather.
    // @Switch gamemode (TextTag) checks the player's gamemode.
    //
    // @Context
    // player (PlayerTag) returns the player that stopped using the item.
    // item (ItemTag) returns the used item.
    // duration (DurationTag) returns the maximum duration the item had remaining before finishing.
    //
    // @Determinations
    // None.
    // -->

    @Override
    public String getName() {
        return "PlayerStopsUsingItem";
    }

    @Override
    public boolean couldMatch(ScriptEventData data) {
        return data.eventPath.startsWith("player stops using item");
    }

    @Override
    public boolean matches(ScriptEventData data) {
        Player playerInternal = player.getOnline(this::error);
        World world = playerInternal.getWorld();
        return D2SpongeEventHelper.checkItem(item, data, this::error, "item")
                && D2SpongeEventHelper.checkWorld(world, data, this::error) && D2SpongeEventHelper.checkCuboid(
                new LocationTag(playerInternal.getLocation()).getInternal(), data, this::error)
                && D2SpongeEventHelper.checkWeather(Utilities.getIdWithoutDefaultPrefix(
                world.getWeather().getId()), data, this::error) && D2SpongeEventHelper.checkGamemode(
                Utilities.getIdWithoutDefaultPrefix(playerInternal.gameMode().get().getId()), data, this::error);
    }

    public PlayerTag player;

    public ItemTag item;

    public DurationTag duration;

    public UseItemStackEvent.Stop internal;

    @Override
    public HashMap<String, AbstractTagObject> getDefinitions(ScriptEventData data) {
        HashMap<String, AbstractTagObject> defs = super.getDefinitions(data);
        defs.put("player", player);
        defs.put("item", item);
        defs.put("duration", duration);
        return defs;
    }

    @Override
    public void enable() {
        Sponge.getEventManager().registerListeners(Denizen2Sponge.instance, this);
    }

    @Override
    public void disable() {
        Sponge.getEventManager().unregisterListeners(this);
    }

    @Listener
    public void onPlayerStopsUsingItem(UseItemStackEvent.Stop evt, @Root Player player) {
        PlayerStopsUsingItemScriptEvent event = (PlayerStopsUsingItemScriptEvent) clone();
        event.internal = evt;
        event.player = new PlayerTag(player);
        event.item = new ItemTag(evt.getItemStackInUse().createStack());
        event.duration = new DurationTag(evt.getRemainingDuration() / 20.0);
        event.cancelled = evt.isCancelled();
        event.run();
        evt.setCancelled(event.cancelled);
    }

    @Override
    public void applyDetermination(boolean errors, String determination, AbstractTagObject value) {
        super.applyDetermination(errors, determination, value);
    }
}
