package haiytb.plugin.listener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class HomeListener implements Listener {
    private Plugin plugin;
    private FileConfiguration homeConfig;
    private File homeFile;

    public HomeListener(Plugin plugin) {
        this.plugin = plugin;

        // Tạo hoặc tải tệp home.yml
        homeFile = new File(plugin.getDataFolder(), "home.yml");
        if (!homeFile.exists()) {
            plugin.saveResource("home.yml", false);
        }
        homeConfig = YamlConfiguration.loadConfiguration(homeFile);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        Location newLocation = event.getTo();
        homeConfig.set("homes." + playerName, newLocation);
        saveHomeConfig();
    }

    private void saveHomeConfig() {
        try {
            homeConfig.save(homeFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Lưu thất bại home.yml: " + e.getMessage());
        }
    }
}
