package Types.UnitInfo

import Geometry.TileRectangle
import Startup.With
import bwapi._
import Utilities.Enrichment.EnrichUnitType._
import Utilities.Enrichment.EnrichPosition._

abstract class UnitInfo (var baseUnit:bwapi.Unit) {
  def alive:Boolean
  def id:Int = baseUnit.getID
  def lastSeen:Int
  def possiblyStillThere:Boolean
  def player:Player
  def position:Position
  def walkPosition:WalkPosition
  def tileTopLeft:TilePosition
  def hitPoints:Int
  def shieldPoints:Int
  def utype:UnitType
  def complete:Boolean
  def flying:Boolean
  def visible:Boolean
  def cloaked:Boolean
  def top:Int
  def left:Int
  def right:Int
  def bottom:Int
  def mineralsLeft:Int = 0
  def gasLeft:Int = 0
  def initialResources: Int = 0
  
  //This ignores spellcasters
  //TODO: Move this onto EnhancedUnitType
  def canFight: Boolean = {
    complete && utype.canAttack || List(UnitType.Protoss_Carrier, UnitType.Protoss_Reaver, UnitType.Terran_Bunker).contains(utype)
  }
  
  def x                                           : Int                     = position.getX
  def y                                           : Int                     = position.getY
  def attackFrames                                : Int                     = 8 + (if (List(UnitType.Protoss_Dragoon, UnitType.Zerg_Devourer).contains(utype)) 6 else 0)
  def isOurs                                      : Boolean                 = player == With.game.self
  def isFriendly                                  : Boolean                 = isOurs || player.isAlly(With.game.self)
  def isEnemy                                     : Boolean                 = player.isEnemy(With.game.self)
  def isMelee                                     : Boolean                 = range <= 32
  def totalHealth                                 : Int                     = hitPoints + shieldPoints
  def maxTotalHealth                              : Int                     = utype.maxHitPoints + utype.maxShields
  def range                                       : Int                     = utype.range
  def enemyOf(otherUnit:UnitInfo)                 : Boolean                 = player.isEnemy(otherUnit.player)
  def groundDps                                   : Int                     = if (canFight) utype.groundDps else 0
  def totalCost                                   : Int                     = utype.totalCost
  def isMinerals                                  : Boolean                 = utype.isMinerals
  def isGas                                       : Boolean                 = utype.isGas
  def isResource                                  : Boolean                 = isMinerals || isGas
  def tileCenter                                  : TilePosition            = position.toTilePosition
  def tileArea                                    : TileRectangle           = new TileRectangle(tileTopLeft, new Position(right, bottom).tileIncluding.add(1, 1))
  def edgeDistance(otherUnit:UnitInfo)            : Double                  = distance(otherUnit.position) - utype.width - otherUnit.utype.width //Improve by counting angle
  def distance(otherUnit:UnitInfo)                : Double                  = distance(otherUnit.position)
  def distance(otherPosition:Position)            : Double                  = position.getDistance(otherPosition)
  def distance(otherPosition:TilePosition)        : Double                  = distance(otherPosition.toPosition)
  def distanceSquared(otherUnit:UnitInfo)         : Double                  = distanceSquared(otherUnit.position)
  def distanceSquared(otherPosition:Position)     : Double                  = position.pixelDistanceSquared(otherPosition)
  def distanceSquared(otherPosition:TilePosition) : Double                  = distance(otherPosition.toPosition)
  def impactsCombat                               : Boolean                 = canFight || List(UnitType.Terran_Medic).contains(utype)
  def enemiesInRange                              : Iterable[UnitInfo]      = With.units.inRadius(position, range).filter(enemyOf)
}
