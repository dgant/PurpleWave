package Gameplans.Terran.TvP

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Macro.Actions.Enemy
import ProxyBwapi.Races.{Protoss, Terran}
import Utilities.UnitFilters.IsWarrior

class TerranVsProtoss extends GameplanImperative {

  override def executeBuild(): Unit = {
    scoutAt(13)
    once(9, Terran.SCV)
    once(Terran.SupplyDepot)
    once(11, Terran.SCV)
    once(Terran.Barracks)
    once(12, Terran.SCV)
    once(Terran.Refinery)
    if (unitsEver(Terran.Vulture) < 3 && enemyStrategy(With.fingerprints.workerRush, With.fingerprints.proxyGateway)) {
      pump(Terran.Vulture, 3)
      pump(Terran.Marine, 4)
      buildBunkersAtMain(1)
      get(Terran.Factory)
    }
    once(15, Terran.SCV)
    once(2, Terran.SupplyDepot)
    if (unitsComplete(Terran.Factory) < 2 && enemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.forgeFe, With.fingerprints.coreBeforeZ)) {
      pumpRatio(Terran.Marine, 2, 9, Seq(Enemy(Protoss.Zealot, 3.0)))
    }
    once(16, Terran.SCV)
    once(Terran.Factory)
    once(18, Terran.SCV)
    once(2, Terran.Factory)
  }

  override def doWorkers(): Unit = {
    pumpWorkers(oversaturate = true)
  }

  def executeMain(): Unit = {

    if ( ! haveEver(Terran.MachineShop)) {
      pumpRatio(Terran.Vulture, 0, 5, Seq(Enemy(Protoss.Zealot, 1.0)))
    }
    once(Terran.MachineShop)
    once(Terran.SiegeTankUnsieged)
    once(2, Terran.MachineShop)
    once(3, Terran.SiegeTankUnsieged)
    get(Terran.VultureSpeed)
    get(Terran.SpiderMinePlant)
    once(8, Terran.Vulture)

    if (unitsEver(Terran.SiegeTankUnsieged) >= 3 && safePushing && (armySupply200 > 150 || enemies(IsWarrior) < 7 || enemyMiningBases > miningBases + 2)) {
      attack()
      pump(Terran.Vulture)
    } else {
      pump(Terran.SiegeTankUnsieged)
    }

    get(Terran.EngineeringBay)
    buildTurretsAtOpenings(1)
    pumpRatio(Terran.Goliath, 1, 48, Seq(Enemy(Protoss.Scout, 1.0), Enemy(Protoss.Shuttle, 1.0), Enemy(Protoss.Carrier, 6.0)))
    pumpRatio(Terran.ScienceVessel, 1, 3, Seq(Enemy(Protoss.Arbiter, 1.0)))
    pump(Terran.Goliath, 1)
    pump(Terran.Wraith, 1)
    pump(Terran.SiegeTankUnsieged)
    pump(Terran.Vulture)
    requireMiningBases(2)
    pumpGasPumps()
    get(Terran.Armory)
    get(Terran.MechDamage)
    get(Terran.Academy)
    pump(Terran.Comsat)
    get(Terran.SiegeMode)
    get(2, Terran.MachineShop)
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