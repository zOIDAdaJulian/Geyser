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

package org.geysermc.connector.entity.type;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.EntityMetadata;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.Pose;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityMetadataPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnEntityPacket;
import com.github.steveice10.packetlib.packet.Packet;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.data.entity.EntityData;
import com.nukkitx.protocol.bedrock.data.entity.EntityDataMap;
import com.nukkitx.protocol.bedrock.data.entity.EntityFlag;
import com.nukkitx.protocol.bedrock.data.entity.EntityFlags;
import com.nukkitx.protocol.bedrock.packet.AddEntityPacket;
import com.nukkitx.protocol.bedrock.packet.MoveEntityAbsolutePacket;
import com.nukkitx.protocol.bedrock.packet.RemoveEntityPacket;
import com.nukkitx.protocol.bedrock.packet.SetEntityDataPacket;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.geysermc.connector.entity.EntityDefinition;
import org.geysermc.connector.network.session.GeyserSession;
import org.geysermc.connector.network.translators.chat.MessageTranslator;
import org.geysermc.connector.utils.MathUtils;

@Getter
@Setter
public class Entity {
    protected final EntityDefinition<? extends Entity> definition;
    protected final GeyserSession session;

    protected long entityId;
    protected final long geyserId;

    protected Vector3f position;
    protected Vector3f motion;

    /**
     * x = Yaw, y = Pitch, z = HeadYaw
     */
    protected Vector3f rotation;

    /**
     * Saves if the entity should be on the ground. Otherwise entities like parrots are flapping when rotating
     */
    protected boolean onGround;

    protected boolean valid;

    protected LongOpenHashSet passengers = new LongOpenHashSet();
    protected EntityDataMap metadata = new EntityDataMap();

    public Entity(EntityDefinition<? extends Entity> definition, GeyserSession session, long entityId, long geyserId, Vector3f position, Vector3f motion, Vector3f rotation) {
        this.definition = definition;
        this.session = session;
        this.entityId = entityId;
        this.geyserId = geyserId;
        this.motion = motion;
        this.rotation = rotation;

        this.valid = false;

        setPosition(position);
        setAir(getMaxAir());

        metadata.put(EntityData.SCALE, 1f);
        metadata.put(EntityData.COLOR, 0);
        metadata.put(EntityData.MAX_AIR_SUPPLY, getMaxAir());
        metadata.put(EntityData.LEASH_HOLDER_EID, -1L);
        metadata.put(EntityData.BOUNDING_BOX_HEIGHT, definition.height());
        metadata.put(EntityData.BOUNDING_BOX_WIDTH, definition.width());
        EntityFlags flags = new EntityFlags();
        flags.setFlag(EntityFlag.HAS_GRAVITY, true);
        flags.setFlag(EntityFlag.HAS_COLLISION, true);
        flags.setFlag(EntityFlag.CAN_SHOW_NAME, true);
        flags.setFlag(EntityFlag.CAN_CLIMB, true);
        metadata.putFlags(flags);
    }

    public void spawnEntity(GeyserSession session) {
        AddEntityPacket addEntityPacket = new AddEntityPacket();
        addEntityPacket.setIdentifier(this.definition.identifier());
        addEntityPacket.setRuntimeEntityId(geyserId);
        addEntityPacket.setUniqueEntityId(geyserId);
        addEntityPacket.setPosition(position);
        addEntityPacket.setMotion(motion);
        addEntityPacket.setRotation(getBedrockRotation());
        addEntityPacket.setEntityType(this.definition.bedrockId());
        addEntityPacket.getMetadata().putAll(metadata);
        addAdditionalSpawnData(addEntityPacket);

        valid = true;
        session.sendUpstreamPacket(addEntityPacket);

        session.getConnector().getLogger().debug("Spawned entity " + this.definition.identifier() + " at location " + position + " with id " + geyserId + " (java id " + entityId + ")");
    }

    /**
     * To be overridden in other entity classes, if additional things need to be done to the spawn entity packet.
     */
    public void addAdditionalSpawnData(AddEntityPacket addEntityPacket) {

    }

    /**
     * Called right after when an entity is first spawned but
     * before the entity is spawned visually to the player.
     */
    public void postInitialize(Packet packet) {
    }

    /**
     * Despawns the entity
     *
     * @param session The GeyserSession
     * @return can be deleted
     */
    public boolean despawnEntity(GeyserSession session) {
        if (!valid) return true;

        for (long passenger : passengers) { // Make sure all passengers on the despawned entity are updated
            Entity entity = session.getEntityCache().getEntityByJavaId(passenger);
            if (entity == null) continue;
            entity.getMetadata().getOrCreateFlags().setFlag(EntityFlag.RIDING, false);
            entity.updateBedrockMetadata();
        }

        RemoveEntityPacket removeEntityPacket = new RemoveEntityPacket();
        removeEntityPacket.setUniqueEntityId(geyserId);
        session.sendUpstreamPacket(removeEntityPacket);

        valid = false;
        return true;
    }

    public void moveRelative(GeyserSession session, double relX, double relY, double relZ, float yaw, float pitch, boolean isOnGround) {
        moveRelative(session, relX, relY, relZ, Vector3f.from(yaw, pitch, this.rotation.getZ()), isOnGround);
    }

    public void moveRelative(GeyserSession session, double relX, double relY, double relZ, Vector3f rotation, boolean isOnGround) {
        setRotation(rotation);
        setOnGround(isOnGround);
        this.position = Vector3f.from(position.getX() + relX, position.getY() + relY, position.getZ() + relZ);

        MoveEntityAbsolutePacket moveEntityPacket = new MoveEntityAbsolutePacket();
        moveEntityPacket.setRuntimeEntityId(geyserId);
        moveEntityPacket.setPosition(position);
        moveEntityPacket.setRotation(getBedrockRotation());
        moveEntityPacket.setOnGround(isOnGround);
        moveEntityPacket.setTeleported(false);

        session.sendUpstreamPacket(moveEntityPacket);
    }

    public void moveAbsolute(GeyserSession session, Vector3f position, float yaw, float pitch, boolean isOnGround, boolean teleported) {
        moveAbsolute(session, position, Vector3f.from(yaw, pitch, this.rotation.getZ()), isOnGround, teleported);
    }

    public void moveAbsolute(GeyserSession session, Vector3f position, Vector3f rotation, boolean isOnGround, boolean teleported) {
        setPosition(position);
        setRotation(rotation);
        setOnGround(isOnGround);

        MoveEntityAbsolutePacket moveEntityPacket = new MoveEntityAbsolutePacket();
        moveEntityPacket.setRuntimeEntityId(geyserId);
        moveEntityPacket.setPosition(position);
        moveEntityPacket.setRotation(getBedrockRotation());
        moveEntityPacket.setOnGround(isOnGround);
        moveEntityPacket.setTeleported(teleported);

        session.sendUpstreamPacket(moveEntityPacket);
    }

    /**
     * Teleports an entity to a new location. Used in JavaEntityTeleportTranslator.
     * @param session GeyserSession.
     * @param position The new position of the entity.
     * @param yaw The new yaw of the entity.
     * @param pitch The new pitch of the entity.
     * @param isOnGround Whether the entity is currently on the ground.
     */
    public void teleport(GeyserSession session, Vector3f position, float yaw, float pitch, boolean isOnGround) {
        moveAbsolute(session, position, yaw, pitch, isOnGround, false);
    }

    /**
     * Updates an entity's head position. Used in JavaEntityHeadLookTranslator.
     * @param session GeyserSession.
     * @param headYaw The new head rotation of the entity.
     */
    public void updateHeadLookRotation(GeyserSession session, float headYaw) {
        moveRelative(session, 0, 0, 0, Vector3f.from(headYaw, rotation.getY(), rotation.getZ()), onGround);
    }

    /**
     * Updates an entity's position and rotation. Used in JavaEntityPositionRotationTranslator.
     * @param session GeyserSession
     * @param moveX The new X offset of the current position.
     * @param moveY The new Y offset of the current position.
     * @param moveZ The new Z offset of the current position.
     * @param yaw The new yaw of the entity.
     * @param pitch The new pitch of the entity.
     * @param isOnGround Whether the entity is currently on the ground.
     */
    public void updatePositionAndRotation(GeyserSession session, double moveX, double moveY, double moveZ, float yaw, float pitch, boolean isOnGround) {
        moveRelative(session, moveX, moveY, moveZ, Vector3f.from(rotation.getX(), pitch, yaw), isOnGround);
    }

    /**
     * Updates an entity's rotation. Used in JavaEntityRotationTranslator.
     * @param session GeyserSession.
     * @param yaw The new yaw of the entity.
     * @param pitch The new pitch of the entity.
     * @param isOnGround Whether the entity is currently on the ground.
     */
    public void updateRotation(GeyserSession session, float yaw, float pitch, boolean isOnGround) {
        updatePositionAndRotation(session, 0, 0, 0, yaw, pitch, isOnGround);
    }

    /**
     * Applies the Java metadata to the local Bedrock metadata copy
     * @param entityMetadata the Java entity metadata
     * @param session GeyserSession
     */
    @Deprecated
    public void updateBedrockMetadata(EntityMetadata entityMetadata, GeyserSession session) {
    }

    public void setFlags(byte flags) {
        metadata.getFlags().setFlag(EntityFlag.ON_FIRE, ((flags & 0x01) == 0x01) && !metadata.getFlags().getFlag(EntityFlag.FIRE_IMMUNE)); // Otherwise immune entities sometimes flicker onfire
        metadata.getFlags().setFlag(EntityFlag.SNEAKING, (flags & 0x02) == 0x02);
        metadata.getFlags().setFlag(EntityFlag.SPRINTING, (flags & 0x08) == 0x08);
        // Swimming is ignored here and instead we rely on the pose
        metadata.getFlags().setFlag(EntityFlag.GLIDING, (flags & 0x80) == 0x80);

        setInvisible((flags & 0x20) == 0x20);
    }

    public void setPose(Pose pose) {
        metadata.getFlags().setFlag(EntityFlag.SLEEPING, pose.equals(Pose.SLEEPING));
        // Triggered when crawling
        metadata.getFlags().setFlag(EntityFlag.SWIMMING, pose.equals(Pose.SWIMMING));
        setDimensions(pose);
    }

    public void setNoGravity(boolean noGravity) {
        this.metadata.getFlags().setFlag(EntityFlag.HAS_GRAVITY, !noGravity);
    }

    /**
     * Sends the Bedrock metadata to the client
     */
    public void updateBedrockMetadata() {
        if (!valid) return;

        SetEntityDataPacket entityDataPacket = new SetEntityDataPacket();
        entityDataPacket.setRuntimeEntityId(geyserId);
        entityDataPacket.getMetadata().putAll(metadata);
        session.sendUpstreamPacket(entityDataPacket);
    }

    /**
     * If true, the entity should be shaking on the client's end.
     *
     * @return whether {@link EntityFlag#SHAKING} should be set to true.
     */
    protected boolean isShaking(GeyserSession session) {
        return false;
    }

    public void setDisplayName(Component name) {
        if (name != null) {
            String displayName = MessageTranslator.convertMessage(name, session.getLocale());
            metadata.put(EntityData.NAMETAG, displayName);
        } else if (!metadata.getString(EntityData.NAMETAG).isEmpty()) {
            // Clear nametag
            metadata.put(EntityData.NAMETAG, "");
        }
    }

    public void setDisplayNameVisible(boolean displayNameVisible) {
        metadata.put(EntityData.NAMETAG_ALWAYS_SHOW, (byte) (displayNameVisible ? 1 : 0));
    }

    /**
     * Set the height and width of the entity's bounding box
     */
    protected void setDimensions(Pose pose) {
        // No flexibility options for basic entities
        //TODO don't even set this for basic entities since we already set it on entity initialization
        metadata.put(EntityData.BOUNDING_BOX_WIDTH, this.definition.width());
        metadata.put(EntityData.BOUNDING_BOX_HEIGHT, this.definition.height());
    }

    /**
     * Set a float from 0-1 - how strong the "frozen" overlay should be on screen.
     */
    public void setFreezing(float amount) {
        metadata.put(EntityData.FREEZING_EFFECT_STRENGTH, amount);
    }

    /**
     * Set an int from 0 - this entity's maximum air - (air / maxAir) represents the percentage of bubbles left
     * @param amount the amount of air
     */
    public void setAir(int amount) {
        metadata.put(EntityData.AIR_SUPPLY, (short) MathUtils.constrain(amount, 0, getMaxAir()));
    }

    public int getMaxAir() {
        return 300;
    }

    /**
     * Set a boolean - whether the entity is invisible or visible
     *
     * @param value true if the entity is invisible
     */
    public void setInvisible(boolean value) {
        metadata.getFlags().setFlag(EntityFlag.INVISIBLE, value);
    }

    /**
     * x = Pitch, y = HeadYaw, z = Yaw
     *
     * @return the bedrock rotation
     */
    public Vector3f getBedrockRotation() {
        return Vector3f.from(rotation.getY(), rotation.getZ(), rotation.getX());
    }

    @SuppressWarnings("unchecked")
    public <I extends Entity> I as(Class<I> entityClass) {
        return entityClass.isInstance(this) ? (I) this : null;
    }

    public <I extends Entity> boolean is(Class<I> entityClass) {
        return entityClass.isInstance(this);
    }
}
