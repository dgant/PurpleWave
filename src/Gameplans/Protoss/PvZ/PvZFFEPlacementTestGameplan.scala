package Gameplans.Protoss.PvZ

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Placement.Access.{PlaceLabels, PlacementQuery}
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClasses.UnitClass

class PvZFFEPlacementTestGameplan extends GameplanImperative {

  doAutosupply = false

  private def wallPlacement(unitClass: UnitClass): PlacementQuery =
    new PlacementQuery(unitClass)
      .preferLabelYes()
      .preferLabelNo()
      .preferZone(With.geography.ourFoyer.edges.flatMap(_.zones).distinct: _*)
      .preferBase()
      .preferTile()
      .requireLabelYes(PlaceLabels.Wall)

  private def buildInWall(quantity: Int, unitClass: UnitClass): Unit =
    get(quantity, unitClass, wallPlacement(unitClass))

  override def executeBuild(): Unit = {
    if (With.placement.wall.isEmpty) return

    val wallPylons = With.placement.wall.get.buildings.count(_._2 == Protoss.Pylon)
    buildInWall(1, Protoss.Pylon)
    buildInWall(1, Protoss.Forge)
    buildInWall(2, Protoss.PhotonCannon)
    buildInWall(Math.min(2, wallPylons), Protoss.Pylon)
    buildInWall(6, Protoss.PhotonCannon)
    buildInWall(1, Protoss.Gateway)
    get(Protoss.Nexus, new PlacementQuery(Protoss.Nexus).requireBase(With.geography.ourNatural))
  }

  override def executeMain(): Unit = {}
}
