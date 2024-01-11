package iguanaman.hungeroverhaul;

import java.io.File;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.VillagerRegistry;
import iguanaman.hungeroverhaul.config.IguanaConfig;
import iguanaman.hungeroverhaul.food.FoodEventHandler;
import iguanaman.hungeroverhaul.food.FoodModifier;
import iguanaman.hungeroverhaul.json.JsonModule;
import iguanaman.hungeroverhaul.module.ModuleBOP;
import iguanaman.hungeroverhaul.module.ModuleBonemeal;
import iguanaman.hungeroverhaul.module.ModuleGrassSeeds;
import iguanaman.hungeroverhaul.module.ModuleHarvestCraft;
import iguanaman.hungeroverhaul.module.ModuleNatura;
import iguanaman.hungeroverhaul.module.ModulePlantGrowth;
import iguanaman.hungeroverhaul.module.ModuleRandomPlants;
import iguanaman.hungeroverhaul.module.ModuleRespawnHunger;
import iguanaman.hungeroverhaul.module.ModuleTConstruct;
import iguanaman.hungeroverhaul.module.ModuleTemperatePlants;
import iguanaman.hungeroverhaul.module.ModuleVanilla;
import iguanaman.hungeroverhaul.module.ModuleWeeeFlowers;
import iguanaman.hungeroverhaul.module.PamsModsHelper;
import iguanaman.hungeroverhaul.potion.PotionWellFed;
import iguanaman.hungeroverhaul.util.ComponentVillageCustomField;
import iguanaman.hungeroverhaul.util.IMCHandler;
import iguanaman.hungeroverhaul.util.IguanaEventHook;
import iguanaman.hungeroverhaul.util.ItemTweaks;
import iguanaman.hungeroverhaul.util.RecipeRemover;
import iguanaman.hungeroverhaul.util.VillageHandlerCustomField;

@Mod(
        modid = "HungerOverhaul",
        name = "Hunger Overhaul",
        version = "GRADLETOKEN_VERSION",
        dependencies = "required-after:Forge@[10.13,);required-after:AppleCore;after:TConstruct;after:harvestcraft;after:temperateplants;after:randomplants;after:weeeflowers;after:Natura;after:IC2;after:*")
public class HungerOverhaul {

    public static final Logger Log = LogManager.getLogger("HungerOverhaul");

    // The instance of your mod that Forge uses.
    @Instance("HungerOverhaul")
    public static HungerOverhaul instance;

    public static Potion potionWellFed;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        IguanaConfig.init(
                new File(event.getModConfigurationDirectory(), "HungerOverhaul"),
                event.getSuggestedConfigurationFile());
        FMLCommonHandler.instance().bus().register(new IguanaConfig());
        JsonModule.preinit(event.getModConfigurationDirectory());
        potionWellFed = new PotionWellFed();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new FoodEventHandler());
        MinecraftForge.EVENT_BUS.register(new FoodModifier());
        MinecraftForge.EVENT_BUS.register(new ModulePlantGrowth());
        MinecraftForge.EVENT_BUS.register(new ModuleBonemeal());
        ModuleVanilla.init();
        if (Loader.isModLoaded("harvestcraft")) {
            PamsModsHelper.loadHC();
            ModuleHarvestCraft.init();
        }
        if (Loader.isModLoaded("temperateplants")) ModuleTemperatePlants.init();
        if (Loader.isModLoaded("randomplants")) ModuleRandomPlants.init();
        if (Loader.isModLoaded("weeeflowers")) {
            PamsModsHelper.loadWF();
            ModuleWeeeFlowers.init();
        }
        if (Loader.isModLoaded("TConstruct")) ModuleTConstruct.init();
        if (Loader.isModLoaded("Natura")) ModuleNatura.init();
        if (Loader.isModLoaded("BiomesOPlenty")) ModuleBOP.init();
        JsonModule.init();
        if (IguanaConfig.addCustomVillageField
                && IguanaConfig.fieldNormalWeight + IguanaConfig.fieldReedWeight + IguanaConfig.fieldStemWeight > 0) {
            MapGenStructureIO.func_143031_a(ComponentVillageCustomField.class, "IguanaField");
            VillagerRegistry.instance().registerVillageCreationHandler(new VillageHandlerCustomField());
        }
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        if (IguanaConfig.removeTallGrassSeeds || IguanaConfig.allSeedsEqual) ModuleGrassSeeds.init();
        ItemTweaks.init();
        MinecraftForge.EVENT_BUS.register(new IguanaEventHook());
        FMLCommonHandler.instance().bus().register(new ModuleRespawnHunger());

        if (IguanaConfig.removeHoeRecipes) {
            RecipeRemover.removeAnyRecipe(new ItemStack(Items.wooden_hoe));
            RecipeRemover.removeAnyRecipe(new ItemStack(Items.stone_hoe));
        }
    }

    @EventHandler
    public void handleIMCMessages(FMLInterModComms.IMCEvent event) {
        IMCHandler.processMessages(event.getMessages());
    }
}
