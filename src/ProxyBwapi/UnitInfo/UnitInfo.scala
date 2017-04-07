package ProxyBwapi.UnitInfo

import ProxyBwapi.Races.{Protoss, Zerg}
import Lifecycle.With
import Mathematics.Positions.TileRectangle
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
  def pixelsFromEdge(enemy:UnitInfo)            : Double  = pixelDistance(enemy) - unitClass.radialHypotenuse - enemy.unitClass.radialHypotenuse
  def pixelDistance(otherPosition:Position)         : Double  = pixelCenter.getDistance(otherPosition)
  def pixelDistance(enemy:UnitInfo)             : Double  = pixelDistance(enemy.pixelCenter)
  def tileDistance(otherPosition:TilePosition)      : Double  = pixelDistance(otherPosition.toPosition)
  def pixelDistanceSquared(enemy:UnitInfo)      : Double  = pixelDistanceSquared(enemy.pixelCenter)
  def pixelDistanceSquared(otherPixel:Position)     : Double  = pixelCenter.distancePixelsSquared(otherPixel)
  def tileDistanceSquared (otherTile:TilePosition)  : Double  = pixelDistance(otherTile.toPosition)
  def travelPixels(destination:TilePosition)        : Double  = travelPixels(tileCenter, destination)
  def travelPixels(origin:TilePosition, destination:TilePosition): Double =
    if (flying)
      origin.pixelCenter.pixelDistance(destination.pixelCenter)
    else
      With.paths.groundPixels(origin, destination)
  
  def canMove:Boolean = canDoAnythingThisFrame && unitClass.canMove
  def topSpeed:Double = unitClass.topSpeed
  
  ////////////
  // Combat //
  ////////////
  
  def melee:Boolean = unitClass.maxAirGroundRange <= 32 * 2
  
  def armorHealth:Int = unitClass.armor
  def armorShield: Int = 0
  def cooldownLeft:Int = Math.max(airCooldownLeft, groundCooldownLeft)
  def totalHealth: Int = hitPoints + shieldPoints + defensiveMatrixPoints
  def fractionalHealth:Double = totalHealth.toDouble / unitClass.maxTotalHealth
  def interceptors: Int = 8
  def scarabs: Int = 5
  
  def airRange    : Double = unitClass.airRange
  def groundRange : Double = unitClass.groundRange
  def airDps      : Double = unitClass.airDps
  def groundDps   : Double = unitClass.groundDps
  
  def cooldownAgainst(enemy:UnitInfo): Int = if (enemy.flying) airCooldownLeft else groundCooldownLeft
  def rangeAgainst(enemy:UnitInfo): Double = if (enemy.flying) airRange else groundRange
  def dpsAgainst(enemy:UnitInfo): Double = if (enemy.flying) airDps else groundDps
  
  def attacksAgainst (enemy:UnitInfo) : Int =
    if (enemy.flying)
      unitClass.rawAirDamageFactor * unitClass.maxAirHits
    else
      unitClass.rawGroundDamageFactor * unitClass.maxGroundHits
    
  def damageTypeAgainst(enemy:UnitInfo): DamageType = if (enemy.flying) unitClass.rawAirDamageType else unitClass.rawGroundDamageType
  def damageScaleAgainst(enemy:UnitInfo): Double = damageTypeAgainst(enemy) match {
    case DamageType.Concussive => enemy.unitClass.size match {
      case UnitSizeType.Large   => 0.25
      case UnitSizeType.Medium  => 0.5
      case _                    => 1.0
    }
    case DamageType.Explosive => enemy.unitClass.size match {
      case UnitSizeType.Small   => 0.5
      case UnitSizeType.Medium  => 0.75
      case _                    => 1.0
    }
    case _ => 1.0
  }
  def damageAgainst(enemy:UnitInfo, enemyShields:Int = 0) : Int = {
    val hits = attacksAgainst(enemy)
    val damageOnHit = if (enemy.flying) unitClass.rawAirDamage else unitClass.rawGroundDamage
    val damageScale = damageScaleAgainst(enemy)
    val damageToShields = Math.max(0, Math.min(enemy.shieldPoints, hits * (damageOnHit - enemy.armorShield)))
    val damageToHealth = Math.max(0, hits * (damageOnHit - enemy.armorHealth) - damageToShields)
    damageToHealth + damageToShields
  }
  
  def inTileRadius(tiles:Int)   : Set[UnitInfo] = With.units.inTileRadius(tileCenter, tiles)
  def inPixelRadius(pixels:Int) : Set[UnitInfo] = With.units.inPixelRadius(pixelCenter, pixels)
  
  def canDoAnythingThisFrame:Boolean =
    alive &&
    complete &&
    ! stasised &&
    ! maelstrommed
    //And lockdown
  
  def canAttackThisSecond:Boolean =
    canDoAnythingThisFrame &&
    unitClass.canAttack &&
    (unitClass != Protoss.Carrier || interceptors > 0) &&
    (unitClass != Protoss.Reaver  || scarabs > 0) &&
    (unitClass != Zerg.Lurker     || burrowed)
  
  def canAttackThisSecond(enemy:UnitInfo):Boolean =
    canAttackThisSecond &&
      enemy.alive &&
      enemy.totalHealth > 0 &&
      enemy.visible &&
      (enemy.detected || ! enemy.cloaked || ! enemy.burrowed) &&
      ! enemy.invincible &&
      ! enemy.stasised &&
      (if (enemy.flying) unitClass.attacksAir else unitClass.attacksGround)
  
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
  
  def inRangeToAttack(enemy:UnitInfo):Boolean = pixelsFromEdge(enemy) <= unitClass.maxAirGroundRange
  
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
  
  def isEnemyOf(enemy:UnitInfo): Boolean = player.isEnemy(enemy.player)
}
