package Micro.Heuristics.Targeting2

import Mathematics.Heuristics.HeuristicMathMultiplicative
import Micro.Heuristics.Targeting.{TargetEvaluator, TargetHeuristicDetectors}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object EvaluateTargets2 extends TargetEvaluator {
  
  def best(attacker: FriendlyUnitInfo, targets: Iterable[UnitInfo]): Option[UnitInfo] = {
    val output = ByOption.maxBy(targets)(evaluate(attacker, _))
    output
  }

  def audit(attacker: FriendlyUnitInfo, targets: Iterable[UnitInfo]): Seq[(UnitInfo, Double)] = {
    var output = targets.map(target => (target, evaluate(attacker, target))).toVector
    output = output.sortBy(-_._2)
    output
  }

  def participatingInCombat(target: UnitInfo): Boolean = (
    target.matchups.targets.nonEmpty
      || target.isAny(
        Terran.Dropship,
        Terran.Medic,
        Terran.ScienceVessel,
        Terran.SpiderMine,
        Protoss.DarkArchon,
        Protoss.HighTemplar,
        Protoss.Shuttle,
        Zerg.Defiler))

  def evaluate(attacker: FriendlyUnitInfo, target: UnitInfo): Double = {
    evaluateInner(attacker, target, recur = true)
  }

  def evaluateInner(attacker: FriendlyUnitInfo, target: UnitInfo, recur: Boolean): Double = {
    val dpfAgainstTarget      = attacker.dpfOnNextHitAgainst(target)
    val teamFramesOfSafety    = attacker.matchups.teamFramesOfSafety
    val framesToReachTarget   = Math.max(
      attacker.framesToGetInRange(target),
      attacker.framesToGetInRange(target, target.projectFrames(attacker.framesBeforeAttacking(target))))
    val framesToGoal          = attacker.framesToTravelTo(attacker.agent.destination)
    val framesToGoalAtTarget  = attacker.framesToTravelPixels(attacker.pixelDistanceTravelling(attacker.agent.destination, attacker.pixelToFireAt(target)))
    val framesOfFreedom       = Math.max(attacker.cooldownLeft, teamFramesOfSafety)
    val framesOutOfWayToGoal  = if (attacker.canMove) Math.max(0, framesToGoalAtTarget - framesToGoal) else 0
    val framesOutOfWayToShoot = if (attacker.canMove) Math.max(0, attacker.framesToGetInRange(target) - framesOfFreedom) else 0

    var output = (
      target.baseTargetValue()
      * dpfAgainstTarget
      / Math.max(1.0, target.totalHealth)
      / Math.max(6.0, framesOutOfWayToGoal + framesOutOfWayToShoot)
    )

    // Accessibility bonus
    val accessibleCombatUnit = attacker.inRangeToAttack(target) && target.participatingInCombat()
    if (accessibleCombatUnit) {
      output *= 1.5
    }

    // Repair bonus
    val repairValue = target.orderTarget.map(targetOrderTarget =>
      if (target.repairing && ! targetOrderTarget.repairing && recur)
        1.1 * evaluateInner(attacker, target, recur = false)
      else
        0.0).getOrElse(0.0)
    output = Math.max(output, repairValue)

    // Detector bonus
    val isDetecting = TargetHeuristicDetectors.evaluate(attacker, target) > HeuristicMathMultiplicative.default
    if (isDetecting) {
      output *= 8.0
    }

    // Free shots
    val freeShot = attacker.enemyRangeGrid.get(attacker.pixelToFireAt(target).tileIncluding) == 0
    if (freeShot) {
      output *= 2.0
    }

    // Anti-air bonus
    val antiAirBonus = attacker.matchups.allies.exists(_.isAny(Protoss.Carrier, Protoss.Scout, Zerg.Mutalisk, Zerg.Guardian)) && target.attacksAgainstAir > 0
    if (antiAirBonus) {
      output *= 2.0
    }

    output
  }

  def getTargetBaseValue(target: UnitInfo): Double = {
    var output = target.subjectiveValue

    // Gathering bonus
    val gathering = target.gathering
    if (gathering) {
      output *= 2.0
    }

    // Construction bonus
    val constructing = target.constructing
    if (constructing) {
      output *= 2.0
    }

    // Combat bonus
    if (target.participatingInCombat()) {
      output *= target.matchups.splashFactorMax
      if (target.complete) {
        output *= 2.0
      } else {
        val buildFrames = target.unitClass.buildFrames
        output *= (1.0 + (buildFrames - target.remainingCompletionFrames) / buildFrames)
      }
    }

    // Temporary visibility bonus
    val temporarilyVisible = (target.cloaked || target.burrowed) && target.matchups.enemyDetectors.forall(_.is(Terran.SpellScannerSweep))
    if (temporarilyVisible) {
      output *= 2.0
    }

    output
  }
}
