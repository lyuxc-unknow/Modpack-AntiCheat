package me.lyuxc.AntiCheat.me.lyuxc.AntiCheat;

import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.List;

@Mod("modpack_anti_cheat")
@Mod.EventBusSubscriber
public class AntiCheat {
    //最大模组数
    public static final int MAX_MOD_COUNT = 200;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> banMods;
    public static  ForgeConfigSpec.IntValue maxModCount;
    public static  ForgeConfigSpec.ConfigValue<List<? extends String>> banCommand;
    public static ForgeConfigSpec commonConfig;
    //列表 - 禁用命令
    public static String[] DISABLE_COMMAND = {
            "gamemode", "give", "attribute", "advancement", "difficulty", "effect", "fill", "gamerule",
            "item", "loot", "place", "setblock", "summon", "teleport", "tp", "weather", "xp"
    };
    //禁用 - 模组
    public static String[] DISABLE_MOD_ID = {
            "torcherino"
    };

    public AntiCheat() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON,commonConfig);
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::onCommonSetupEvent);
        MinecraftForge.EVENT_BUS.register(this);
    }

    //配置文件部分
    static {
        //构建器
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("AntiCheat").push("Config");
        //禁用模组列表
        banMods = builder.defineList("Disable Mod List", List.of(AntiCheat.DISABLE_MOD_ID), obj -> true);
        //禁用指令列表
        banCommand = builder.defineList("Disable Command List", List.of(AntiCheat.DISABLE_COMMAND), obj -> true);
        //最大加载上限
        maxModCount = builder.defineInRange("Max Mod Count",MAX_MOD_COUNT,3,Integer.MAX_VALUE);
        builder.pop();
        commonConfig = builder.build();
    }

    public void onCommonSetupEvent(FMLCommonSetupEvent event) {
        //当禁用模组列表中有本模组或者未空时候
        if(banMods.get().contains("modpack_anti_cheat")
                || banMods.get().isEmpty()
                || banMods.get().contains("")) {
            //重置禁用列表
            banMods.get().remove("modpack_anti_cheat");
            banMods.set(List.of(AntiCheat.DISABLE_MOD_ID));
        }
        //如果检测到被禁用的模组
        for(String modIds : banMods.get()) {
            if(ModList.get().isLoaded(modIds)) {
                //抛出异常
                throw new RuntimeException("disable mod:" + modIds);
            }
        }
        //如果加载到的数组数量大于设定的上限
        if(ModList.get().getMods().size() > maxModCount.get()) {
            //抛出异常
            throw new RuntimeException("Exceeded the maximum number of mods");
        }
    }
    @SubscribeEvent
    public void onCommandEvent(CommandEvent event) {
        //获取输入的指令，并以空格进行切割
        String[] command = event.getParseResults().getReader().getRead().split(" ");
        //遍历禁用命令列表
        for(String disable_command : banCommand.get()) {
            //如果输入的命令分割后第零下标为禁用列表中的命令
            if (command[0].equals(disable_command)) {
                event.getParseResults().getContext().getSource().sendSystemMessage(Component.translatable("command.disabled"));
                event.setCanceled(true);
            }
        }
    }
}
