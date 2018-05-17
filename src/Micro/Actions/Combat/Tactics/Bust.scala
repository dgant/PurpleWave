package Micro.Actions.Combat.Tactics

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.Leave
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import bwapi.Race

object Bust extends Action {
  
  // TODO:
  // jaj22: quick bunker test, range goon has at least a 33 pixel variation for where it stops on a straight line attack command.
  // Conclusion: We need to identify where Dragoons are supposed to stand to shoot at a Bunker, then tell them to Hold Position
  
  // Killing bunkers with Dragoons is an important technique that we can't yet perform on first princples.
  // Range-upgraded Dragoons just barely outrange a Bunker containing non-range-upgraded Marines.
  
  protected def safeFromThreat(
    dragoon: FriendlyUnitInfo,
    threat: UnitInfo,
    pixel: Pixel): Boolean = (
    threat.is(Terran.Bunker)
    || threat.is(Terran.SCV)
    || threat.is(Terran.Marine)
    || threat.is(Terran.Vulture)
    || threat.pixelDistanceEdge(dragoon, pixel) > threat.pixelRangeAgainst(dragoon) + 48.0
  )
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.totalHealth >= unit.unitClass.maxHitPoints + 12 // Don't take hull damage
    && With.enemies.exists(_.raceCurrent == Race.Terran)
    && unit.agent.canFight
    && unit.canMove
    && unit.is(Protoss.Dragoon)
    && With.self.hasUpgrade(Protoss.DragoonRange)
    && unit.matchups.threats.forall(threat => safeFromThreat(unit, threat, unit.pixelCenter))
    && unit.matchups.targets.exists(bunker =>
        (bunker.visible || With.grids.altitudeBonus.get(unit.tileIncludingCenter) == With.grids.altitudeBonus.get(bunker.tileIncludingCenter))
        && bunker.aliveAndComplete
        && ! bunker.player.hasUpgrade(Terran.MarineRange)
        && bunker.is(Terran.Bunker)
        && unit.matchups.threats.forall(threat =>
          safeFromThreat(
            unit,
            threat,
            unit.pixelCenter.project(bunker.pixelCenter, unit.pixelDistanceEdge(bunker) - unit.pixelRangeAgainst(bunker)))))
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    // Goal: Take down the bunker. Don't take any damage from it.
    // If we're getting shot at by the bunker, back off.
    val bunkers = unit.matchups.threats.filter(_.is(Terran.Bunker))
    if (With.framesSince(unit.lastFrameTakingDamage) < GameTime(0, 1)()) {
      Leave.delegate(unit)
    }
    else if (unit.matchups.targetsInRange.nonEmpty) {
      With.commander.hold(unit)
    }
  }
}
