package Information.Battles.Prediction.Simulation

import Mathematics.Maff
import Micro.Targeting.TargetScoring
import ProxyBwapi.Races.Protoss

object BehaviorFight extends SimulacrumBehavior {
  val fighting: Boolean = true
  @inline override def act(simulacrum: Simulacrum): Unit = {
    // Remove target if invalid
    // If no valid target, pick target
    simulacrum.setTarget(simulacrum.target
      .filter(t =>
        simulacrum.cooldownTargeting > 0
        && validTarget(simulacrum, t)
        && simulacrum.inRangeToAttack(t))
      .orElse({
        simulacrum.cooldownTargeting = 72
        simulacrum.targets.removeIf(t => ! validTarget(simulacrum, t))
        Maff.maxBy(simulacrum.targets)(TargetScoring.fast(simulacrum, _))
      }))

    // If no valid target, flee
    if (simulacrum.target.isEmpty) {
      simulacrum.doBehavior(BehaviorFlee)
      return
    }

    // TODO: Siege mode
    // TODO: Lurker burrow/unburrow
    // TODO: Non-Lurker Unburrow

    val target    = simulacrum.target.get
    val threat    = simulacrum.threat
    val distance  = simulacrum.pixelDistanceEdge(target)
    val range     = simulacrum.pixelRangeAgainst(target)

    // Update the target's threat (to help it kite)
    if (target.threat.forall(_.pixelsToGetInRange(target) > distance - range)) {
      target.threat = Some(simulacrum)
    }

    // Kite
    if (threat.isDefined
      && simulacrum.cooldownLeft > 0
      && (simulacrum.unitClass.abuseAllowed || simulacrum.unitClass == Protoss.Reaver)
      && ( ! target.canAttack(simulacrum) || simulacrum.pixelRangeAgainst(target) > target.pixelRangeAgainst(simulacrum))) {
      val freeDistance = simulacrum.cooldownLeft * simulacrum.topSpeed - distance
      if (freeDistance > 0) {
        simulacrum.move(
          threat.get.pixel.project(
            simulacrum.pixel,
            threat.get.pixelDistanceCenter(simulacrum) + freeDistance * 0.5),
          Some("Kiting"))
        return
      }
    }

    // Fire!
    if (distance <= range) {
      simulacrum.simulation.engaged = true
      simulacrum.moveGoal           = None
      if (simulacrum.cooldownLeft <= 0) {
        simulacrum.dealDamageTo(target)
      } else {
        simulacrum.sleep(Math.min(simulacrum.cooldownLeft, simulacrum.simulation.resolution), Some("Cooldown"))
      }
    } else if (simulacrum.canMove) {
      // TODO: Find walkable firing pixel (that's still in range)
      simulacrum.move(simulacrum.pixel.project(target.pixel, distance - range + 2), Some("Approaching target"))
    } else {
      simulacrum.sleep(simulacrum.simulation.resolution, Some("Target out of reach"))
    }
  }

  @inline def validTarget(attacker: Simulacrum, target: Simulacrum): Boolean = {
    (target.alive
      && attacker.canAttack(target)
      && (attacker.isFriendly || target.shouldScoreIfOurs)) // Enemy units should not target unscored units
  }
}
