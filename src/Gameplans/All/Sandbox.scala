package Gameplans.All

import Lifecycle.With
import Placement.Access.{PlaceLabels, PlacementQuery}
import ProxyBwapi.Races.Terran
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
    once(9, Terran.SCV)
    get(Terran.SupplyDepot, wallPlacement(Terran.SupplyDepot))
    once(11, Terran.SCV)
    get(Terran.Barracks, wallPlacement(Terran.Barracks))
    once(13, Terran.SCV)
    get(2, Terran.Barracks)
    once(15, Terran.SCV)
    get(2, Terran.SupplyDepot, wallPlacement(Terran.SupplyDepot))
    once(Terran.Marine)
    scoutOn(Terran.Barracks)
  }

  override def doWorkers(): Unit = {
    pumpWorkers(oversaturate = true, maximumTotal = 150)
  }

  override def executeMain(): Unit = {
    pump(Terran.Marine, 50)
    requireMiningBases(3)
    buildBunkersAtBases(2, PlaceLabels.DefendEntrance)
    buildBunkersAtBases(2, PlaceLabels.DefendHall)
    requireMiningBases(12)
    get(Terran.EngineeringBay)
    buildTurretsAtBases(6, PlaceLabels.DefendHall)
    buildTurretsAtBases(1, PlaceLabels.DefendEntrance)
    pumpGasPumps()
  }
}
