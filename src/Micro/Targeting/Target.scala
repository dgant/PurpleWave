package Micro.Targeting

import Lifecycle.With
import Mathematics.Maff
import Micro.Targeting.FiltersRequired.{TargetFilterAntiAir, TargetFilterCanAttack, TargetFilterEnemy, TargetFilterFocus, TargetFilterMissing, TargetFilterShuttleReaver, TargetFilterRush, TargetFilterScourge, TargetFilterStayCloaked, TargetFilterType, TargetFilterVsTank, TargetFilterVulture}
import Micro.Targeting.FiltersSituational.TargetFilterWhitelist
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.?

object Target {

  val defaultFilters: Vector[TargetFilter] = Vector(
    TargetFilterEnemy,
    TargetFilterCanAttack,
    TargetFilterMissing,
    TargetFilterType,
    TargetFilterFocus,
    TargetFilterStayCloaked,
    TargetFilterScourge,
    TargetFilterAntiAir,
    TargetFilterShuttleReaver,
    TargetFilterRush,
    TargetFilterVulture,
    TargetFilterVsTank)

  def choose(attacker: FriendlyUnitInfo, required: TargetFilter*): Option[UnitInfo] = {
    attacker.agent.toAttack = best(attacker, required: _*)
    attacker.agent.toAttack
  }

  private def hasCombatPriority(unit: UnitInfo): Boolean = {
    unit.unitClass.attacksOrCastsOrDetectsOrTransports
  }
  private def acceleratesDemise(attacker: UnitInfo, target: UnitInfo): Boolean = {
    target.doomFrameAbsolute > With.frame + attacker.framesToConnectDamage(target) + 24
  }

  def defaultTargets(attacker: FriendlyUnitInfo, filters: TargetFilter*): (Boolean, Seq[UnitInfo]) = {
    val assigned      = attacker.targetsAssigned.getOrElse(Seq.empty)
    val matchups      = attacker.matchups.targets
    val engaged       = attacker.team.exists(_.engagedUpon)
    val targetInRange = attacker.matchups.targetNearest.exists(t => hasCombatPriority(t) && attacker.inRangeToAttack(t))
    val allFilters    = filtersRequired(attacker) ++ filters
    val strataUnfilteredInRange = ?(
      targetInRange,
      Seq(
        (true, assigned.view.filter(hasCombatPriority).filter(attacker.inRangeToAttack).filter(acceleratesDemise(attacker, _))),
        (true, matchups.view.filter(hasCombatPriority).filter(attacker.inRangeToAttack).filter(acceleratesDemise(attacker, _))),
        (true, assigned.view.filter(hasCombatPriority).filter(attacker.inRangeToAttack)),
        (true, matchups.view.filter(hasCombatPriority).filter(attacker.inRangeToAttack))),
      Seq.empty)
    val strataUnfiltered = strataUnfilteredInRange ++ Seq(
      (true, ?(engaged, assigned.view.filter(hasCombatPriority).filter(acceleratesDemise(attacker, _)), Seq.empty)),
      (true, ?(engaged, assigned.view.filter(hasCombatPriority),                                        Seq.empty)),
      (false, assigned),
      (true, ?(engaged, matchups.view.filter(hasCombatPriority).filter(acceleratesDemise(attacker, _)), Seq.empty)),
      (true, ?(engaged, matchups.view.filter(hasCombatPriority),                                        Seq.empty)),
      (true, matchups))

    // Returns (rank by score?, targets)
    strataUnfiltered.view
      .map(p => (p._1, legalWithFilters(attacker, p._2, allFilters))).find(_._2.nonEmpty)
      .getOrElse((false, Seq.empty))
  }

  def best(attacker: FriendlyUnitInfo, filters: TargetFilter*): Option[UnitInfo] = {
    val assigned      = attacker.targetsAssigned.getOrElse(Seq.empty)
    val matchups      = attacker.matchups.targets
    val engaged       = attacker.team.exists(_.engagedUpon)
    val allFilters    = (filtersRequired(attacker) ++ filters).toVector
    val targetInRange = attacker.matchups.targetNearest.exists(t => hasCombatPriority(t) && attacker.inRangeToAttack(t))
    var output: Option[UnitInfo] = None
    if (targetInRange) {
      output = bestInStratum(attacker, assigned, allFilters, rankByScore = true,  requireCombatPriority = true,  requireInRange = true,  requireAcceleratesDemise = true)
      if (output.isDefined) return output
      output = bestInStratum(attacker, matchups, allFilters, rankByScore = true,  requireCombatPriority = true,  requireInRange = true,  requireAcceleratesDemise = true)
      if (output.isDefined) return output
      output = bestInStratum(attacker, assigned, allFilters, rankByScore = true,  requireCombatPriority = true,  requireInRange = true,  requireAcceleratesDemise = false)
      if (output.isDefined) return output
      output = bestInStratum(attacker, matchups, allFilters, rankByScore = true,  requireCombatPriority = true,  requireInRange = true,  requireAcceleratesDemise = false)
      if (output.isDefined) return output
    }
    if (engaged) {
      output = bestInStratum(attacker, assigned, allFilters, rankByScore = true,  requireCombatPriority = true,  requireInRange = false, requireAcceleratesDemise = true)
      if (output.isDefined) return output
      output = bestInStratum(attacker, assigned, allFilters, rankByScore = true,  requireCombatPriority = true,  requireInRange = false, requireAcceleratesDemise = false)
      if (output.isDefined) return output
    }
    output = bestInStratum(attacker, assigned, allFilters, rankByScore = false, requireCombatPriority = false, requireInRange = false, requireAcceleratesDemise = false)
    if (output.isDefined) return output
    if (engaged) {
      output = bestInStratum(attacker, matchups, allFilters, rankByScore = true, requireCombatPriority = true, requireInRange = false, requireAcceleratesDemise = true)
      if (output.isDefined) return output
      output = bestInStratum(attacker, matchups, allFilters, rankByScore = true, requireCombatPriority = true, requireInRange = false, requireAcceleratesDemise = false)
      if (output.isDefined) return output
    }
    bestInStratum(attacker, matchups, allFilters, rankByScore = true, requireCombatPriority = false, requireInRange = false, requireAcceleratesDemise = false)
  }

  private def bestInStratum(
    attacker: FriendlyUnitInfo,
    targets: Iterable[UnitInfo],
    allFilters: IndexedSeq[TargetFilter],
    rankByScore: Boolean,
    requireCombatPriority: Boolean,
    requireInRange: Boolean,
    requireAcceleratesDemise: Boolean): Option[UnitInfo] = {
    var best        : UnitInfo = null
    var bestScore   : Double   = 0.0
    var found       : Boolean  = false
    val targetIterator = targets.iterator
    while (targetIterator.hasNext) {
      val target = targetIterator.next()
      val legal = ( ! requireCombatPriority || hasCombatPriority(target)) && ( ! requireInRange || attacker.inRangeToAttack(target)) && ( ! requireAcceleratesDemise || acceleratesDemise(attacker, target)) && legalByFilters(attacker, target, allFilters)
      if (legal) {
        if ( ! rankByScore) return Some(target)
        val score = attacker.targetScore(target)
        if ( ! found || score > bestScore) {
          found = true
          best = target
          bestScore = score
        }
      }
    }
    ?(found, Some(best), None)
  }

  private def legalByFilters(attacker: FriendlyUnitInfo, target: UnitInfo, allFilters: IndexedSeq[TargetFilter]): Boolean = {
    if (With.yolo.active) {
      attacker.canAttack(target) && TargetFilterType.legal(attacker, target)
    } else {
      var filterIndex = 0
      while (filterIndex < allFilters.length) {
        if ( ! allFilters(filterIndex).legal(attacker, target)) return false
        filterIndex += 1
      }
      true
    }
  }

  def best(attacker: FriendlyUnitInfo, whitelist: Iterable[UnitInfo]): Option[UnitInfo] = {
    best(attacker, TargetFilterWhitelist(whitelist))
  }

  def bestUnfiltered(attacker: FriendlyUnitInfo, targets: Iterable[UnitInfo]): Option[UnitInfo] = {
    Maff.maxBy(targets)(attacker.targetScore)
  }

  def legal(attacker: FriendlyUnitInfo, targets: Seq[UnitInfo], filters: TargetFilter*): Seq[UnitInfo] = {
    legalWithFilters(attacker, targets, filtersRequired(attacker) ++ filters)
  }

  private def legalWithFilters(attacker: FriendlyUnitInfo, targets: Seq[UnitInfo], allFilters: Seq[TargetFilter]): Seq[UnitInfo] = {
    targets.view.filter(target => ?(With.yolo.active,
      attacker.canAttack(target) && TargetFilterType.legal(attacker, target),
      allFilters.forall(_.legal(attacker, target))))
  }

  def filtersRequired(attacker: FriendlyUnitInfo): Seq[TargetFilter] = {
    defaultFilters.view.filter(_.appliesTo(attacker))
  }

  def auditLegality(attacker: FriendlyUnitInfo, additionalFiltersRequired: TargetFilter*): Vector[(UnitInfo, Vector[(Boolean, TargetFilter)])] = {
    defaultTargets(attacker)._2
      .map(target => (
        target,
        (filtersRequired(attacker) ++ additionalFiltersRequired.filter(_.appliesTo(attacker)))
          .map(filter => (filter.legal(attacker, target), filter))
          .toVector
          .sortBy(_._1)
      ))
      .toVector
  }

  def auditScore(attacker: FriendlyUnitInfo): Vector[(UnitInfo, String, String, String)] = {
    defaultTargets(attacker)._2
      .map(target => (
        target,
        attacker.pixelDistanceEdge(target) * Maff.inv32,
        target.targetValue,
        attacker.targetScore(target)))
      .toVector
      .sortBy(-_._4)
      .map(s =>
        (s._1,
          s._2.toInt.toString,
          s._3.toInt.toString,
          s._4.toInt.toString))
  }
}
