package Micro.Heuristics

import Lifecycle.With
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object EvaluateTargets extends {
  
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

  // Used by combat simulation as well, to keep targeting behavior somewhat consistent
  // Thus the implementation needs to be FAST.
  @inline final def baseAttackerToTargetValue(
    baseTargetValue: Double,
    totalHealth: Double,
    framesOutOfTheWay: Double,
    dpf: Double = 1.0): Double = {
    baseTargetValue * dpf / (Math.max(1.0, totalHealth) * Math.max(6.0, framesOutOfTheWay))
  }

  def evaluateInner(attacker: FriendlyUnitInfo, target: UnitInfo, recur: Boolean): Double = {
    val framesToGoal          = attacker.framesToTravelTo(attacker.agent.destination)
    val framesToGoalAtTarget  = attacker.framesToTravelPixels(attacker.pixelDistanceTravelling(attacker.agent.destination, attacker.pixelToFireAt(target)))
    val framesOfFreedom       = Math.max(attacker.cooldownLeft, attacker.matchups.teamFramesOfSafety)
    val framesOutOfWayToGoal  = if (attacker.canMove) Math.max(0, framesToGoalAtTarget - framesToGoal) else 0
    val framesOutOfWayToShoot = if (attacker.canMove) Math.max(0, attacker.framesToGetInRange(target) - framesOfFreedom) else 0

    var output = baseAttackerToTargetValue(
      baseTargetValue = target.baseTargetValue(),
      totalHealth = target.totalHealth,
      framesOutOfTheWay = framesOutOfWayToGoal + framesOutOfWayToShoot,
      dpf = attacker.dpfOnNextHitAgainst(target))

    // Accessibility bonus
    val accessibleCombatUnit = attacker.inRangeToAttack(target) && target.participatingInCombat() && ! target.isInterceptor()
    if (accessibleCombatUnit) {
      output *= 1.5
    }

    // Melee hugging, like Zealots against Siege Tanks
    val meleeHug = ! attacker.unitClass.ranged && target.topSpeed < attacker.topSpeed
    if (meleeHug) {
      output *= (if (target.canMove) 1.5 else 2.0)
    }

    // Free shots
    val freeShot = attacker.enemyRangeGrid.get(attacker.pixelToFireAt(target).tileIncluding) == 0
    if (freeShot) {
      output *= 2.0
    }

    output
  }

  def getTargetBaseValue(target: UnitInfo, recur: Boolean = true): Double = {
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

    // Repair bonus
    val repairValue = target.orderTarget.map(targetOrderTarget =>
      if (target.repairing && ! targetOrderTarget.repairing && recur)
        1.1 * getTargetBaseValue(targetOrderTarget, recur = false)
      else
        0.0).getOrElse(0.0)
    output = Math.max(output, repairValue)

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

    // Immediate combat value bonus
    val hurtingUsBonus = target.matchups.targetsInRange.nonEmpty || target.unitClass.maxEnergy > 0
    if (hurtingUsBonus) {
      output *= 2.0
    }

    // Anti-air bonus
    val antiAirBonus = target.matchups.targets.exists(_.isAny(Protoss.Carrier, Protoss.Scout, Zerg.Mutalisk, Zerg.Guardian)) && target.attacksAgainstAir > 0
    if (antiAirBonus) {
      output *= 2.0
    }

    // Temporary visibility bonus
    val temporarilyVisible = (target.cloaked || target.burrowed) && target.matchups.enemyDetectors.forall(_.is(Terran.SpellScannerSweep))
    if (temporarilyVisible) {
      output *= 2.0
    }

    // Detection bonus
    val weHaveCloakedThreat = target.matchups.enemies.headOption.exists(_.matchups.alliesInclSelfCloaked.exists(_.matchups.targets.nonEmpty)) && ! target.matchups.enemies.exists(_.is(Protoss.Arbiter))
    if (weHaveCloakedThreat) {
      val building = target.constructing || target.repairing
      val buildTarget = if (building) target.target else None
      val detects = aidsDetection(target) || buildTarget.exists(aidsDetection)
      if (detects) {
        output *= 40.0
      }
    }

    // Interceptor penalty
    if (target.isInterceptor()) {
      output *= 0.25
    }

    // Expansion bonus
    if (target.unitClass.isTownHall && target.base.forall(base => ! With.intelligence.enemyMain.contains(base) && ! With.intelligence.enemyNatural.contains(base))) {
      output *= 3.0
    }

    output
  }

  def aidsDetection(target: UnitInfo): Boolean = {
    if (target.unitClass.isDetector) {
      return true
    }
    if (target.isAny(Terran.Comsat, Terran.ControlTower, Protoss.RoboticsFacility)) {
      return true
    }
    if (target.isAny(Terran.Academy, Terran.ScienceFacility, Protoss.Observatory) && ! target.complete) {
      return true
    }
    if (target.isAny(Terran.EngineeringBay, Protoss.Forge)) {
      return true
    }
    false
  }
}
