package Gameplans.Terran.TvE

import Lifecycle.With
import Macro.Actions.Friendly
import Placement.Access.PlaceLabels
import ProxyBwapi.Races.{Protoss, Terran}
import Utilities.UnitFilters.IsTank

class TvEBBS extends TerranGameplan {


  override def executeBuild(): Unit = {
    emergencyReactions()

    once(8, Terran.SCV)
    get(2, Terran.Barracks)
    once(9, Terran.SCV)
    get(Terran.SupplyDepot)
    once(3, Terran.Marine)
    once(10, Terran.SCV)
    once(5, Terran.Marine)
    once(11, Terran.SCV)

    BunkerRush()

    once(2, Terran.SupplyDepot)
    once(7, Terran.Marine)
    once(12, Terran.SCV)

    scoutOn(u => Terran.Barracks(u) && u.complete)

    if (enemyLurkersLikely || enemyDarkTemplarLikely) {
      get(Terran.Academy)
      pump(Terran.Comsat)
    }
  }

  override def doWorkers(): Unit = {
    pumpWorkers(oversaturate = false, maximumTotal = 18 + 3 + 2 + units(IsTank))
  }

  override def executeMain(): Unit = {
    if (techStarted(Terran.SiegeMode)) {
      pump(Terran.SiegeTankUnsieged)
    }
    if ( ! safeDefending && (enemyIsZerg || enemyHasShown(Terran.Factory, Terran.Vulture, Protoss.Dragoon))) {
      pump(Terran.Marine, 4)
      buildBunkersAtFoyer(1, PlaceLabels.DefendEntrance)
    }

    pumpRatio(Terran.Medic, 0, 3, Seq(Friendly(Terran.Marine, 0.2)))
    if (have(Terran.Factory)) {
      get(2, Terran.Factory)
      get(Terran.SiegeMode)
      get(2, Terran.MachineShop)
    }
    pump(Terran.Marine)
    pumpGasPumps()
    if (With.enemies.exists(_.isZerg) || enemyDarkTemplarLikely || have(Terran.Academy)) {
      get(Terran.Academy)
      get(Terran.Stim)
      get(Terran.MarineRange)
    }
    get(Terran.Factory)

    if (safePushing || ! enemyHasUpgrade(Protoss.DragoonRange)) {
      attack()
    }
  }
}
