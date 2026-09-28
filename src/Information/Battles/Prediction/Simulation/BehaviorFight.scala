package Information.Battles.Prediction.Simulation

import Micro.Targeting.TargetScoring
import ProxyBwapi.Races.Protoss

object BehaviorFight extends SimulacrumBehavior {
  val fighting: Boolean = true
  @inline override def act(simulacrum: Simulacrum): Unit = {
    var measuredTarget    : Simulacrum = null
    var measuredDistance  : Double     = 0.0
    var measuredRange     : Double     = 0.0
    // Remove target if invalid
    // If no valid target, pick target
    val targetChosen = simulacrum.target match {
      case target @ Some(t) if simulacrum.cooldownTargeting > 0 && validTarget(simulacrum, t) && {
        measuredTarget    = t
        measuredDistance  = simulacrum.pixelDistanceEdgeSimulation(t)
        measuredRange     = simulacrum.pixelRangeAgainst(t)
        measuredDistance <= measuredRange && (simulacrum.pixelRangeMin <= 0.0 || measuredDistance > simulacrum.pixelRangeMin)
      } =>
        target
      case _ =>
        simulacrum.cooldownTargeting = 72
        chooseTarget(simulacrum)
    }
    simulacrum.setTarget(targetChosen)

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
    val distance  = if (target eq measuredTarget) measuredDistance else simulacrum.pixelDistanceEdgeSimulation(target)
    val range     = if (target eq measuredTarget) measuredRange else simulacrum.pixelRangeAgainst(target)

    // Update the target's threat (to help it kite)
    val targetThreat = target.threat
    if (targetThreat.isEmpty || targetThreat.get.pixelsToGetInRangeSimulation(target) > distance - range) {
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
    if (distance <= range && (simulacrum.pixelRangeMin <= 0.0 || distance > simulacrum.pixelRangeMin)) {
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
      && (attacker.isFriendlyCached || target.shouldScoreIfOurs)) // Enemy units should not target unscored units
  }

  @inline private def chooseTarget(attacker: Simulacrum): Option[Simulacrum] = {
    val targets = attacker.targets
    val scoring = TargetScoring.forSimulation(attacker)
    var i = 0
    var output: Simulacrum = null
    var outputValue = 0.0
    var found = false
    while (i < targets.length) {
      val target = targets(i)
      if ( ! validTarget(attacker, target)) {
        targets.removeAt(i)
      } else {
        val targetValue = scoring(target)
        if ( ! found || java.lang.Double.compare(targetValue, outputValue) > 0) {
          found = true
          output = target
          outputValue = targetValue
        }
        i += 1
      }
    }
    if (found) Some(output) else None
  }
}
