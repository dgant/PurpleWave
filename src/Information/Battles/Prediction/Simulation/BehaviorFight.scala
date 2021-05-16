package Information.Battles.Prediction.Simulation

import Micro.Actions.Combat.Targeting.Target
import Utilities.ByOption

object BehaviorFight extends SimulacrumBehavior {
  val fighting: Boolean = true
  @inline override def act(simulacrum: Simulacrum): Unit = {
    // Remove target if invalid
    // If no valid target, pick target
    simulacrum.setTarget(simulacrum.target.filter(t => validTarget(simulacrum, t) && simulacrum.inRangeToAttack(t)).orElse({
      simulacrum.targets.removeIf(t => ! validTarget(simulacrum, t))
      ByOption.maxBy(simulacrum.targets)(Target.baseAttackerToTargetValue(simulacrum, _))
    }))

    // TODO: Retarget when target out of range

    // If no valid target, flee
    if (simulacrum.target.isEmpty) {
      simulacrum.doBehavior(BehaviorFlee)
      return
    }

    // TODO: Set target's threat
    // TODO: Siege mode
    // TODO: Lurker burrow/unburrow
    // TODO: Non-Lurker Unburrow

    val target = simulacrum.target.get
    val distance = simulacrum.pixelDistanceEdge(target)
    val range = simulacrum.pixelRangeAgainst(target)
    if (distance <= range) {
      if (simulacrum.cooldownLeft <= 0) {
        simulacrum.dealDamageTo(target)
      }
      if (false) {
        // TODO: Kite
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
    // TODO: Check detection
    target.alive && attacker.canAttack(target)
  }
}
