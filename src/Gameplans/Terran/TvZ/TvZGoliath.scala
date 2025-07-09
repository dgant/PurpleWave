package Gameplans.Terran.TvZ

import Gameplans.Terran.TvE.TerranGameplan
import Placement.Access.{PlaceLabels, PlacementQuery}
import ProxyBwapi.Races.{Terran, Zerg}
import Utilities.?
import Utilities.Time.Seconds
import Utilities.UnitFilters.IsTank

// https://liquipedia.net/starcraft/5_Factory_Goliaths_(vs._Zerg)
class TvZGoliath extends TerranGameplan {

  override def executeBuild(): Unit = {
    emergencyReactions()

    once(9, Terran.SCV)
    get(Terran.SupplyDepot, new PlacementQuery(Terran.SupplyDepot).preferLabelYes(PlaceLabels.Wall))
    once(11, Terran.SCV)
    get(Terran.Barracks, new PlacementQuery(Terran.SupplyDepot).preferLabelYes(PlaceLabels.Wall))
    once(12, Terran.SCV)
    get(Terran.Refinery)
    once(14, Terran.SCV)
    once(3, Terran.Marine)
    get(2, Terran.SupplyDepot)
    //get(2, Terran.SupplyDepot, new PlacementQuery(Terran.SupplyDepot).preferLabelYes(PlaceLabels.Wall))
    once(16, Terran.SCV)
    get(Terran.Factory)
    if (gatheredGas >= 100 && bases == 1 && minerals < 400) {
      gasWorkerCeiling(0)
    }

    scoutOn(Terran.Refinery)
  }

  override def doWorkers(): Unit = {
    pump(Terran.Comsat)
    pumpWorkers(oversaturate = true)
  }

  override def executeMain(): Unit = {
    maintainMiningBases(armySupply200 / 30)
    requireMiningBases(armySupply200 / 40)

    once(3, Terran.Vulture)
    requireMiningBases(2)

    get(Terran.MechArmor)
    get(Terran.MechDamage)
    upgradeContinuously(Terran.MechArmor)
    upgradeContinuously(Terran.MechDamage)
    pump(Terran.ScienceVessel)
    if (haveComplete(Terran.ScienceVessel)) {
      get(Terran.Irradiate)
    }
    if (units(Terran.Factory) >= 5) {
      pump(Terran.SiegeTankUnsieged)
      get(Terran.SiegeMode)
    }
    get(Terran.Armory)
    get(2, Terran.Factory)
    get(Terran.MachineShop)
    get(Terran.GoliathAirRange)
    pump(Terran.Goliath)
    get(3, Terran.Factory)
    get(Terran.EngineeringBay)
    buildTurretsAtFoyer(?(enemyLurkersLikely, 2, 1), PlaceLabels.DefendEntrance)
    buildTurretsAtBases(?(enemyMutalisksLikely, 3, ?(enemyLurkersLikely, 1, 2)))
    if (gas < 400) {
      pumpGasPumps(units(Terran.SCV) / 10)
    }
    get(5, Terran.Factory)
    get(Terran.Academy)
    requireMiningBases(3)

    get(Terran.Starport, Terran.ScienceFacility, Terran.ControlTower)
    get(Terran.Irradiate)
    get(Terran.VultureSpeed)
    get(Terran.SpiderMinePlant)
    get(8, Terran.Factory)
    requireMiningBases(4)

    pump(Terran.Vulture)
    get(12, Terran.Factory)
    get(2, Terran.MachineShop)
    requireMiningBases(5)

    get(20, Terran.Factory)
    requireMiningBases(7)

    if (haveComplete(Terran.Vulture) && ! haveComplete(IsTank) && ! enemyHasUpgrade(Zerg.ZerglingSpeed) && safeSkirmishing && safePushing && ! enemyMutalisksLikely) {
      attack()
    }

    if (techComplete(Terran.SiegeMode, Seconds(25)()) && safePushing) {
      attack()
    }
  }
}