package com.br.dr4gula.events;

import com.br.dr4gula.Main;
import com.br.dr4gula.utils.BancoGUI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BancoListener implements Listener {

    private final BancoGUI bancoGUI;
    private final Economy economy;

    private final Map<Integer, Integer> notas = new HashMap<>();

    public BancoListener(BancoGUI bancoGUI) {
        this.bancoGUI = bancoGUI;
        this.economy = Main.getEconomy();

        notas.put(1000, 4430);
        notas.put(100, 4429);
        notas.put(10, 4428);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView() == null || event.getView().getTitle() == null) {
            return;
        }

        String inventoryTitle = event.getView().getTitle();

        if (!inventoryTitle.contains("[Banco]") &&
                !inventoryTitle.contains("[Saque]") &&
                !inventoryTitle.contains("[Depósito]")) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String itemName = clickedItem.getItemMeta().getDisplayName();


        if (itemName.contains("Voltar")) {
            bancoGUI.reloadMenus(Main.getInstance().getConfig());
            player.openInventory(bancoGUI.getBancoMenu());
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        if (itemName.contains("Sacar Ryo")) {
            player.openInventory(bancoGUI.getSaqueMenu());
            return;
        }

        if (itemName.contains("Depositar Ryo")) {
            player.openInventory(bancoGUI.getDepositoMenu());
            return;
        }

        if (inventoryTitle.contains("[Saque]")) {
            if (itemName.contains("Sacar Tudo")) {
                double saldo = economy.getBalance(player);
                sacarDinheiro(player, saldo);
            } else {
                int amount = extrairValor(itemName, "Sacar ");
                sacarDinheiro(player, amount);
            }
            return;
        }

        if (inventoryTitle.contains("[Depósito]")) {
            if (itemName.contains("Depositar Tudo")) {
                depositarDinheiro(player, true);
            } else {
                int amount = extrairValor(itemName, "Depositar ");
                depositarDinheiro(player, false, amount);
            }
            return;
        }

        if (itemName.contains("Fechar")) {
            player.closeInventory();
        }
    }




    private void sacarDinheiro(Player player, double amount) {
        
        if (economy.getBalance(player) < amount) {
            player.sendMessage("§cVocê não tem dinheiro suficiente para sacar!");
            return;
        }

        List<Map.Entry<Integer, Integer>> notasOrdenadas = new ArrayList<>(notas.entrySet());
        notasOrdenadas.sort((a, b) -> Integer.compare(b.getKey(), a.getKey()));

        List<ItemStack> itensParaAdicionar = new ArrayList<>();
        double totalSacado = 0;

        for (Map.Entry<Integer, Integer> nota : notasOrdenadas) {
            int valorNota = nota.getKey();
            int idNota = nota.getValue();
            int quantidade = (int) (amount / valorNota);

            if (quantidade > 0) {
                amount -= quantidade * valorNota;
                totalSacado += quantidade * valorNota;

                while (quantidade > 0) {
                    int stackSize = Math.min(quantidade, 64);
                    quantidade -= stackSize;
                    itensParaAdicionar.add(criarNota(idNota, stackSize, valorNota));
                }
            }
        }

        if (!temEspacoSuficiente(player, itensParaAdicionar)) {
            player.sendMessage("§cSeu inventário não tem espaço suficiente para sacar essa quantia!");
            return;
        }

        for (ItemStack item : itensParaAdicionar) {
            player.getInventory().addItem(item);
        }

        economy.withdrawPlayer(player, totalSacado);

        player.sendMessage("§aVocê sacou " + totalSacado + " Ryos!");
    }





    private boolean temEspacoSuficiente(Player player, List<ItemStack> itens) {
        Inventory inv = player.getInventory();
        Inventory invClone = Bukkit.createInventory(null, inv.getSize());

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null) invClone.setItem(i, item.clone());
        }

        for (ItemStack item : itens) {
            Map<Integer, ItemStack> sobras = invClone.addItem(item);
            if (!sobras.isEmpty()) {
                return false;
            }
        }

        return true;
    }


    private void depositarDinheiro(Player player, boolean depositarTudo, int... valores) {

        int totalDeposito = 0;
        int limiteDeposito = depositarTudo ? Integer.MAX_VALUE : valores[0];
        List<ItemStack> itensParaRemover = new ArrayList<>();

        List<Map.Entry<Integer, Integer>> notasOrdenadas = new ArrayList<>(notas.entrySet());
        notasOrdenadas.sort((a, b) -> Integer.compare(b.getKey(), a.getKey()));

        for (Map.Entry<Integer, Integer> nota : notasOrdenadas) {
            int valorNota = nota.getKey();

            for (ItemStack item : player.getInventory().getContents()) {
                if (item == null || !ehNotaValida(item) || getValorNota(item) != valorNota) {
                    continue;
                }

                int quantidade = Math.min(item.getAmount(), limiteDeposito / valorNota);
                if (quantidade <= 0) continue;

                totalDeposito += valorNota * quantidade;
                limiteDeposito -= valorNota * quantidade;

                ItemStack itemRemover = item.clone();
                itemRemover.setAmount(quantidade);
                itensParaRemover.add(itemRemover);

                if (limiteDeposito <= 0) break;
            }

            if (limiteDeposito <= 0) break;
        }

        if (totalDeposito == 0) {
            player.sendMessage("§cVocê não tem notas/moedas suficientes para depositar.");
            return;
        }

        for (ItemStack item : itensParaRemover) {
            player.getInventory().removeItem(item);
        }

        economy.depositPlayer(player, totalDeposito);

        player.sendMessage("§aVocê depositou " + totalDeposito + " Ryos!");
    }


    private boolean ehNotaValida(ItemStack item) {
        int id = item.getTypeId();
        return notas.containsValue(id);
    }

    private int getValorNota(ItemStack item) {
        int id = item.getTypeId();
        for (Map.Entry<Integer, Integer> nota : notas.entrySet()) {
            if (nota.getValue() == id) {
                return nota.getKey();
            }
        }
        return 0;
    }

    private int extrairValor(String texto, String prefixo) {
        try {
            String textoLimpo = texto.replaceAll("§[0-9a-fk-or]", "");

            String numeroTexto = textoLimpo.replace(prefixo, "").replace(" Ryos", "").trim();

            return Integer.parseInt(numeroTexto);
        } catch (NumberFormatException e) {
            System.out.println("⚠ Erro ao converter valor de: " + texto);
            return 0;
        }
    }


    private ItemStack criarNota(int id, int quantidade, int valor) {
        ItemStack item = new ItemStack(id, quantidade);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String cor = (valor == 1000) ? "§2" : (valor == 100) ? "§a" : "§8";
            meta.setDisplayName(cor + valor + " Ryos");
            item.setItemMeta(meta);
        }

        return item;
    }

}
