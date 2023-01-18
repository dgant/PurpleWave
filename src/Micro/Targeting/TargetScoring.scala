package Micro.Targeting

import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{CombatUnit, FriendlyUnitInfo, UnitInfo}
import Utilities.?
import Utilities.UnitFilters.IsTownHall

object TargetScoring {

  @inline def framesOutOfTheWay(attacker: CombatUnit, target: CombatUnit): Double = {
    ?(attacker.canMove, Math.max(0, attacker.framesToGetInRange(target) - attacker.cooldownLeft), 0)
  }

  // Used by combat simulation as well, to keep targeting behavior somewhat consistent
  // Thus the implementation needs to be FAST.
  @inline def baseAttackerToTargetValue(attacker: CombatUnit, target: CombatUnit): Double = baseAttackerToTargetValueRaw(
    baseTargetValue   = target.unitClass.subjectiveValue,
    totalHealth       = target.totalHealth,
    framesOutOfTheWay = framesOutOfTheWay(attacker, target),
    dpf               = 1.0)

  @inline def baseAttackerToTargetValueRaw(
      baseTargetValue   : Double,
      totalHealth       : Double,
      framesOutOfTheWay : Double,
      dpf               : Double = 1.0): Double = (
    baseTargetValue * dpf / (Math.max(1.0, totalHealth) * Math.max(6.0, framesOutOfTheWay)))

  @inline def score(attacker: FriendlyUnitInfo, target: UnitInfo): Double = {
    val _framesOutOfWay = framesOutOfTheWay(attacker, target)
    val scoreBasic      = baseAttackerToTargetValueRaw(
      baseTargetValue   = target.targetBaseValue(),
      totalHealth       = target.totalHealth,
      framesOutOfTheWay = _framesOutOfWay,
      dpf               = attacker.dpfOnNextHitAgainst(target))
    val preferences     = TargetFilterGroups.filtersPreferred.view.filter(_.appliesTo(attacker)).count(_.legal(attacker, target))
    val preferenceBonus = Math.pow(100, preferences)
    val threatPenalty   = if (With.reaction.sluggishness > 1) _framesOutOfWay else {
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
    var splashBonus = 1.0
    if (attacker.isAny(Terran.SiegeTankSieged, Protoss.Reaver, Zerg.Lurker)) {
      splashBonus = Math.max(splashBonus, target.tile.adjacent9.iterator.filter(_.valid).map(_.units.count(u => attacker.canAttack(u) && u.isEnemy)).sum)
    }
    val output = splashBonus * scoreBasic * preferenceBonus / threatPenalty / threatPenalty
    output
  }

  def getTargetBaseValue(target: UnitInfo): Double = {
    var output = target.subjectiveValue

    // Injury bonus
    output *= Math.max(0.1, target.injury())

    // Immobility bonus
    output *= mod(1.5, target.constructing)
    output *= mod(1.25, ! target.canMove)
    output *= mod(1.1, target.gathering)

    // Combat bonus
    if (target.unitClass.attacksOrCastsOrDetectsOrTransports) {
      output *= 2.0
      if (target.complete) {
        output *= 2.0
      } else {
        // Prefer nearly-complete buildings
        val maxBuildFrames = target.unitClass.buildFrames
        output *= (1.0 + Maff.clamp((maxBuildFrames - target.remainingCompletionFrames) / maxBuildFrames, 0, 1.0))
      }
    }

    // Immediate combat value bonus
    output *= mod(1.25, target.presumptiveTarget.exists(target.inRangeToAttack))

    // Anti-air bonus
    output *= mod(2.0, target.presumptiveTarget.exists(t => t.isFriendly && t.flying && t.attacksAgainstGround > 0))

    // Visibility bonus (Goose chase penalty)
    output *= mod(4.0, target.visible)
    output *= mod(4.0, target.likelyStillThere)

    // Scanned bonus
    output *= mod(2.0, With.self.isTerran && (target.cloaked || target.burrowed) && With.units.existsOurs(Terran.SpellScannerSweep))

    // Detection bonus
    val aggressivelyDenyDetection = With.reaction.sluggishness > 0
    val weHaveCloakedThreat = if (aggressivelyDenyDetection) target.matchups.isCloakedAttacker && target.matchups.groupOf.arbiters.nonEmpty
    else (
      With.self.hasTech(Terran.WraithCloak)
        || With.self.hasTech(Zerg.LurkerMorph)
        || With.units.existsOurs(Protoss.Arbiter, Protoss.DarkTemplar))
    if (weHaveCloakedThreat) {
      val building = target.constructing || target.repairing
      val buildTarget = if (building) target.target else None
      val detects = aidsDetection(target) || buildTarget.exists(aidsDetection)
      if (detects) {
        output *= 12.0
      }
    }

    // Cloaked bonus: Prefer taking out cloaked units before any detection is eliminated
    output *= mod(2.0, target.cloaked && ! With.units.existsEnemy(Protoss.Arbiter))

    // Interceptor penalty
    output *= mod(0.1, Protoss.Interceptor(target))

    // Expansion bonus
    output *= mod(3.0, IsTownHall(target) && target.base.forall(base => ! With.scouting.enemyMain.contains(base) && ! With.scouting.enemyNatural.contains(base)))

    // Repair bonus
    output = Math.max(output, target.orderTarget.filter(target.repairing && ! _.repairing).map(1.1 * _.targetBaseValue()).getOrElse(0.0))

    output
  }

  def aidsDetection(target: UnitInfo): Boolean = {
    var output = false
    output ||= target.unitClass.isDetector
    output ||= Terran.Comsat(target)
    output ||= Protoss.RoboticsFacility(target) && ! With.units.existsEnemy(Protoss.Observer)
    output ||= Protoss.Forge(target)            && ! With.units.existsEnemy(Protoss.PhotonCannon)
    output ||= Terran.EngineeringBay(target)    && ! With.units.existsEnemy(Terran.MissileTurret)
    output ||= Terran.Academy(target)           && ! With.units.existsEnemy(Terran.Comsat)
    output
  }

  @inline private def mod(value: Double, predicate: Boolean): Double = {
    ?(predicate, value, 1.0)
  }
}
