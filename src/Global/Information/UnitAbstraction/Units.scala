package Global.Information.UnitAbstraction

import Startup.With
import Types.UnitInfo.{EnemyUnitInfo, FriendlyUnitInfo, UnitInfo}

class Units {
  
  val friendlyUnits = new FriendlyUnitTracker
  val enemyUnits = new EnemyUnitTracker
  
  def all:Iterable[UnitInfo] = {
    friendlyUnits.units ++ enemyUnits.units
  }
  
  def ours:Iterable[FriendlyUnitInfo] = {
    friendlyUnits.units.filter(_.player == With.game.self)
  }
  
  def enemy:Iterable[EnemyUnitInfo] = {
    enemyUnits.units
  }
  
  def ally:Iterable[FriendlyUnitInfo] = {
    friendlyUnits.units.filter(_.player != With.game.self)
  }
  
  def onFrame() {
    friendlyUnits.onFrame()
    enemyUnits.onFrame()
  }
  
  def onUnitDestroy(unit: bwapi.Unit) {
    friendlyUnits.onUnitDestroy(unit)
    enemyUnits.onUnitDestroy(unit)
  }
}
