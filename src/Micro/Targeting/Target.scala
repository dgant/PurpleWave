package Micro.Targeting

import Lifecycle.With
import Mathematics.Maff
import Micro.Targeting.FiltersSituational.TargetFilterWhitelist
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{CombatUnit, FriendlyUnitInfo, UnitInfo}


object Target extends {
  def choose(attacker: FriendlyUnitInfo, required: TargetFilter*): Option[UnitInfo] = {
    attacker.agent.toAttack = best(attacker, required: _*)
    attacker.agent.toAttack
  }

  def best(attacker: FriendlyUnitInfo, filters: TargetFilter*): Option[UnitInfo] = {
    val squadQueueRaw = attacker.squad.flatMap(_.targetQueue)

    // If we have no squad guidance at all, uwse default targeting
    if (squadQueueRaw.isEmpty) return bestUnfiltered(attacker, legal(attacker, filters: _*))

    val squadQueue = legal(attacker, squadQueueRaw.get, filters: _*)
    lazy val combatTargetInRangeSquad = squadQueue.find(t => t.unitClass.attacksOrCastsOrDetectsOrTransports && attacker.inRangeToAttack(t))
    lazy val combatTargetInRangeAny = bestUnfiltered(attacker, legal(attacker, attacker.matchups.targetsInRange.filter(_.unitClass.attacksOrCastsOrDetectsOrTransports), filters: _*))
    val output = combatTargetInRangeSquad
      .orElse(combatTargetInRangeAny)
      .orElse(squadQueue.find(t => t.doomFrameAbsolute > With.frame + attacker.framesToConnectDamage(t) + 24)) // The +delta is a buffer to avoid being too greedy about hastening a unit's death
      .orElse(squadQueue.headOption)
    output
  }

  def best(attacker: FriendlyUnitInfo, whitelist: Iterable[UnitInfo]): Option[UnitInfo] = {
    best(attacker, TargetFilterWhitelist(whitelist))
  }

  def bestUnfiltered(attacker: FriendlyUnitInfo, targets: Iterable[UnitInfo]): Option[UnitInfo] = {
    Maff.maxBy(targets)(score(attacker, _))
  }

  def legal(attacker: FriendlyUnitInfo, filters: TargetFilter*): Seq[UnitInfo] = {
    legal(attacker, attacker.matchups.targets, filters: _*)
  }

  def legal(attacker: FriendlyUnitInfo, targets: Seq[UnitInfo], filters: TargetFilter*): Seq[UnitInfo] = {
    val allFilters = filtersRequired(attacker) ++ filters
    targets.view.filter(target => With.yolo.active() || allFilters.forall(_.legal(attacker, target)))
  }

  def filtersRequired(attacker: FriendlyUnitInfo): Seq[TargetFilter] = {
    TargetFilterGroups.filtersRequired.view.filter(_.appliesTo(attacker))
  }

  def auditLegality(attacker: FriendlyUnitInfo, additionalFiltersRequired: TargetFilter*): Vector[(UnitInfo, Vector[(Boolean, TargetFilter)], Vector[(Boolean, TargetFilter)])] = {
    attacker.matchups.targets
      .map(target => (
        target,
        (filtersRequired(attacker) ++ additionalFiltersRequired.filter(_.appliesTo(attacker)))
          .map(filter => (filter.legal(attacker, target), filter)).toVector.sortBy(_._1),
        TargetFilterGroups.filtersPreferred.filter(_.appliesTo(attacker))
          .map(filter => (filter.legal(attacker, target), filter)).toVector.sortBy(_._1)
      ))
      .toVector
  }

  def auditScore(attacker: FriendlyUnitInfo): Seq[(UnitInfo, Double, Double, Double)] = {
    attacker.matchups.targets.view.map(target => (target, attacker.pixelDistanceEdge(target) / 32d, target.targetBaseValue(), score(attacker, target))).toVector.sortBy(-_._3)
  }

  @inline def framesOutOfTheWay(attacker: CombatUnit, target: CombatUnit): Double = {
    if (attacker.canMove) Math.max(0, attacker.framesToGetInRange(target) - attacker.cooldownLeft) else 0
  }

  // Used by combat simulation as well, to keep targeting behavior somewhat consistent
  // Thus the implementation needs to be FAST.
  @inline final def baseAttackerToTargetValue(attacker: CombatUnit, target: CombatUnit): Double = baseAttackerToTargetValueRaw(
    baseTargetValue = target.unitClass.subjectiveValue,
    totalHealth = target.totalHealth,
    framesOutOfTheWay = framesOutOfTheWay(attacker, target),
    dpf = 1.0)
  @inline final def baseAttackerToTargetValueRaw(
    baseTargetValue: Double,
    totalHealth: Double,
    framesOutOfTheWay: Double,
    dpf: Double = 1.0): Double = (
    baseTargetValue
    * dpf
    / (
      Math.max(1.0, totalHealth)
      * Math.max(6.0, framesOutOfTheWay)))

  // TODO: Re-inline
  def score(attacker: FriendlyUnitInfo, target: UnitInfo): Double = {
    val _framesOutOfWay = framesOutOfTheWay(attacker, target)
    val scoreBasic = baseAttackerToTargetValueRaw(
      baseTargetValue = target.targetBaseValue(),
      totalHealth = target.totalHealth,
      framesOutOfTheWay = _framesOutOfWay,
      dpf = attacker.dpfOnNextHitAgainst(target))
    val preferences = TargetFilterGroups.filtersPreferred.view.filter(_.appliesTo(attacker)).count(_.legal(attacker, target))
    val preferenceBonus = Math.pow(100, preferences)
    val threatPenalty = if (With.reaction.sluggishness > 1) _framesOutOfWay else {
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

  def injury(target: UnitInfo): Double = (target.unitClass.maxTotalHealth - target.totalHealth) / target.unitClass.maxTotalHealth

  def getTargetBaseValue(target: UnitInfo, recur: Boolean = true): Double = {
    var output = target.subjectiveValue

    // Injury bonus
    output /= Math.max(0.1, injury(target))

    // Immobility bonus
    if (target.constructing || target.repairing) {
      output *= 1.5
    } else if ( ! target.canMove) {
      output *= 1.25
    } else if (target.gathering) {
      output *= 1.1
    }

    // Combat bonus
    if (target.unitClass.attacksOrCastsOrDetectsOrTransports) {
      output *= 2.0
      if (target.complete) {
        output *= 2.0
      } else {
        // Prefer nearly-complete buildings
        val buildFrames = target.unitClass.buildFrames
        output *= (1.0 + Maff.clamp((buildFrames - target.remainingCompletionFrames) / buildFrames, 0, 1.0))
      }
    }

    // Immediate combat value bonus
    val hurtingUsBonus = target.presumptiveTarget.exists(target.inRangeToAttack)
    if (hurtingUsBonus) {
      output *= 1.25
    }

    // Anti-air bonus
    val antiAirBonus = target.attacksAgainstAir > 0 && target.presumptiveTarget.exists(t => t.flying && t.attacksAgainstGround > 0)
    if (antiAirBonus) {
      output *= 2.0
    }

    // Visibility bonus
    if (target.visible) {
      output *= 4.0
    }
    if (target.likelyStillThere) {
      output *= 4.0
    }

    // Temporary visibility bonus
    val temporarilyVisible = (target.cloaked || target.burrowed) && With.self.isTerran && With.units.existsOurs(Terran.SpellScannerSweep)
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
