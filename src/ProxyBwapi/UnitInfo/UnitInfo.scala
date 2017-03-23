package ProxyBwapi.UnitInfo

import Geometry.TileRectangle
import ProxyBwapi.Races.{Protoss, Terran}
import Startup.With
import Utilities.EnrichPosition._
import bwapi._

abstract class UnitInfo (base:bwapi.Unit) extends UnitProxy(base) {
  
  def friendly  : Option[FriendlyUnitInfo]  = None
  def foreign   : Option[ForeignUnitInfo]   = None
  
  override def toString:String = unitClass.toString + " " + tileCenter.toString
  
  ////////////
  // Health //
  ////////////
  
  def mineralsLeft      : Int = if (unitClass.isMinerals) resourcesLeft else 0
  def gasLeft           : Int = if (unitClass.isGas)      resourcesLeft else 0
  
  //////////////
  // Geometry //
  //////////////
  
  def x: Int = pixelCenter.getX
  def y: Int = pixelCenter.getY
  
  def walkPosition:WalkPosition = pixelCenter.toWalkPosition
  def hypotenuse: Double = unitClass.width * 1.41421356
  def tileCenter: TilePosition = pixelCenter.toTilePosition
  def tileArea: TileRectangle = unitClass.tileArea.add(tileTopLeft)
  
  def canTraverse(tile:TilePosition):Boolean = flying || With.grids.walkable.get(tile)
  
  //Could improve this by counting angle or using the native BWAPI method
  def pixelsFromEdge(otherUnit:UnitInfo) : Double = pixelDistance(otherUnit) - hypotenuse - otherUnit.hypotenuse
  
  def pixelDistance(otherPosition:Position)        : Double = pixelCenter.getDistance(otherPosition)
  def pixelDistance(otherUnit:UnitInfo)            : Double = pixelDistance(otherUnit.pixelCenter)
  def tileDistance(otherPosition:TilePosition)     : Double = pixelDistance(otherPosition.toPosition)
  def pixelDistanceSquared(otherUnit:UnitInfo)     : Double = pixelDistanceSquared(otherUnit.pixelCenter)
  def pixelDistanceSquared(otherPixel:Position)    : Double = pixelCenter.distancePixelsSquared(otherPixel)
  def tileDistanceSquared (otherTile:TilePosition) : Double = pixelDistance(otherTile.toPosition)
  
  def travelPixels(destination:TilePosition): Double = travelPixels(tileCenter, destination)
  
  def travelPixels(origin:TilePosition, destination:TilePosition): Double =
    if (flying)
      origin.pixelCenter.distancePixels(destination.pixelCenter)
    else
      With.paths.groundPixels(tileCenter, destination)
  
  ////////////
  // Combat //
  ////////////
  
  def airDps    : Double = unitClass.groundDps
  def groundDps : Double = unitClass.groundDps
  
  def totalHealth: Int = hitPoints + shieldPoints + defensiveMatrixPoints
  def interceptors: Int = 8
  def scarabs: Int = 5
  
  def inPixelRadius(pixelRadius:Int): Set[UnitInfo] = With.units.inPixelRadius(pixelCenter, pixelRadius)
  
  //TODO: Account for ground/flying
  def enemiesInRange: Set[UnitInfo] =
    With.units
      .inPixelRadius(pixelCenter, unitClass.maxAirGroundRange + 96)
      .filter(pixelsFromEdge(_) <= unitClass.maxAirGroundRange)
      .filter(isEnemyOf)
  
  def canDoAnything:Boolean = alive && complete && ! stasised && ! maelstrommed //And lockdown
  def canAttack:Boolean = canDoAnything && unitClass.canAttack
  def canAttack(otherUnit:UnitInfo):Boolean = canAttack && (if (otherUnit.flying) unitClass.attacksAir else unitClass.attacksGround)
  def impactsCombat: Boolean = canDoAnything && (canAttack || unitClass.isSpellcaster || Set(Terran.Bunker, Terran.Medic).contains(unitClass))
  
  def requiredAttackDelay: Int = {
    // The question:
    // If we order this unit to attack, how many frames after issuing an order (and waiting on latency) before it can attack again?
    //
    // This is also important for preventing the Goon Stop bug. See BehaviorDragoon for details.
    //
    if      (unitClass == Protoss.Dragoon) 8
    else if (unitClass == Protoss.Carrier) 48
    else                                   4
  }
  
  /////////////
  // Players //
  /////////////
  
  def isOurs     : Boolean = player == With.self
  def isFriendly : Boolean = isOurs || player.isAlly(With.self)
  def isEnemy    : Boolean = player.isEnemy(With.self)
  
  def isEnemyOf(otherUnit:UnitInfo): Boolean = player.isEnemy(otherUnit.player)
}
