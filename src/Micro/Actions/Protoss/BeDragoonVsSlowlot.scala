package Micro.Actions.Protoss

import Debugging.Visualizations.Forces
import Lifecycle.With
import Macro.Facts.MacroFacts
import Mathematics.Maff
import Micro.Actions.Action
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import Micro.Coordination.Pushing.TrafficPriorities
import Micro.Heuristics.Potential
import Micro.Targeting.Target
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.?
import Utilities.Time.Minutes

object BeDragoonVsSlowlot extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    Protoss.Dragoon(unit)
    && With.frame < Minutes(7)()
    && unit.matchups.threats.forall(_.isAny(Protoss.Probe, Protoss.Zealot))
    && ! MacroFacts.enemyHasUpgrade(Protoss.ZealotSpeed))

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val margin    = Maff.min(unit.matchups.threatsInPixels(160).map(t => t.pixelsToGetInRange(unit) + 32 - Maff.or0(16, t.presumptiveTarget.contains(unit)) - 16 * t.dot(unit.pixel))).getOrElse(320d).toInt
    val buffer    = margin / 32
    val hitsLeft  = unit.totalHealth / 14
    val safety    = buffer + hitsLeft

    unit.agent.escalatePriority(
      ?(safety < 3, TrafficPriorities.Shove,
      ?(safety < 5, TrafficPriorities.Bump,
      ?(safety < 7, TrafficPriorities.Nudge,
                    TrafficPriorities.Pardon))))

    val marginRequired  = Math.max(32, 128 - 8 * hitsLeft)
    val marginDelta     = margin - marginRequired

    unit.agent.forces.put(Forces.spacing,  Potential.preferSpacing(unit))
    unit.agent.forces.put(Forces.pushing,  Potential.followPushes(unit))
    unit.agent.forces.put(Forces.threat,   Potential.hardAvoidThreatRange(unit, Math.max(32, marginDelta)))

    if (marginDelta >= 0) {
      Target.choose(unit)
      if (unit.agent.toAttack.isDefined) {
        if (unit.readyForAttackOrder) {
          Commander.attack(unit)
        } else if (marginDelta > 32 && ! unit.matchups.threatsInPixels(160).exists(_.presumptiveTarget.contains(unit))) {
          unit.agent.forces.put(Forces.target, Potential.towards(unit, unit.pixelToFireAtExhaustive(unit.agent.toAttack.get)))
          unit.agent.forced.set(MicroPathing.getWaypointInDirection(unit, unit.agent.forces.sum.radians).orElse(unit.agent.destinationNext.pixel))
          Commander.move(unit)
        } else if (unit.pixelsToGetInRange(unit.agent.toAttack.get) < -32) {
          retreat(unit)
        } else {
          Commander.attack(unit)
        }
      }
      // Allow default movement to take over
    } else {
      retreat(unit)
    }
  }

  def retreat(unit: FriendlyUnitInfo): Unit = {
    val force = unit.agent.forces.sum.radians
    MicroPathing.getWaypointInDirection(unit, force, requireSafety = true)

    lazy val waypointSimple   = MicroPathing.getWaypointInDirection(unit, force, requireSafety = true)
    lazy val waypointPath     = MicroPathing.getWaypointAlongTilePath(unit, tilePath).map(_.add(unit.pixel.offsetFromTileCenter))
    lazy val waypointForces   = Seq(true, false).view.map(safety => MicroPathing.getWaypointInDirection(unit, force, requireSafety = safety)).find(_.nonEmpty).flatten
    lazy val tilePath         = MicroPathing.getThreatAwarePath(unit, preferHome = false)
    val waypoint              = waypointSimple.orElse(waypointPath).orElse(waypointForces)

    waypoint.foreach(p => {
      unit.agent.decision.set(p)
      Commander.move(unit)
    })
  }
}
