package Global.Information

import Startup.With
import bwapi.{Player, UnitType}

import scala.collection.JavaConverters._
import scala.collection.mutable

class History {
  
  val _destroyedUnitsByPlayer = new mutable.HashMap[Player, mutable.HashMap[UnitType, Int]] {
    override def default(key:Player) = { _newUnitMap }
  }
  
  def onUnitDestroy(unit:bwapi.Unit) {
    _destroyedUnitsByPlayer(unit.getPlayer)(unit.getType) += 1
  }
  
  def destroyedEnemyUnits:mutable.HashMap[UnitType, Int] = {
    val output = _newUnitMap
    With.game.enemies.asScala.map(_destroyedUnitsByPlayer(_))
      .foreach(map =>
        map.keys.foreach(unitType =>
          output(unitType) += map(unitType)))
    output
  }
  
  def _newUnitMap:mutable.HashMap[UnitType, Int] = {
    new mutable.HashMap[UnitType, Int] {
      override def default(key: UnitType) = 0
    }
  }
}
