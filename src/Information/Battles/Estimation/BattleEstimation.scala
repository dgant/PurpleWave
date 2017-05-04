package Information.Battles.Estimation

import Information.Battles.BattleTypes.Battle
import Information.Battles.TacticsTypes.{TacticsDefault, TacticsOptions}
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class BattleEstimation(
  val tacticsUs     : TacticsOptions = TacticsDefault.get,
  val tacticsEnemy  : TacticsOptions = TacticsDefault.get)
  extends BattleEstimationCalculator {
  
  private val unitsOurs = new mutable.HashMap[UnitInfo, BattleEstimationUnit]
  private val unitsEnemy = new mutable.HashMap[UnitInfo, BattleEstimationUnit]
  
  val avatarUs = new BattleEstimationUnit
  val avatarEnemy = new BattleEstimationUnit
  
  def addUnits(battle: Battle) {
    battle.us.units.foreach(addUnit)
    battle.enemy.units.foreach(addUnit)
  }
  
  def addUnit(unit: UnitInfo) {
    if (unit.isFriendly) {
      unitsOurs.put(unit, new BattleEstimationUnit(unit, tacticsUs))
      avatarUs.add(unitsOurs(unit))
    }
    else {
      unitsEnemy.put(unit, new BattleEstimationUnit(unit, tacticsEnemy))
      avatarEnemy.add(unitsEnemy(unit))
    }
  }
  
  def removeUnit(unit: UnitInfo) {
    if (unit.isFriendly) {
      avatarUs.remove(unitsOurs(unit))
      unitsOurs.remove(unit)
    }
    else {
      avatarEnemy.remove(unitsEnemy(unit))
      unitsEnemy.remove(unit)
    }
  }
  
  def updateUnit(unit: UnitInfo) {
    removeUnit(unit)
    addUnit(unit)
  }
}
