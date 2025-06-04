package com.br.dr4gula;

import com.br.dr4gula.commands.BancoCommand;
import com.br.dr4gula.commands.BancoReloadCommand;
import com.br.dr4gula.events.BancoListener;
import com.br.dr4gula.utils.BancoGUI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Economy econ = null;
    private BancoGUI bancoGUI;
    private static Main instance;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Vault não encontrado! O plugin será desativado.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        FileConfiguration config = getConfig();

        bancoGUI = new BancoGUI(config);
        instance = this;

        getCommand("banco").setExecutor(new BancoCommand(bancoGUI));
        getCommand("bancoreload").setExecutor(new BancoReloadCommand(this, bancoGUI));
        getServer().getPluginManager().registerEvents(new BancoListener(bancoGUI), this);

        getLogger().info("[Dr4Bank] Banco carregado com sucesso!");

    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin de Banco desativado.");
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static Main getInstance() {
        return instance;
    }
}
