package Gameplans.Terran.TvT

import Gameplans.Terran.TvE.TerranGameplan
import Lifecycle.With
import Macro.Actions.{Enemy, Flat}
import Placement.Access.{PlaceLabels, PlacementQuery}
import ProxyBwapi.Races.Terran
import Utilities.UnitFilters.IsTank

class TvTFE extends TerranGameplan {

  override def executeBuild(): Unit = {
    emergencyReactions()

    scoutOn(Terran.CommandCenter, 2)

    once(9, Terran.SCV)
    get(Terran.SupplyDepot, new PlacementQuery(Terran.SupplyDepot).preferLabelYes(PlaceLabels.Wall))
    once(14, Terran.SCV)
    requireMiningBases(2)
    once(15, Terran.SCV)
    get(1, Terran.Barracks, new PlacementQuery(Terran.Barracks).preferLabelYes(PlaceLabels.Wall))
    once(16, Terran.SCV)
    get(Terran.Refinery, new PlacementQuery(Terran.Refinery).preferBase(With.geography.ourMain))
    get(2, Terran.SupplyDepot, new PlacementQuery(Terran.SupplyDepot).preferLabelYes(PlaceLabels.Wall))
  }

  override def doWorkers(): Unit = {
    pump(Terran.Comsat)
    pumpWorkers(oversaturate = true)
  }

  var firstAttackTrigger: Boolean = false

  override def executeMain(): Unit = {
    once(3, Terran.Marine)
    get(Terran.Factory)
    buildBunkersAtFoyer(1, PlaceLabels.DefendEntrance)
    if (gas < 400) {
      pumpGasPumps()
    }
    get(Terran.MachineShop)
    once(Terran.SiegeTankUnsieged)
    get(2, Terran.Factory)
    get(2, Terran.MachineShop)
    get(Terran.SiegeMode)

    if (enemyHasShown(Terran.Wraith, Terran.Dropship, Terran.Valkyrie, Terran.Battlecruiser)) {
      get(Terran.GoliathAirRange)
    }
    pumpRatio(Terran.Goliath, 1, 25, Seq(Flat(1.0), Enemy(Terran.Wraith, 2.0), Enemy(Terran.Battlecruiser, 4.0)))
    pump(Terran.Wraith, 1)
    pump(Terran.ScienceVessel, 1)
    pump(Terran.SiegeTankUnsieged)
    pump(Terran.Vulture, 36)
    pump(Terran.Goliath)

    get(Terran.Armory)
    get(3, Terran.Factory)

    upgradeContinuously(Terran.MechDamage)
    get(Terran.VultureSpeed)
    get(Terran.SpiderMinePlant)
    get(Terran.Starport, Terran.ScienceFacility)
    get(2, Terran.Armory)
    upgradeContinuously(Terran.MechArmor)

    get(6, Terran.Factory)
    requireMiningBases(3)

    get(4, Terran.MachineShop)
    get(9, Terran.Factory)
    get(5, Terran.MachineShop)
    get(Terran.ControlTower)
    requireMiningBases(4)

    get(12, Terran.Factory)
    get(8, Terran.MachineShop)

    get(16, Terran.Factory)
    get(12, Terran.MachineShop)

    requireMiningBases(6)

    firstAttackTrigger ||= unitsComplete(IsTank) >= 5
    if (firstAttackTrigger) {
      With.blackboard.floatableBuildings.set(Vector(Terran.Barracks, Terran.EngineeringBay, Terran.ScienceFacility))

      if (safePushing) {
        attack()
      }
    }
  }
}