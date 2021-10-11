/*
 * Copyright (c) 2019-2021 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Geyser
 */

package org.geysermc.connector.entity;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.EntityMetadata;
import com.github.steveice10.mc.protocol.data.game.entity.type.EntityType;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityMetadataPacket;
import com.nukkitx.math.vector.Vector3f;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.geysermc.connector.GeyserConnector;
import org.geysermc.connector.entity.metadata.MetadataRemapper;
import org.geysermc.connector.entity.type.Entity;
import org.geysermc.connector.network.session.GeyserSession;
import org.geysermc.connector.registry.Registries;
import org.geysermc.connector.utils.LanguageUtils;

import java.util.List;

@Getter
@Setter
@Accessors(fluent = true, chain = true)
public final class EntityDefinition<T extends Entity> {
    private EntityType type;
    private int bedrockId;
    private String identifier;
    private float width;
    private float height;
    private float offset;

    @Getter(AccessLevel.NONE) private final EntityFactory<T> entityFactory;
    @Getter(AccessLevel.NONE) private final List<MetadataRemapper<? super T>> mappers = new ObjectArrayList<>();

    private EntityDefinition(EntityFactory<T> entityFactory) {
        this.entityFactory = entityFactory;
    }

    protected EntityDefinition<T> inherits(EntityDefinition<? super T> definition) {
        this.type = definition.type;
        this.bedrockId = definition.bedrockId;
        this.identifier = definition.identifier;
        this.width = definition.width;
        this.height = definition.height;
        this.offset = definition.offset;
        this.mappers.addAll(definition.mappers);
        return this;
    }

    protected EntityDefinition<T> metaRemapper(MetadataRemapper<T> mapper) {
        this.mappers.add(mapper);
        return this;
    }

    protected EntityDefinition<T> register() {
        Registries.ENTITY_DEFINITIONS.register(this.type, this);
        return this;
    }

    public void updateMetadata(T entity, ServerEntityMetadataPacket packet) {
        for (EntityMetadata metadata : packet.getMetadata()) {
            MetadataRemapper<? super T> remapper = this.mappers.get(metadata.getId());
            if (remapper == null) {
                GeyserConnector.getInstance().getLogger().warning("Failed to find entity metadata with id " + metadata.getId());
                continue;
            }

            try {
                remapper.handle(entity, metadata.getValue());
            } catch (ClassCastException ex) {
                // Class cast exceptions are really the only ones we're going to get in normal gameplay
                // Because some entity rewriters forget about some values
                // Any other errors are actual bugs
                GeyserConnector.getInstance().getLogger().warning(LanguageUtils.getLocaleStringLog("geyser.network.translator.metadata.failed", metadata, entity.getDefinition().identifier()));
                GeyserConnector.getInstance().getLogger().debug("Entity Java ID: " + entity.getEntityId() + ", Geyser ID: " + entity.getGeyserId());
                if (GeyserConnector.getInstance().getConfig().isDebugMode()) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public T newEntity(GeyserSession session, long entityId, long geyserId, Vector3f position, Vector3f motion, Vector3f rotation) {
        return this.entityFactory.newEntity(this, session, entityId, geyserId, position, motion, rotation);
    }

    protected static <T extends Entity> EntityDefinition<T> of(EntityFactory<T> entityFactory) {
        return new EntityDefinition<>(entityFactory);
    }
}