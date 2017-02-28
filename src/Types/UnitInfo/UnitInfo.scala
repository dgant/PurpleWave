package Types.UnitInfo

import Startup.With
import bwapi._
import Utilities.Enrichment.EnrichUnitType._

abstract class UnitInfo (var baseUnit:bwapi.Unit) {
  def alive:Boolean;
  def id:Int = baseUnit.getID;
  def lastSeen:Int;
  def possiblyStillThere:Boolean;
  def player:Player;
  def position:Position;
  def walkPosition:WalkPosition;
  def tilePosition:TilePosition;
  def hitPoints:Int;
  def shieldPoints:Int;
  def unitType:UnitType;
  def complete:Boolean;
  def flying:Boolean;
  def visible:Boolean
  def cloaked:Boolean;
  
  //This ignores spellcasters
  def canFight: Boolean = {
    complete && unitType.canAttack || List(UnitType.Protoss_Carrier, UnitType.Protoss_Reaver).contains(unitType)
  }
  
  def x:Int = position.getX
  def y:Int = position.getY
  def attackFrames                    : Int     = { 4 + (if (List(UnitType.Protoss_Dragoon, UnitType.Zerg_Devourer).contains(unitType)) 3 else 0) }
  def isOurs                          : Boolean = { player == With.game.self }
  def isFriendly                      : Boolean = { isOurs || player.isAlly(With.game.self) }
  def isEnemy                         : Boolean = { player.isEnemy(With.game.self) }
  def totalHealth                     : Int     = { hitPoints + shieldPoints }
  def maxTotalHealth                  : Int     = { unitType.maxHitPoints + unitType.maxShields }
  def range                           : Int     = { unitType.range }
  def enemyOf(otherUnit:UnitInfo)     : Boolean = { player.isEnemy(otherUnit.player) }
  def groundDps                       : Int     = { if (canFight) unitType.groundDps else 0 }
  def totalCost                       : Int     = { unitType.totalCost }
}
