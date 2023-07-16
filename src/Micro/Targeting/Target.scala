package Micro.Targeting

import Lifecycle.With
import Mathematics.Maff
import Micro.Targeting.FiltersRequired.{TargetFilterCanAttack, TargetFilterEnemy, TargetFilterFocus, TargetFilterMissing, TargetFilterReaver, TargetFilterRush, TargetFilterScourge, TargetFilterStayCloaked, TargetFilterType, TargetFilterVsTank, TargetFilterVulture}
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
    TargetFilterReaver,
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
      .map(p => (p._1, legal(attacker, p._2, filters: _*))).find(_._2.nonEmpty)
      .getOrElse((false, Seq.empty))
  }

  def best(attacker: FriendlyUnitInfo, filters: TargetFilter*): Option[UnitInfo] = {
    val targets = defaultTargets(attacker, filters: _*)
    ?(targets._1,
      bestUnfiltered(attacker, legal(attacker, targets._2, filters: _*)),
      targets._2.headOption)
  }

  def best(attacker: FriendlyUnitInfo, whitelist: Iterable[UnitInfo]): Option[UnitInfo] = {
    best(attacker, TargetFilterWhitelist(whitelist))
  }

  def bestUnfiltered(attacker: FriendlyUnitInfo, targets: Iterable[UnitInfo]): Option[UnitInfo] = {
    Maff.maxBy(targets)(attacker.targetScore)
  }

  def legal(attacker: FriendlyUnitInfo, targets: Seq[UnitInfo], filters: TargetFilter*): Seq[UnitInfo] = {
    val allFilters = filtersRequired(attacker) ++ filters
    targets.view.filter(target => With.yolo.active || allFilters.forall(_.legal(attacker, target)))
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
