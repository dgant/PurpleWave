package Information.Battles.Estimation

import Information.Battles.BattleTypes.Battle
import Information.Battles.TacticsTypes.{TacticsDefault, TacticsOptions}
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class BattleEstimation(
  val tacticsUs     : TacticsOptions = TacticsDefault.get,
  val tacticsEnemy  : TacticsOptions = TacticsDefault.get)
  extends BattleEstimationCalculator {
  
  def addUnits(battle:Battle) {
    battle.us.units.foreach(addUnit)
    battle.enemy.units.foreach(addUnit)
  }
  
  def addUnit(unit:UnitInfo) {
    units.put(unit, new BattleEstimationUnit(unit))
    avatar(unit).add(units(unit))
  }
  
  def removeUnit(unit:UnitInfo) {
    avatar(unit).remove(units(unit))
    units.remove(unit)
  }
  
  def updateUnit(unit:UnitInfo) {
    removeUnit(unit)
    addUnit(unit)
  }
  
  private def avatar(unit:UnitInfo):BattleEstimationUnit = {
    if (unit.isFriendly) avatarUs else avatarEnemy
  }
  
  private val units = new mutable.HashMap[UnitInfo, BattleEstimationUnit]
  val avatarUs    = new BattleEstimationUnit
  val avatarEnemy = new BattleEstimationUnit
}
