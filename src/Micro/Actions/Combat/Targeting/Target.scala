package Micro.Actions.Combat.Targeting

import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Combat.Targeting.Filters.{TargetFilter, TargetFilterWhitelist}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object Target extends {
  def choose(attacker: FriendlyUnitInfo, required: TargetFilter*): Unit = {
    attacker.agent.toAttack = best(attacker, required: _*)
  }

  def choose(attacker: FriendlyUnitInfo, whitelist: Iterable[UnitInfo]): Unit = {
    choose(attacker, TargetFilterWhitelist(whitelist))
  }

  def chooseUnfiltered(attacker: FriendlyUnitInfo, targets: Iterable[UnitInfo]): Unit = {
    attacker.agent.toAttack = bestUnfiltered(attacker, targets)
  }

  def best(attacker: FriendlyUnitInfo, required: TargetFilter*): Option[UnitInfo] = {
    bestUnfiltered(attacker, preferred(attacker, required: _*))
  }

  def best(attacker: FriendlyUnitInfo, whitelist: Iterable[UnitInfo]): Option[UnitInfo] = {
    best(attacker, TargetFilterWhitelist(whitelist))
  }

  private def bestUnfiltered(attacker: FriendlyUnitInfo, targets: Iterable[UnitInfo]): Option[UnitInfo] = {
    ByOption.maxBy(targets)(score(attacker, _))
  }

  def legal(attacker: FriendlyUnitInfo, required: TargetFilter*): Seq[UnitInfo] = {
    val filters = filtersRequired(attacker) ++ required
    attacker.matchups.targets.view.filter(target => filters.forall(_.legal(attacker, target)))
  }

  def preferred(attacker: FriendlyUnitInfo, required: TargetFilter*): Iterable[UnitInfo] = {
    val filtersPreferred = TargetFilterGroups.filtersPreferred.view.filter(_.appliesTo(attacker))
    val legalTargets = legal(attacker, required: _*).toVector
    (0 to filtersPreferred.length)
      .map(i => legalTargets.view.filter(target => filtersPreferred.drop(i).forall(_.legal(attacker, target))))
      .find(_.nonEmpty)
      .getOrElse(legalTargets)
  }

  def filtersRequired(attacker: FriendlyUnitInfo): Seq[TargetFilter] = {
    TargetFilterGroups.filtersRequired.view ++ attacker.agent.intent.targetFilters
  }

  def auditLegality(attacker: FriendlyUnitInfo, additionalFiltersRequired: TargetFilter*): Vector[(UnitInfo, Vector[(Boolean, TargetFilter)])] = {
    attacker.matchups.targets
      .map(target => (
        target,
        (TargetFilterGroups.filtersRequired.view ++ additionalFiltersRequired ++ TargetFilterGroups.filtersPreferred)
          .map(filter => (filter.legal(attacker, target), filter))
          .toVector
          .sortBy(_._1)))
      .toVector
  }

  def auditScore(attacker: FriendlyUnitInfo): Seq[(UnitInfo, Double, Double)] = {
    attacker.matchups.targets.view.map(target => (target, target.targetBaseValue(), score(attacker, target))).toVector.sortBy(-_._3)
  }

  @inline final def score(attacker: FriendlyUnitInfo, target: UnitInfo): Double = {
    val framesOfFreedom = attacker.cooldownLeft
    val framesOutOfWayToShoot = if (attacker.canMove) Math.max(0, attacker.framesToGetInRange(target) - framesOfFreedom) else 0
    val output = baseAttackerToTargetValue(
      baseTargetValue = target.targetBaseValue(),
      totalHealth = target.totalHealth,
      framesOutOfTheWay = framesOutOfWayToShoot,
      dpf = attacker.dpfOnNextHitAgainst(target))
    output
  }

  // Used by combat simulation as well, to keep targeting behavior somewhat consistent
  // Thus the implementation needs to be FAST.
  @inline final def baseAttackerToTargetValue(
    baseTargetValue: Double,
    totalHealth: Double,
    framesOutOfTheWay: Double,
    dpf: Double = 1.0): Double = (
    baseTargetValue
    * dpf
    / Math.max(1.0, totalHealth)
    / Math.max(6.0, framesOutOfTheWay)
  )

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

    // Temporary visibility bonus
    val temporarilyVisible = (target.cloaked || target.burrowed) && target.matchups.enemyDetectors.forall(_.isScannerSweep())
    if (temporarilyVisible) {
      output *= 2.0
    }

    // Detection bonus
    val weHaveCloakedThreat = target.matchups.enemies.exists(e => e.cloaked && e.matchups.targets.nonEmpty) && ! target.matchups.enemies.exists(_.isArbiter())
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
