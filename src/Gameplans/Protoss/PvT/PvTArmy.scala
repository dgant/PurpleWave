package Gameplans.Protoss.PvT

import Lifecycle.With
import Macro.Actions.{Enemy, Flat, Friendly, MacroActions}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss.PvTDT
import Utilities.?
import Utilities.UnitFilters.IsWarrior

object PvTArmy extends MacroActions {

  def counterBio: Boolean = {
    With.fingerprints.bio() && enemies(Terran.Marine, Terran.Firebat, Terran.Medic) >= enemies(Terran.Vulture) * 1.5
  }

  def highPriority(): Unit = {
    if (PvTDT()) {
      once(2, Protoss.DarkTemplar)
    }
    if ( ! enemyHasShown(Terran.MissileTurret, Terran.ScienceVessel)) {
      pump(Protoss.DarkTemplar)
    }
    pump(Protoss.Observer, ?(enemyHasShown(Terran.SpiderMine), 2, 1))
    pumpRatio(
      Protoss.Dragoon,
      6, 16,
      Seq(
        Flat(6),
        Enemy(Terran.Vulture,       .75),
        Enemy(Terran.Wraith,        1.0),
        Enemy(Terran.Battlecruiser, 3.0)))

    if (have(Protoss.RoboticsSupportBay)) {
      pumpShuttleAndReavers(6, shuttleFirst = ! counterBio)
    }

    pump(Protoss.Carrier, 4)
    pump(Protoss.Arbiter, 1)
  }

  def normalPriority(): Unit = {
    pumpRatio(
      Protoss.Dragoon,
      ?(counterBio, 6, 12), 24,
      Seq(
        Enemy(Terran.Vulture,       .75),
        Enemy(Terran.Wraith,        1.0),
        Enemy(Terran.Battlecruiser, 4.0),
        Friendly(Protoss.Zealot,    0.5)))

    pumpRatio(
      Protoss.Observer,
      ?(enemyHasShown(Terran.SpiderMine), 2, 3), 4,
      Seq(Friendly(IsWarrior, 1.0 / 12.0)))

    if (techStarted(Protoss.PsionicStorm)) {
      pumpRatio(Protoss.HighTemplar, 2, 4, Seq(Friendly(IsWarrior, 0.2)))
    }

    if (have(Protoss.HighTemplar)) {
      pumpRatio(Protoss.Shuttle, 0, 6, Seq(Friendly(Protoss.Reaver, 0.5), Flat(1), Friendly(Protoss.HighTemplar, 1.0 / 4.0)))
    }
    if (have(Protoss.FleetBeacon) && (enemyHasTech(Terran.WraithCloak) || enemies(Terran.Wraith) > 2)) {
      pumpRatio(Protoss.Observer, 3, 8, Seq(Enemy(Terran.Wraith, 0.5)))
    }

    pump(Protoss.Carrier)
    pumpRatio(Protoss.Arbiter, 1, 4, Seq(Friendly(IsWarrior, 0.2)))
    if (have(Protoss.ArbiterTribunal) && ! have(Protoss.FleetBeacon)) {
      pump(Protoss.Scout, 1)
    }

    if (upgradeStarted(Protoss.ZealotSpeed)) {
      pump(Protoss.Zealot, units(Protoss.Dragoon))
    }
    pump(Protoss.Dragoon)
  }
}
