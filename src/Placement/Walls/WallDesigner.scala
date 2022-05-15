package Placement.Walls

import Information.Geography.Types.Zone
import Lifecycle.With
import Placement.Walls.WallSpans.{TerrainGas, TerrainHall, TerrainTerrain}
import ProxyBwapi.Races.{Protoss, Zerg}

object WallDesigner {

  def apply(zone: Zone): Option[Wall] = {
    if (With.self.isProtoss) protoss(zone) else if (With.self.isTerran) terran(zone) else zerg(zone)
  }

  def terran(zone: Zone): Option[Wall] = None
  def zerg(zone: Zone): Option[Wall] = None
  def protoss(zone: Zone): Option[Wall] = {
    val constraints = Vector(
      //WallConstraint(1, Zerg.Zergling,  TerrainTerrain, Protoss.Gateway, Protoss.Forge),
      WallConstraint(1, Zerg.Hydralisk, TerrainTerrain, Protoss.Gateway, Protoss.Forge),
      WallConstraint(1, Zerg.Hydralisk, TerrainTerrain, Protoss.Gateway, Protoss.Forge, Protoss.Pylon),
      //WallConstraint(2, Zerg.Hydralisk, TerrainTerrain, Protoss.Gateway, Protoss.Forge, Protoss.Pylon),
      //WallConstraint(1, Zerg.Zergling,  TerrainGas,     Protoss.Gateway, Protoss.Forge),
      WallConstraint(1, Zerg.Hydralisk, TerrainGas,     Protoss.Gateway, Protoss.Forge),
      WallConstraint(1, Zerg.Hydralisk, TerrainGas,     Protoss.Gateway, Protoss.Forge, Protoss.Pylon),
      //WallConstraint(1, Zerg.Zergling,  TerrainHall,    Protoss.Gateway, Protoss.Forge),
      WallConstraint(1, Zerg.Hydralisk, TerrainHall,    Protoss.Gateway, Protoss.Forge),
      WallConstraint(1, Zerg.Hydralisk, TerrainHall,    Protoss.Gateway, Protoss.Forge, Protoss.Pylon))
    val cache = new WallCache
    val wall = cache.generate(zone, constraints)
    With.logger.debug(f"${if (wall.isDefined) "CREATED WALL" else "FAILED to create wall"} for $zone")
    With.logger.debug(f"Tiles considered:         ${cache.metricTilesConsidered}")
    With.logger.debug(f"Unbuildable (Terrain):    ${cache.metricUnbuildableTerrain}")
    With.logger.debug(f"Unbuildable (Granular):   ${cache.metricUnbuildableGranular}")
    With.logger.debug(f"Intersection (Previous):  ${cache.metricIntersectsPrevious}")
    With.logger.debug(f"Intersection (Hall):      ${cache.metricIntersectsHall}")
    With.logger.debug(f"Gap too narrow:           ${cache.metricGapTooNarrow}")
    With.logger.debug(f"Gap too wide:             ${cache.metricGapTooWide}")
    With.logger.debug(f"Failed recursively:       ${cache.metricFailedRecursively}")
    wall
  }
}
