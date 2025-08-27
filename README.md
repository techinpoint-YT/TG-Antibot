# TG-AntiBot - Advanced Minecraft Security Plugin

[![Version](https://img.shields.io/badge/version-2.1.0-blue.svg)](https://github.com/TechinpointGamerz/TG-AntiBot)
[![Minecraft](https://img.shields.io/badge/minecraft-1.21.8-green.svg)](https://www.minecraft.net/)
[![License](https://img.shields.io/badge/license-MIT-yellow.svg)](LICENSE)

## 🛡️ Overview

TG-AntiBot is a next-generation security plugin for Minecraft servers, featuring advanced bot detection, VPN/Proxy blocking, and AI-powered threat analysis. Built specifically for Minecraft 1.21.8 with full backward compatibility.

## ✨ Features

### 🤖 Advanced Bot Detection
- **Machine Learning Engine**: AI-powered pattern recognition
- **Behavioral Analysis**: Real-time player behavior monitoring
- **Connection Pattern Detection**: Identifies suspicious connection patterns
- **Multi-layered Security**: Multiple detection algorithms working together

### 🌐 Network Security
- **VPN/Proxy Detection**: Advanced proxy detection with multiple providers
- **Geographic Filtering**: Country-based connection restrictions
- **IP Reputation System**: Dynamic IP scoring and blacklisting
- **Firewall Integration**: Built-in network firewall with automatic rules

### 📊 Real-time Monitoring
- **Live Dashboard**: Real-time statistics and monitoring
- **Attack Analytics**: Detailed attack pattern analysis
- **Performance Metrics**: Comprehensive performance monitoring
- **Alert System**: Multi-channel notification system

### 🔧 Advanced Configuration
- **Flexible Settings**: Highly customizable protection levels
- **Database Support**: SQLite and MySQL database backends
- **Cache System**: High-performance caching with Caffeine
- **API Integration**: PlaceholderAPI and other plugin integrations

## 🚀 Installation

1. **Download** the latest release from [GitHub Releases](https://github.com/TechinpointGamerz/TG-AntiBot/releases)
2. **Place** the JAR file in your server's `plugins` folder
3. **Restart** your server
4. **Configure** the plugin using `/tga reload` after editing `config.yml`

## 📋 Requirements

- **Minecraft Version**: 1.21.8 (backward compatible to 1.16)
- **Java Version**: 17 or higher
- **Server Software**: Spigot, Paper, or Purpur
- **Memory**: Minimum 512MB RAM allocated to the plugin

## 🔧 Configuration

### Basic Setup

```yaml
# Enable the plugin
enabled: true

# Protection thresholds
protection:
  join-threshold: 10
  ping-threshold: 50
  cooldown-time: 60000

# VPN Protection
antivpn:
  enabled: true
  api-key: "YOUR_API_KEY_HERE"
```

### Advanced Features

```yaml
# Machine Learning
advanced:
  ml-detection: true
  learning-mode: true
  accuracy-threshold: 0.85

# Database Configuration
database:
  type: "SQLITE"  # or "MYSQL"
  
# Performance Optimization
advanced:
  cache-size: 10000
  async-processing: true
```

## 📖 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/tga help` | Show help menu | `tga.use` |
| `/tga stats` | View protection statistics | `tga.stats` |
| `/tga status` | Check current protection status | `tga.status` |
| `/tga reload` | Reload configuration | `tga.reload` |
| `/tga whitelist <add/remove> <ip>` | Manage IP whitelist | `tga.whitelist` |
| `/tga blacklist <add/remove> <ip>` | Manage IP blacklist | `tga.blacklist` |
| `/tga profile <player>` | View player security profile | `tga.profile` |
| `/tga attacks [limit]` | View recent attacks | `tga.attacks` |

## 🔐 Permissions

### Basic Permissions
- `tga.use` - Basic command access
- `tga.bypass` - Bypass all protection measures
- `tga.alerts` - Receive security alerts

### Administrative Permissions
- `tga.admin` - Full administrative access
- `tga.staff` - Staff monitoring access
- `tga.reload` - Reload configuration

## 🔌 API Usage

### PlaceholderAPI Integration

```
%tgantibot_protection_mode% - Current protection mode
%tgantibot_joins_per_second% - Joins per second
%tgantibot_is_whitelisted% - Player whitelist status
%tgantibot_suspicion_score% - Player suspicion score
%tgantibot_total_attacks_blocked% - Total attacks blocked
```

### Developer API

```java
// Get plugin instance
TGAntiBotPlugin plugin = TGAntiBotPlugin.getInstance();

// Check if IP is blocked
boolean blocked = plugin.getThreatDetectionEngine().shouldBlockConnection(uuid, address, name);

// Get player security profile
PlayerProfile profile = plugin.getThreatDetectionEngine().getPlayerProfiles().get(ip);

// Add to whitelist
plugin.getThreatDetectionEngine().addToWhitelist(ip);
```

## 📈 Performance

TG-AntiBot is designed for high-performance servers:

- **Asynchronous Processing**: All heavy operations run asynchronously
- **Efficient Caching**: Caffeine-based caching system
- **Database Optimization**: Connection pooling with HikariCP
- **Memory Management**: Automatic cleanup and optimization

## 🤝 Integration

### Supported Plugins
- **PlaceholderAPI** - Custom placeholders
- **ProtocolLib** - Advanced packet handling
- **AuthMe** - Authentication integration
- **Essentials** - Command integration
- **LuckPerms** - Permission integration
- **ViaVersion** - Multi-version support

## 🐛 Troubleshooting

### Common Issues

1. **High CPU Usage**
   - Reduce `cache-size` in config
   - Disable `ml-detection` if not needed
   - Increase `cleanup-interval`

2. **False Positives**
   - Adjust `suspicion-threshold`
   - Add legitimate IPs to whitelist
   - Fine-tune protection thresholds

3. **Database Errors**
   - Check database permissions
   - Verify connection settings
   - Enable debug mode for detailed logs

## 📊 Statistics

View real-time statistics with `/tga stats`:

```
Protection Mode: Normal
Joins/sec: 2
Pings/sec: 15
Total Attacks Blocked: 1,247
Block Rate: 12.5%
Whitelist Size: 23
Blacklist Size: 156
```

## 🔄 Updates

TG-AntiBot features automatic update checking:

- **Version Notifications**: Alerts when new versions are available
- **Changelog Integration**: View changes directly in-game
- **Backup System**: Automatic configuration backups before updates

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📞 Support

- **Discord**: [Join our Discord](https://discord.gg/techinpoint)
- **GitHub Issues**: [Report bugs](https://github.com/TechinpointGamerz/TG-AntiBot/issues)
- **Documentation**: [Wiki](https://github.com/TechinpointGamerz/TG-AntiBot/wiki)

## 🏆 Credits

Developed by **Techinpoint Gamerz**

Special thanks to:
- The Spigot/Paper development team
- Contributors and beta testers
- The Minecraft server community

---

⭐ **Star this repository if you find it useful!**