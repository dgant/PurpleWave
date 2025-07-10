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
    get(Zerg.Extractor)
    once(11, Zerg.Drone)
    get(Zerg.SpawningPool)
    once(13, Zerg.Drone)
    once(6, Zerg.Zergling)
    get(Zerg.Lair)
  }

  override def executeMain(): Unit = {
    pump(Zerg.Mutalisk)
    if (enemyStrategy(With.fingerprints.fourPool, With.fingerprints.ninePool)) {
      fillMacroHatches(2)
    } else {
      requireMiningBases(2)
    }
    once(6, Zerg.Mutalisk)
    get(Zerg.Spire)
    pump(Zerg.Zergling)
    attack()
  }
}
