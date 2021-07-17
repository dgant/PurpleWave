package Tactics.Squads

import Lifecycle.With
import Planning.Plans.Scouting.MonitorBases
import Planning.Predicates.MacroFacts
import Planning.UnitMatchers.MatchMobileDetector
import ProxyBwapi.Races.{Protoss, Terran}
import Tactics.Tactic

class MonitorTerranWithObserver extends Tactic {
  val monitorBases = new MonitorBases(Protoss.Observer)
  override def launch(): Unit = {
    if ( ! With.enemies.exists(_.isTerran)) return
    if (With.units.existsEnemy(MatchMobileDetector)) return
    if (With.units.countEnemy(Terran.Factory) > 7) return
    if (With.enemies.exists(_.hasTech(Terran.WraithCloak))) return
    if (With.units.countOurs(Protoss.Observer) < 3 && MacroFacts.enemyHasShown(Terran.SpiderMine)) return
    monitorBases.update()
  }
}
