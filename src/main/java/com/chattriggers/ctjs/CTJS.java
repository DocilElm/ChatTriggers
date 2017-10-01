package com.chattriggers.ctjs;

import com.chattriggers.ctjs.commands.CTCommand;
import com.chattriggers.ctjs.libs.MinecraftVars;
import com.chattriggers.ctjs.listeners.ChatListener;
import com.chattriggers.ctjs.listeners.WorldListener;
import com.chattriggers.ctjs.loader.ScriptLoader;
import com.chattriggers.ctjs.objects.DisplayHandler;
import com.chattriggers.ctjs.triggers.TriggerRegister;
import com.chattriggers.ctjs.utils.ImagesPack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.resources.IResourcePack;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

@Mod(modid = Reference.MODID, name = Reference.MODNAME, version = Reference.MODVERSION, clientSideOnly = true)
public class CTJS {
    @Getter
    private static CTJS instance;

    @Getter @Setter
    private ScriptEngine scriptEngine;
    @Getter @Setter
    private Invocable invocableEngine;
    @Getter @Setter
    private ScriptLoader scriptLoader;
    @Getter @Setter
    private DisplayHandler displayHandler;
    @Getter
    private ImagesPack imagesPack;
    @Getter
    private File assetsDir;

    @EventHandler
    public void init(FMLInitializationEvent e) {
        instance = this;

        this.displayHandler = new DisplayHandler();
        initMain(true);

        MinecraftForge.EVENT_BUS.register(displayHandler);
        MinecraftForge.EVENT_BUS.register(new WorldListener());
        MinecraftForge.EVENT_BUS.register(new ChatListener());

        ClientRegistry.registerKeyBinding(MinecraftVars.keyLeftArrow);
        ClientRegistry.registerKeyBinding(MinecraftVars.keyRightArrow);
        ClientRegistry.registerKeyBinding(MinecraftVars.keyUpArrow);
        ClientRegistry.registerKeyBinding(MinecraftVars.keyDownArrow);

        ClientCommandHandler.instance.registerCommand(new CTCommand());
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        this.injectResourcePack(e.getModConfigurationDirectory().toString());
    }

    public void initMain(boolean firstTime) {
        this.scriptEngine = new ScriptEngineManager(null).getEngineByName("nashorn");
        this.invocableEngine = ((Invocable) scriptEngine);

        if (!firstTime) {
            MinecraftForge.EVENT_BUS.unregister(scriptLoader);
            TriggerRegister.TriggerTypes.clearAllTriggers();
        }

        this.displayHandler.clearDisplays();

        this.scriptLoader = new ScriptLoader();
        MinecraftForge.EVENT_BUS.register(scriptLoader);

        System.gc();
    }

    private void injectResourcePack(String path) {
        try {
            File pictures = new File(path, "ctjs/images/");
            Field field = FMLClientHandler.class.getDeclaredField("resourcePackList");
            field.setAccessible(true);

            List<IResourcePack> packs = (List<IResourcePack>) field.get(FMLClientHandler.instance());
            packs.add(imagesPack = new ImagesPack(pictures));
            pictures.mkdirs();
            assetsDir = pictures;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}

class Reference {
    public static final String MODID = "ct.js";
    public static final String MODNAME = "ChatTriggers.js";
    public static final String MODVERSION = "0.1-SNAPSHOT";
    public static final String MAPPINGURL = "http://export.mcpbot.bspk.rs/mcp_stable_nodoc/22-1.8.9/mcp_stable_nodoc-22-1.8.9.zip";
}