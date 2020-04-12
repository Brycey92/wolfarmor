package dev.satyrn.wolfarmor.advancements;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WolfArmorTrigger implements ICriterionTrigger<WolfArmorTrigger.Instance> {

    private final Map<PlayerAdvancements, Listeners> listeners = Maps.newHashMap();
    private final ResourceLocation id;

    public WolfArmorTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    @Nonnull
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public void addListener(@Nonnull PlayerAdvancements playerAdvancementsIn, @Nonnull Listener<Instance> listener) {
        Listeners listeners = this.listeners.get(playerAdvancementsIn);

        if(listeners == null) {
            listeners = new Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, listeners);
        }

        listeners.add(listener);
    }

    @Override
    public void removeListener(@Nonnull PlayerAdvancements playerAdvancementsIn, @Nonnull Listener<Instance> listener) {
        Listeners listeners = this.listeners.get(playerAdvancementsIn);

        if(listeners != null) {
            listeners.remove(listener);

            if(listeners.isEmpty()) {
                this.listeners.remove(playerAdvancementsIn);
            }
        }
    }

    @Override
    public void removeAllListeners(@Nonnull PlayerAdvancements playerAdvancementsIn) {
        this.listeners.remove(playerAdvancementsIn);
    }

    @Override
    @Nonnull
    public Instance deserializeInstance(@Nonnull JsonObject json, @Nonnull JsonDeserializationContext context) {
        return new Instance(this.id, EntityPredicate.deserialize(json.get("entity")));
    }

    public void trigger(ServerPlayerEntity player, Entity entity) {
        Listeners listeners = this.listeners.get(player.getAdvancements());

        if(listeners != null) {
            listeners.trigger(player, entity);
        }
    }

    static final class Instance extends CriterionInstance {
        private EntityPredicate target;

        Instance(ResourceLocation criterion, EntityPredicate target) {
            super(criterion);
            this.target = target;
        }

        boolean test(ServerPlayerEntity player, Entity entity) {
            return this.target.test(player, entity);
        }
    }

    static final class Listeners {
        private final PlayerAdvancements playerAdvancements;
        private final Set<Listener<Instance>> listeners = Sets.newHashSet();

        /**
         * Creates a new instance of the listener set wrapper.
         * @param playerAdvancements The <tt>PlayerAdvancements</tt> object to initialize from.
         */
        Listeners(@Nonnull PlayerAdvancements playerAdvancements) {
            this.playerAdvancements = playerAdvancements;
        }

        /**
         * Checks if the underlying hash set is empty
         * @return <tt>true</tt> if the listener set is empty; otherwise, <tt>false</tt>.
         */
        boolean isEmpty() {
            return listeners.isEmpty();
        }

        /**
         * Adds an item to the listener set.
         * @param listener The listener instance to add.
         */
        void add(Listener<Instance> listener) {
            listeners.add(listener);
        }

        /**
         * Removes a listener from the listener set.
         * @param listener The listener instance to remove.
         */
        void remove(Listener<Instance> listener) {
            listeners.remove(listener);
        }

        public void trigger(ServerPlayerEntity player, Entity entity) {
            List<Listener<Instance>> list = null;

            for(Listener<Instance> listener : listeners) {
                if(listener.getCriterionInstance().test(player, entity)) {
                    if(list == null) {
                        list = Lists.newArrayList();
                    }
                    list.add(listener);
                }
            }

            if(list != null) {
                for(Listener<Instance> listener : list) {
                    listener.grantCriterion(this.playerAdvancements);
                }
            }
        }
    }
}
