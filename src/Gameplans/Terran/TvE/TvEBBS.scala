package Gameplans.Terran.TvE

import Lifecycle.With
import Macro.Actions.Friendly
import Placement.Access.PlaceLabels.DefendHall
import Placement.Access.PlacementQuery
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.UnitFilters.{IsTank, IsWarrior}

class TvEBBS extends TerranGameplan {

  override def executeBuild(): Unit = {
    emergencyReactions()

    once(10, Terran.SCV)
    get(2, Terran.Barracks)
    get(Terran.SupplyDepot)
    once(2, Terran.Marine)
    once(11, Terran.SCV)
    once(4, Terran.Marine)

    With.scouting.enemyNatural
      .filter(_.townHall.isDefined)
      .foreach(natural => {
        if (enemies(IsWarrior, Protoss.PhotonCannon) == 0) {
          get(1, Terran.Bunker, new PlacementQuery(Terran.Bunker).requireBase(natural).preferLabelYes(DefendHall))
        }
      })

    once(2, Terran.SupplyDepot)
    once(12, Terran.SCV)
    once(6, Terran.Marine)
    once(13, Terran.SCV)
    once(8, Terran.Marine)
    once(15, Terran.SCV)

    scoutAt(12)
  }

  override def doWorkers(): Unit = {
    pumpWorkers(oversaturate = false, maximumTotal = 18 + 3 + 2 + units(IsTank))
  }

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
    pumpGasPumps()
    get(Terran.Factory)
    get(Terran.MachineShop)
    get(Terran.Academy)
    get(Terran.Stim)
    get(Terran.MarineRange)
    get(Terran.Comsat)
    get(2, Terran.Factory)
    get(2, Terran.MachineShop)
    attack()
  }
}
