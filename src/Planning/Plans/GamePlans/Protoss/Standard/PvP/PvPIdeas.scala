package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Planning.Predicates.MacroCounting
import Planning.UnitMatchers.MatchWarriors
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss._
import Utilities.Time.Minutes

object PvPIdeas extends MacroCounting {
  def enemyLowUnitStrategy: Boolean = enemyBases > 1 || enemyStrategy(
    With.fingerprints.nexusFirst,
    With.fingerprints.gatewayFe,
    With.fingerprints.forgeFe,
    With.fingerprints.robo,
    With.fingerprints.dtRush,
    With.fingerprints.cannonRush)

  def attackFirstZealot: Boolean = trackRecordLacks(With.fingerprints.twoGate, With.fingerprints.proxyGateway)

  def recentlyExpandedFirst: Boolean = With.scouting.weExpandedFirst && With.framesSince(With.scouting.firstExpansionFrameUs) < Minutes(3)()

  def shouldAttack: Boolean = {
    // Attack subject to global safety
    var output = enemyLowUnitStrategy
    output ||= unitsComplete(MatchWarriors) > 0 && enemiesComplete(MatchWarriors, Protoss.PhotonCannon) == 0 && (attackFirstZealot || With.frame > Minutes(4)() || unitsComplete(MatchWarriors) > 2)
    output ||= employing(PvP1012) && (unitsComplete(Protoss.Zealot) > 3 || ! enemyStrategy(With.fingerprints.twoGate))
    output ||= employing(PvPGateCoreGate) && unitsComplete(Protoss.Dragoon) > enemies(Protoss.Dragoon) && bases < 2
    output ||= employing(PvP3GateGoon) && unitsComplete(Protoss.Gateway) >= 3 && unitsComplete(MatchWarriors) >= 6
    output ||= employing(PvP4GateGoon) && unitsComplete(Protoss.Gateway) >= 4 && unitsComplete(MatchWarriors) >= 6
    output ||= enemyStrategy(With.fingerprints.dtRush) && unitsComplete(Protoss.Observer) > 1
    output ||= enemyStrategy(With.fingerprints.dtRush) && enemies(Protoss.DarkTemplar) == 0
    output ||= unitsComplete(Protoss.Shuttle) > 0 && unitsComplete(Protoss.Reaver) > 0 && unitsComplete(MatchWarriors) >= 6
    output ||= upgradeComplete(Protoss.ZealotSpeed)
    output ||= enemyMiningBases > miningBases
    output ||= bases > 2
    output ||= bases > miningBases
    output &&= safeToMoveOut
    output &&= ! recentlyExpandedFirst

    // Attack disregarding global safety
    output ||= enemyBases > 1 && miningBases < 2
    output ||= unitsComplete(Protoss.DarkTemplar) > 0 && enemies(Protoss.Observer) == 0

    // Don't let cannon rushes encroach
    output ||= With.fingerprints.cannonRush()

    // Attack when we have range advantage (and they're not hiding behind a wall
    if ( ! enemyStrategy(With.fingerprints.forgeFe, With.fingerprints.gatewayFe, With.fingerprints.forgeFe)) {
      output ||= unitsComplete(Protoss.Dragoon) > 0     && ! enemyHasShown(Protoss.Dragoon)         && (enemiesShown(Protoss.Zealot) > 2 || With.fingerprints.twoGate())
      output ||= upgradeComplete(Protoss.DragoonRange)  && ! enemyHasUpgrade(Protoss.DragoonRange)
    }

    // Require DT backstab protection before attacking through a DT (unless we're going into a base trade! how exciting)
    output &&= unitsComplete(Protoss.Observer, Protoss.PhotonCannon) > 1 || ! enemyHasShown(Protoss.DarkTemplar) || (unitsComplete(Protoss.DarkTemplar) > 0 && enemiesComplete(Protoss.PhotonCannon, Protoss.Observer) == 0)
    output
  }
}
