# Refined Storage Changelog

### 0.5
**Bugfixes**
- Fix clicking sound in Grid
- Fix NPE in Grid while sorting
- Fix exporter not exporting is some cases
- Fix importer not importing in some cases
- Fix controller drawing RF every 20 ticks instead of every tick
- Fix not being able to shift click from Crafting Grid crafting slots
- Fix crash with interfaces
- Check if the Contructor can actually place said block in the world

**Features**
- New textures
- Updated to the latest Forge and JEI
- Renamed Drives to Disk Drives
- Renamed Storage Cells to Storage Disks
- Drives have a better interface and there are now blacklist and whitelist filters for the Storage Disks in it too.
- Destructors have the ability to whitelist and blacklist certain items now
- Shift clicking stuff in the Interface
- Scrollbar in Grid and Crafting Grid
- Made the normal Grid 1 row larger
- Display of connected machines in the Controller GUI
- Deep Storage Unit integration (with this several barrel mods are now supported too!)
- Machines don't need to be connected with cables anymore, they can be next to each other too
- Made the amount text in the Grid for items smaller
- Nice formatting for items >= 1K
- When placing Importer, Exporter or External Storage with SHIFT, it will have the opposite direction. This is for easy placement behind other blocks (furnaces for example)

### 0.4.1
**Bugfixes**
- Fix ID duplication issues

### 0.4
**Bugfixes**
- Cables now have actual collision
- Fullness percentage in Creative Storage Blocks going under 0%
- The Controller shouldn't display the base usage when not working
- Fix Minecraft reporting that retrieving Grid type fails
- Check isItemValidForSlot on trying to push to inventories

**Features**
- Relays
- Interfaces

### 0.3
- Initial release