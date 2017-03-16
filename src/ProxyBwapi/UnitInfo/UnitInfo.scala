package ProxyBwapi.UnitInfo

import Geometry.TileRectangle
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClass.UnitClass
import Startup.With
import Utilities.TypeEnrichment.EnrichPosition._
import bwapi._

abstract class UnitInfo (var baseUnit:bwapi.Unit) {
  val _id = baseUnit.getID
  
  def friendly:Option[FriendlyUnitInfo] = None
  def foreign:Option[ForeignUnitInfo] = None
  
  def alive:Boolean
  def id:Int = _id
  def lastSeen:Int
  def possiblyStillThere:Boolean
  def player:Player
  def pixel:Position
  def walkPosition:WalkPosition
  def tileTopLeft:TilePosition
  def hitPoints:Int
  def shieldPoints:Int
  def utype:UnitClass
  def complete:Boolean
  def flying:Boolean
  def visible:Boolean
  def cloaked:Boolean
  def burrowed:Boolean
  def detected:Boolean
  def morphing:Boolean
  def invincible:Boolean
  def top:Int
  def left:Int
  def right:Int
  def bottom:Int
  def mineralsLeft:Int = 0
  def gasLeft:Int = 0
  def initialResources: Int = 0
  
  def canFight                                    : Boolean                 = complete && utype.canAttack || List(Protoss.Carrier, Protoss.Reaver, Terran.Bunker).contains(utype)
  def impactsCombat                               : Boolean                 = canFight || List(Terran.Medic).contains(utype) //This ignores spellcasters
  def x                                           : Int                     = pixel.getX
  def y                                           : Int                     = pixel.getY
  def attackFrames                                : Int                     = 8 + (if (List(Protoss.Dragoon, Zerg.Devourer).contains(utype)) 6 else 0)
  def isOurs                                      : Boolean                 = player == With.self
  def isFriendly                                  : Boolean                 = isOurs || player.isAlly(With.self)
  def isEnemy                                     : Boolean                 = player.isEnemy(With.self)
  def isMelee                                     : Boolean                 = range <= 32
  def isDetector                                  : Boolean                 = utype.isDetector
  def totalHealth                                 : Int                     = hitPoints + shieldPoints
  def maxTotalHealth                              : Int                     = utype.maxHitPoints + utype.maxShields
  def rangeAir                                    : Int                     = utype.airWeapon.maxRange
  def rangeGround                                 : Int                     = utype.groundWeapon.maxRange
  def range                                       : Int                     = Math.max(rangeAir, rangeGround)
  def enemyOf(otherUnit:UnitInfo)                 : Boolean                 = player.isEnemy(otherUnit.player)
  def groundDps                                   : Int                     = if (canFight) utype.groundDps else 0
  def totalCost                                   : Int                     = utype.totalCost
  def isMinerals                                  : Boolean                 = utype.isMinerals
  def isGas                                       : Boolean                 = utype.isGas
  def isResource                                  : Boolean                 = isMinerals || isGas
  def tileCenter                                  : TilePosition            = pixel.toTilePosition
  def hypotenuse                                  : Double                  = utype.width * 1.41421356
  def tileArea                                    : TileRectangle           = new TileRectangle(tileTopLeft, new Position(right, bottom).tileIncluding.add(1, 1))
  def distanceFromEdge(otherUnit:UnitInfo)        : Double                  = distance(otherUnit) - hypotenuse - otherUnit.hypotenuse //Improve by counting angle
  def distance(otherUnit:UnitInfo)                : Double                  = distance(otherUnit.pixel)
  def distance(otherPosition:Position)            : Double                  = pixel.getDistance(otherPosition)
  def distance(otherPosition:TilePosition)        : Double                  = distance(otherPosition.toPosition)
  def distanceSquared(otherUnit:UnitInfo)         : Double                  = distanceSquared(otherUnit.pixel)
  def distanceSquared(otherPosition:Position)     : Double                  = pixel.pixelDistanceSquared(otherPosition)
  def distanceSquared(otherPosition:TilePosition) : Double                  = distance(otherPosition.toPosition)
  
}
