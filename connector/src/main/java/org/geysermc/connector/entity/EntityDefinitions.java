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

import com.github.steveice10.mc.protocol.data.game.entity.type.EntityType;
import org.geysermc.connector.entity.type.Entity;
import org.geysermc.connector.entity.type.FallingBlockEntity;
import org.geysermc.connector.entity.type.LivingEntity;
import org.geysermc.connector.entity.type.player.PlayerEntity;

import static org.geysermc.connector.entity.metadata.MetadataRemappers.*;

public final class EntityDefinitions {
    public static final EntityDefinition<Entity> ENTITY = EntityDefinition.of(Entity::new)
            .bedrockId(0)
            .identifier("minecraft:entity")
            .width(0.6f)
            .height(1.6f)
            .metaRemapper(FLAGS) // 0
            .metaRemapper(AIR) // 1
            .metaRemapper(CUSTOM_NAME) // 2
            .metaRemapper(CUSTOM_NAME_VISIBLE) // 3
            .metaRemapper(SILENT) // 4
            .metaRemapper(NO_GRAVITY) // 5
            .metaRemapper(POSE) // 6
            .metaRemapper(FREEZING); // 7

    public static final EntityDefinition<LivingEntity> LIVING_ENTITY = EntityDefinition.of(LivingEntity::new)
            .inherits(ENTITY)
            .metaRemapper(BLOCKING) // 8
            .metaRemapper(HEALTH) // 9
            .metaRemapper(COLOR) // 10
            .metaRemapper(AMBIENCE) // 11
            .metaRemapper(empty()) // 12
            .metaRemapper(empty()) // 13
            .metaRemapper(BED_POSITION); // 14

    public static final EntityDefinition<PlayerEntity> PLAYER = EntityDefinition.of(PlayerEntity::new)
            .inherits(LIVING_ENTITY)
            .type(EntityType.PLAYER)
            .bedrockId(63)
            .identifier("minecraft:player")
            .width(0.6f)
            .height(1.8f)
            .offset(1.64f)
            .metaRemapper(ABSORPTION_HEARTS) // 15
            .metaRemapper(empty()) // 16
            .metaRemapper(SKIN_CUSTOMIZATION) // 17
            .metaRemapper(empty()) // 18
            .metaRemapper(RIDING_SHOULDER_LEFT) // 19
            .metaRemapper(RIDING_SHOULDER_RIGHT) // 20
            .register();

    public static final EntityDefinition<FallingBlockEntity> FALLING_BLOCK = EntityDefinition.of(FallingBlockEntity::new)
            .inherits(ENTITY)
            .type(EntityType.FALLING_BLOCK)
            .bedrockId(66)
            .identifier("minecraft:falling_block")
            .width(0.98f)
            .height(0.98f)
            .register();

    public static final EntityDefinition<Entity> EVOKER_FANGS = EntityDefinition.of(Entity::new)
            .inherits(ENTITY)
            .type(EntityType.EVOKER_FANGS)
            .bedrockId(103)
            .identifier("minecraft:evocation_fang")
            .width(0.5f)
            .height(0.8f)
            .register();
}
