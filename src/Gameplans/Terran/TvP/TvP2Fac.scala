package Gameplans.Terran.TvP

import Gameplans.Terran.TvE.TerranGameplan
import Lifecycle.With
import Macro.Actions.Enemy
import Macro.Requests.RequestUnit
import ProxyBwapi.Races.{Protoss, Terran}
import Utilities.Time.Seconds
import Utilities.UnitFilters.IsWarrior

class TvP2Fac extends TerranGameplan {

  override def executeBuild(): Unit = {
    emergencyReactions()

    scoutAt(13)
    once(9, Terran.SCV)
    once(Terran.SupplyDepot)
    once(11, Terran.SCV)
    once(Terran.Barracks)
    once(12, Terran.SCV)
    once(Terran.Refinery)
    once(15, Terran.SCV)
    once(2, Terran.SupplyDepot)
    if (unitsComplete(Terran.Factory) < 2 && ! enemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.forgeFe, With.fingerprints.coreBeforeZ)) {
      pumpRatio(Terran.Marine, 2, 9, Seq(Enemy(Protoss.Zealot, 3.0)))
    }
    once(16, Terran.SCV)
    once(Terran.Factory)
    once(18, Terran.SCV)
    once(2, Terran.Factory)
  }

  override def doWorkers(): Unit = {
    if (enemyDarkTemplarLikely) {
      get(RequestUnit(Terran.Academy, minStartFrameArg = With.scouting.earliestArrival(Protoss.DarkTemplar) - Terran.Academy.buildFrames - Terran.Comsat.buildFrames - Seconds(5)()))
      pump(Terran.Comsat)
    }
    pumpWorkers(oversaturate = true)
  }

  var initialMarines: Boolean = true

  def executeMain(): Unit = {

    if ( ! haveEver(Terran.MachineShop)) {
      pumpRatio(Terran.Vulture, 0, 5, Seq(Enemy(Protoss.Zealot, 1.0)))
    }
    if (enemyHasShown(Protoss.Scout, Protoss.Stargate, Protoss.Corsair, Protoss.Carrier, Protoss.FleetBeacon)) {
      get(Terran.Armory)
      if (enemyHasShown(Protoss.Carrier, Protoss.FleetBeacon)) {
        once(8, Terran.Goliath)
      }
      get(Terran.GoliathAirRange)
    }
    pumpRatio(Terran.Goliath, 1, 36, Seq(Enemy(Protoss.Corsair, 0.5), Enemy(Protoss.Scout, 1.5), Enemy(Protoss.Carrier, 6.0)))
    pumpRatio(Terran.Vulture, 0, 8, Seq(Enemy(Protoss.DarkTemplar, 2.0)))
    once(Terran.MachineShop)
    once(Terran.SiegeTankUnsieged)
    once(2, Terran.MachineShop)
    once(3, Terran.SiegeTankUnsieged)
    get(Terran.VultureSpeed)
    get(Terran.SpiderMinePlant)
    once(8, Terran.Vulture)
    if (initialMarines) {
      pump(Terran.Marine)
    } else {
      With.blackboard.floatableBuildings.set(Vector(Terran.Barracks))
    }

    if (unitsEver(Terran.SiegeTankUnsieged) >= 3 && safePushing && (armySupply200 > 150 || enemies(IsWarrior) < 7 || enemyMiningBases > miningBases + 2)) {
      attack()
      pump(Terran.Vulture)
      initialMarines = false
    } else {
      pump(Terran.SiegeTankUnsieged)
    }

    pumpRatio(Terran.Goliath, 1, 48, Seq(Enemy(Protoss.Scout, 1.0), Enemy(Protoss.Shuttle, 1.0), Enemy(Protoss.Carrier, 6.0)))
    pumpRatio(Terran.ScienceVessel, 1, 3, Seq(Enemy(Protoss.Arbiter, 1.0)))
    pump(Terran.Goliath, 1)
    pump(Terran.Wraith, 1)
    pump(Terran.SiegeTankUnsieged)
    pump(Terran.Vulture)
    requireMiningBases(2)
    pumpGasPumps()
    get(Terran.SiegeMode)
    get(Terran.EngineeringBay)
    buildTurretsAtOpenings(1)
    get(Terran.Armory)
    get(Terran.MechDamage)
    get(Terran.Academy)
    pump(Terran.Comsat)
    get(Terran.MechArmor)
    get(5, Terran.Factory)
    requireMiningBases(3)
    get(Terran.Starport)
    get(Terran.ScienceFacility)
    get(Terran.ControlTower)
    upgradeContinuously(Terran.MechDamage) && upgradeContinuously(Terran.MechArmor)
    get(12, Terran.Factory)
    get(4, Terran.MachineShop)
  }
}