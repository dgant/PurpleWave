package Micro.Actions.Scouting

import Lifecycle.With
import Mathematics.Maff
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Agency.Commander
import Utilities.UnitFilters.IsWorker
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Poke extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    With.scouting.enemyMain.isDefined
    && unit.canAttack
    && unit.totalHealth > 39
    && unit.matchups.targets.exists(IsWorker)
    && unit.matchups.threats.forall(IsWorker)
    && ( // Save our strength for blocking Hatcheries
      ! With.enemy.isZerg
      || unit.matchups.threats.size <= 5
      || unit.totalHealth > 25))
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val targets = unit.matchups.targets.filter(IsWorker)
    if (targets.isEmpty) return

    val targetHeart = unit.base.map(_.heart.center).getOrElse(unit.zone.centroid)
    val target = targets.minBy(target =>
      target.pixelDistanceEdge(unit)
      - Maff.min(unit.matchups.threats.filter(_ != target).map(_.pixelDistanceEdge(target))).getOrElse(0.0))

    val exit          = unit.zone.exitOriginal.map(_.pixelCenter).getOrElse(With.geography.home.center)
    val otherThreats  = targets.filter(t => t != target && t.pixelDistanceCenter(exit) <= unit.pixelDistanceCenter(exit))
    val framesAway    = targets.map(unit.framesToGetInRange).min
    val framesToWait  = unit.framesToBeReadyForAttackOrder
    
    if (otherThreats.exists(_.pixelDistanceEdge(unit) < 32)) {
      unit.agent.toGather = With.geography.ourBases.headOption.flatMap(_.minerals.headOption)
      unit.agent.decision.set(With.geography.home.center)
      Commander.gather(unit)
      Commander.move(unit)
    }
    
    lazy val targetDistance = unit.pixelRangeAgainst(target) + target.unitClass.radialHypotenuse + unit.unitClass.radialHypotenuse
    lazy val targetPosition = target.pixel.project(target.zone.exitOriginal.map(_.pixelCenter).getOrElse(unit.pixel), targetDistance)
    unit.agent.toAttack = Some(target)
    unit.agent.decision.set(targetPosition)
    if ( ! unit.is(Terran.SCV) && framesToWait > 0) {
      if (framesToWait > framesAway && ! target.gathering) {
        Retreat.delegate(unit)
      }
      Commander.move(unit)
    }
    Commander.attack(unit)
  }
}
