package Micro.Actions.Combat.Spells

import ProxyBwapi.Races.Protoss
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

object Stasis extends TargetedSpell {
  
  override protected def casterClass    : UnitClass = Protoss.Arbiter
  override protected def tech           : Tech      = Protoss.Stasis
  override protected def aoe            : Boolean   = true
  override protected def castRangeTiles : Int       = 9
  override protected def thresholdValue : Double    = casterClass.subjectiveValue
  
  override protected def valueTarget(target: UnitInfo): Double = {
    if (target.unitClass.isBuilding) return 0.0
    if (target.underStorm) return 0.0
    if (target.invincible) return 0.0

    val teamValue = (
      Math.min(1.0, target.matchups.targets.size / 3.0)
      * (if (target.battle.exists(_.teamOf(target).centroid.base.exists(_.owner.isEnemy))
        && ! target.unitClass.isWorker
        && target.matchups.targetsInRange.isEmpty) 0.3 else 1.0)
      * (if (target.isFriendly)
          -2.0
        else if (target.isEnemy) (
          1.0
          + (if (target.unitClass.isDetector) 2.0 else 0.0)
          + (if (target.isSiegeTankSieged()) 0.5 else if (target.isSiegeTankUnsieged()) 0.25 else 0.0)
        )
      else 0.0)
      )
    
    val output = target.subjectiveValue * teamValue
    output
  }
}
