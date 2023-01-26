package Micro.Targeting

import Lifecycle.With
import Mathematics.Maff
import Micro.Targeting.FiltersSituational.TargetFilterWhitelist
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Target {
  def choose(attacker: FriendlyUnitInfo, required: TargetFilter*): Option[UnitInfo] = {
    attacker.agent.toAttack = best(attacker, required: _*)
    attacker.agent.toAttack.foreach(_.addTargeter(attacker))
    attacker.agent.toAttack
  }

  private def hasCombatPriority(unit: UnitInfo): Boolean = {
    unit.unitClass.attacksOrCastsOrDetectsOrTransports
  }
  private def acceleratesDemise(attacker: UnitInfo, target: UnitInfo): Boolean = {
    target.doomFrameAbsolute > With.frame + attacker.framesToConnectDamage(target) + 24
  }

  def best(attacker: FriendlyUnitInfo, filters: TargetFilter*): Option[UnitInfo] = {
    val assigned  = attacker.targetsAssigned.getOrElse(Seq.empty)
    val matchups  = attacker.matchups.targets
    val engaged   = attacker.team.exists(_.engagedUpon)
    val strataUnfiltered = Seq(
      (true, assigned.view.filter(attacker.inRangeToAttack).filter(hasCombatPriority).filter(acceleratesDemise(attacker, _))),
      (true, matchups.view.filter(attacker.inRangeToAttack).filter(hasCombatPriority).filter(acceleratesDemise(attacker, _))),
      (true, assigned.view.filter(attacker.inRangeToAttack).filter(hasCombatPriority)),
      (true, matchups.view.filter(attacker.inRangeToAttack).filter(hasCombatPriority)),
      (true, (if (engaged) assigned.view.filter(hasCombatPriority).filter(acceleratesDemise(attacker, _)) else Seq.empty)),
      (true, (if (engaged) assigned.view.filter(hasCombatPriority) else Seq.empty)),
      (false, assigned),
      (true, (if (engaged) matchups.view.filter(hasCombatPriority).filter(acceleratesDemise(attacker, _)) else Seq.empty)),
      (true, (if (engaged) matchups.view.filter(hasCombatPriority) else Seq.empty)),
      (true, matchups))
    val stratum = strataUnfiltered.view.map(p => (p._1, legal(attacker, p._2, filters: _*))).find(_._2.nonEmpty)
    val output = stratum.flatMap(p => if (p._1) bestUnfiltered(attacker, p._2) else p._2.headOption)
    output
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
    TargetFilterGroups.filtersRequired.view.filter(_.appliesTo(attacker))
  }

  def auditLegality(attacker: FriendlyUnitInfo, additionalFiltersRequired: TargetFilter*): Vector[(UnitInfo, Vector[(Boolean, TargetFilter)])] = {
    attacker.matchups.targets
      .map(target => (
        target,
        (filtersRequired(attacker) ++ additionalFiltersRequired.filter(_.appliesTo(attacker)))
          .map(filter => (filter.legal(attacker, target), filter)).toVector.sortBy(_._1)
      ))
      .toVector
  }

  def auditScore(attacker: FriendlyUnitInfo): Seq[(UnitInfo, Double, Double, Double)] = {
    attacker.matchups.targets.view.map(target => (target, attacker.pixelDistanceEdge(target) / 32d, target.targetValue, attacker.targetScore(target))).toVector.sortBy(-_._3)
  }
}
