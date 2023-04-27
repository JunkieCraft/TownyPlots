package me.junkiecraft.TownyPlots;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import me.junkiecraft.TownyPlots.commands.CommandManager;
import me.junkiecraft.TownyPlots.menus.MenuManager;
import me.junkiecraft.TownyPlots.utils.ChatUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandExecutor;

public class Main extends JavaPlugin {
    private static Main instance;
    private static ChatUtils chatUtils;
    private MenuManager menuManager;
    private static Economy economy = null;

    @Override
    public void onEnable() {
        chatUtils = new ChatUtils(this);
        instance = this;
        saveDefaultConfig();
        Logger log = Bukkit.getLogger();
        log.info("##############################################################");
        log.info("###################   Enabled TownyPlots   ###################");
        log.info("###################      Author SrVoRy     ###################");
        log.info("###################       Version 0.3      ###################");
        log.info("##############################################################");        
        registrarComandos();
        menuManager = new MenuManager();
        Bukkit.getPluginManager().registerEvents(menuManager, this);
        
    }

    public static Main getInstance() {
        return instance;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public static ChatUtils getChatUtils() {
        return chatUtils;
    }    

    public static Economy getEconomy() {
        return economy;
    }

    public void registrarComandos() {
        getCommand("townyplots").setExecutor((CommandExecutor) new CommandManager(this));
        getCommand("townyplots").setTabCompleter(new CommandManager(this));
    }
}