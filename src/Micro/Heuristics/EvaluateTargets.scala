package Micro.Heuristics

import Lifecycle.With
import Mathematics.PurpleMath
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object EvaluateTargets extends {
  
  def best(attacker: FriendlyUnitInfo, targets: Iterable[UnitInfo]): Option[UnitInfo] = {
    val output = ByOption.maxBy(targets)(evaluate(attacker, _))
    output
  }

  def audit(attacker: FriendlyUnitInfo, targets: Iterable[UnitInfo]): Seq[(UnitInfo, Double, Double)] = {
    var output = targets.map(target => (target, target.baseTargetValue(), evaluate(attacker, target))).toVector
    output = output.sortBy(-_._3)
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

    // Diving penalty
    if (attacker.matchups.threatsInRange.exists(_ != target) && ! attacker.inRangeToAttack(target)) {
      output *= 0.25
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
      output *= Math.max(1.0, target.matchups.splashFactorMax)
      output *= 2.0
      if (target.complete) {
        output *= 2.0
      } else {
        // Prefer nearly-complete buildings
        val buildFrames = target.unitClass.buildFrames
        output *= (1.0 + PurpleMath.clamp((buildFrames - target.remainingCompletionFrames) / buildFrames, 0, 1.0))
      }
    }

    // Immediate combat value bonus
    val hurtingUsBonus = target.matchups.targetsInRange.nonEmpty || (target.energy > 40 && ! target.isAny(Terran.Wraith, Protoss.Corsair))
    if (hurtingUsBonus) {
      output *= 1.25
    }

    // Immobility bonus
    if ( ! target.canMove) {
      output *= 1.25
    }

    // Anti-air bonus
    val antiAirBonus = target.attacksAgainstAir > 0 && target.matchups.targets.exists(t => t.flying && t.attacksAgainstGround > 0)
    if (antiAirBonus) {
      output *= 2.0
    }
    if (target.isAny(Terran.Armory, Protoss.CyberneticsCore, Zerg.Spire, Zerg.HydraliskDen)
      && With.units.existsOurs(Terran.Battlecruiser, Terran.Wraith, Terran.ScienceVessel, Protoss.Carrier, Protoss.Scout, Zerg.Mutalisk, Zerg.Guardian)) {
      output *= 2.5
    }

    // Visibility bonus
    if (target.visibleToOpponents) {
      output *= 2.0
    }
    if (target.likelyStillThere) {
      output *= 1.25
    }
    if (target.possiblyStillThere) {
      output *= 1.25
    }

    // Temporary visibility bonus
    val temporarilyVisible = (target.cloaked || target.burrowed) && target.matchups.enemyDetectors.forall(_.isScannerSweep())
    if (temporarilyVisible) {
      output *= 2.0
    }

    // Detection bonus
    val weHaveCloakedThreat = target.matchups.enemies.headOption.exists(_.matchups.alliesInclSelfCloaked.exists(_.matchups.targets.nonEmpty)) && ! target.matchups.enemies.exists(_.isArbiter())
    if (weHaveCloakedThreat) {
      val building = target.constructing || target.repairing
      val buildTarget = if (building) target.target else None
      val detects = aidsDetection(target) || buildTarget.exists(aidsDetection)
      if (detects) {
        output *= 40.0
      }
    }

    // Cloaked bonus
    // Prefer taking out cloaked units before any detection is eliminated
    if (target.cloaked && target.matchups.threats.nonEmpty && target.matchups.enemyDetectors.forall(_.matchups.threats.nonEmpty) && target.matchups.nearestArbiter.isEmpty) {
      output *= 2.0
    }

    // Interceptor penalty
    if (target.isInterceptor()) {
      output *= 0.25
    }

    // Expansion bonus
    if (target.unitClass.isTownHall && target.base.forall(base => ! With.scouting.enemyMain.contains(base) && ! With.scouting.enemyNatural.contains(base))) {
      output *= 3.0
    }

    output
  }

  def aidsDetection(target: UnitInfo): Boolean = {
    if (target.unitClass.isDetector) {
      return true
    }
    if (target.isComsat() || target.isControlTower() || target.isRoboticsFacility() || target.isEngineeringBay() || target.isForge()) {
      return true
    }
    if ((target.isAcademy() || target.isScienceFacility() || target.isObservatory()) && ! target.complete) {
      return true
    }
    false
  }
}
