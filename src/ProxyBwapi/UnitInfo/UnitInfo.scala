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
  def pixelCenter:Position
  def walkPosition:WalkPosition
  def tileTopLeft:TilePosition
  def hitPoints:Int
  def shieldPoints:Int
  def unitClass:UnitClass
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
  
  val alsoImpactsCombat = Set(Terran.Bunker, Protoss.ShieldBattery)
  def impactsCombat                               : Boolean                 = alive && complete && (unitClass.canAttack || unitClass.isSpellcaster || alsoImpactsCombat.contains(unitClass))
  def x                                           : Int                     = pixelCenter.getX
  def y                                           : Int                     = pixelCenter.getY
  def isOurs                                      : Boolean                 = player == With.self
  def isFriendly                                  : Boolean                 = isOurs || player.isAlly(With.self)
  def isEnemy                                     : Boolean                 = player.isEnemy(With.self)
  def isEnemyOf(otherUnit:UnitInfo)               : Boolean                 = player.isEnemy(otherUnit.player)
  def isMelee                                     : Boolean                 = range <= 32
  def isDetector                                  : Boolean                 = unitClass.isDetector
  def isMinerals                                  : Boolean                 = unitClass.isMinerals
  def isGas                                       : Boolean                 = unitClass.isGas
  def isResource                                  : Boolean                 = isMinerals || isGas
  def totalHealth                                 : Int                     = hitPoints + shieldPoints
  def maxTotalHealth                              : Int                     = unitClass.maxHitPoints + unitClass.maxShields
  def rangeAir                                    : Int                     = unitClass.airWeapon.maxRange
  def rangeGround                                 : Int                     = unitClass.groundWeapon.maxRange
  def range                                       : Int                     = Math.max(rangeAir, rangeGround)
  def airDps                                      : Double                  = unitClass.airDps
  def groundDps                                   : Double                  = unitClass.groundDps
  def totalCost                                   : Int                     = unitClass.totalCost
  def tileCenter                                  : TilePosition            = pixelCenter.toTilePosition
  def hypotenuse                                  : Double                  = unitClass.width * 1.41421356
  def tileArea                                    : TileRectangle           = new TileRectangle(tileTopLeft, new Position(right, bottom).tileIncluding.add(1, 1))
  def distanceFromEdge(otherUnit:UnitInfo)        : Double                  = distance(otherUnit) - hypotenuse - otherUnit.hypotenuse //Improve by counting angle
  def distance(otherUnit:UnitInfo)                : Double                  = distance(otherUnit.pixelCenter)
  def distance(otherPosition:Position)            : Double                  = pixelCenter.getDistance(otherPosition)
  def distance(otherPosition:TilePosition)        : Double                  = distance(otherPosition.toPosition)
  def distanceSquared(otherUnit:UnitInfo)         : Double                  = distanceSquared(otherUnit.pixelCenter)
  def distanceSquared(otherPosition:Position)     : Double                  = pixelCenter.distancePixelsSquared(otherPosition)
  def distanceSquared(otherPosition:TilePosition) : Double                  = distance(otherPosition.toPosition)
  def inRadius(radius:Int)                        : Set[UnitInfo]           = With.units.inRadius(pixelCenter, radius)
  def enemiesInRange                              : Set[UnitInfo]           = With.units.inRadius(pixelCenter, range + 96).filter(unit => isEnemyOf(unit) && distanceFromEdge(unit) <= range)
  
  def attackFrames: Int = {
    val baseRate = 8
    val slowAttack = (if (List(Protoss.Dragoon, Zerg.Devourer).contains(unitClass)) 6 else 0)
    baseRate + slowAttack
  }
}
