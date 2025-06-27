package Gameplans.Terran.TvT

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Macro.Actions.{Enemy, Flat, Friendly}
import ProxyBwapi.Races.Terran
import Utilities.UnitFilters.{IsTank, IsWarrior}

class TvT3FacVult extends GameplanImperative {

  override def executeBuild(): Unit = {
    scoutAt(13)

    once(9, Terran.SCV)
    get(Terran.SupplyDepot)
    once(11, Terran.SCV)
    get(Terran.Barracks)
    once(13, Terran.SCV)
    get(Terran.Refinery)
    once(15, Terran.SCV)
    once(Terran.Marine)
    get(2, Terran.SupplyDepot)
    once(16, Terran.SCV)
    get(Terran.Factory)
    once(18, Terran.SCV)
    once(2, Terran.Marine)
  }

  override def doWorkers(): Unit = {
    pump(Terran.Comsat)
    pumpWorkers(oversaturate = true)
  }

  // Reference: https://youtu.be/Xcs9oRtjAz4?t=7180
  override def executeMain(): Unit = {
    if (safePushing && unitsComplete(IsWarrior) > 2) {
      attack()
    }
    if (miningBases < 2) {
      gasWorkerCeiling(2)
    }
    if (haveComplete(Terran.Factory)) {
      With.blackboard.floatableBuildings.set(Vector(Terran.Barracks, Terran.EngineeringBay))
    }

    if (units(Terran.Factory) >= 3) {
      get(Terran.MachineShop)
      get(Terran.VultureSpeed)
      get(Terran.SpiderMinePlant)
    }
    if (units(Terran.Vulture) >= 24) {
      get(Terran.SiegeMode)
    }
    upgradeContinuously(Terran.MechDamage)
    if (upgradeComplete(Terran.MechDamage, 1, Terran.Starport.buildFrames + Terran.ScienceFacility.buildFrames)) {
      get(Terran.Starport)
    }
    if (upgradeComplete(Terran.MechDamage, 1, Terran.ScienceFacility.buildFrames)) {
      get(Terran.ScienceFacility)
    }

    pumpRatio(Terran.ScienceVessel, 1, 5, Seq(Friendly(IsTank, 0.2), Friendly(Terran.Battlecruiser, 0.2)))
    pump(Terran.Battlecruiser)
    if (techStarted(Terran.SiegeMode)) {
      pump(Terran.SiegeTankUnsieged)
    }
    if (enemyHasShown(Terran.Wraith, Terran.Dropship, Terran.Valkyrie, Terran.Battlecruiser)) {
      get(Terran.GoliathAirRange)
    }
    pumpRatio(Terran.Goliath, 1, 25, Seq(Flat(1.0), Enemy(Terran.Wraith, 1.0), Enemy(Terran.Battlecruiser, 4.0)))
    pump(Terran.Wraith, 3)
    pump(Terran.Vulture, 36)

    if (With.fingerprints.bunkerRush()) {
      get(Terran.MachineShop)
      get(Terran.SiegeMode)
      get(2, Terran.Factory)
    }

    requireMiningBases(2)

    get(3, Terran.Factory)
    get(Terran.Armory)
    get(Terran.Academy)
    get(Terran.Starport)
    get(5, Terran.Factory)

    if (safeDefending) {
      requireMiningBases(3)
    }

    if (gas < 500) {
      pumpGasPumps()
    }
    get(2, Terran.MachineShop)
    get(7, Terran.Factory)
    get(Terran.ScienceFacility)
    get(Terran.SiegeMode)
    get(3, Terran.MachineShop)

    if (safeDefending) {
      requireMiningBases(4)
    }

    get(2, Terran.Armory)
    upgradeContinuously(Terran.MechArmor)
    get(Terran.ControlTower)
    get(9, Terran.Factory)
    get(4, Terran.MachineShop)
    get(2, Terran.Starport)
    get(Terran.PhysicsLab)
    get(units(Terran.Starport), Terran.ControlTower)
    get(10, Terran.Factory)
    get(5, Terran.MachineShop)
    get(12, Terran.Factory)
    get(6, Terran.MachineShop)
  }
}