/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.biomizer.nms.v1_19_3;

import com.mojang.serialization.Lifecycle;
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
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_19_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_19_R2.CraftServer;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R2.util.CraftNamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class NMSImpl implements NMS {

    private static final ClassAccessor<MappedRegistry> classAccessor = ClassAccessor.of(MappedRegistry.class);
    private static final FieldAccessor<Boolean> frozen = classAccessor.lookupField(boolean.class).stream().findAny().get();
    private static FieldAccessor<Biome> BIOME_HOLDER_VALUE;
    DedicatedServer server = ((CraftServer)Bukkit.getServer()).getHandle().getServer();
    MappedRegistry<Biome> BIOMES = (MappedRegistry<Biome>) this.server.registryAccess().registry(Registries.BIOME).get();
    Registry<ParticleType<?>> PARTICLE_TYPES = server.registryAccess().registry(Registries.PARTICLE_TYPE).get();
    Registry<SoundEvent> SOUND_EVENT = server.registryAccess().registry(Registries.SOUND_EVENT).get();

    public void onEnable(Plugin plugin) {
        frozen.set(BIOMES, false);
        Packets.addHandler(ClientboundLoginPacket.class, this::patchLoginPacket);
        Packets.addHandler(ClientboundLevelChunkWithLightPacket.class, this::patchChunkPacket);
        Holder.Reference<Biome> ref = BIOMES.holders().findFirst().get();
        ClassAccessor<Holder.Reference> c = new ClassAccessor<>(Holder.Reference.class);

        for (Map.Entry<String, FieldAccessor> e : c.getFields().entrySet()) {
            Object o = e.getValue().get(ref);
            if(o == null) {
                BIOME_HOLDER_VALUE = e.getValue();
            } else if(o instanceof Biome) {
                BIOME_HOLDER_VALUE = e.getValue();
                break;
            }
        }
        if(BIOME_HOLDER_VALUE == null) {
            plugin.getLogger().log(Level.SEVERE, "Cannot find Accessor for " + Holder.Reference.class.getCanonicalName() + " at " + Biome.class.getCanonicalName() + " field!");
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
//        try {
//            List<Class<RegistryAccess>> classes = findAllSubclasses(
//                    new JarFile(new File("D:\\Servers\\1.19.3 test server\\versions\\1.19.3\\paper-1.19.3.jar")),
//                    RegistryAccess.class,
//                    DedicatedServer.class.getClassLoader(),
//                    false,
//                    new Class[] {});
//            for (Class<RegistryAccess> aClass : classes) {
//                if(aClass.isInstance(server.registryAccess())) {
//                    System.out.println(aClass.getName());
//                }
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    /**
     * Returns a {@link List} of every class that inherits from the given class
     * <code>clazz</code> that can be found in this searcher's {@link JarFile}.
     *
     * <p>This is useful if addons to a plugin create an new implementation for a
     * feature, so the main plugin doesn't have to write a registry system for
     * each feature. Instead, the main plugin can pull all features from
     * registered jars.
     *
     * @param clazz            The class that the subclasses inherit from. This
     *                         is generally an abstract class/interface.
     * @param clazzLoader      The class that is used to load classes
     * @param isIgnoreAbstract If this is <code>true</code>, then any subclass
     *                         that is an interface or is an abstract class
     *                         will not be included in the returned list.
     * @param classes          Class blacklist. Any classes included in this
     *                         array will not be included in the returned list.
     * @param <T>              The parent class that all of these subclasses
     *                         will have in common.
     * @return A {@link List} of every subclass.
     */
    public <T> List<Class<T>> findAllSubclasses(JarFile jar, Class<T> clazz, ClassLoader clazzLoader, boolean isIgnoreAbstract, Class<?>... classes) {
        if (clazz == null) throw new IllegalArgumentException("clazz cannot be null");
        List<Class<?>> classList = new ArrayList<>(Arrays.asList(classes));
        classList.add(clazz);
        Set<String> blacklist = classList.stream()
                .map(Class::getName)
                .map(str -> str.replaceAll("\\.", "/") + ".class")
                .collect(Collectors.toSet());
        Enumeration<JarEntry> entries = jar.entries();
        List<Class<T>> subclasses = new ArrayList<>();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();
            if (!entryName.endsWith(".class") || blacklist.contains(entryName)) continue;
            String name = entryName.replaceAll("/", "\\.").replace(".class", "");
            Class<?> subclass;
            try {
//                InputStream stream = jar.getInputStream(entry);
//                byte[] bytes = ByteStreams.toByteArray(stream);
//                int runtimeVersion = 44 + javaVersion; // ! THIS MAY NOT ALWAYS BE TRUE, but it is true for java 1.2+, so it is probably fine forever
//                int classVersion = (((bytes[6] & 0xFF) << 8) | (bytes[6 + 1] & 0xFF));
//                if (classVersion > runtimeVersion) {
//                    System.out.println("Skipping " + name + " because it has class version " + classVersion + "(We are expecting " + runtimeVersion + ")");
//                    continue;
//                }
                subclass = Class.forName(name, false, clazzLoader);
            } catch (Throwable ex) {
                System.out.println("Error for class '" + name + "'");
                ex.printStackTrace();
                continue;
            }
            int mod = subclass.getModifiers();
            if (!clazz.isAssignableFrom(subclass)) continue;
            else if (isIgnoreAbstract && (Modifier.isAbstract(mod) || Modifier.isInterface(mod))) continue;
            subclasses.add((Class<T>) subclass);
        }
        return subclasses;
    }

    public ClientboundLevelChunkWithLightPacket patchChunkPacket(Client c, ClientboundLevelChunkWithLightPacket p) {
        if(c.getPlayer() == null) {
            c.waitForPlayer((pl) -> {
                ServerLevel world = ((CraftWorld) ((Player)pl).getWorld()).getHandle();
                ((CraftPlayer)pl).getHandle().connection.connection.send(new ClientboundLevelChunkWithLightPacket(
                        world.getChunk(p.getX(), p.getZ()),
                        world.getLightEngine(),
                        null, null, true
                ));
            });
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
            return p;
        }
    }

    public ClientboundLoginPacket patchLoginPacket(Client c, ClientboundLoginPacket p) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeWithCodec(RegistrySynchronization.NETWORK_CODEC, p.registryHolder());
        NBTCompoundTag nbt = (NBTCompoundTag) Biomizer.INSTANCE.getDataPlus().fromNMS(buf.readNbt());
        LoginPacketEditor editor = new LoginPacketEditor(nbt);
        Biomizer.INSTANCE.getKnowItAll().getCustomBiomes().forEach(editor::addBiome);
        c.waitForPlayer((pl) -> Biomizer.INSTANCE.getKnowItAll().add((Player) pl, editor.getBiomes()
                .stream()
                .filter((b) -> Biomizer.INSTANCE.getKnowItAll().getCustomBiome(b.getName()) != null)
                .filter(me.aj4real.dataplus.api.login.Biome::isMod)
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
                buf2.readWithCodec(RegistrySynchronization.NETWORK_CODEC).freeze(),
                p.dimensionType(),
                p.dimension(),
                p.seed(),
                p.maxPlayers(),
                p.chunkRadius(),
                p.simulationDistance(),
                p.reducedDebugInfo(),
                p.showDeathScreen(),
                p.isDebug(),
                p.isFlat(),
                p.lastDeathLocation()
        );
    }

    public Biome newBiome(String namespace, org.bukkit.block.Biome extended) {
        if(extended == org.bukkit.block.Biome.CUSTOM) throw new UnsupportedOperationException("A custom biome cannot extend org.bukkit.block.Biome.CUSTOM");
        Biome after = CraftBlock.biomeToBiomeBase(BIOMES, extended).value();
        Biome newBiome = new Biome.BiomeBuilder()
                .generationSettings(BiomeGenerationSettings.EMPTY)
                .downfall(after.getDownfall())
                .temperature(after.getBaseTemperature())
                .mobSpawnSettings(after.getMobSettings())
                .precipitation(after.getPrecipitation())
                .specialEffects(after.getSpecialEffects())
                .build();
        Holder.Reference<Biome> ref = BIOMES.register(ResourceKey.create(BIOMES.key(), ResourceLocation.tryParse(namespace)), newBiome, Lifecycle.stable());
        BIOME_HOLDER_VALUE.set(ref, newBiome);
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
        return PARTICLE_TYPES.keySet().stream().map((k) -> CraftNamespacedKey.fromMinecraft(k)).collect(Collectors.toSet());
    }
    public Set<String> getGrassColorModifiers() {
        return Arrays.stream(BiomeSpecialEffects.GrassColorModifier.values()).map(Enum::name).collect(Collectors.toSet());
    }
    public Set<String> getAvailableSounds() {
        return SOUND_EVENT.keySet().stream().map(ResourceLocation::toString).collect(Collectors.toSet());
    }
}
