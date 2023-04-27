package me.junkiecraft.TownyPlots.menus;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.confirmations.Confirmation;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.PlotGroup;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.tasks.PlotClaim;

import me.junkiecraft.TownyPlots.Main;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class MenuManager implements Listener {
    private Main plugin;
    private Economy econ;
    String PREFIX = Main.getChatUtils().getMessage("prefix");

    public MenuManager() {
        plugin = Main.getInstance();
        if (!setupEconomy()) {
            plugin.getLogger().severe("Vault not found, disabling plugin...");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    private Town getTown(Player player) {
        Town town = null;
        try {
            town = getResident(player).getTown();
        } catch (NotRegisteredException ex) {
            ex.printStackTrace();
        }
        return town;
    }

    private Resident getResident(Player player) {
        Resident resident = null;
        resident = TownyUniverse.getInstance().getResident(player.getName());

        return resident;
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public void openParcelMenu(Player player, int pageNumber) throws NotRegisteredException {

        Resident resident;
        Town town;

        resident = getResident(player);
        try {
            resident = getResident(player);
        } catch (Exception e) {
            player.sendMessage(PREFIX + ": " + Main.getChatUtils().getMessage("residentError"));
            return;
        }

        if (!resident.hasTown()) {
            player.sendMessage(PREFIX + ": " + Main.getChatUtils().getMessage("noCity"));
            return;
        }

        town = getTown(player);

        List<TownBlock> availablePlots = new ArrayList<>();

        Set<PlotGroup> uniqueGroups = new HashSet<>();
        for (TownBlock townBlock : town.getTownBlocks()) {
            if (townBlock.getPlotPrice() > 0 && townBlock.getResidentOrNull() == null) {
                if (townBlock.hasPlotObjectGroup()) {
                    uniqueGroups.add(townBlock.getPlotObjectGroup());
                } else {
                    availablePlots.add(townBlock);
                }
            }
        }
        availablePlots.addAll(uniqueGroups.stream().map(group -> group.getTownBlocks().iterator().next()).collect(Collectors.toList()));

        int plotsPerPage = 45;
        int totalPages = (int) Math.ceil(availablePlots.size() / (double) plotsPerPage);
        int startIndex = (pageNumber - 1) * plotsPerPage;
        int endIndex = Math.min(startIndex + plotsPerPage, availablePlots.size());

        if (availablePlots.size() <= 0) {
            player.sendMessage(PREFIX + ": " + Main.getChatUtils().getMessage("noPlots"));
        } else {
            Inventory inventory = Bukkit.createInventory(player, 54, Main.getChatUtils().getMessage("menuTitle"));
            for (int i = startIndex; i < endIndex; i++) {
                TownBlock townBlock = availablePlots.get(i);
                ItemStack plotItem = new ItemStack(Material.GRASS_BLOCK);
                ItemMeta meta = plotItem.getItemMeta();

                meta.setDisplayName(
                        Main.getChatUtils().getMessage("plot") + " " + townBlock.getX() + "," + townBlock.getZ());

                ArrayList<String> lore = new ArrayList<>();
                if (townBlock.hasPlotObjectGroup()) {
                    if (townBlock.getPlotObjectGroup().getName().isEmpty()) {
                        lore.add(Main.getChatUtils().getMessage("name") + " "
                                + Main.getChatUtils().getMessage("noName"));
                    } else {
                        lore.add(Main.getChatUtils().getMessage("name") + " "
                                + townBlock.getPlotObjectGroup().getName());
                    }
                    lore.add(Main.getChatUtils().getMessage("price") + " "
                            + econ.format(townBlock.getPlotObjectGroup().getPrice()));
                    int groupSize = townBlock.getPlotObjectGroup().getTownBlocks().size();
                    lore.add(Main.getChatUtils().getMessage("groupSize") + " " + groupSize + " " + Main.getChatUtils().getMessage("groupSizePlots"));
                } else {
                    if (townBlock.getName().isEmpty()) {
                        lore.add(Main.getChatUtils().getMessage("name") + " "
                                + Main.getChatUtils().getMessage("noName"));
                    } else {
                        lore.add(Main.getChatUtils().getMessage("name") + " " + townBlock.getName());
                    }
                    lore.add(Main.getChatUtils().getMessage("price") + " " + econ.format(townBlock.getPlotPrice()));
                }

                meta.setLore(lore);
                plotItem.setItemMeta(meta);

                inventory.addItem(plotItem);
            }
            if (pageNumber > 1) {
                ItemStack prevButton = new ItemStack(Material.ARROW);
                ItemMeta prevMeta = prevButton.getItemMeta();
                String prevPageDisplay = Main.getChatUtils().getMessage("previousPage") + " " + (pageNumber - 1);
                prevMeta.setDisplayName(prevPageDisplay);
                prevButton.setItemMeta(prevMeta);
                inventory.setItem(45, prevButton);
            }

            if (pageNumber < totalPages) {
                ItemStack nextButton = new ItemStack(Material.ARROW);
                ItemMeta nextMeta = nextButton.getItemMeta();
                String nextPageDisplay = Main.getChatUtils().getMessage("nextPage") + " " + (pageNumber + 1);
                nextMeta.setDisplayName(nextPageDisplay);
                nextButton.setItemMeta(nextMeta);
                inventory.setItem(53, nextButton);
            }
            
            player.openInventory(inventory);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Resident resident;
        Town town;

        if (!event.getView().getTitle().equals(Main.getChatUtils().getMessage("menuTitle"))) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        } 
        if (event.getSlot() == 45) {
            if (event.getInventory().getItem(45) == null) {
                return;
            } else {
                String currentPages = event.getInventory().getItem(45).getItemMeta().getDisplayName()
                .replace(ChatColor.stripColor(Main.getChatUtils().getMessage("previousPage")) + " ", "");
                currentPages = ChatColor.stripColor(currentPages);
                int currentPage = Integer.parseInt(currentPages);
                try {
                    openParcelMenu(player, currentPage);
                } catch (NotRegisteredException e) {
                    player.sendMessage(PREFIX + ": " + Main.getChatUtils().getMessage("errorMenu"));
                }
            }

        } else if (event.getSlot() == 53) {
            String currentPages = event.getInventory().getItem(53).getItemMeta().getDisplayName()
                    .replace(ChatColor.stripColor(Main.getChatUtils().getMessage("nextPage")) + " ", "");
            currentPages = ChatColor.stripColor(currentPages);
            int currentPage = Integer.parseInt(currentPages);
            try {
                openParcelMenu(player, currentPage);
            } catch (NotRegisteredException e) {
                player.sendMessage(PREFIX + ": " + Main.getChatUtils().getMessage("errorMenu"));
            }
        } else {
            String[] plotCoords = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName())
                    .replace(ChatColor.stripColor(Main.getChatUtils().getMessage("plot")) + " ", "").split(",");

            int x = Integer.parseInt(plotCoords[0]);
            int z = Integer.parseInt(plotCoords[1]);

            try {
                resident = getResident(player);
            } catch (Exception e) {
                player.sendMessage(PREFIX + ": " + Main.getChatUtils().getMessage("residentError"));
                return;
            }

            town = getTown(player);
            TownBlock townBlock;

            try {
                WorldCoord worldCoord = new WorldCoord(town.getWorld().getName(), x, z);
                townBlock = TownyAPI.getInstance().getTownBlock(worldCoord);
            } catch (Exception e) {
                player.sendMessage(PREFIX + ": " + Main.getChatUtils().getMessage("errorPlotPurchase"));
                return;
            }

            player.closeInventory();

            int centerX = (x * TownySettings.getTownBlockSize()) + (TownySettings.getTownBlockSize() / 2);
            int centerZ = (z * TownySettings.getTownBlockSize()) + (TownySettings.getTownBlockSize() / 2);

            int y = town.getWorld().getHighestBlockYAt(centerX, centerZ);

            Location plotCenter = new Location(town.getWorld(), centerX, y+1, centerZ);
            player.teleport(plotCenter);
            PlotGroup group = townBlock.getPlotObjectGroup();
            Confirmation.runOnAccept(() -> {
                if (townBlock.hasPlotObjectGroup()) {
                    double plotPrice2 = townBlock.getPlotObjectGroup().getPrice();
                    UUID uuid = player.getUniqueId();
                    player.sendMessage(PREFIX + ": " + Main.getChatUtils().getMessage("buySuccess"));
                    if (econ.getBalance(Bukkit.getOfflinePlayer(uuid)) < plotPrice2) {
                        player.sendMessage(PREFIX + ": " + Main.getChatUtils().getMessage("noMoney"));
                        return;
                    }
                    try {
                        ArrayList<WorldCoord> coords = new ArrayList<>();
                        group.getTownBlocks().forEach((tblock) -> coords.add(tblock.getWorldCoord()));
                        new PlotClaim(Towny.getPlugin(), player, resident, coords, true, false, true).start();
                        TownyUniverse.getInstance().getDataSource().saveResident(resident);
                        TownyUniverse.getInstance().getDataSource().saveTownBlock(townBlock);
                        player.sendMessage(PREFIX + ": " + Main.getChatUtils().getMessage("buySuccess"));
                    } catch (Exception e) {
                        player.sendMessage(PREFIX + ": " + Main.getChatUtils().getMessage("errorPlotPurchase"));
                    }
                } else {
                    double plotPrice = townBlock.getPlotPrice();
                    UUID uuid = player.getUniqueId();

                    if (econ.getBalance(Bukkit.getOfflinePlayer(uuid)) < plotPrice) {
                        player.sendMessage(PREFIX + ": " + Main.getChatUtils().getMessage("noMoney"));
                        return;
                    }
                    try {
                        econ.withdrawPlayer(Bukkit.getOfflinePlayer(uuid), plotPrice);
                        townBlock.setResident(resident);
                        townBlock.setPlotPrice(-1);
                        TownyUniverse.getInstance().getDataSource().saveResident(resident);
                        TownyUniverse.getInstance().getDataSource().saveTownBlock(townBlock);
                        player.sendMessage(PREFIX + ": " + Main.getChatUtils().getMessage("buySuccess"));
                    } catch (Exception e) {
                        player.sendMessage(PREFIX + ": " + Main.getChatUtils().getMessage("errorPlotPurchase"));
                    }

                }
            }).runOnCancel(() -> {
                int currentPage = 1;
                try {
                    openParcelMenu(player, currentPage);
                } catch (Exception e) {
                    player.sendMessage(PREFIX + ": " + Main.getChatUtils().getMessage("errorMenu"));
                }

            }).setTitle(Main.getChatUtils().getMessage("confirmPurchase")).sendTo(player);
        }

    }

}