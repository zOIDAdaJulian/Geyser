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

package org.geysermc.connector.entity.metadata;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.Pose;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.Position;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.nukkitx.protocol.bedrock.data.entity.EntityData;
import com.nukkitx.protocol.bedrock.data.entity.EntityFlag;
import net.kyori.adventure.text.Component;
import org.geysermc.connector.entity.type.Entity;
import org.geysermc.connector.entity.type.LivingEntity;
import org.geysermc.connector.entity.type.player.PlayerEntity;

public class MetadataRemappers {
    // Entity
    public static final MetadataRemapper<Entity> FLAGS = (entity, obj) -> entity.setFlags((byte) obj);
    public static final MetadataRemapper<Entity> AIR = (entity, obj) -> entity.setAir((int) obj);
    public static final MetadataRemapper<Entity> CUSTOM_NAME = (entity, obj) -> entity.setDisplayName((Component) obj);
    public static final MetadataRemapper<Entity> CUSTOM_NAME_VISIBLE = (entity, obj) -> entity.setDisplayNameVisible((boolean) obj);
    public static final MetadataRemapper<Entity> SILENT = MetadataRemapper.flag(EntityFlag.SILENT);
    public static final MetadataRemapper<Entity> NO_GRAVITY = (entity, obj) -> entity.setNoGravity((boolean) obj);
    public static final MetadataRemapper<Entity> POSE = (entity, obj) -> entity.setPose((Pose) obj);
    public static final MetadataRemapper<Entity> FREEZING = (entity, obj) -> entity.setFreezing(Math.min((int) obj, 140) / 140f);

    // Living Entity
    public static final MetadataRemapper<LivingEntity> BLOCKING = (entity, obj) -> entity.setBlocking((byte) obj);
    public static final MetadataRemapper<LivingEntity> HEALTH = (entity, obj) -> entity.setHealthData((float) obj);
    public static final MetadataRemapper<LivingEntity> COLOR = MetadataRemapper.generic(EntityData.EFFECT_COLOR);
    public static final MetadataRemapper<LivingEntity> AMBIENCE = MetadataRemapper.generic(EntityData.EFFECT_AMBIENT, obj -> (byte) ((boolean) obj ? 1 : 0));
    public static final MetadataRemapper<LivingEntity> BED_POSITION = (entity, obj) -> entity.setBedPosition((Position) obj);

    // Player
    public static final MetadataRemapper<PlayerEntity> ABSORPTION_HEARTS = (entity, obj) -> entity.setAbsorptionHearts((float) obj);
    public static final MetadataRemapper<PlayerEntity> SKIN_CUSTOMIZATION = (entity, obj) -> entity.setSkinCustomization((byte) obj);
    public static final MetadataRemapper<PlayerEntity> RIDING_SHOULDER_LEFT = (entity, obj) -> entity.setRidingShoulder((CompoundTag) obj, true);
    public static final MetadataRemapper<PlayerEntity> RIDING_SHOULDER_RIGHT = (entity, obj) -> entity.setRidingShoulder((CompoundTag) obj, false);

    public static <T extends Entity> MetadataRemapper<T> empty() {
        return (entity, obj) -> { };
    }
}
