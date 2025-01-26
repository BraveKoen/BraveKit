## AI Assistant
### BraveKit Plugin

BraveKit is a Minecraft Bukkit/Spigot plugin designed to provide robust functionality for creating, managing, and utilizing kits in-game. The plugin allows for dynamic kit creation, configuration management, inventory UI for kit selection, database integration, and cooldown handling for kit usage.

---

## Features


- **Kit Management**: Add, create, and configure kits through in-game commands or configuration files.
- **Inventory UI**: A user-friendly inventory interface for players to select and use available kits.
- **Database Integration**: Uses MySQL (via HikariCP) for storing kits, player usage, and related data.
- **Dynamic Creation**: Build kits dynamically using in-game tools for icons, descriptions, and contents.
- **Cooldowns**: Enforces cooldowns to prevent kits from being used repeatedly in short intervals.
- **Configuration Parsing**: Load kits directly from configuration files at startup or reload them dynamically.
- **Ease of Use**: Commands to refresh kits, open the UI, or debug kit-related issues.

---

## Getting Started

### Requirements

- Java 17 or higher.
- A supported Minecraft server running Bukkit/Spigot.
- MySQL database for storing kit data and player usage details.
- NBT-API plugin for handling serialized item stacks (must be installed on the server).

---

### Installation

1. **Download the Plugin:**
   - Obtain the BraveKit plugin `.jar` file and place it into the server's `plugins/` directory.

2. **Install Prerequisites:**
   - [NBT-API](https://www.spigotmc.org/resources/nbt-api.7939/) must be installed for proper item serialization.

3. **Setup MySQL Database:**
   - Configure MySQL details (username, password, database, etc.) in the plugin’s `config.yml` file.

4. **Start the Server:**
   - Start (or restart) your server to allow the plugin to generate the default configuration files.

5. **Configure Kits:**
   - Add kit configurations manually in the `config.yml` file or use in-game commands to create and manage kits dynamically.

---

## Commands

| Command            | Description                                         | Permission          |
|--------------------|-----------------------------------------------------|---------------------|
| `/kits`            | Opens the inventory UI for available kits.          | `bravekit.kits`     |
| `/createkit <name>`| Starts the process to create a new kit in-game.      | `bravekit.create`   |
| `/refreshkits`     | Refreshes and reloads the kit data from the database.| `bravekit.refresh`  |
| `/debugbrave`      | Prints debug information about loaded kits.          | `bravekit.debug`    |

---

## Configuration

The plugin uses a single configuration file, `config.yml`, for managing settings such as the UI name, secret keys, and database credentials. Here's an example of a typical `config.yml`:

```yaml
kits:
  example_kit:
    name: "Starter Kit"
    description:
      - "Use this kit to start your adventure!"
      - "It has all the essentials."
    icon: "DIAMOND_SWORD"
    items:
      - material: "DIAMOND_SWORD"
        amount: 1
        enchantments:
          - type: "DAMAGE_ALL"
            level: 3
      - material: "GOLDEN_APPLE"
        amount: 10
    cooldown: 3600 # seconds (1 hour)

mysql:
  host: "localhost"
  port: 3306
  database: "minecraft"
  username: "your_username"
  password: "your_password"

kit-ui-name: "Available Kits"
kit-key: "KitSecred"
```

---

## How to Use

1. **Basic Kit Management**:
   - Use `/createkit <name>` to start the creation process.
   - Kits can be assigned custom icons, descriptions, and inventory items.

2. **Selecting Kits**:
   - Use `/kits` to open the kit selection inventory UI and choose a kit.

3. **Refreshing Kits**:
   - When new kits are added to the database or configuration file, you can reload them using `/refreshkits`.

4. **Debugging**:
   - `/debugbrave` shows all loaded kits and their details, helping troubleshoot issues.

---

## Permissions

| Permission         | Description                       |
|--------------------|-----------------------------------|
| `bravekit.kits`    | Access to the kit selection UI.   |
| `bravekit.create`  | Permission to create new kits.    |
| `bravekit.refresh` | Permission to refresh kits.       |
| `bravekit.debug`   | Permission to debug kits.         |

---

## Developer Documentation

### Key Classes Overview

- **`Kit`**: Represents an individual kit. Includes metadata like name, description, items, icon, and timeout.
- **`KitParser`**: Handles parsing kits from the configuration file.
- **`KitBuilder`**: Facilitates building new kits dynamically in-game with custom inputs.
- **`KitUI`**: Manages the visual interface for kit selection.
- **`DatabaseManager`**: Handles interactions with the MySQL database for storing and retrieving kit data.
- **`OpenKitSelector`**: Central command handler for all kit-related commands and interactions.

---

## Future Enhancements

- **PlaceholderAPI Integration**: Add support for placeholders in descriptions.
- **Enhanced UI**: Implement pagination for the inventory UI to handle a large number of kits.
- **Logging**: Better logging for kit usage and database failures.
- **Localization**: Make the plugin compatible with language files for translation support.

---

## Credits

- Plugin Developer: **Koen**
- [NBT-API](https://www.spigotmc.org/resources/nbt-api.7939/) for handling serialized item-stacks.
- Thanks to the Minecraft and Bukkit communities for providing resources and support.

---

If you encounter any issues or have feature requests, feel free to reach out or open a bug report! 🎮
