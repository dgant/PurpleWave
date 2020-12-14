package Micro.Actions.Combat.Tactics

import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.DefaultCombat.Engage
import Micro.Actions.Combat.Maneuvering.Retreat
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.{ByOption, Minutes, Seconds}
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
    With.frame < Minutes(8)() // Performance short-circuit
    && unit.totalHealth >= unit.unitClass.maxHitPoints + 12 // Don't take hull damage
    && With.enemies.exists(_.raceCurrent == Race.Terran)
    && unit.agent.canFight
    && unit.canMove
    && unit.is(Protoss.Dragoon)
    && With.self.hasUpgrade(Protoss.DragoonRange)
    && unit.matchups.threats.forall(threat => safeFromThreat(unit, threat, unit.pixelCenter))
    && unit.matchups.targets.exists(bunker =>
      (bunker.visible || bunker.altitudeBonus <= unit.altitudeBonus)
      && bunker.aliveAndComplete
      && bunker.is(Terran.Bunker)
      && ! bunker.player.hasUpgrade(Terran.MarineRange)
      && unit.matchups.threats.forall(threat =>
        safeFromThreat(
          unit,
          threat,
          unit.pixelCenter.project(bunker.pixelCenter, unit.pixelDistanceEdge(bunker) - unit.pixelRangeAgainst(bunker))))))
  
  override protected def perform(unit: FriendlyUnitInfo) {
    // Goal: Take down the bunker. Don't take any damage from it.
    // If we're getting shot at by the bunker, back off.
    lazy val bunkers = unit.matchups.threats.filter(_.is(Terran.Bunker))
    lazy val repairers = bunkers.flatMap(_.matchups.repairers)
    lazy val goons = unit.matchups.allies.filter(u => u.is(Protoss.Dragoon) && u.matchups.targetsInRange.exists(_.is(Terran.Bunker)))

    if (unit.readyForAttackOrder
      && unit.matchups.targetsInRange.exists(_.is(Terran.Bunker))
      && goons.length >= 6
      && repairers.nonEmpty) {
      unit.agent.toAttack = Some(repairers.minBy(_.pixelDistanceCenter(PurpleMath.centroid(goons.map(_.pixelCenter)))))
      With.commander.attack(unit)
    }

    if (With.framesSince(unit.lastFrameTakingDamage) < Seconds(1)()) {
      Retreat.delegate(unit)
    }
    else if (unit.matchups.targetsInRange.exists(_.canAttack(unit)) && unit.velocity.lengthSquared > 0) {
      With.commander.hold(unit)
    }
    unit.agent.toAttack = ByOption.minBy(unit.matchups.threats)(_.pixelDistanceEdge(unit))
    Engage.delegate(unit)
  }
}
