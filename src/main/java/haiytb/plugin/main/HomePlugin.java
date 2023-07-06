package haiytb.plugin.main;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HomePlugin extends JavaPlugin {
    private Map<String, HashMap<String, Location>> homes;
    private File homeFile;

    @Override
    public void onEnable() {
        homeFile = new File(getDataFolder(), "home.yml");
        if (!homeFile.exists()) {
            saveResource("home.yml", false);
        }

        homes = new HashMap<>();
        loadHomeConfig();

        getLogger().info("HomePlugin đã được bật.");
    }

    @Override
    public void onDisable() {
        saveHomeConfig();
        getLogger().info("HomePlugin đã bị tắt.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Lệnh này chỉ có thể được thực hiện bởi người chơi.");
            return true;
        }

        Player player = (Player) sender;
        String playerName = player.getName();

        if (cmd.getName().equalsIgnoreCase("sethome")) {
            if (args.length < 1) {
                player.sendMessage("Sử dụng: /sethome <ten_home>");
                return true;
            }

            String homeName = args[0];
            Location homeLocation = player.getLocation();

            HashMap<String, Location> playerHomes = homes.getOrDefault(playerName, new HashMap<String, Location>());
            playerHomes.put(homeName, homeLocation);
            homes.put(playerName, playerHomes);

            saveHomeConfig();

            player.sendMessage("Home '" + homeName + "' đã được đặt.");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("home")) {
            if (args.length < 1) {
                player.sendMessage("Sử dụng: /home <ten_home>");
                return true;
            }

            String homeName = args[0];
            HashMap<String, Location> playerHomes = homes.get(playerName);

            if (playerHomes == null || !playerHomes.containsKey(homeName)) {
                player.sendMessage("Home '" + homeName + "' không tồn tại hoặc bạn chưa đặt home này.");
                return true;
            }

            Location homeLocation = playerHomes.get(homeName);
            player.teleport(homeLocation);
            player.sendMessage("Dịch chuyển đến home " + homeName + ".");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("delhome")) {
            if (args.length < 1) {
                player.sendMessage("Sử dụng: /delhome <ten_home>");
                return true;
            }

            String homeName = args[0];
            HashMap<String, Location> playerHomes = homes.get(playerName);

            if (playerHomes == null || !playerHomes.containsKey(homeName)) {
                player.sendMessage("Home '" + homeName + "' không tồn tại hoặc bạn chưa đặt home này.");
                return true;
            }

            playerHomes.remove(homeName);
            saveHomeConfig();

            player.sendMessage("Home '" + homeName + "' đã bị xóa.");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("updatehome")) {
            if (args.length < 1) {
                player.sendMessage("Sử dụng: /updatehome <ten_home>");
                return true;
            }

            String homeName = args[0];
            Location newLocation = player.getLocation();
            HashMap<String, Location> playerHomes= homes.get(playerName);

            if (playerHomes == null || !playerHomes.containsKey(homeName)) {
                player.sendMessage("Home '" + homeName + "' không tồn tại hoặc bạn chưa đặt home này.");
                return true;
            }

            playerHomes.put(homeName, newLocation);
            saveHomeConfig();

            player.sendMessage("Home '" + homeName + "' đã được cập nhật vào vị trí hiện tại của bạn.");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("homes")) {
            HashMap<String, Location> playerHomes = homes.get(playerName);

            if (playerHomes == null || playerHomes.isEmpty()) {
                player.sendMessage("Bạn chưa đặt home nào.");
                return true;
            }

            player.sendMessage("Homes: ");
            for (String homeName : playerHomes.keySet()) {
                player.sendMessage("- " + homeName);
            }
            return true;
        }

        return false;
    }

    private void loadHomeConfig() {
        FileConfiguration homeConfig = YamlConfiguration.loadConfiguration(homeFile);

        if (homeConfig.contains("homes")) {
            for (String playerName : homeConfig.getConfigurationSection("homes").getKeys(false)) {
                HashMap<String, Location> playerHomes = new HashMap<>();
                for (String homeName : homeConfig.getConfigurationSection("homes." + playerName).getKeys(false)) {
                    double x = homeConfig.getDouble("homes." + playerName + "." + homeName + ".x");
                    double y = homeConfig.getDouble("homes." + playerName + "." + homeName + ".y");
                    double z = homeConfig.getDouble("homes." + playerName + "." + homeName + ".z");
                    String worldName = homeConfig.getString("homes." + playerName + "." + homeName + ".world");

                    Location homeLocation = new Location(getServer().getWorld(worldName), x, y, z);
                    playerHomes.put(homeName, homeLocation);
                }
                homes.put(playerName, playerHomes);
            }
        }
    }

    private void saveHomeConfig() {
        FileConfiguration homeConfig = YamlConfiguration.loadConfiguration(homeFile);
        homeConfig.set("homes", null);

        for (Map.Entry<String, HashMap<String, Location>> playerEntry : homes.entrySet()) {
            String playerName = playerEntry.getKey();
            HashMap<String, Location> playerHomes = playerEntry.getValue();

            for (Map.Entry<String, Location> homeEntry : playerHomes.entrySet()) {
                String homeName = homeEntry.getKey();
                Location homeLocation = homeEntry.getValue();

                homeConfig.set("homes." + playerName + "." + homeName + ".x", homeLocation.getX());
                homeConfig.set("homes." + playerName + "." + homeName + ".y", homeLocation.getY());
                homeConfig.set("homes." + playerName + "." + homeName + ".z", homeLocation.getZ());
                homeConfig.set("homes." + playerName + "." + homeName + ".world", homeLocation.getWorld().getName());
            }
        }

        try {
            homeConfig.save(homeFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
