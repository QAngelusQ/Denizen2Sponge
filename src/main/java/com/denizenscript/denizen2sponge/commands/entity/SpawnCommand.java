package com.denizenscript.denizen2sponge.commands.entity;

import com.denizenscript.denizen2core.commands.AbstractCommand;
import com.denizenscript.denizen2core.commands.CommandEntry;
import com.denizenscript.denizen2core.commands.CommandQueue;
import com.denizenscript.denizen2core.tags.AbstractTagObject;
import com.denizenscript.denizen2core.tags.objects.BooleanTag;
import com.denizenscript.denizen2core.tags.objects.IntegerTag;
import com.denizenscript.denizen2core.tags.objects.MapTag;
import com.denizenscript.denizen2core.tags.objects.NumberTag;
import com.denizenscript.denizen2core.utilities.CoreUtilities;
import com.denizenscript.denizen2core.utilities.debugging.ColorSet;
import com.denizenscript.denizen2sponge.tags.objects.EntityTag;
import com.denizenscript.denizen2sponge.tags.objects.EntityTypeTag;
import com.denizenscript.denizen2sponge.tags.objects.FormattedTextTag;
import com.denizenscript.denizen2sponge.tags.objects.LocationTag;
import com.denizenscript.denizen2sponge.utilities.UtilLocation;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class SpawnCommand extends AbstractCommand {

    // <--[command]
    // @Name spawn
    // @Arguments <entity type> <location> [map of properties]
    // @Short Spawns a new entity.
    // @Updated 2016/09/26
    // @Group Entities
    // @Minimum 2
    // @Maximum 3
    // @Tag <[spawn_success]> (BooleanTag) returns whether the spawn passed.
    // @Tag <[spawn_entity]> (EntityTag) returns the entity that was spawned (only if the spawn passed).
    // @Description
    // Spawns an entity at the specified location. Optionally, specify a MapTag of properties
    // to spawn the entity with those values automatically set on it.
    // @Example
    // # Spawns a sheep that feels the burn.
    // - spawn sheep <[player].location> display_name:<texts.for_input[text:Bahhhb]>|max_health:300|health:300|fire_ticks:999999|is_sheared:true
    // -->

    @Override
    public String getName() {
        return "spawn";
    }

    @Override
    public String getArguments() {
        return "<entity type> <location> [map of properties]";
    }

    @Override
    public int getMinimumArguments() {
        return 2;
    }

    @Override
    public int getMaximumArguments() {
        return 3;
    }

    @Override
    public void execute(CommandQueue queue, CommandEntry entry) {
        EntityTypeTag entityTypeTag = EntityTypeTag.getFor(queue.error, entry.getArgumentObject(queue, 0));
        EntityType entityType = entityTypeTag.getInternal();
        LocationTag locationTag = LocationTag.getFor(queue.error, entry.getArgumentObject(queue, 1));
        UtilLocation location = locationTag.getInternal();
        if (location.world == null) {
            queue.handleError(entry, "Invalid location with no world in Spawn command!");
            return;
        }
        Entity entity = location.world.createEntity(entityType, location.toVector3d());
        Collection<Key> keys = Sponge.getRegistry().getAllOf(Key.class);
        if (entry.arguments.size() > 2) {
            MapTag propertyMap = MapTag.getFor(queue.error, entry.getArgumentObject(queue, 2));
            for (Map.Entry<String, AbstractTagObject> mapEntry : propertyMap.getInternal().entrySet()) {
                String property = CoreUtilities.toLowerCase(mapEntry.getKey());
                Key found = null;
                for (Key key : keys) {
                    if (property.equals(CoreUtilities.after(key.getId(), ":"))
                            || property.equals(key.getId())) {
                        found = key;
                    }
                }
                if (found == null) {
                    queue.handleError(entry, "Invalid property '" + property + "' in Spawn command!");
                    return;
                }
                if (!entity.supports(found)) {
                    queue.handleError(entry, "The entity type '" + entityType.getName()
                            + "' does not support the property '" + found.getId() + "'!");
                    return;
                }
                Class clazz = found.getElementToken().getRawType();
                if (Boolean.class.isAssignableFrom(clazz)) {
                    entity.offer(found, BooleanTag.getFor(queue.error, mapEntry.getValue()).getInternal());
                }
                else if (CatalogType.class.isAssignableFrom(clazz)) {
                    String string = mapEntry.getValue().toString();
                    Optional optCatalogType = Sponge.getRegistry().getType(clazz, string);
                    if (!optCatalogType.isPresent()) {
                        queue.handleError(entry, "Invalid value '" + string + "' for property '" + found.getId() + "'!");
                        return;
                    }
                    entity.offer(found, optCatalogType.get());
                }
                else if (Double.class.isAssignableFrom(clazz)) {
                    entity.offer(found, NumberTag.getFor(queue.error, mapEntry.getValue()).getInternal());
                }
                else if (Enum.class.isAssignableFrom(clazz)) {
                    entity.offer(found, Enum.valueOf(clazz, mapEntry.getValue().toString().toUpperCase()));
                }
                else if (Integer.class.isAssignableFrom(clazz)) {
                    entity.offer(found, (int) IntegerTag.getFor(queue.error, mapEntry.getValue()).getInternal());
                }
                else if (Text.class.isAssignableFrom(clazz)) {
                    entity.offer(found, FormattedTextTag.getFor(queue.error, mapEntry.getValue()).getInternal());
                }
                else {
                    queue.handleError(entry, "The value type '" + clazz.getName() + "' is not supported yet!");
                    return;
                }
            }
        }
        if (queue.shouldShowGood()) {
            queue.outGood("Spawning an entity of type "
                    + ColorSet.emphasis + entityType.getId() + ColorSet.base
                    + " with the specified properties at location "
                    + ColorSet.emphasis+ locationTag + ColorSet.base + "...");
        }
        boolean passed = location.world.spawnEntity(entity, Cause.source(EntitySpawnCause.builder()
                .entity(entity).type(SpawnTypes.PLUGIN)).build());
        // TODO: "Cause" argument!
        if (queue.shouldShowGood()) {
            queue.outGood("Spawning " + (passed ? "succeeded" : "was blocked") + "!");
        }
        queue.commandStack.peek().setDefinition("spawn_success", new BooleanTag(passed));
        if (passed) {
            queue.commandStack.peek().setDefinition("spawn_entity", new EntityTag(entity));
        }
    }
}
