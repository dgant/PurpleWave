package Global.Allocation

import Development.Configuration
import Geometry.Influence.InfluenceMap
import Global.Allocation.Intents.Intent
import Startup.With
import bwapi.{Color, Position, TilePosition, Unit, UnitSizeType, UnitType}

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
    val tileValues = _getAdjacentTiles(unit.getTilePosition)
      .filter(tile => _canTraverse(unit, tile))
      .map(tile => (tile, _evaluateTile(unit, tile, intent)))
    
    val attackTarget = _getAttackTarget(unit, intent)
    if (Configuration.enableOverlay && tileValues.nonEmpty) {
      var displayPosition = _tileCenter(tileValues.minBy(_._2)._1)
      if (attackTarget.nonEmpty) {
        displayPosition = attackTarget.get.getPosition
        With.game.drawCircleMap(displayPosition, 16, Color.Green)
      }
      With.game.drawLineMap(unit.getPosition, displayPosition, Color.Green)
    }
    
    if (_nextOrderFrame(unit) > With.game.getFrameCount) { return }
    //Compensate for possibility of cancelling attack animation
    //See https://github.com/tscmoo/tsc-bwai/blob/master/src/unit_controls.h#L1569
    //and https://github.com/davechurchill/ualbertabot/blob/922966f5f1442029f811d9c6a34d9ba94fc871df/UAlbertaBot/Source/CombatData.cpp#L221
    _nextOrderFrame(unit) = With.game.getFrameCount + 4 + With.game.getLatencyFrames +
      (if (List(UnitType.Protoss_Dragoon, UnitType.Zerg_Devourer).contains(unit.getType)) 3 else 0)
    
    //Attack, if possible and we're not trying to escape
    if (attackTarget.nonEmpty && _evaluateTile(unit, unit.getTilePosition, intent) <= 0) {
      unit.attack(attackTarget.get)
      return
    }
  
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
    unit.move(_tileCenter(bestTile))
  }
  
  def _tileCenter(tile:TilePosition):Position = {
    new Position(tile.getX * 32 + 16, tile.getY * 32 + 16)
  }
  
  def _getAttackTarget(unit:bwapi.Unit, intent:Intent):Option[bwapi.Unit] = {
    if (!unit.canAttack) { return None }
    With.game.getUnitsInRadius(
      unit.getPosition,
      Math.max(96, List(unit.getType.groundWeapon.maxRange, unit.getType.airWeapon.maxRange).max + 16))
      .asScala
      .filter(target => _canAttack(unit, target))
      .sortBy(target => _evaluateTarget(unit, target))
      .headOption
  }
  
  def _getAdjacentTiles(tilePosition:TilePosition):Iterable[TilePosition] = {
    (-1 to 1).flatten(dx =>
      (-1 to 1).map(dy =>
        new TilePosition(tilePosition.getX + dx, tilePosition.getY + dy)))
  }
  
  def _canTraverse(unit:bwapi.Unit, tile:TilePosition):Boolean = {
    if (unit.getTilePosition == tile) { return true }
    if (unit.isFlying) { return true }
    With.geography.isWalkable(tile) && With.game.hasPath(unit.getPosition, tile.toPosition)
  }
  
  def _evaluateTile(unit:bwapi.Unit, tile:TilePosition, intent:Intent):Int = {
    if (_canAttack(unit)) {
        Math.min(100, With.influence.friendlyGroundDamage.get(tile) * With.influence.groundAttractors.get(tile))
    }
    else {
      _getEnemyDamageMap(unit).get(tile)
    }
    
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
  
  def _canAttack(unit:bwapi.Unit):Boolean = {
    unit.getGroundWeaponCooldown + unit.getAirWeaponCooldown <= 0
  }
  
  def _canAttack(unit:bwapi.Unit, target:bwapi.Unit):Boolean = {
    _canAttack(unit) && target.getPlayer.isEnemy(With.game.self) && unit.canAttack(target)
  }
  
  def _evaluateTarget(unit:bwapi.Unit, target:bwapi.Unit):Int = {
    target.getHitPoints + target.getShields
  }
}
