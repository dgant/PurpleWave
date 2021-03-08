package Micro.Actions.Combat.Targeting

import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Combat.Targeting.Filters.{TargetFilter, TargetFilterWhitelist}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object Target extends {
  def choose(attacker: FriendlyUnitInfo, required: TargetFilter*): Option[UnitInfo] = {
    attacker.agent.toAttack = best(attacker, required: _*)
    attacker.agent.toAttack
  }

  def choose(attacker: FriendlyUnitInfo, whitelist: Iterable[UnitInfo]): Option[UnitInfo] = {
    choose(attacker, TargetFilterWhitelist(whitelist))
  }

  def chooseUnfiltered(attacker: FriendlyUnitInfo, targets: Iterable[UnitInfo]): Option[UnitInfo] = {
    attacker.agent.toAttack = bestUnfiltered(attacker, targets)
    attacker.agent.toAttack
  }

  def best(attacker: FriendlyUnitInfo, filters: TargetFilter*): Option[UnitInfo] = {
    bestUnfiltered(attacker, legal(attacker, filters: _*))
  }

  def best(attacker: FriendlyUnitInfo, whitelist: Iterable[UnitInfo]): Option[UnitInfo] = {
    best(attacker, TargetFilterWhitelist(whitelist))
  }

  private def bestUnfiltered(attacker: FriendlyUnitInfo, targets: Iterable[UnitInfo]): Option[UnitInfo] = {
    ByOption.maxBy(targets)(score(attacker, _))
  }

  def legal(attacker: FriendlyUnitInfo, filters: TargetFilter*): Seq[UnitInfo] = {
    val allFilters = filtersRequired(attacker) ++ filters
    attacker.matchups.targets.view.filter(target => allFilters.forall(_.legal(attacker, target)))
  }

  def filtersRequired(attacker: FriendlyUnitInfo): Seq[TargetFilter] = {
    TargetFilterGroups.filtersRequired.view ++ attacker.agent.intent.targetFilters
  }

  def auditLegality(attacker: FriendlyUnitInfo, additionalFiltersRequired: TargetFilter*): Vector[(UnitInfo, Vector[(Boolean, TargetFilter)])] = {
    attacker.matchups.targets
      .map(target => (
        target,
        (TargetFilterGroups.filtersRequired.view ++ additionalFiltersRequired)
          .map(filter => (filter.legal(attacker, target), filter))
          .toVector
          .sortBy(_._1)))
      .toVector
  }

  def auditScore(attacker: FriendlyUnitInfo): Seq[(UnitInfo, Double, Double, Double)] = {
    attacker.matchups.targets.view.map(target => (target, attacker.pixelDistanceEdge(target) / 32d, target.targetBaseValue(), score(attacker, target))).toVector.sortBy(-_._3)
  }

  // TODO: Re-inline
  def score(attacker: FriendlyUnitInfo, target: UnitInfo): Double = {
    val framesOutOfWay = if (attacker.canMove) Math.max(0, attacker.framesToGetInRange(target) - attacker.cooldownLeft) else 0
    val scoreBasic = baseAttackerToTargetValue(
      baseTargetValue = target.targetBaseValue(),
      totalHealth = target.totalHealth,
      framesOutOfTheWay = framesOutOfWay,
      dpf = attacker.dpfOnNextHitAgainst(target))
    val preferences = TargetFilterGroups.filtersPreferred.view.filter(_.appliesTo(attacker)).count(_.legal(attacker, target))
    val preferenceBonus = Math.pow(100, preferences)
    val threatPenalty = if (With.reaction.sluggishness > 1) framesOutOfWay else {
      val firingPixel = attacker.pixelToFireAt(target)
      // TODO: Do it through argument to pixelToFireAt
      val firingPixelSafer = target.pixel.project(attacker.pixel, attacker.pixelRangeAgainst(target) + attacker.unitClass.dimensionMin + target.unitClass.dimensionMin)
      val firingPixelFrames = attacker.pixelDistanceCenter(firingPixel) / (0.01 + attacker.topSpeed)
      1 + attacker.matchups.threats.count(threat =>
        // In range to attack at the firing pixel?
        threat.inRangeToAttack(
          attacker,
          threat.pixel.projectUpTo(firingPixel, threat.topSpeed * firingPixelFrames),
          firingPixel)
          // In range to attack if we choose somewhere safer to shoot from?
          && threat.inRangeToAttack(
          attacker,
          threat.pixel.projectUpTo(firingPixelSafer, threat.topSpeed * firingPixelFrames),
          firingPixelSafer))
    }
    val output = scoreBasic * preferenceBonus / threatPenalty / threatPenalty
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
      output *= 1.1
    }

    // Construction bonus
    val constructing = target.constructing
    if (constructing) {
      output *= 1.5
    }

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
    val hurtingUsBonus = target.presumptiveTarget.exists(target.inRangeToAttack)
    if (hurtingUsBonus) {
      output *= 1.25
    }

    // Immobility bonus
    if ( ! target.canMove) {
      output *= 1.25
    }

    // Anti-air bonus
    val antiAirBonus = target.attacksAgainstAir > 0 && target.presumptiveTarget.exists(t => t.flying && t.attacksAgainstGround > 0)
    if (antiAirBonus) {
      output *= 2.0
    }
    if (target.isAny(Terran.Armory, Protoss.CyberneticsCore, Zerg.Spire, Zerg.HydraliskDen)
      && With.units.existsOurs(Terran.Battlecruiser, Terran.Wraith, Terran.ScienceVessel, Protoss.Carrier, Protoss.Scout, Zerg.Mutalisk, Zerg.Guardian)) {
      output *= 2.5
    }

    // Visibility bonus
    if (target.visible) {
      output *= 2.0
    }
    if (target.likelyStillThere) {
      output *= 1.25
    }

    // Temporary visibility bonus
    val temporarilyVisible = (
      (target.cloaked || target.burrowed)
      && With.self.isTerran
      && With.units.existsOurs(Terran.SpellScannerSweep))
    if (temporarilyVisible) {
      output *= 2.0
    }

    // Detection bonus
    val aggressivelyDenyDetection = With.reaction.sluggishness > 0
    val weHaveCloakedThreat = if (aggressivelyDenyDetection) target.matchups.enemies.exists(e => e.cloaked && e.matchups.targets.nonEmpty) && ! target.matchups.enemies.exists(Protoss.Arbiter)
    else (
      With.self.hasTech(Terran.WraithCloak)
      || With.self.hasTech(Zerg.LurkerMorph)
      || With.units.existsOurs(Protoss.Arbiter)
      || With.units.existsOurs(Protoss.DarkTemplar))
    if (weHaveCloakedThreat) {
      val building = target.constructing || target.repairing
      val buildTarget = if (building) target.target else None
      val detects = aidsDetection(target) || buildTarget.exists(aidsDetection)
      if (detects) {
        if (aggressivelyDenyDetection) {
          output *= 40.0
        } else {
          output *= 4.0
        }
      }
    }

    // Cloaked bonus
    // Prefer taking out cloaked units before any detection is eliminated
    if (target.cloaked && ! With.units.existsEnemy(Protoss.Arbiter)) {
      output *= 2.0
    }

    // Interceptor penalty
    if (target.is(Protoss.Interceptor)) {
      output *= 0.25
    }

    // Expansion bonus
    if (target.unitClass.isTownHall && target.base.forall(base => ! With.scouting.enemyMain.contains(base) && ! With.scouting.enemyNatural.contains(base))) {
      output *= 3.0
    }

    // Repair bonus
    val repairValue = target.orderTarget.map(targetOrderTarget =>
      if (target.repairing && ! targetOrderTarget.repairing && recur)
        1.1 * getTargetBaseValue(targetOrderTarget, recur = false)
      else
        0.0).getOrElse(0.0)
    output = Math.max(output, repairValue)

    output
  }

  def aidsDetection(target: UnitInfo): Boolean = {
    if (target.unitClass.isDetector) {
      return true
    }
    if (target.isAny(Terran.Comsat, Terran.ControlTower, Protoss.RoboticsFacility, Terran.EngineeringBay, Protoss.Forge)) {
      return true
    }
    if (target.isAny(Terran.Academy, Terran.ScienceFacility, Protoss.Observatory) && ! target.complete) {
      return true
    }
    false
  }
}
