package ProxyBwapi.UnitInfo

import Geometry.TileRectangle
import ProxyBwapi.Races.{Protoss, Zerg}
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
  
  def mineralsLeft  : Int = if (unitClass.isMinerals) resourcesLeft else 0
  def gasLeft       : Int = if (unitClass.isGas)      resourcesLeft else 0
  
  //////////////
  // Geometry //
  //////////////
  
  def x: Int = pixelCenter.getX
  def y: Int = pixelCenter.getY
  
  def tileCenter: TilePosition = pixelCenter.toTilePosition
  def tileArea: TileRectangle = unitClass.tileArea.add(tileTopLeft)
  
  def canTraverse(tile:TilePosition)                : Boolean = flying || With.grids.walkable.get(tile)
  def pixelsFromEdge(otherUnit:UnitInfo)            : Double  = pixelDistance(otherUnit) - unitClass.radialHypotenuse - otherUnit.unitClass.radialHypotenuse
  def pixelDistance(otherPosition:Position)         : Double  = pixelCenter.getDistance(otherPosition)
  def pixelDistance(otherUnit:UnitInfo)             : Double  = pixelDistance(otherUnit.pixelCenter)
  def tileDistance(otherPosition:TilePosition)      : Double  = pixelDistance(otherPosition.toPosition)
  def pixelDistanceSquared(otherUnit:UnitInfo)      : Double  = pixelDistanceSquared(otherUnit.pixelCenter)
  def pixelDistanceSquared(otherPixel:Position)     : Double  = pixelCenter.distancePixelsSquared(otherPixel)
  def tileDistanceSquared (otherTile:TilePosition)  : Double  = pixelDistance(otherTile.toPosition)
  def travelPixels(destination:TilePosition)        : Double  = travelPixels(tileCenter, destination)
  def travelPixels(origin:TilePosition, destination:TilePosition): Double =
    if (flying)
      origin.pixelCenter.pixelDistance(destination.pixelCenter)
    else
      With.paths.groundPixels(origin, destination)
  
  ////////////
  // Combat //
  ////////////
  
  def cooldownLeft:Int = Math.max(airCooldownLeft, groundCooldownLeft)
  def totalHealth: Int = hitPoints + shieldPoints + defensiveMatrixPoints
  def fractionalHealth:Double = totalHealth.toDouble / unitClass.maxTotalHealth
  def interceptors: Int = 8
  def scarabs: Int = 5
  
  def inTileRadius(tiles:Int)   : Set[UnitInfo] = With.units.inTileRadius(tileCenter, tiles)
  def inPixelRadius(pixels:Int) : Set[UnitInfo] = With.units.inPixelRadius(pixelCenter, pixels)
  
  def canDoAnythingRightNow:Boolean =
    alive &&
    complete &&
    ! stasised &&
    ! maelstrommed
    //And lockdown
  
  def canAttackThisSecond:Boolean =
    canDoAnythingRightNow &&
    unitClass.canAttack &&
    (unitClass != Protoss.Carrier || interceptors > 0) &&
    (unitClass != Protoss.Reaver  || scarabs > 0) &&
    (unitClass != Zerg.Lurker     || burrowed)
  
  def canAttackThisSecond(otherUnit:UnitInfo):Boolean =
    canAttackThisSecond &&
      otherUnit.alive &&
      otherUnit.totalHealth > 0 &&
      otherUnit.visible &&
      (otherUnit.detected || ! otherUnit.cloaked || ! otherUnit.burrowed) &&
      ! otherUnit.invincible &&
      ! otherUnit.stasised &&
      (if (otherUnit.flying) unitClass.attacksAir else unitClass.attacksGround)
  
  def canAttackThisFrame:Boolean =
    canAttackThisSecond &&
    cooldownLeft < With.latency.framesRemaining
  
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
  
  // The range of this unit's potential impact at a distance in the future
  def pixelRangeAir                             : Double = unitClass.airRange
  def pixelRangeGround                          : Double = unitClass.groundRange
  def pixelReachTravel    (framesAhead  : Int)  : Double = unitClass.topSpeed * framesAhead
  def pixelReachAir       (framesAhead  : Int)  : Double = pixelReachTravel(framesAhead) + pixelRangeAir
  def pixelReachGround    (framesAhead  : Int)  : Double = pixelReachTravel(framesAhead) + pixelRangeGround
  def pixelReachDamage    (framesAhead  : Int)  : Double = Math.max(pixelReachAir(framesAhead), pixelReachGround(framesAhead))
  
  def inRangeToAttack(otherUnit:UnitInfo):Boolean = pixelsFromEdge(otherUnit) <= unitClass.maxAirGroundRange
  
  def attackableEnemiesInRange: Set[UnitInfo] =
    With.units
      .inPixelRadius(pixelCenter, unitClass.maxAirGroundRange + 32)
      .filter(inRangeToAttack)
      .filter(isEnemyOf)
      .filter(canAttackThisSecond)
  
  /////////////
  // Players //
  /////////////
  
  def isOurs     : Boolean = player == With.self
  def isNeutral  : Boolean = player == With.neutral
  def isFriendly : Boolean = isOurs || player.isAlly(With.self)
  def isEnemy    : Boolean = player.isEnemy(With.self)
  
  def isEnemyOf(otherUnit:UnitInfo): Boolean = player.isEnemy(otherUnit.player)
}
