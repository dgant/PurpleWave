package Information.Battles.Prediction.Simulation

import Utilities.ByOption

object BehaviorFight extends SimulacrumBehavior {
  val fighting: Boolean = true
  @inline override def act(simulacrum: NewSimulacrum): Unit = {

    simulacrum.target = simulacrum.target.filter(validTarget(simulacrum, _)).orElse({
      simulacrum.targets.removeIf(t => ! validTarget(simulacrum, t))
      ByOption.minBy(simulacrum.targets)(_.pixelDistanceSquared(simulacrum))
    })
    // Remove target if invalid
    // If no valid target, pick target
    // If no valid target, flee
    // If we are on cooldown and in range of target and are vulture/dragoon, kite
    // If in range of target, fire at it
    // If not in range of target, tween towards firing pixel
  }

  @inline def validTarget(attacker: NewSimulacrum, target: NewSimulacrum): Boolean = {
    // TODO: Check detection
    target.alive && attacker.attacksAgainst(target) > 0
  }
}
