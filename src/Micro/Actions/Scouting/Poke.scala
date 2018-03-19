package Micro.Actions.Scouting

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Attacking.Filters.TargetFilterWhitelist
import Micro.Actions.Combat.Attacking.TargetAction
import Micro.Actions.Combat.Techniques.Avoid
import Micro.Actions.Commands.{Attack, Move}
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Poke extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    With.intelligence.enemyMain.isDefined
    && unit.canAttack
    && unit.totalHealth > 15
    && ! With.enemy.isZerg // Save our strength for blocking Hatcheries
    && unit.matchups.targets.exists(_.unitClass.isWorker)
    && unit.matchups.threats.forall(_.unitClass.isWorker)
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val targets       = unit.matchups.targets.filter(_.unitClass.isWorker)
    val target        = targets.minBy(_.pixelDistanceCenter(unit.zone.exit.map(_.pixelCenter).getOrElse(unit.pixelCenter)))
    val exit          = unit.zone.exit.map(_.pixelCenter).getOrElse(With.geography.home.pixelCenter)
    val otherThreats  = targets.filter(t => t != target && t.pixelDistanceCenter(exit) <= unit.pixelDistanceCenter(exit))
    val targetAction  = new TargetAction(TargetFilterWhitelist(targets))
    val framesAway    = targets.map(unit.framesToGetInRange).min
    val framesToWait  = unit.framesToBeReadyForAttackOrder
    
    if (otherThreats.exists(_.pixelDistanceEdge(unit) < 32)) {
      val mineral = With.geography.ourBases.headOption.flatMap(_.minerals.headOption)
      mineral.foreach(With.commander.gather(unit, _))
      unit.agent.toTravel = Some(With.geography.home.pixelCenter)
      Move.delegate(unit)
    }
    
    lazy val targetDistance = unit.pixelRangeAgainst(target) + target.unitClass.radialHypotenuse + unit.unitClass.radialHypotenuse
    lazy val targetPosition = target.pixelCenter.project(target.zone.exit.map(_.pixelCenter).getOrElse(unit.pixelCenter), targetDistance)
    unit.agent.toAttack = Some(target)
    unit.agent.toTravel = Some(targetPosition)
    if ( ! unit.is(Terran.SCV) && framesToWait > 0) {
      if (framesToWait > framesAway && ! target.gathering) {
        Avoid.delegate(unit)
      }
      else {
        Move.delegate(unit)
      }
    }
    Attack.delegate(unit)
  }
}
