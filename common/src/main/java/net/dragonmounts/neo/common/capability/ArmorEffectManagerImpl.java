package net.dragonmounts.neo.common.capability;

import net.dragonmounts.neo.common.init.DMDataComponents;
import net.dragonmounts.neo.common.network.s2c.InitCooldownPayload;
import net.dragonmounts.neo.common.network.s2c.SyncCooldownPayload;
import net.dragonmounts.neo.compat.platform.ServerNetworkHandler;
import net.dragonmounts.neo.compat.registry.ArmorEffect;
import net.dragonmounts.neo.compat.registry.CooldownCategory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import static java.lang.System.arraycopy;
import static java.util.Arrays.fill;
import static net.dragonmounts.neo.common.DragonMountsShared.NAMESPACE;

public final class ArmorEffectManagerImpl implements ArmorEffectManager {
    public static final String SERIALIZATION_KEY = NAMESPACE + ":armor_effect_manager";
    public static final int INITIAL_COOLDOWN_SIZE = 8;
    public static final int INITIAL_LEVEL_SIZE = 5;
    private static ArmorEffectManagerImpl LOCAL_MANAGER = null;

    public static void onPlayerClone(Player player, Player priorPlayer) {
        var manager = ((ArmorEffectManager.Provider) player).neodragonmounts$getManager();
        var priorManager = ((ArmorEffectManager.Provider) priorPlayer).neodragonmounts$getManager();
        manager.cdRef = priorManager.cdRef;
        manager.cdKey = priorManager.cdKey;
        manager.cdDat = priorManager.cdDat;
        manager.cdMask = priorManager.cdMask;
        manager.cdN = priorManager.cdN;
    }

    public final Player player;
    private int[] cdRef;
    private int[] cdKey;
    private int[] cdDat;
    private int cdMask;
    private int cdN;
    private int[] lvlRef;//active effects
    private ArmorEffect[] lvlKey;//all effects
    private int[] lvlDat;
    private int lvlSize;
    private int lvlN;
    private int activeN;

    public ArmorEffectManagerImpl(Player player) {
        this.player = player;
        if (player.isLocalPlayer()) {
            LOCAL_MANAGER = this;
        }
        this.cdMask = INITIAL_COOLDOWN_SIZE - 1;
        this.cdRef = new int[INITIAL_COOLDOWN_SIZE];
        fill(this.cdKey = new int[INITIAL_COOLDOWN_SIZE], -1);
        this.cdDat = new int[INITIAL_COOLDOWN_SIZE];
        this.lvlSize = INITIAL_LEVEL_SIZE;
        this.lvlRef = new int[INITIAL_LEVEL_SIZE];
        this.lvlKey = new ArmorEffect[INITIAL_LEVEL_SIZE];
        this.lvlDat = new int[INITIAL_LEVEL_SIZE];
    }

    public static ArmorEffectManagerImpl getLocal() {
        return LOCAL_MANAGER;
    }

    public static int getLocalCooldown(CooldownCategory category) {
        return LOCAL_MANAGER == null ? 0 : LOCAL_MANAGER.getCooldown(category);
    }

    public static void init(int[] data) {
        if (LOCAL_MANAGER == null) return;//?
        LOCAL_MANAGER.cdN = 0;
        final int length = data.length >> 1;
        if (length > LOCAL_MANAGER.cdMask) {
            int size = LOCAL_MANAGER.cdRef.length << 1;
            while (length >= size) size <<= 1;
            LOCAL_MANAGER.cdMask = size - 1;
            LOCAL_MANAGER.cdRef = new int[size];
            fill(LOCAL_MANAGER.cdKey = new int[size], -1);
            LOCAL_MANAGER.cdDat = new int[size];
        } else {
            fill(LOCAL_MANAGER.cdKey, -1);
        }
        for (int i = 0, j = 0, k; i < length; ++i) {
            if ((k = data[i++]) >= 0) {
                j = LOCAL_MANAGER.setCdImpl(k, data[i], j);
            }
        }
    }

    private void reassign(final int pos, final int arg) {
        final int[] cdRef = this.cdRef, cdKey = this.cdKey, cdDat = this.cdDat;
        for (int i = this.cdN - 1, j, k, mask = this.cdMask; i > arg; --i) {
            if (((k = cdKey[j = cdRef[i]]) & mask) == pos) {
                cdRef[i] = pos;
                cdKey[pos] = k;
                cdDat[pos] = cdDat[j];
                this.reassign(j, i);
                return;
            }
        }
        cdKey[pos] = -1;//it is unnecessary to reset `this.cdDat[pos]`
    }

    private int setCdImpl(final int category, final int cooldown, int cursor) {
        final int[] cdRef = this.cdRef, cdKey = this.cdKey, cdDat = this.cdDat;
        int mask = this.cdMask, pos = category & mask;
        do {
            int key = cdKey[pos];
            if (key == -1) {
                if (cooldown > 0) {
                    cdRef[this.cdN++] = pos;
                    cdKey[pos] = category;
                    cdDat[pos] = cooldown;
                }
                return cursor == pos ? pos + 1 : cursor;
            } else if (key == category) {
                if (cooldown > 0) {
                    cdDat[pos] = cooldown;
                } else for (int i = 0; i < this.cdN; ++i) {
                    if (cdRef[i] == pos) {
                        arraycopy(cdRef, i + 1, cdRef, i, --this.cdN - i);
                        this.reassign(pos, i - 1);
                        return cursor == pos ? pos + 1 : cursor;
                    }
                }
                return cursor == pos ? pos + 1 : cursor;
            }
        } while ((pos = cursor++) <= mask);
        throw new IndexOutOfBoundsException();
    }

    private void setCooldown(int category, int cooldown) {
        if (this.cdN == this.cdRef.length) {
            final int[] ref = this.cdRef, key = this.cdKey, dat = this.cdDat;
            final int n = this.cdN;
            int temp = n << 1;//temp: new array size
            this.cdMask = temp - 1;
            this.cdN = 0;
            this.cdRef = new int[temp];
            fill(this.cdKey = new int[temp], -1);
            this.cdDat = new int[temp];
            for (int i = temp = 0, j; i < n; ++i) {//temp: cursor
                temp = this.setCdImpl(key[j = ref[i]], dat[j], temp);
            }
            this.setCdImpl(category, cooldown, temp);
        } else {
            this.setCdImpl(category, cooldown, 0);
        }
    }

    @Override
    public void setCooldown(final CooldownCategory category, final int cooldown) {
        final int id = category.getId();
        if (id < 0) return;
        this.setCooldown(id, cooldown);
        if (this.player instanceof ServerPlayer $player) {
            ServerNetworkHandler.sendTo($player, new SyncCooldownPayload(id, cooldown));
        }
    }

    @Override
    public CompoundTag saveNBT() {
        final int[] cdRef = this.cdRef, cdKey = this.cdKey, cdDat = this.cdDat;
        var tag = new CompoundTag();
        for (int i = 0, j, v; i < this.cdN; ++i) {
            if ((v = cdDat[j = cdRef[i]]) > 0) {
                var category = CooldownCategory.REGISTRY.byId(cdKey[j]);
                if (category != null) {
                    tag.putInt(category.identifier.toString(), v);
                }
            }
        }
        return tag;
    }


    @Override
    public void readNBT(CompoundTag tag) {
        for (var category : CooldownCategory.REGISTRY) {
            var name = category.identifier.toString();
            if (tag.contains(name)) {
                this.setCooldown(category.getId(), tag.getInt(name));
            }
        }
    }

    @Override
    public void sendInitPacket() {
        final int n = this.cdN;
        if (n == 0) return;
        final int[] cdRef = this.cdRef, cdKey = this.cdKey, cdDat = this.cdDat, data = new int[n << 1];
        int index = 0;
        for (int i = 0, j, k; i < n; ++i) {
            if ((k = cdKey[j = cdRef[i]]) != -1) {
                data[index++] = k;
                data[index++] = cdDat[j];
            }
        }
        ServerNetworkHandler.sendTo((ServerPlayer) this.player, new InitCooldownPayload(index, data));
    }

    @Override
    public int getCooldown(final CooldownCategory category) {
        final int id = category.getId();
        if (id < 0) return 0;
        int pos = id & this.cdMask, key = this.cdKey[pos];
        if (key == -1) return 0;
        if (key == id) return this.cdDat[pos];
        final int[] cdRef = this.cdRef, cdKey = this.cdKey, cdDat = this.cdDat;
        for (int i = 0, n = this.cdN; i < n; ++i) {
            if (cdRef[i] == pos) {
                while (++i < n) {
                    if (cdKey[pos = cdRef[i]] == id) {
                        return cdDat[pos];
                    }
                }
                return 0;
            }
        }
        return 0;
    }

    @Override
    public boolean isAvailable(final CooldownCategory category) {
        final int id = category.getId();
        if (id < 0) return true;
        int pos = id & this.cdMask, key = this.cdKey[pos];
        if (key == -1) return true;
        if (key == id) return this.cdDat[pos] <= 0;
        final int[] cdRef = this.cdRef, cdKey = this.cdKey, cdDat = this.cdDat;
        for (int i = 0, n = this.cdN; i < n; ++i) {
            if (cdRef[i] == pos) {
                while (++i < n) {
                    if (cdKey[pos = cdRef[i]] == id) {
                        return cdDat[pos] <= 0;
                    }
                }
                return true;
            }
        }
        return true;
    }

    private void validateLvlSize() {
        if (this.lvlN == this.lvlSize) {
            this.lvlSize += 4;
            final ArmorEffect[] key = new ArmorEffect[this.lvlSize];
            final int[] dat = new int[this.lvlSize];
            arraycopy(this.lvlKey, 0, key, 0, this.lvlN);
            arraycopy(this.lvlDat, 0, dat, 0, this.lvlN);
            this.lvlKey = key;
            this.lvlDat = dat;
        }
    }

    @Override
    public int setLevel(final ArmorEffect effect, final int level) {
        final ArmorEffect[] lvlKey = this.lvlKey;
        final int n = this.lvlN;
        for (int i = 0; i < n; ++i) {
            if (lvlKey[i] == effect) {
                return this.lvlDat[i] = level;
            }
        }
        this.validateLvlSize();//may assign new array to `this.lvlKey`
        this.lvlKey[n] = effect;
        return this.lvlDat[this.lvlN++] = level;
    }

    @Override
    public int addLevel(final ArmorEffect effect, int delta) {
        final ArmorEffect[] lvlKey = this.lvlKey;
        final int n = this.lvlN;
        for (int i = 0; i < n; ++i) {
            if (lvlKey[i] == effect) {
                return this.lvlDat[i] += delta;
            }
        }
        this.validateLvlSize();//may assign new array to `this.lvlKey`
        this.lvlKey[n] = effect;
        return this.lvlDat[this.lvlN++] = delta;
    }

    @Override
    public boolean isActive(final ArmorEffect effect) {
        final ArmorEffect[] lvlKey = this.lvlKey;
        final int[] lvlRef = this.lvlRef;
        for (int i = 0, n = this.activeN; i < n; ++i) {
            if (lvlKey[lvlRef[i]] == effect) {
                return true;
            }
        }
        return false;
    }


    @Override
    public int getLevel(final ArmorEffect effect, final boolean filtered) {
        final ArmorEffect[] lvlKey = this.lvlKey;
        if (filtered) {
            final int[] lvlRef = this.lvlRef;
            for (int i = 0, j, n = this.activeN; i < n; ++i) {
                if (lvlKey[j = lvlRef[i]] == effect) {
                    return this.lvlDat[j];
                }
            }
        } else for (int i = 0; i < this.lvlN; ++i) {
            if (lvlKey[i] == effect) {
                return this.lvlDat[i];
            }
        }
        return 0;
    }

    @Override
    public void tick() {
        final int[] cdRef = this.cdRef, cdDat = this.cdDat, lvlDat = this.lvlDat;
        final var player = this.player;
        for (int i = 0, j; i < this.cdN; ++i) {
            if (--cdDat[j = cdRef[i]] < 1) {
                arraycopy(cdRef, i + 1, cdRef, i, --this.cdN - i);
                this.reassign(j, --i);
            }
        }
        int sum = this.activeN = this.lvlN = 0;
        for (var stack : player.getArmorSlots()) {
            var source = stack.get(DMDataComponents.ARMOR_EFFECT_SOURCE);
            if (source != null) {
                source.affect(this, player, stack);
            }
        }
        final ArmorEffect[] lvlKey = this.lvlKey;
        int[] lvlRef = this.lvlRef;
        for (int i = 0, end = this.lvlN; i < end; ++i) {
            final var effect = lvlKey[i];
            if (effect.activate(this, player, lvlDat[i])) {
                if (sum == lvlRef.length) {
                    arraycopy(this.lvlRef, 0, lvlRef = new int[sum + 4], 0, sum);
                    this.lvlRef = lvlRef;
                }
                lvlRef[sum++] = i;
            }
        }
        this.activeN = sum;
    }
}
