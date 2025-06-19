package Gameplans.Terran.TvP

import Gameplans.All.GameplanImperative
import Macro.Actions.Friendly
import ProxyBwapi.Races.Terran

class TerranVsProtoss extends GameplanImperative {

  override def executeBuild(): Unit = {
    once(9, Terran.SCV)
    once(Terran.SupplyDepot)
    once(11, Terran.SCV)
    once(Terran.Barracks)
    once(13, Terran.SCV)
    once(2, Terran.Barracks)
    once(14, Terran.SCV)
    once(2, Terran.SupplyDepot)
    once(Terran.Marine)
    once(15, Terran.SCV)
    once(3, Terran.Barracks)
    once(16, Terran.SCV)
    once(2, Terran.Marine)
    once(17, Terran.SCV)
    once(4, Terran.Marine)
    once(Terran.Academy)
  }

  override def doWorkers(): Unit = {
    pumpWorkers(oversaturate = false, maximumTotal = 22)
  }

  def executeMain(): Unit = {
    gasWorkerCeiling(1)
    once(8, Terran.Marine)
    once(2, Terran.Medic)
    once(16, Terran.Marine)
    pumpRatio(Terran.Medic,   1, 12, Seq(Friendly(Terran.Firebat, 0.25), Friendly(Terran.Marine, 0.25)))
    pump(Terran.Marine)
    get(Terran.Refinery)
    get(Terran.Academy)
    get(Terran.EngineeringBay)
    get(Terran.MissileTurret)
    get(Terran.Stim)
    get(Terran.BioDamage)
    get(Terran.MarineRange)
    get(Terran.BioArmor)
    get(5, Terran.Barracks)
    if (Terran.Stim()) {
      attack()
    }
    aggression(2.0)
  }
}