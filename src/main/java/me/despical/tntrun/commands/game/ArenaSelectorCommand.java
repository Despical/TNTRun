package me.despical.tntrun.commands.game;

import me.despical.commonsbox.compat.XMaterial;
import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.number.NumberUtils;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaManager;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.commands.SubCommand;
import me.despical.tntrun.handlers.ChatManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ArenaSelectorCommand extends SubCommand implements Listener {

    private final ChatManager chatManager;

    public ArenaSelectorCommand() {
        super("arenas");
        setPermission("oitc.arenas");

        this.chatManager = getPlugin().getChatManager();
        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
    }

    @Override
    public String getPossibleArguments() {
        return null;
    }

    @Override
    public int getMinimumArguments() {
        return 0;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;

        if (ArenaRegistry.getArenas().isEmpty()) {
            player.sendMessage(chatManager.getPrefix() + chatManager.colorMessage("Commands.No-Free-Arenas"));
            return;
        }

        Inventory inventory = Bukkit.createInventory(player, NumberUtils.serializeInt(ArenaRegistry.getArenas().size()), chatManager.colorMessage("Arena-Selector.Inventory-Title"));

        for (Arena arena : ArenaRegistry.getArenas()) {
            ItemStack itemStack;

            switch (arena.getArenaState()) {
                case WAITING_FOR_PLAYERS:
                    itemStack = XMaterial.LIME_CONCRETE.parseItem();
                    break;
                case STARTING:
                    itemStack = XMaterial.YELLOW_CONCRETE.parseItem();
                    break;
                default:
                    itemStack = XMaterial.RED_CONCRETE.parseItem();
                    break;
            }

            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(arena.getId());

            ArrayList<String> lore = new ArrayList<>();
            FileConfiguration config = ConfigUtils.getConfig(getPlugin(), "messages");

            for (String string : config.getStringList("Arena-Selector.Item.Lore")) {
                lore.add(formatItem(string, arena, getPlugin()));
            }

            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
            inventory.addItem(itemStack);
        }

        player.openInventory(inventory);
    }

    private String formatItem(String string, Arena arena, Main plugin) {
        String formatted = string;
        formatted = StringUtils.replace(formatted, "%mapname%", arena.getMapName());

        if (arena.getPlayers().size() >= arena.getMaximumPlayers()) {
            formatted = StringUtils.replace(formatted, "%state%", chatManager.colorMessage("Signs.Game-States.Full-Game"));
        } else {
            formatted = StringUtils.replace(formatted, "%state%", plugin.getSignManager().getGameStateToString().get(arena.getArenaState()));
        }

        formatted = StringUtils.replace(formatted, "%playersize%", String.valueOf(arena.getPlayers().size()));
        formatted = StringUtils.replace(formatted, "%maxplayers%", String.valueOf(arena.getMaximumPlayers()));
        formatted = chatManager.colorRawMessage(formatted);
        return formatted;
    }

    @EventHandler
    public void onArenaSelectorMenuClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(chatManager.colorMessage("Arena-Selector.Inventory-Title"))) {
            return;
        }

        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) {
            return;
        }

        Player player = (Player) e.getWhoClicked();
        player.closeInventory();

        Arena arena = ArenaRegistry.getArena(e.getCurrentItem().getItemMeta().getDisplayName());

        if (arena != null) {
            ArenaManager.joinAttempt(player, arena);
        } else {
            player.sendMessage(chatManager.getPrefix() + chatManager.colorMessage("Commands.No-Arena-Like-That"));
        }
    }

    @Override
    public List<String> getTutorial() {
        return null;
    }

    @Override
    public CommandType getType() {
        return CommandType.HIDDEN;
    }

    @Override
    public SenderType getSenderType() {
        return SenderType.PLAYER;
    }
}