package Planning.Plans.GamePlans.Protoss.PvZ

import Planning.Plans.GamePlans.All.GameplanImperative

class PvZNew extends GameplanImperative {

  override def executeBuild(): Unit = {

  }
  override def executeMain(): Unit = {
    // FFE: Scout on pylon
    // GFE: Scout with Zealot or just on gate
    // GFE: Can attack withh first Zealot unless Zerg made 6 Zerglings
    // On maps where you can completely block with forge-forge-gateway:
      // FFE: Khala: "When you fail to scout Zerg at first try do 12 forge because you can block 9 pool with sim city)" Not sure if map-specific: https://youtu.be/Zm-t_mpHWG0?t=102
      // FFE: Khala: Can send second scout if first one misses to catch 4/5 pool

    // Cannons vs. ling/hydra bust
    // Cannons vs. muta
    // +1/+1 air vs. muta
    // 2nd Stargate vs. committed muta
    // Archon, delaying storm, vs. committed muta
    // Maelstrom vs. committed muta
    // Obs vs. Lurker
    // Reaver composition vs. 3HH/mass sunken?
    // Main composition: Zealots-Weapons-Speed-Corsair-Templar/Storm-Amulet-Dragoon-Range-Observer-Speed shuttle-Reaver
  }
}
