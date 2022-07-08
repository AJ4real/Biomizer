/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.biomizer.nms.v1_18_R1;

import io.netty.buffer.Unpooled;
import me.aj4real.biomizer.Biomizer;
import me.aj4real.biomizer.NMS;
import me.aj4real.dataplus.api.ChunkDataPacketEditor;
import me.aj4real.dataplus.api.login.LoginPacketEditor;
import me.aj4real.dataplus.api.nbt.NBTCompoundTag;
import me.aj4real.dataplus.reflection.ClassAccessor;
import me.aj4real.dataplus.reflection.FieldAccessor;
import me.aj4real.simplepackets.Client;
import me.aj4real.simplepackets.Packets;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_18_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_18_R1.util.CraftNamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class NMSImpl implements NMS {

    private static final ClassAccessor<MappedRegistry> classAccessor = ClassAccessor.of(MappedRegistry.class);
    private static final FieldAccessor<Boolean> frozen = classAccessor.lookupField(boolean.class).stream().findAny().get();

    DedicatedServer server = ((CraftServer)Bukkit.getServer()).getHandle().getServer();
    MappedRegistry<Biome> BIOMES = (MappedRegistry<Biome>) this.server.registryAccess().registry(Registry.BIOME_REGISTRY).get();
    public void onEnable(Plugin plugin) {
        frozen.set(BIOMES, false);
        Packets.addHandler(ClientboundLoginPacket.class, this::patchLoginPacket);
        Packets.addHandler(ClientboundLevelChunkWithLightPacket.class, this::patchChunkPacket);
    }

    public ClientboundLevelChunkWithLightPacket patchChunkPacket(Client c, ClientboundLevelChunkWithLightPacket p) {
        if(c.getPlayer() == null) {
            return p;
        }
        Chunk chunk = c.getPlayer().getWorld().getChunkAt(p.getX(), p.getZ());
        Biome biome = (Biome) Biomizer.INSTANCE.getKnowItAll().should(c.getPlayer(), chunk);
        if(biome == null) return p;
        else try {
            ChunkDataPacketEditor editor = ChunkDataPacketEditor.newInstance(c.getPlayer().getWorld(), p);
            editor.setAllNMSBiome(biome);
            return (ClientboundLevelChunkWithLightPacket) editor.build();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            Biomizer.INSTANCE.getPlugin().getLogger().log(Level.WARNING, "Failed to patch chunk data packet!", e);
        }
        return p;
    }

    public ClientboundLoginPacket patchLoginPacket(Client c, ClientboundLoginPacket p) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeWithCodec(RegistryAccess.RegistryHolder.NETWORK_CODEC, p.registryHolder());
        NBTCompoundTag nbt = (NBTCompoundTag) Biomizer.INSTANCE.getDataPlus().fromNMS(buf.readNbt());
        LoginPacketEditor editor = new LoginPacketEditor(nbt);
        Biomizer.INSTANCE.getKnowItAll().getCustomBiomes().forEach(editor::addBiome);
        c.waitForPlayer((pl) -> Biomizer.INSTANCE.getKnowItAll().add((Player) pl, editor.getBiomes()
                .stream()
                .map(me.aj4real.dataplus.api.login.Biome::getName)
                .collect(Collectors.toSet())));
        nbt = editor.build();
        FriendlyByteBuf buf2 = new FriendlyByteBuf(Unpooled.buffer());
        buf2.writeNbt((CompoundTag) Biomizer.INSTANCE.getDataPlus().toNMS(nbt));
        return new ClientboundLoginPacket(
                p.playerId(),
                p.hardcore(),
                p.gameType(),
                p.previousGameType(),
                p.levels(),
                buf2.readWithCodec(RegistryAccess.RegistryHolder.NETWORK_CODEC),
                p.dimensionType(),
                p.dimension(),
                p.seed(),
                p.maxPlayers(),
                p.chunkRadius(),
                p.simulationDistance(),
                p.reducedDebugInfo(),
                p.showDeathScreen(),
                p.isDebug(),
                p.isFlat()
        );
    }
    public Biome newBiome(String namespace, org.bukkit.block.Biome extended) {
        if(extended == org.bukkit.block.Biome.CUSTOM) throw new UnsupportedOperationException("A custom biome cannot extend org.bukkit.block.Biome.CUSTOM");
        Biome after = CraftBlock.biomeToBiomeBase(BIOMES, extended);
        Biome newBiome = new Biome.BiomeBuilder()
                .downfall(after.getDownfall())
                .temperature(after.getBaseTemperature())
                .generationSettings(BiomeGenerationSettings.EMPTY)
                .mobSpawnSettings(MobSpawnSettings.EMPTY)
                .precipitation(Biome.Precipitation.NONE)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(after.getWaterColor())
                        .waterFogColor(after.getWaterFogColor())
                        .skyColor(after.getSkyColor())
                        .fogColor(after.getFogColor())
                        .build())
                .build();
        Registry.register(BIOMES, namespace, newBiome);
        return newBiome;
    }
    public int getBiomeId(Object nmsBiome) {
        return BIOMES.getId((Biome) nmsBiome);
    }
    public void sendChunkUpdate(Chunk chunk) {
        ((CraftWorld)chunk.getWorld()).getHandle()
                .getChunkSource()
                .chunkMap
                .getPlayers(((CraftChunk)chunk).getHandle().getPos(), false)
                .forEach((p) ->
                        p.connection.connection.send(new ClientboundLevelChunkWithLightPacket(
                                ((CraftChunk)chunk).getHandle(),
                                ((CraftWorld)chunk.getWorld()).getHandle().getLightEngine(),
                                null, null, true
                        )));
    }
    public Set<NamespacedKey> getParticleTypes() {
        return Registry.PARTICLE_TYPE.keySet().stream().map((k) -> CraftNamespacedKey.fromMinecraft(k)).collect(Collectors.toSet());
    }
    public Set<String> getGrassColorModifiers() {
        return Arrays.stream(BiomeSpecialEffects.GrassColorModifier.values()).map(Enum::name).collect(Collectors.toSet());
    }
    public Set<String> getAvailableSounds() {
        return Registry.SOUND_EVENT.keySet().stream().map(ResourceLocation::toString).collect(Collectors.toSet());
    }
}