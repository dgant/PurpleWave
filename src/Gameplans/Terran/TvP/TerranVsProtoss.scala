package Gameplans.Terran.TvP

import Gameplans.All.GameplanImperative
import ProxyBwapi.Races.Terran

class TerranVsProtoss extends GameplanImperative {

  override def executeBuild(): Unit = {
    scoutAt(13)
    once(9, Terran.SCV)
    once(Terran.SupplyDepot)
    once(11, Terran.SCV)
    once(Terran.Barracks)
    once(12, Terran.SCV)
    once(Terran.Refinery)
    once(15, Terran.SCV)
    once(2, Terran.SupplyDepot)
    once(Terran.Marine)
    once(16, Terran.SCV)
    once(Terran.Factory)
    once(17, Terran.SCV)
    once(2, Terran.Marine)
    once(18, Terran.SCV)
    once(2, Terran.Factory)
    once(19, Terran.SCV)
    once(Terran.MachineShop)
    once(3, Terran.SupplyDepot)
    once(Terran.SiegeTankUnsieged)
    once(2, Terran.MachineShop)
    once(3, Terran.SiegeTankUnsieged)
    once(23, Terran.SCV)
    once(2, Terran.Vulture)
  }

  override def doWorkers(): Unit = {
    pumpWorkers(oversaturate = true)
  }

  def executeMain(): Unit = {
    once(Terran.VultureSpeed)
    if (units(Terran.Factory) > 2) {
      pump(Terran.SiegeTankUnsieged)
      pump(Terran.Vulture)
    }
    if (unitsEver(Terran.SiegeTankUnsieged) >= 3 && unitsEver(Terran.Vulture) >= 2 && safePushing) {
      attack()
      pump(Terran.Vulture)
    } else {
      pump(Terran.SiegeTankUnsieged)
    }
    get(Terran.SpiderMine)
    get(Terran.SiegeMode)
    once(8, Terran.Marine)
    requireMiningBases(2)
    get(Terran.Armory)
    get(Terran.MechDamage)
    get(Terran.MechArmor)
    get(6, Terran.Factory)
  }
}