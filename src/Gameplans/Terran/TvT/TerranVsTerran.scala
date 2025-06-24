package Gameplans.Terran.TvT

import Gameplans.All.GameplanImperative
import Macro.Actions.Enemy
import ProxyBwapi.Races.Terran

class TerranVsTerran extends GameplanImperative {

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
  }

  override def doWorkers(): Unit = {
    pump(Terran.Comsat)
    pumpWorkers(oversaturate = true)
  }

  override def executeMain(): Unit = {
    if (safePushing && techComplete(Terran.WraithCloak)) {
      attack()
    } else if (unitsComplete(Terran.Battlecruiser) >= 12) {
      attack()
    }
    harass()

    if (miningBases < 2) {
      gasWorkerCeiling(2)
    }

    pump(Terran.Battlecruiser)
    if (haveEver(Terran.Wraith)) {
      get(Terran.ControlTower)
    }
    if (have(Terran.PhysicsLab)) {
      pump(Terran.ControlTower)
    }
    if (units(Terran.Factory) > 1) {
      get(Terran.MachineShop)
    }
    if (unitsComplete(Terran.PhysicsLab) == 0) {
      pump(Terran.Wraith)
    }
    if (unitsComplete(Terran.Armory) > 2 && armySupply200 > 60) {
      upgradeContinuously(Terran.AirDamage) && upgradeContinuously(Terran.MechDamage)
      upgradeContinuously(Terran.AirArmor)  && upgradeContinuously(Terran.MechArmor)
    }

    once(3, Terran.Vulture)
    pumpRatio(Terran.Goliath, 4, 18, Seq(Enemy(Terran.Wraith, 1.0), Enemy(Terran.Battlecruiser, 4.0), Enemy(Terran.Vulture, 0.5)))
    pump(Terran.Vulture)
    get(Terran.Armory)
    get(Terran.Starport)
    requireMiningBases(2)
    pumpGasPumps()
    get(2, Terran.Starport)
    get(Terran.ControlTower)
    get(Terran.WraithCloak)
    get(2, Terran.Factory)
    get(Terran.SpiderMinePlant)
    get(Terran.AirDamage)
    get(Terran.Academy)
    get(Terran.AirArmor)
    get(Terran.ScienceFacility)
    get(Terran.PhysicsLab)
    get(Terran.VultureSpeed)
    requireMiningBases(3)
    get(2, Terran.Armory)
    upgradeContinuously(Terran.AirDamage)
    upgradeContinuously(Terran.AirArmor)
    get(Terran.BattlecruiserEnergy)
    get(Terran.Yamato)
    get(3, Terran.Factory)
    get(3, Terran.Starport)
    requireMiningBases(4)
    get(7, Terran.Starport)
  }
}