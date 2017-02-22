package Global.Allocation

import Geometry.Influence.InfluenceMap
import Global.Allocation.Intents.Intent
import Startup.With
import bwapi.{TilePosition, Unit, UnitSizeType}

import scala.collection.JavaConverters._
import scala.collection.mutable

class Commander {
  
  val _intents = new mutable.HashMap[bwapi.Unit, Intent]
  val _nextOrderFrame = new mutable.HashMap[bwapi.Unit, Int] { override def default(key: Unit): Int = 0 }
  
  def intend(unit:bwapi.Unit, intent:Intent){
    _intents.put(unit, intent)
  }
  
  def onFrame() {
    _intents.foreach(intent => _order(intent._1, intent._2))
    _intents.clear()
  }
  
  def _order(unit:bwapi.Unit, intent:Intent) {
    if (_nextOrderFrame(unit) > With.game.getFrameCount) { return }
    _nextOrderFrame(unit) = With.game.getFrameCount + 7 + With.game.getLatencyFrames
    
    //Attack, if possible
    if (unit.canAttack) {
      val attackTarget = With.game.getUnitsInRadius(
        unit.getPosition,
        List(unit.getType.groundWeapon.maxRange, unit.getType.airWeapon.maxRange).max + 16)
        .asScala
        .filter(target => _canAttack(unit, target))
        .sortBy(target => _evaluateTarget(unit, target))
        .headOption
      if (attackTarget.nonEmpty) {
        unit.attack(attackTarget.get)
        return
      }
    }
  
    val tileValues = _getAdjacentTiles(unit.getTilePosition)
      .filter(tile => _canTraverse(unit, tile))
      .map(tile => (tile, _evaluateTile(unit, tile, intent)))
  
    if (tileValues.isEmpty) {
      //Goofy situation, but possible
      unit.attack(intent.position.get)
      return
    }
  
    if (tileValues.forall(_._2 == 0)) {
      unit.attack(intent.position.get)
      return
    }
  
    val bestTile = tileValues.minBy(_._2)._1
    unit.move(bestTile.toPosition)
  }
  
  def _getAdjacentTiles(tilePosition:TilePosition):Iterable[TilePosition] = {
    (-1 to 1).flatten(dx =>
      (-1 to 1).map(dy =>
        new TilePosition(tilePosition.getX + dx, tilePosition.getY + dy)))
  }
  
  def _canTraverse(unit:bwapi.Unit, tile:TilePosition):Boolean = {
    if (unit.isFlying) { return true }
    With.geography.isWalkable(tile) && With.game.hasPath(unit.getPosition, tile.toPosition)
  }
  
  def _evaluateTile(unit:bwapi.Unit, tile:TilePosition, intent:Intent):Int = {
    _getEnemyDamageMap(unit).get(tile) - With.influence.friendlyGroundDamage.get(tile) * With.influence.groundAttractors.get(tile)
  }
  
  def _getEnemyDamageMap(unit:bwapi.Unit):InfluenceMap = {
    (unit.getType.size, unit.isFlying) match {
      case (UnitSizeType.Small,   false)  => With.influence.enemySmallGroundDamage
      case (UnitSizeType.Small,   true)   => With.influence.enemySmallAirDamage
      case (UnitSizeType.Medium,  false)  => With.influence.enemyMediumGroundDamage
      case (UnitSizeType.Medium,  true)   => With.influence.enemyMediumAirDamage
      case (UnitSizeType.Large,   false)  => With.influence.enemyLargeGroundDamage
      case (UnitSizeType.Large,   true)   => With.influence.enemyLargeAirDamage
      case _                              => With.influence.enemyMediumGroundDamage
    }
  }
  
  def _canAttack(unit:bwapi.Unit, target:bwapi.Unit):Boolean = {
    target.getPlayer.isEnemy(With.game.self) && unit.canAttack(target)
  }
  
  def _evaluateTarget(unit:bwapi.Unit, target:bwapi.Unit):Int = {
    target.getHitPoints + target.getShields
  }
}
