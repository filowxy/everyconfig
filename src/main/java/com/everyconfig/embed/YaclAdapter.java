package com.everyconfig.embed;

import com.everyconfig.api.ConfigAdapter;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;

/**
 * YACL (Yet Another Config Lib) adapter. Enabled at runtime only when the
 * {@code yet_another_config_lib_v3} mod is installed; {@link #createScreen}
 * touches YACL classes and is only invoked after the {@link #isInstalled()} guard.
 *
 * <p>(ponytail: YACL demands a fresh {@link YetAnotherConfigLib} per screen —
 * {@code generateScreen} must not be re-called on the same instance, so every
 * call to {@link #createScreen} rebuilds the builder.)
 */
public final class YaclAdapter implements ConfigAdapter {
    @Override
    public String libraryId() {
        return "yet_another_config_lib_v3";
    }

    @Override
    public String displayName() {
        return "YACL";
    }

    @Override
    public boolean isInstalled() {
        return ModList.get().isLoaded(libraryId());
    }

    @Override
    public Screen createScreen(Screen parent) {
        return createScreen(parent, () -> { });
    }

    @Override
    public Screen createScreen(Screen parent, Runnable onSaved) {
        DemoConfig.ensureLoaded();
        return YetAnotherConfigLib.createBuilder()
                .title(Component.literal(displayName() + " (demo)"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("General"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Enabled"))
                                .description(OptionDescription.of(Component.literal("Toggle the demo option")))
                                .binding(true, () -> DemoConfig.enabled, v -> DemoConfig.enabled = v)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Component.literal("Amount"))
                                .description(OptionDescription.of(Component.literal("A number between 0 and 10")))
                                .binding(3, () -> DemoConfig.amount, v -> DemoConfig.amount = v)
                                .controller(IntegerFieldControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Component.literal("Label"))
                                .description(OptionDescription.of(Component.literal("Free-form demo text")))
                                .binding("EveryConfig demo", () -> DemoConfig.label, v -> DemoConfig.label = v)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .build())
                // YACL applies each option's setter (via the bindings above) and only
                // then runs this — the "really saved" moment for a YACL embed.
                .save(() -> {
                    DemoConfig.save();
                    onSaved.run();
                })
                .build()
                .generateScreen(parent);
    }
}
