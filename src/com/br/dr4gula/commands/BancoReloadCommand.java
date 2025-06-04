package com.br.dr4gula.commands;

import com.br.dr4gula.Main;
import com.br.dr4gula.utils.BancoGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BancoReloadCommand implements CommandExecutor {

    private final Main plugin;
    private final BancoGUI bancoGUI;

    public BancoReloadCommand(Main plugin, BancoGUI bancoGUI) {
        this.plugin = plugin;
        this.bancoGUI = bancoGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("banco.reload")) {
            player.sendMessage("§cVocê não tem permissão para executar este comando!");
            return true;
        }

        plugin.reloadConfig();
        bancoGUI.reloadMenus(plugin.getConfig());
        player.sendMessage("§aConfiguração do banco recarregada com sucesso!");

        return true;
    }
}
