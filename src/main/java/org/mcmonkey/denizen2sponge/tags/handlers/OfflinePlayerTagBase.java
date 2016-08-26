package org.mcmonkey.denizen2sponge.tags.handlers;

import org.mcmonkey.denizen2core.tags.AbstractTagBase;
import org.mcmonkey.denizen2core.tags.AbstractTagObject;
import org.mcmonkey.denizen2core.tags.TagData;
import org.mcmonkey.denizen2sponge.tags.objects.OfflinePlayerTag;

public class OfflinePlayerTagBase extends AbstractTagBase {

    // <--[tagbase]
    // @Base offline_player[<OfflinePlayerTag>]
    // @Group Sponge Base Types
    // @ReturnType OfflinePlayerTag
    // @Returns the input as an OfflinePlayerTag.
    // -->

    @Override
    public String getName() {
        return "offline_player";
    }

    @Override
    public AbstractTagObject handle(TagData data) {
        return OfflinePlayerTag.getFor(data.error, data.getNextModifier()).handle(data.shrink());
    }
}
