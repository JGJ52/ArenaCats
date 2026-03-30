# ArenaCats
![Time](https://hackatime-badge.hackclub.com/U0922GMGGTU/ArenaCats?label=Time+I+spent+on+this)

**Commands**:\
There is only one command:\
/arena:
- permission: arenacats.command.arena
- does by default: open a gui of all arenas created. if you have permission you can place one by clicking on them (lore shows up if you do)
- subcommands:
  - create:
    - permission: arenacats.command.arena.create
    - does: open a gui where you can make a new arena
    - explanation:
        
        When you click the brick, you get put into a world where you can build your arena.\
        Once you're done with that, you need to select it with WorldEdit\
        (I personally recommend using FastAsyncWorldEdit instead of the normal one so placing big arenas won't freeze your server that much)\
        and then do **//copy where you want the center to be**\
        ArenaCats also save the WorldGuard regions which are in the selection,\
        but only the flags of it, and the priority (so no members, owners etc. saved)
  - delete:
    - permission: arenacats.command.arena.delete
    - does: delete an arena
  - place:
    - permission: arenacats.command.arena.place
    - does: place an arena and teleport you to it
    - you need this permission to place it from the gui
  - waypoint:
    - permission: arenacats.command.arena.waypoint
    - does: place a waypoint if you are making an arena, tp to a waypoint if you are in a placed arena
    - you need to be in one of those editors to run it

**Building**:
```shell
git clone https://github.com/JGJ52/ArenaCats.git
cd ArenaCats
./gradlew build
```

**API Usage**:
```java
import hu.jgj52.arenaCats.Types.*;

import java.util.UUID;

class yourClass {
    public void getAnArenaByNameAndPlaceIt(String arena) {
        Arena arena = Arena.of(name);
        if (arena == null) return; // arena doesnt exists
        PlacedArena placed = arena.place();
    }

    public void arenaExplanationVoid() {
        Arena arena = Arena.of("name");
        // with this you can only place or delete or get its name
        arena.place();
        arena.delete();
    }

    public void placedArenaExplanationVoid() {
        PlacedArena placed = Arena.of("name").place();
        // you can do a bit more with this
        // worldguard regions:
        // you need to put worldguard in pom.xml to use this one
        placed.getRegions(); // list of regions
        placed.getRegion("region name"); // region by name
        // waypoints:
        // the idea is the same
        placed.getWaypoints(); // list of waypoints
        placed.getWaypoint("waypoint name"); // waypoint name
        placed.teleportToWaypoint(myPlayer, placed.getWaypoint("my waypoint")); // teleport a player to a waypoint

        placed.getWorld(); // get the world where the arena got placed
        PlacedArena.getPlacedArena(UUID.fromString(myPlayer.getWorld().getName().replace("_map_placed_arenacats"))); // get it from outside of this method just by a player
        
        placed.teleportToCenter(player); // teleport a player to center (center is where you wrote //copy)
    }
    
    public void waypointExplanationVoid() {
        Waypoint waypoint = Arena.of("name").place().getWaypoint("name");
        // you can get these:
        waypoint.x();
        waypoint.y();
        waypoint.z();
        waypoint.yaw();
        waypoint.pitch();
        waypoint.name();
        waypoint.location(); // bukkit location. you can get x,y,z,yaw,pitch from this too
    }
    
    public void regionExplanationVoid() {
        // read worldguard docs
    }
}
```