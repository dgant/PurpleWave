package Placement.Walls

import Information.Geography.Types.Zone
import Lifecycle.With
import Placement.Walls.WallSpans.{TerrainGas, TerrainHall, TerrainTerrain}
import ProxyBwapi.Races.{Protoss, Zerg}

object FindWall {

  def apply(zone: Zone): Option[WallFinder] = {
    if      (With.self.isProtoss) protoss(zone)
    else if (With.self.isTerran)  terran(zone)
    else                          zerg(zone)
  }

  def terran(zone: Zone): Option[WallFinder] = None
  def zerg(zone: Zone): Option[WallFinder] = None

  def protoss (zone: Zone): Option[WallFinder] = {
    val exit = zone.exitOriginal
    if (exit.isEmpty) return None

    val entrance = zone.entranceOriginal.map(_.pixelCenter.tile).getOrElse(zone.heart)
    val constraints = Vector(
      WallConstraint(1, Zerg.Zergling,    TerrainTerrain, Protoss.Gateway, Protoss.Forge),
      WallConstraint(1, Zerg.Hydralisk,   TerrainTerrain, Protoss.Gateway, Protoss.Forge),
      WallConstraint(1, Protoss.Dragoon,  TerrainTerrain, Protoss.Gateway, Protoss.Forge),
      WallConstraint(1, Protoss.Dragoon,  TerrainTerrain, Protoss.Gateway, Protoss.Forge, Protoss.Pylon),
      WallConstraint(2, Protoss.Dragoon,  TerrainTerrain, Protoss.Gateway, Protoss.Forge, Protoss.Pylon),
      WallConstraint(1, Zerg.Zergling,    TerrainGas,     Protoss.Gateway, Protoss.Forge),
      WallConstraint(1, Zerg.Hydralisk,   TerrainGas,     Protoss.Gateway, Protoss.Forge),
      WallConstraint(1, Zerg.Hydralisk,   TerrainGas,     Protoss.Gateway, Protoss.Forge, Protoss.Pylon),
      WallConstraint(1, Zerg.Zergling,    TerrainHall,    Protoss.Gateway, Protoss.Forge),
      WallConstraint(1, Zerg.Hydralisk,   TerrainHall,    Protoss.Gateway, Protoss.Forge),
      WallConstraint(1, Zerg.Hydralisk,   TerrainHall,    Protoss.Gateway, Protoss.Forge, Protoss.Pylon))

    val wallFinder = new WallFinder(zone, exit.get, entrance, constraints, Seq(
      Protoss.Pylon,
      Protoss.PhotonCannon,
      Protoss.PhotonCannon,
      Protoss.PhotonCannon,
      Protoss.PhotonCannon,
      Protoss.PhotonCannon,
      Protoss.PhotonCannon,
      Protoss.PhotonCannon,
      Protoss.PhotonCannon))
    wallFinder.generate()

    With.logger.debug(f"$zone: ${if (wallFinder.wall.isDefined) "CREATED WALL" else "FAILED to create wall"}")
    if (wallFinder.wall.isDefined) {
      With.logger.debug(f"Constraints:              ${wallFinder.wall.get.constraint}" )
      With.logger.debug(f"Acceptable walls:         ${wallFinder.metrics.acceptable}")
    }
    With.logger.debug(f"Permutations:             ${wallFinder.metrics.permutations}")
    With.logger.debug(f"Tiles considered:         ${wallFinder.metrics.tilesConsidered}")
    With.logger.debug(f"Unbuildable (Terrain):    ${wallFinder.metrics.unbuildableTerrain}")
    With.logger.debug(f"Unbuildable (Granular):   ${wallFinder.metrics.unbuildableGranular}")
    With.logger.debug(f"Intersection (Previous):  ${wallFinder.metrics.intersectsPrevious}")
    With.logger.debug(f"Intersection (Hall):      ${wallFinder.metrics.intersectsHall}")
    With.logger.debug(f"Wrong zone:               ${wallFinder.metrics.wrongZone}")
    With.logger.debug(f"Insufficiently tight:     ${wallFinder.metrics.insufficientlyTight}")
    With.logger.debug(f"Gap too narrow:           ${wallFinder.metrics.gapTooNarrow}")
    With.logger.debug(f"Gap too wide:             ${wallFinder.metrics.gapTooWide}")
    With.logger.debug(f"Unpowered:                ${wallFinder.metrics.unpowered}")
    With.logger.debug(f"Failed recursively:       ${wallFinder.metrics.failedRecursively}")
    With.logger.debug("")

    Some(wallFinder)
  }
}
