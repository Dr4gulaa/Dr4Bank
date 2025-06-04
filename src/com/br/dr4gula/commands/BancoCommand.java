package com.br.dr4gula.commands;

import com.br.dr4gula.utils.BancoGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BancoCommand implements CommandExecutor {

    private final BancoGUI bancoGUI;

    public BancoCommand(BancoGUI bancoGUI) {
        this.bancoGUI = bancoGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando!");
            return true;
        }

        Player player = (Player) sender;
        player.openInventory(bancoGUI.getBancoMenu());
        return true;
    }
}
