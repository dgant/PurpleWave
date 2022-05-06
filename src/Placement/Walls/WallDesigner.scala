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
    val wall = With.placement.wall.generate(zone, constraints)
    wall
  }
}
