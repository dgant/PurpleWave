package Gameplans.Terran.TvE

import Macro.Actions.Friendly
import ProxyBwapi.Races.{Protoss, Terran, Zerg}

class TvEBBS extends TerranGameplan {

  override def executeBuild(): Unit = {
    emergencyReactions()

    once(9, Terran.SCV)
    get(2, Terran.Barracks)
    get(Terran.SupplyDepot)
    once(11, Terran.SCV)
    once(2, Terran.Marine)
    once(12, Terran.SCV)
    once(4, Terran.Marine)
    once(2, Terran.SupplyDepot)
    once(6, Terran.Marine)
    once(14, Terran.SCV)
    once(8, Terran.Marine)

    if ( ! foundEnemyBase) {
      scoutOn(u => Terran.Barracks(u) && u.complete)
    }
  }

  override def doWorkers(): Unit = {}

  override def executeMain(): Unit = {
    if (have(Terran.Factory)) {
      get(Terran.MachineShop)
      once(Terran.SiegeTankUnsieged)
      get(Terran.SiegeMode)
    }
    if (enemiesHaveComplete(Terran.Bunker, Zerg.SunkenColony, Protoss.PhotonCannon, Terran.Factory)) {
      pumpGasPumps()
      get(Terran.Factory)
    }
    if (enemyLurkersLikely || enemyDarkTemplarLikely) {
      get(Terran.Academy)
      get(Terran.Comsat)
    }
    pump(Terran.SiegeTankUnsieged)
    pumpRatio(Terran.Medic, 0, 3, Seq(Friendly(Terran.Marine, 0.2)))
    pump(Terran.Marine)
    pumpWorkers(oversaturate = false, maximumTotal = 23)
    pumpGasPumps()
    get(Terran.Factory)
    get(Terran.MachineShop)
    get(Terran.Academy)
    get(Terran.Stim)
    get(Terran.MarineRange)
    get(Terran.Comsat)
    get(2, Terran.Factory)
    get(2, Terran.MachineShop)

    if (enemyDarkTemplarLikely && ! have(Terran.Comsat)) {
      allIn()
    }
    attack()
  }
}
