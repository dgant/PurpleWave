package Information.Battles.Prediction.Simulation

import Utilities.ByOption

object BehaviorFight extends SimulacrumBehavior {
  val fighting: Boolean = true
  @inline override def act(simulacrum: NewSimulacrum): Unit = {
    // Remove target if invalid
    // If no valid target, pick target
    simulacrum.target = simulacrum.target.filter(validTarget(simulacrum, _)).orElse({
      simulacrum.targets.removeIf(t => ! validTarget(simulacrum, t))
      ByOption.minBy(simulacrum.targets)(_.pixelDistanceSquared(simulacrum))
    })

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
        simulacrum.sleep(simulacrum.simulation.resolution)
      }
    } else if (simulacrum.canMove) {
      // TODO: Find walkable firing pixel (that's still in range)
      simulacrum.tween(simulacrum.target.get.pixel.project(simulacrum.pixel, distance - range + 2), Some("Approaching target"))
    } else {
      simulacrum.sleep(simulacrum.simulation.resolution, Some("Target out of reach"))
    }
  }

  @inline def validTarget(attacker: NewSimulacrum, target: NewSimulacrum): Boolean = {
    // TODO: Check detection
    target.alive && attacker.canAttack(target)
  }
}
