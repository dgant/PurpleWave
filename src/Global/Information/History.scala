package Global.Information

import Startup.With
import Utilities.CountMap
import bwapi.{Player, UnitType}

import scala.collection.JavaConverters._
import scala.collection.mutable

class History {
  
  val _destroyedUnitsByPlayer = new mutable.HashMap[Player, CountMap[UnitType]] {
    override def default(key:Player) = { put(key, new CountMap[UnitType]); this(key) }
  }
  
  def onUnitDestroy(unit:bwapi.Unit) {
    _destroyedUnitsByPlayer(unit.getPlayer)(unit.getType) += 1
  }
  
  def destroyedEnemyUnits:CountMap[UnitType] = {
    val output = new CountMap[UnitType]
    With.game.enemies.asScala.map(_destroyedUnitsByPlayer(_))
      .foreach(map =>
        map.keys.foreach(unitType =>
          output(unitType) += map(unitType)))
    output
  }
}
