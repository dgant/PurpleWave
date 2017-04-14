package ProxyBwapi.UnitInfo

import Lifecycle.With
import Mathematics.Pixels.{Pixel, Tile, TileRectangle}
import Performance.Caching.CacheFrame
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClass.UnitClass
import bwapi._

abstract class UnitInfo (base:bwapi.Unit) extends UnitProxy(base) {
  
  def friendly  : Option[FriendlyUnitInfo]  = None
  def foreign   : Option[ForeignUnitInfo]   = None
  
  override def toString:String = unitClass.toString + " " + tileIncludingCenter.toString
  
  def is(unitClasses:UnitClass*):Boolean = unitClasses.exists(_ == unitClass)
  
  ////////////
  // Health //
  ////////////
  
  def mineralsLeft  : Int = if (unitClass.isMinerals) resourcesLeft else 0
  def gasLeft       : Int = if (unitClass.isGas)      resourcesLeft else 0
  
  //////////////
  // Geometry //
  //////////////
  
  def x: Int = pixelCenter.x
  def y: Int = pixelCenter.y
  
  def tileIncludingCenter: Tile = pixelCenter.tileIncluding
  def tileArea: TileRectangle = unitClass.tileArea.add(tileTopLeft)
  
  def pixelRangeAir: Double =
    unitClass.airRange +
      (if (is(Terran.Marine)    && player.getUpgradeLevel(Terran.MarineRange)     > 0)  32.0 else 0.0)
      (if (is(Terran.Goliath)   && player.getUpgradeLevel(Terran.GoliathAirRange) > 0)  96.0 else 0.0)
      (if (is(Protoss.Dragoon)  && player.getUpgradeLevel(Protoss.DragoonRange)   > 0)  64.0 else 0.0)
      (if (is(Zerg.Hydralisk)   && player.getUpgradeLevel(Zerg.HydraliskRange)    > 0)  32.0 else 0.0)
  
  def pixelRangeGround: Double =
    unitClass.groundRange +
      (if (is(Terran.Marine)    && player.getUpgradeLevel(Terran.MarineRange)     > 0)  32.0 else 0.0)
      (if (is(Protoss.Dragoon)  && player.getUpgradeLevel(Protoss.DragoonRange)   > 0)  64.0 else 0.0)
      (if (is(Zerg.Hydralisk)   && player.getUpgradeLevel(Zerg.HydraliskRange)    > 0)  32.0 else 0.0)
  
  def canTraverse           (tile:        Tile)     : Boolean = flying || With.grids.walkable.get(tile)
  def pixelsFromEdgeSlow    (otherUnit:   UnitInfo) : Double  = pixelDistanceSlow(otherUnit) - unitClass.radialHypotenuse - otherUnit.unitClass.radialHypotenuse
  def pixelsFromEdgeFast    (otherUnit:   UnitInfo) : Double  = pixelDistanceFast(otherUnit) - unitClass.radialHypotenuse - otherUnit.unitClass.radialHypotenuse
  def pixelDistanceSlow     (otherPixel:  Pixel)    : Double  = pixelCenter.pixelDistanceSlow(otherPixel)
  def pixelDistanceSlow     (otherUnit:   UnitInfo) : Double  = pixelDistanceSlow(otherUnit.pixelCenter)
  def pixelDistanceFast     (otherPixel:  Pixel)    : Double  = pixelCenter.pixelDistanceFast(otherPixel)
  def pixelDistanceFast     (otherUnit:   UnitInfo) : Double  = pixelDistanceFast(otherUnit.pixelCenter)
  def pixelDistanceSquared  (otherUnit:   UnitInfo) : Double  = pixelDistanceSquared(otherUnit.pixelCenter)
  def pixelDistanceSquared  (otherPixel:  Pixel)    : Double  = pixelCenter.pixelDistanceSquared(otherPixel)
  def travelPixels          (destination: Tile)     : Double  = travelPixels(tileIncludingCenter, destination)
  def travelPixels          (from: Tile,  to: Tile) : Double  =
    if (flying)
      from.pixelCenter.pixelDistanceSlow(to.pixelCenter)
    else
      With.paths.groundPixels(from, to)
  
  def canMove:Boolean = canDoAnythingThisFrame && unitClass.canMove
  
  def topSpeed:Double =
    unitClass.topSpeed + (if (
      (is(Terran.Vulture)   && player.getUpgradeLevel(Terran.VultureSpeed)    > 0) ||
      (is(Protoss.Observer) && player.getUpgradeLevel(Protoss.ObserverSpeed)  > 0) ||
      (is(Protoss.Scout)    && player.getUpgradeLevel(Protoss.ScoutSpeed)     > 0) ||
      (is(Protoss.Shuttle)  && player.getUpgradeLevel(Protoss.ShuttleSpeed)   > 0) ||
      (is(Protoss.Zealot)   && player.getUpgradeLevel(Protoss.ZealotSpeed)    > 0) ||
      (is(Zerg.Overlord)    && player.getUpgradeLevel(Zerg.OverlordSpeed)     > 0) ||
      (is(Zerg.Zergling)    && player.getUpgradeLevel(Zerg.ZerglingSpeed)     > 0) ||
      (is(Zerg.Hydralisk)   && player.getUpgradeLevel(Zerg.HydraliskSpeed)    > 0) ||
      (is(Zerg.Ultralisk)   && player.getUpgradeLevel(Zerg.UltraliskSpeed)    > 0))
      unitClass.topSpeed/2.0 else 0.0)
  
  def project(framesToLookAhead:Int):Pixel = pixelCenter.add((velocityX * framesToLookAhead).toInt, (velocityY * framesToLookAhead).toInt)
  
  ////////////
  // Combat //
  ////////////
  
  def melee:Boolean = unitClass.maxAirGroundRange <= 32 * 2
  
  def armorHealth : Int = unitClass.armor + player.getUpgradeLevel(unitClass.armorUpgrade)
  def armorShield : Int = 0               + player.getUpgradeLevel(Protoss.Shields)
  
  def cooldownLeft:Int = Math.max(airCooldownLeft, groundCooldownLeft)
  def totalHealth: Int = hitPoints + shieldPoints + defensiveMatrixPoints
  def fractionalHealth:Double = totalHealth.toDouble / unitClass.maxTotalHealth
  
  def interceptors  : Int = 8
  def scarabs       : Int = 5
  
  def airDps    : Double = unitClass.airDps
  def groundDps : Double = unitClass.groundDps
  
  def cooldownAgainst   (enemy:UnitInfo)  : Int         = if (enemy.flying) airCooldownLeft             else groundCooldownLeft
  def rangeAgainst      (enemy:UnitInfo)  : Double      = if (enemy.flying) pixelRangeAir               else pixelRangeGround
  def damageTypeAgainst (enemy:UnitInfo)  : DamageType  = if (enemy.flying) unitClass.rawAirDamageType  else unitClass.rawGroundDamageType
  def dpsAgainst        (enemy:UnitInfo)  : Double      = if (enemy.flying) airDps                      else groundDps
  def attacksAgainst    (enemy:UnitInfo)  : Int =
    if (enemy.flying)
      unitClass.rawAirDamageFactor * unitClass.maxAirHits
    else
      unitClass.rawGroundDamageFactor * unitClass.maxGroundHits
  
  def damageScaleAgainst(enemy:UnitInfo): Double =
    if (enemy.shieldPoints > 5) 1.0 else
    damageTypeAgainst(enemy) match {
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
    val damageToHealth  = Math.max(0, hits * (damageOnHit - enemy.armorHealth) - damageToShields)
    
    //Note that Armor can't reduce damage below 0.5
    damageToHealth + damageToShields
  }
  
  def inTileRadius  (tiles:Int)  : Traversable[UnitInfo] = With.units.inTileRadius(tileIncludingCenter, tiles)
  def inPixelRadius (pixels:Int) : Traversable[UnitInfo] = With.units.inPixelRadius(pixelCenter, pixels)
  
  def canDoAnythingThisFrame:Boolean =
    alive &&
    complete &&
    ! stasised &&
    ! maelstrommed
    //And lockdown
  
  def canBeAttackedThisFrame:Boolean = canBeAttackedThisFrameCache.get
  private val canBeAttackedThisFrameCache = new CacheFrame(() =>
      alive &&
      totalHealth > 0 &&
      visible &&
      ! invincible &&
      ! stasised)
  
  def canAttackThisSecond:Boolean = canAttackThisSecondCache.get
  private val canAttackThisSecondCache = new CacheFrame(() =>
    canDoAnythingThisFrame &&
    (
      unitClass.canAttack ||
      (
        ( ! is(Protoss.Carrier) || interceptors > 0) &&
        ( ! is(Protoss.Reaver)  || scarabs > 0) &&
        ( ! is(Zerg.Lurker)     || burrowed)
      )
    ))
  
  def canAttackThisSecond(enemy:UnitInfo):Boolean =
    canAttackThisSecond &&
    enemy.canBeAttackedThisFrame &&
    (enemy.detected || ! enemy.cloaked || ! enemy.burrowed) &&
    (if (enemy.flying) unitClass.attacksAir else unitClass.attacksGround)
  
  def canAttackThisFrame:Boolean = canAttackThisSecond && cooldownLeft < With.latency.framesRemaining
  
  def requiredAttackDelay: Int = {
    // The question:
    // If we order this unit to attack, how many frames after issuing an order (and waiting on latency) before it can attack again?
    //
    // This is also important for preventing the Goon Stop bug. See BehaviorDragoon for details.
    //
    if      (is(Protoss.Dragoon)) 8
    else if (is(Protoss.Carrier)) 48
    else                          4
  }
  
  def pixelImpactTravel   (framesAhead  : Int)  : Double = unitClass.topSpeed * framesAhead
  def pixelImpactAir      (framesAhead  : Int)  : Double = pixelImpactTravel(framesAhead) + pixelRangeAir
  def pixelImpactGround   (framesAhead  : Int)  : Double = pixelImpactTravel(framesAhead) + pixelRangeGround
  def pixelImpactMax      (framesAhead  : Int)  : Double = Math.max(pixelImpactAir(framesAhead), pixelImpactGround(framesAhead))
  def pixelImpactAgainst  (framesAhead  : Int, enemy:UnitInfo): Double = if (enemy.flying) pixelImpactAir(framesAhead) else pixelImpactGround(framesAhead)
  
  def inRangeToAttackSlow(enemy:UnitInfo):Boolean = pixelsFromEdgeSlow(enemy) <= rangeAgainst(enemy)
  def inRangeToAttackFast(enemy:UnitInfo):Boolean = pixelsFromEdgeFast(enemy) <= rangeAgainst(enemy)
  
  /////////////
  // Players //
  /////////////
  
  def isOurs     : Boolean = player.isUs
  def isNeutral  : Boolean = player.isNeutral
  def isFriendly : Boolean = player.isAlly || isOurs
  def isEnemy    : Boolean = player.isEnemy
  def isEnemyOf(otherUnit:UnitInfo): Boolean = (isFriendly && otherUnit.isEnemy) || (isEnemy && otherUnit.isFriendly)
}
