package Gameplans.Terran.TvR

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Macro.Actions.Friendly
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import bwapi.Race

class TerranVsRandom extends GameplanImperative {

  override def activated: Boolean = With.enemies.exists(_.raceInitial == Race.Unknown)

  override def executeBuild(): Unit = {
    once(8, Terran.SCV)
    get(2, Terran.Barracks)
    once(9, Terran.SCV)
    get(Terran.SupplyDepot)
    once(10, Terran.SCV)

    if ( ! foundEnemyBase) {
      scoutOn(u => Terran.Barracks(u) && u.complete)
    }
  }

  override def doWorkers(): Unit = {}

  override def executeMain(): Unit = {
    if (have(Terran.Factory)) {
      once(Terran.MachineShop)
      once(Terran.SiegeTankUnsieged)
      get(Terran.SiegeMode)
    }
    if (enemiesHaveComplete(Terran.Bunker, Zerg.SunkenColony, Protoss.PhotonCannon, Terran.Factory)) {
      get(Terran.Refinery)
      get(Terran.Factory)
    }
    if (enemyLurkersLikely || enemyDarkTemplarLikely) {
      get(Terran.Academy)
      get(Terran.Comsat)
    }
    pump(Terran.SiegeTankUnsieged)
    pumpRatio(Terran.Medic, 0, 3, Seq(Friendly(Terran.Marine, 0.2)))
    pump(Terran.Marine)
    pumpWorkers(oversaturate = false, maximumTotal = 22)
    get(Terran.Refinery)
    get(Terran.Factory)
    get(Terran.MachineShop)
    get(Terran.Academy)
    get(Terran.Stim)
    get(Terran.MarineRange)
    get(3, Terran.Barracks)
    get(Terran.Comsat)
    get(4, Terran.Barracks)

    if (enemyDarkTemplarLikely && ! have(Terran.Comsat)) {
      allIn()
    }
    attack()
  }
}
