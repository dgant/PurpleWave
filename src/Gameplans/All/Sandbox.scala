package Gameplans.All

import Lifecycle.With
import Placement.Access.{PlaceLabels, PlacementQuery}
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitClasses.UnitClass

class Sandbox extends GameplanImperative {

  private def wallPlacement(unitClass: UnitClass): PlacementQuery =
    new PlacementQuery(unitClass)
      .preferLabelYes()
      .preferLabelNo()
      .preferZone(With.geography.ourFoyer.edges.flatMap(_.zones).distinct: _*)
      .preferBase()
      .preferTile()
      .preferLabelYes(PlaceLabels.Wall)

  override def executeBuild(): Unit = {
    once(9, Zerg.Drone)
    once(2, Zerg.Overlord)
    once(12, Zerg.Drone)
    fillMacroHatches(2)
    once(13, Zerg.Drone)
    requireMiningBases(2)
    once(14, Zerg.Drone)
    fillMacroHatches(4)
    once(15, Zerg.Drone)
    requireMiningBases(3)
    once(16, Zerg.Drone)
    fillMacroHatches(6)
    once(17, Zerg.Drone)
    requireMiningBases(4)
  }

  override def executeMain(): Unit = {
  }
}
