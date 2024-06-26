package club.sk1er.patcher.tweaker.other;

import club.sk1er.patcher.asm.external.mods.essential.EssentialModelRendererTransformer;
import club.sk1er.patcher.asm.external.mods.optifine.*;
import club.sk1er.patcher.asm.external.mods.optifine.reflectionoptimizations.common.BakedQuadReflectionOptimizer;
import club.sk1er.patcher.asm.external.mods.optifine.reflectionoptimizations.common.EntityRendererReflectionOptimizer;
import club.sk1er.patcher.asm.external.mods.optifine.reflectionoptimizations.common.ExtendedBlockStorageReflectionOptimizer;
import club.sk1er.patcher.asm.external.mods.optifine.reflectionoptimizations.common.FaceBakeryReflectionOptimizer;
import club.sk1er.patcher.asm.external.mods.optifine.reflectionoptimizations.common.ModelRotationReflectionOptimizer;
import club.sk1er.patcher.asm.external.mods.optifine.reflectionoptimizations.modern.CustomColorsReflectionOptimizer;
import club.sk1er.patcher.asm.external.mods.optifine.reflectionoptimizations.modern.ItemModelMesherReflectionOptimizer;
import club.sk1er.patcher.asm.external.mods.optifine.reflectionoptimizations.modern.RenderChunkReflectionOptimizer;
import club.sk1er.patcher.asm.external.mods.optifine.signfix.GuiEditSignTransformer;
import club.sk1er.patcher.asm.external.mods.optifine.signfix.TileEntitySignRendererTransformer;
import club.sk1er.patcher.asm.external.mods.optifine.witherfix.EntityWitherTransformer;
import club.sk1er.patcher.asm.external.mods.optifine.xpfix.GuiIngameForgeTransformer;
import club.sk1er.patcher.asm.external.mods.pingtag.TagRendererListenerTransformer;
import club.sk1er.patcher.asm.external.mods.pingtag.TagRendererTransformer;
import club.sk1er.patcher.asm.external.mods.sidebarmod.GuiSidebarTransformer;
import club.sk1er.patcher.asm.external.mods.ve.BetterChatTransformer;
import club.sk1er.patcher.asm.external.optifine.WorldVertexBufferUploaderTransformer;
import club.sk1er.patcher.asm.render.screen.InventoryEffectRendererTransformer;
import club.sk1er.patcher.optifine.OptiFineGenerations;
import club.sk1er.patcher.tweaker.ClassTransformer;
import club.sk1er.patcher.tweaker.transform.PatcherTransformer;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.MixinEnvironment;

/**
 * Used for editing other mods (OptiFine, LevelHead, TNT Timer, etc.) after they've loaded.
 */
public class ModClassTransformer implements IClassTransformer {

    private final Logger logger = LogManager.getLogger("Patcher - Mod Class Transformer");
    private final Multimap<String, PatcherTransformer> transformerMap = ArrayListMultimap.create();

    public ModClassTransformer() {
        MixinEnvironment.getCurrentEnvironment().addTransformerExclusion(getClass().getName());
        // OptiFine loads these classes after we do, overwriting our changes,
        // so transform it AFTER OptiFine loads.
        //#if MC==10809
        registerTransformer(new EntityRendererTransformer());
        //#endif
        registerTransformer(new RendererLivingEntityTransformer());

        // PingTag by Powns
        registerTransformer(new TagRendererTransformer());
        registerTransformer(new TagRendererListenerTransformer());

        // Vanilla Enhancements by OrangeMarshall
        registerTransformer(new BetterChatTransformer());

        // Essential
        registerTransformer(new EssentialModelRendererTransformer());

        // SpiderFrog's oam overwrites our injection (because surely a 1.7 animations mod needs this feature)
        // only run in this transformer if it's loaded in prod
        if (!isDevelopment()) registerTransformer(new InventoryEffectRendererTransformer());

        // Sidebar Mod
        registerTransformer(new GuiSidebarTransformer());

        // OptiFine uses Reflection for compatibility between Forge & itself,
        // and since we know they're using Forge, we're able to change methods back
        // to how they normally were (using Forge's changes).
        //
        // Only I7 and above are supported due to them being the biggest versions of OptiFine.
        final String optifineVersion = ClassTransformer.optifineVersion;
        final OptiFineGenerations generations = ClassTransformer.generations;
        if (generations.getIGeneration().contains(optifineVersion)) {
            registerCommonTransformers();
            registerI7Transformers();
        } else if (generations.getLGeneration().contains(optifineVersion)) {
            registerCommonTransformers();
            registerLSeriesTransformers();
            registerLSeriesFixesTransformers();
        } else if (generations.getMGeneration().contains(optifineVersion) || generations.getFutureGeneration().contains(optifineVersion)) {
            registerCommonTransformers();
            registerLSeriesTransformers();
        } else {
            logger.info("User's OptiFine version ({}) does not support any reflection optimizations, none will be applied.", optifineVersion);
        }
    }

    private void registerTransformer(PatcherTransformer transformer) {
        for (String cls : transformer.getClassName()) {
            transformerMap.put(cls, transformer);
        }
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        return ClassTransformer.createTransformer(transformedName, bytes, transformerMap, logger);
    }

    private void registerCommonTransformers() {
        registerTransformer(new BakedQuadReflectionOptimizer());
        registerTransformer(new FaceBakeryReflectionOptimizer());
        registerTransformer(new ModelRotationReflectionOptimizer());
        registerTransformer(new ExtendedBlockStorageReflectionOptimizer());
        registerTransformer(new EntityRendererReflectionOptimizer());

        registerTransformer(new LagometerTransformer());
        registerTransformer(new GuiIngameForgeTransformer());
        registerTransformer(new OptifineFontRendererTransformer());
        registerTransformer(new OptiFineHookTransformer());
        registerTransformer(new FullbrightTickerTransformer());
        registerTransformer(new EntityCullingTransformer());
        registerTransformer(new WorldVertexBufferUploaderTransformer());
    }

    private void registerI7Transformers() {

    }

    private void registerLSeriesTransformers() {
        registerTransformer(new ItemModelMesherReflectionOptimizer());
        registerTransformer(new CustomColorsReflectionOptimizer());
        registerTransformer(new RenderChunkReflectionOptimizer());
        registerTransformer(new GuiDetailSettingsOFTransformer());
    }

    private void registerLSeriesFixesTransformers() {
        registerTransformer(new GuiEditSignTransformer());
        registerTransformer(new TileEntitySignRendererTransformer());
        registerTransformer(new RandomEntitiesTransformer());
        registerTransformer(new EntityWitherTransformer());
    }

    public static boolean isDevelopment() {
        Object o = Launch.blackboard.get("fml.deobfuscatedEnvironment");
        return o != null && (boolean) o;
    }
}
