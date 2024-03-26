package Placement.Walls

import Information.Geography.Types.Zone
import Lifecycle.With
import Placement.Walls.WallFillers.PylonsCannons
import Placement.Walls.WallSpans.{TerrainGas, TerrainHall, TerrainTerrain}
import ProxyBwapi.Races.{Protoss, Zerg}

object FindWall {

  def apply(zone: Zone): Option[WallFinder] = {
    val wallFinderOption =
      if      (With.self.isProtoss) protoss(zone)
      else if (With.self.isTerran)  terran(zone)
      else                          zerg(zone)

    wallFinderOption.foreach(wallFinder => {
      With.logger.debug(f"$zone: ${if (wallFinder.wall.isDefined) "CREATED WALL" else "FAILED to create wall"}")
      With.logger.debug(f"Permutations:       ${wallFinder.metrics.permutations}")
      With.logger.debug(f"Tiles considered:   ${wallFinder.metrics.tilesConsidered}")
      if (wallFinder.wall.isDefined) {
        With.logger.debug(f"Constraints:      ${wallFinder.wall.get.constraint}" )
        With.logger.debug(f"Acceptable walls:\n${wallFinder.wallsAcceptable.map(_.toString).mkString("\n")}")
      }
      WallProblems.all.view
        .map(p => (p, wallFinder.wallsUnacceptable.count(_.problems.contains(p))))
        .filter(_._2 > 0)
        .foreach(p => With.logger.debug(f"$p._1: p._2"))
      With.logger.debug("All scores:")
      With.logger.debug(wallFinder.wallsAcceptable.sortBy(_.score).map(_.toString).mkString("\n"))
      With.logger.debug("")
    })
    wallFinderOption
  }

  def terran(zone: Zone): Option[WallFinder] = None
  def zerg(zone: Zone): Option[WallFinder] = None

  def protoss (zone: Zone): Option[WallFinder] = {
    val exit = zone.exitOriginal
    if (exit.isEmpty) return None

    val entrance = zone.entranceOriginal.map(_.pixelCenter.tile).getOrElse(zone.heart)
    val constraints = Vector(
      // These should work but are temporarily disabled to ease debugging
      //WallConstraint(1, Zerg.Zergling,    TerrainTerrain, Protoss.Gateway, Protoss.Forge),
      //WallConstraint(1, Zerg.Hydralisk,   TerrainTerrain, Protoss.Gateway, Protoss.Forge),
      WallConstraint(1, Protoss.Dragoon,  TerrainTerrain, Protoss.Gateway, Protoss.Forge),
      WallConstraint(1, Protoss.Dragoon,  TerrainTerrain, Protoss.Gateway, Protoss.Forge, Protoss.Pylon),
      WallConstraint(2, Protoss.Dragoon,  TerrainTerrain, Protoss.Gateway, Protoss.Forge, Protoss.Pylon),
      WallConstraint(1, Zerg.Zergling,    TerrainGas,     Protoss.Gateway, Protoss.Forge),
      WallConstraint(1, Zerg.Hydralisk,   TerrainGas,     Protoss.Gateway, Protoss.Forge),
      WallConstraint(1, Zerg.Hydralisk,   TerrainGas,     Protoss.Gateway, Protoss.Forge, Protoss.Pylon),
      WallConstraint(1, Zerg.Zergling,    TerrainHall,    Protoss.Gateway, Protoss.Forge),
      WallConstraint(1, Zerg.Hydralisk,   TerrainHall,    Protoss.Gateway, Protoss.Forge),
      WallConstraint(1, Zerg.Hydralisk,   TerrainHall,    Protoss.Gateway, Protoss.Forge, Protoss.Pylon))

    val wallFinder = new WallFinder(zone, exit.get, entrance, constraints, PylonsCannons)
    wallFinder.generate()
    Some(wallFinder)
  }
}
