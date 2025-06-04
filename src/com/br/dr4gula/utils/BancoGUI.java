package com.br.dr4gula.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Map;

public class BancoGUI {

    private final Inventory bancoMenu;
    private final Inventory saqueMenu;
    private final Inventory depositoMenu;
    private final FileConfiguration config;

    public BancoGUI(FileConfiguration config) {
        this.config = config;

        bancoMenu = Bukkit.createInventory(null, 27, "§c[Banco]");
        adicionarBordas(bancoMenu, (short) 15, "§8Banco");
        bancoMenu.setItem(11, createItem(getItemById("4469"), "§cSacar Ryo", "§aSaque seus Ryos"));
        bancoMenu.setItem(15, createItem(getItemById("4438"), "§cDepositar Ryo", "§aDeposite seus Ryos"));
        bancoMenu.setItem(22, createItem(getItemById("709"), "§cFechar", "§7Clique para fechar o menu"));

        saqueMenu = Bukkit.createInventory(null, 27, "§c[Saque]");
        adicionarBordas(saqueMenu, (short) 9, "§bSaque");
        addSaqueItems(saqueMenu);
        saqueMenu.setItem(22, createItem(getItemById("4177"), "§cVoltar", "§7Clique para voltar"));

        depositoMenu = Bukkit.createInventory(null, 27, "§c[Depósito]");
        adicionarBordas(depositoMenu, (short) 11, "§9Depósito");
        addDepositoItems(depositoMenu);
        depositoMenu.setItem(22, createItem(getItemById("4177"), "§cVoltar", "§7Clique para voltar"));

        reloadMenus(config);
    }


    private void addSaqueItems(Inventory inventory) {
        Map<String, Object> saqueItems = config.getConfigurationSection("menu.saque").getValues(false);
        int slot = 10;

        for (Map.Entry<String, Object> entry : saqueItems.entrySet()) {
            String key = entry.getKey();
            ItemStack item = getItemById(entry.getValue().toString());

            if (key.equalsIgnoreCase("tudo")) {
                inventory.setItem(slot, createItem(item, "§cSacar Tudo", "§aSaque todo seu dinheiro"));
            } else {
                try {
                    int amount = Integer.parseInt(key);
                    inventory.setItem(slot, createItem(item, "§cSacar " + amount, "§aSaque " + amount + " Ryos"));
                } catch (NumberFormatException e) {
                    Bukkit.getLogger().warning("Configuração inválida no menu.saque: " + key + " não é um número nem 'tudo'!");
                }
            }
            slot++;
        }
    }

    private void addDepositoItems(Inventory inventory) {
        Map<String, Object> depositoItems = config.getConfigurationSection("menu.deposito").getValues(false);
        int slot = 10;

        for (Map.Entry<String, Object> entry : depositoItems.entrySet()) {
            String key = entry.getKey();
            ItemStack item = getItemById(entry.getValue().toString());

            if (key.equalsIgnoreCase("tudo")) {
                inventory.setItem(slot, createItem(item, "§cDepositar Tudo", "§aDeposite todo seu dinheiro"));
            } else {
                try {
                    int amount = Integer.parseInt(key);
                    inventory.setItem(slot, createItem(item, "§cDepositar " + amount, "§aDeposite " + amount + " Ryos"));
                } catch (NumberFormatException e) {
                    Bukkit.getLogger().warning("Configuração inválida no menu.deposito: " + key + " não é um número nem 'tudo'!");
                }
            }
            slot++;
        }
    }

    private ItemStack getItemById(String idConfig) {
        try {
            int id;
            short data = 0;

            if (idConfig.contains(":")) {
                String[] parts = idConfig.split(":");
                id = Integer.parseInt(parts[0]);
                data = Short.parseShort(parts[1]);
            } else {
                id = Integer.parseInt(idConfig);
            }

            return createItemStack(id, data);
        } catch (NumberFormatException e) {
            Bukkit.getLogger().warning("ID inválido no config.yml: " + idConfig);
            return new ItemStack(Material.NETHER_STAR);
        }
    }

    private ItemStack createItemStack(int id, short data) {
        try {
            Class<?> itemStackClass = Class.forName("org.bukkit.inventory.ItemStack");
            Constructor<?> constructor = itemStackClass.getConstructor(int.class, int.class, short.class);
            return (ItemStack) constructor.newInstance(id, 1, data);
        } catch (Exception e) {
            Bukkit.getLogger().warning("Erro ao criar item do ID: " + id);
            return new ItemStack(Material.NETHER_STAR);
        }
    }

    private ItemStack createItem(ItemStack item, String name, String... lore) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    private void adicionarBordas(Inventory inv, short data, String nome) {
        ItemStack vidro = new ItemStack(Material.STAINED_GLASS_PANE, 1, data);
        ItemMeta meta = vidro.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(nome);
            vidro.setItemMeta(meta);
        }

        for (int i = 0; i < inv.getSize(); i++) {
            if (i < 9 || i >= inv.getSize() - 9 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inv.setItem(i, vidro);
            }
        }
    }



    public Inventory getBancoMenu() {
        return bancoMenu;
    }

    public Inventory getSaqueMenu() {
        return saqueMenu;
    }

    public Inventory getDepositoMenu() {
        return depositoMenu;
    }

    public void reloadMenus(FileConfiguration config) {
        this.saqueMenu.clear();
        this.depositoMenu.clear();

        adicionarBordas(bancoMenu, (short) 15, "§8Banco");
        adicionarBordas(saqueMenu, (short) 9, "§bSaque");
        adicionarBordas(depositoMenu, (short) 11, "§9Depósito");

        addSaqueItems(this.saqueMenu);
        addDepositoItems(this.depositoMenu);

        this.saqueMenu.setItem(22, createItem(getItemById("4177"), "§cVoltar", "§7Clique para voltar"));
        this.depositoMenu.setItem(22, createItem(getItemById("4177"), "§cVoltar", "§7Clique para voltar"));
    }

}
