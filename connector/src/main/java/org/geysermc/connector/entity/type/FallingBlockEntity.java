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

import com.github.steveice10.mc.protocol.data.game.entity.object.FallingBlockData;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnEntityPacket;
import com.github.steveice10.packetlib.packet.Packet;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.data.entity.EntityData;
import com.nukkitx.protocol.bedrock.data.entity.EntityFlag;
import org.geysermc.connector.entity.EntityDefinition;
import org.geysermc.connector.network.session.GeyserSession;

public class FallingBlockEntity extends Entity {
    private int javaId;

    public FallingBlockEntity(EntityDefinition<FallingBlockEntity> definition, GeyserSession session, long entityId, long geyserId, Vector3f position, Vector3f motion, Vector3f rotation) {
        super(definition, session, entityId, geyserId, position, motion, rotation);
    }

    @Override
    public void postInitialize(Packet packet) {
        if (packet instanceof ServerSpawnEntityPacket spawnEntityPacket) {
            this.javaId = ((FallingBlockData) spawnEntityPacket.getData()).getId();
        }
    }

    @Override
    public void spawnEntity(GeyserSession session) {
        this.metadata.put(EntityData.VARIANT, session.getBlockMappings().getBedrockBlockId(javaId));
        super.spawnEntity(session);
    }

    public void setNoGravity(boolean noGravity) {
        // Set the NO_AI flag based on the no gravity flag to prevent movement
        this.metadata.getFlags().setFlag(EntityFlag.NO_AI, noGravity);
    }
}
