package Micro.Actions.Combat.Spells

import Lifecycle.With
import Micro.Actions.Action
import Micro.Heuristics.Spells.TargetSingle
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Yamato extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.is(Terran.Battlecruiser)            &&
    unit.energy >= Terran.Yamato.energyCost  &&
    With.self.hasTech(Terran.Yamato)         &&
    unit.matchups.enemies.nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
  
    def valueTarget(target: UnitInfo): Double = {
      val baseValue =
        if ( ! target.isEnemy)
          0.0
        else if (target.unitClass.gasPrice >= 100)
          1.0
        else if (target.canAttack(unit) && target.pixelRangeAgainstFromCenter(unit) > unit.pixelRangeAgainstFromCenter(target))
          1.0
        else
          0.0
      
      val yamatoDamage    = 260.0
      val castTime        = 72.0
      lazy val presumedHp = target.totalHealth - target.matchups.dpfReceivingDiffused * castTime
      lazy val utility    = target.matchups.valuePerDamage * Math.min(yamatoDamage, presumedHp)
      
      val output = baseValue * utility
      output
    }
    
    val target = TargetSingle.chooseTarget(
      unit,
      32.0 * 14.0,
      0.0,
      valueTarget)
    
    target.foreach(With.commander.useTechOnUnit(unit, Terran.Yamato, _))
  }
  
  
}
