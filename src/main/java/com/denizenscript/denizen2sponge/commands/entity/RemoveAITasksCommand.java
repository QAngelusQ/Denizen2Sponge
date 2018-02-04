package com.denizenscript.denizen2sponge.commands.entity;

import com.denizenscript.denizen2core.commands.AbstractCommand;
import com.denizenscript.denizen2core.commands.CommandEntry;
import com.denizenscript.denizen2core.commands.CommandQueue;
import com.denizenscript.denizen2core.tags.objects.TextTag;
import com.denizenscript.denizen2core.utilities.debugging.ColorSet;
import com.denizenscript.denizen2sponge.tags.objects.EntityTag;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.ai.GoalType;
import org.spongepowered.api.entity.ai.GoalTypes;
import org.spongepowered.api.entity.ai.task.AITaskType;
import org.spongepowered.api.entity.living.Agent;

import java.util.Optional;

public class RemoveAITasksCommand extends AbstractCommand {

    // <--[command]
    // @Since 0.4.0
    // @Name removeaitask
    // @Arguments <entity> <task type>
    // @Short removes AI tasks from an entity's goal.
    // @Updated 2018/01/27
    // @Group Entity
    // @Minimum 2
    // @Maximum 2
    // @Named goal (TextTag) Sets to from which goal the tasks will be removed.
    // @Description
    // Removes all AI tasks of the specified type from an entity's goal.
    // Related information: <@link explanation AI Goal Types>AI goal types<@/link>.
    // Related information: <@link explanation AI Task Types>AI task types<@/link>.
    // Related commands: <@link command addaitask>addaitask<@/link>.
    // @Example
    // # Makes the zombie in front of the player stop wandering around.
    // - removeaitask <player.target_entities[type:zombie].get[1]> wander --goal normal
    // -->

    @Override
    public String getName() {
        return "removeaitask";
    }

    @Override
    public String getArguments() {
        return "<entity> <task type>";
    }

    @Override
    public int getMinimumArguments() {
        return 2;
    }

    @Override
    public int getMaximumArguments() {
        return 2;
    }

    @Override
    public void execute(CommandQueue queue, CommandEntry entry) {
        EntityTag entityTag = EntityTag.getFor(queue.error, entry.getArgumentObject(queue, 0));
        try {
            Agent agent = (Agent) entityTag.getInternal();
            TextTag type = TextTag.getFor(queue.error, entry.getArgumentObject(queue, 1));
            Optional<AITaskType> opt = Sponge.getRegistry().getType(AITaskType.class, type.getInternal());
            if (!opt.isPresent()) {
                queue.handleError(entry, "Invalid task type '" + type.debug() + "' in RemoveAITask command!");
                return;
            }
            GoalType goal;
            if (entry.namedArgs.containsKey("goal")) {
                TextTag goalType = TextTag.getFor(queue.error, entry.getNamedArgumentObject(queue, "goal"));
                Optional<GoalType> goalOpt = Sponge.getRegistry().getType(GoalType.class, goalType.getInternal());
                if (goalOpt.isPresent()) {
                    goal = goalOpt.get();
                }
                else {
                    queue.handleError(entry, "Invalid goal type '" + goalType.debug() + "' in RemoveAITask command!");
                    return;
                }
            }
            else {
                goal = GoalTypes.NORMAL;
            }
            agent.getGoal(goal).get().removeTasks(opt.get());
            if (queue.shouldShowGood()) {
                queue.outGood("Removed tasks of type '" + ColorSet.emphasis + opt.get().getId()
                        + ColorSet.good + "' from goal '" + ColorSet.emphasis + goal.getId()
                        + ColorSet.good + "' of entity '" + ColorSet.emphasis + entityTag.debug()
                        + ColorSet.good + "'!");
            }
        }
        catch (ClassCastException e) {
            queue.handleError(entry, "This entity doesn't support AI goals and tasks!");
        }
    }
}
