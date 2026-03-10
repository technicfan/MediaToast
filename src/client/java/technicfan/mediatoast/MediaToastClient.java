package technicfan.mediatoast;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.Codec;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.Version;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class MediaToastClient implements ClientModInitializer {
    public static final String MOD_ID = "mediatoast";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final File CONFIG_FILE = FabricLoader.getInstance()
            .getConfigDir().resolve(MOD_ID + ".json").toFile();

    private static OptionInstance<Boolean> enabledToggle;
    private static OptionInstance<Boolean> replaceToggle;
    private static OptionInstance<String> preferredToggle;
    private static OptionInstance<Boolean> onlyPreferredToggle;

    @Override
    public void onInitializeClient() {
        Minecraft minecraft = Minecraft.getInstance();
        boolean newWay = false;
        try {
            newWay = 0 < Version.parse(minecraft.getLaunchedVersion()).compareTo(Version.parse("1.21.8"));
        } catch (VersionParsingException e) {
        }
        registerKeybindings(newWay);
        MediaTracker.init(minecraft, loadConfig());
        createToggles();
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            MediaTracker.close();
        });
    }

    private static void registerKeybindings(Boolean newWay) {
        Object MOD_CATEGORY;
        Class<?> categoryClass;
        Constructor<KeyMapping> keybindingCtor;
        KeyMapping playPauseBinding, nextBinding, prevBinding, refreshBinding, cycleBinding;
        // Keybinding category with Keybinding.Category for Minecraft >= 1.21.9
        if (newWay) {
            try {
                MOD_CATEGORY = KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath(MOD_ID, MOD_ID));
                keybindingCtor = KeyMapping.class.getConstructor(String.class, InputConstants.Type.class, int.class,
                        KeyMapping.Category.class);
                categoryClass = KeyMapping.Category.class;
            } catch (NoClassDefFoundError | NoSuchMethodException | NoSuchMethodError e) {
                LOGGER.error(e.toString(), e.fillInStackTrace());
                return;
            }
            // Keybinding category with String for Minecraft < 1.21.9
        } else {
            try {
                MOD_CATEGORY = "key.category.mediatoast.mediatoast";
                keybindingCtor = KeyMapping.class.getConstructor(String.class, InputConstants.Type.class, int.class,
                        String.class);
                categoryClass = String.class;
            } catch (NoSuchMethodException | NoSuchMethodError e) {
                LOGGER.error(e.toString(), e.fillInStackTrace());
                return;
            }
        }

        try {
            playPauseBinding = KeyBindingHelper.registerKeyBinding(keybindingCtor.newInstance(
                    "mediatoast.key.playpause",
                    InputConstants.Type.KEYSYM,
                    InputConstants.UNKNOWN.getValue(),
                    categoryClass.cast(MOD_CATEGORY)));
            nextBinding = KeyBindingHelper.registerKeyBinding(keybindingCtor.newInstance(
                    "mediatoast.key.next",
                    InputConstants.Type.KEYSYM,
                    InputConstants.UNKNOWN.getValue(),
                    categoryClass.cast(MOD_CATEGORY)));
            prevBinding = KeyBindingHelper.registerKeyBinding(keybindingCtor.newInstance(
                    "mediatoast.key.prev",
                    InputConstants.Type.KEYSYM,
                    InputConstants.UNKNOWN.getValue(),
                    categoryClass.cast(MOD_CATEGORY)));
            refreshBinding = KeyBindingHelper.registerKeyBinding(keybindingCtor.newInstance(
                    "mediatoast.key.refresh",
                    InputConstants.Type.KEYSYM,
                    InputConstants.UNKNOWN.getValue(),
                    categoryClass.cast(MOD_CATEGORY)));
            cycleBinding = KeyBindingHelper.registerKeyBinding(keybindingCtor.newInstance(
                    "mediatoast.key.cycle",
                    InputConstants.Type.KEYSYM,
                    InputConstants.UNKNOWN.getValue(),
                    categoryClass.cast(MOD_CATEGORY)));
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
            LOGGER.error(e.toString(), e.fillInStackTrace());
            return;
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (playPauseBinding.consumeClick()) {
                MediaTracker.playPause();
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (nextBinding.consumeClick()) {
                MediaTracker.next();
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (prevBinding.consumeClick()) {
                MediaTracker.previous();
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (refreshBinding.consumeClick()) {
                MediaTracker.refresh();
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (cycleBinding.consumeClick()) {
                MediaTracker.cyclePlayers();
            }
        });
    }

    private static void createToggles() {
        enabledToggle = OptionInstance.createBoolean("mediatoast.option.enable", OptionInstance.noTooltip(),
                MediaTracker.getConfig().getEnabled(), (value) -> {
                    setEnabled(value);
                });
        replaceToggle = OptionInstance.createBoolean("mediatoast.option.replace",
                OptionInstance.cachedConstantTooltip(Component.translatable("mediatoast.option.replace.tooltip")),
                MediaTracker.getConfig().getReplace(), (value) -> {
                    setReplace(value);
                });
        preferredToggle = new OptionInstance<String>("mediatoast.option.preferred",
                OptionInstance.cachedConstantTooltip(Component.translatable("mediatoast.option.preferred.tooltip")),
                (optionText, value) -> {
                    if (value.isEmpty()) {
                        return Component.translatable("mediatoast.option.preferred.none");
                    } else {
                        String displayName = MediaTracker.getDisplayName(value);
                        value = value.replaceFirst("org.mpris.MediaPlayer2.", "").replaceFirst("\\.exe$", "");
                        return Component.literal(
                                displayName.isEmpty() ? value.substring(0, 1).toUpperCase() + value.substring(1)
                                        : displayName);
                    }
                }, new OptionInstance.LazyEnum<String>(() -> MediaTracker.getPlayerStream().toList(),
                        (value) -> Optional.of(value), Codec.STRING),
                MediaTracker.getConfig().getPreferred(), (value) -> {
                    setPreferred(value);
                });
        onlyPreferredToggle = OptionInstance.createBoolean("mediatoast.option.only_preferred",
                OptionInstance
                        .cachedConstantTooltip(Component.translatable("mediatoast.option.only_preferred.tooltip")),
                MediaTracker.getConfig().getOnlyPreferred(), (value) -> {
                    setOnlyPreferred(value);
                });
    }

    public static OptionInstance<Boolean> getEnabledToggle() {
        return enabledToggle;
    }

    public static OptionInstance<Boolean> getReplaceToggle() {
        return replaceToggle;
    }

    public static OptionInstance<String> getPreferredToggle() {
        return preferredToggle;
    }

    public static OptionInstance<Boolean> getOnlyPreferredToggle() {
        return onlyPreferredToggle;
    }

    private static void setEnabled(boolean enabled) {
        MediaTracker.setConfig(MediaTracker.getConfig().setEnabled(enabled));
        saveConfig();
    }

    private static void setReplace(boolean replace) {
        MediaTracker.setConfig(MediaTracker.getConfig().setReplace(replace));
        saveConfig();
    }

    private static void setOnlyPreferred(boolean onlyPreferred) {
        MediaTracker.setConfig(MediaTracker.getConfig().setOnlyPreferred(onlyPreferred));
        MediaTracker.updatePreferred();
        saveConfig();
    }

    private static void setPreferred(String preferred) {
        if (preferred.equals("None")) {
            preferred = "";
        }
        if (!MediaTracker.getConfig().getPreferred().equals(preferred)) {
            String displayName = MediaTracker.getDisplayName(preferred);
            MediaTracker.setConfig(MediaTracker.getConfig().setPreferred(preferred, displayName));
            MediaTracker.updatePreferred();
            saveConfig();
        }
    }

    private static Config loadConfig() {
        Config config = new Config();
        if (CONFIG_FILE.exists()) {
            try {
                try (FileReader reader = new FileReader(CONFIG_FILE)) {
                    config = new Gson().fromJson(reader, Config.class);
                    LOGGER.info("MediaToast config loaded");
                }
            } catch (IOException e) {
                LOGGER.warn(e.toString(), e.fillInStackTrace());
            }
        }
        return config;
    }

    protected static void saveConfig() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            writer.write(gson.toJson(MediaTracker.getConfig()));
        } catch (IOException e) {
            LOGGER.error(e.toString(), e.fillInStackTrace());
        }
    }
}
