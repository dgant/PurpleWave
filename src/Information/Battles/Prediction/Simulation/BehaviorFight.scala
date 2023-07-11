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
      .filter(t => validTarget(simulacrum, t) && simulacrum.inRangeToAttack(t))
      .orElse({
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
    val distance  = simulacrum.pixelDistanceEdge(target)
    val range     = simulacrum.pixelRangeAgainst(target)
    if (target.threat.forall(_.pixelsToGetInRange(target) > distance - range)) {
      target.threat = Some(simulacrum)
    }
    if ((simulacrum.unitClass.abuseAllowed || simulacrum.unitClass == Protoss.Reaver)
      && simulacrum.cooldownLeft > 0
      && simulacrum.threat.isDefined
      && ( ! target.canAttack(simulacrum) || simulacrum.pixelRangeAgainst(target) > target.pixelRangeAgainst(simulacrum))) {
      val freeDistance = simulacrum.cooldownLeft * simulacrum.topSpeed - distance
      if (freeDistance > 0) {
        simulacrum.tween(simulacrum.threat.get.pixel.project(simulacrum.pixel, simulacrum.threat.get.pixelDistanceEdge(simulacrum) + freeDistance * 0.5), Some("Kiting"))
        return
      }
    }
    if (distance <= range) {
      simulacrum.simulation.engaged = true
      if (simulacrum.cooldownLeft <= 0) {
        simulacrum.dealDamageTo(target)
      } else {
        simulacrum.sleep(Math.min(simulacrum.cooldownLeft, simulacrum.simulation.resolution), Some("Cooldown"))
      }
    } else if (simulacrum.canMove) {
      // TODO: Find walkable firing pixel (that's still in range)
      simulacrum.tween(simulacrum.pixel.project(target.pixel, distance - range + 2), Some("Approaching target"))
    } else {
      simulacrum.sleep(simulacrum.simulation.resolution, Some("Target out of reach"))
    }
  }

  @inline def validTarget(attacker: Simulacrum, target: Simulacrum): Boolean = {
    target.alive && attacker.canAttack(target)
  }
}
