package ProxyBwapi.UnitInfo
import Performance.Caching.{Cache, CacheFrame}
import ProxyBwapi.UnitClass.{UnitClass, UnitClasses}
import Startup.With
import bwapi.{Player, Position, TilePosition, UnitCommand}

abstract class FriendlyUnitProxy(base:bwapi.Unit) extends UnitInfo(base) {
  
  val _cacheClass     = new Cache[UnitClass]          (5,  () =>  UnitClasses.get(base.getType))
  val _cachePlayer    = new Cache[Player]             (10, () =>  base.getPlayer)
  val _cachePixel     = new CacheFrame[Position]      (() =>  base.getPosition)
  val _cacheTile      = new CacheFrame[TilePosition]  (() =>  base.getTilePosition)
  val _cacheCompleted = new CacheFrame[Boolean]       (() =>  base.isCompleted)
  val _cacheExists    = new CacheFrame[Boolean]       (() =>  base.exists)
  val _cacheId        = new CacheFrame[Int]           (() =>  base.getID)
  
  ///////////////////
  // Tracking info //
  ///////////////////
  
  def player:Player = _cachePlayer.get
  def lastSeen:Int = With.frame
  def possiblyStillThere:Boolean = alive
  
  ////////////
  // Health //
  ////////////
  
  def alive                 : Boolean   = _cacheExists.get
  def complete              : Boolean   = _cacheCompleted.get
  def defensiveMatrixPoints : Int       = base.getDefenseMatrixPoints
  def hitPoints             : Int       = base.getHitPoints
  def initialResources      : Int       = base.getInitialResources
  def invincible            : Boolean   = base.isInvincible
  def resourcesLeft         : Int       = base.getResources
  def shieldPoints          : Int       = base.getShields
  def unitClass             : UnitClass = _cacheClass.get
  def plagued               : Boolean   = base.isPlagued
  
  ////////////
  // Combat //
  ////////////
  
  def attacking                 : Boolean = base.isAttacking
  def attackStarting            : Boolean = base.isStartingAttack
  def attackAnimationHappening  : Boolean = base.isAttackFrame
  def airWeaponCooldownLeft     : Int     = base.getAirWeaponCooldown
  def groundWeaponCooldownLeft  : Int     = base.getGroundWeaponCooldown
  
  //////////////
  // Geometry //
  //////////////
  
  def pixelCenter : Position      = _cachePixel.get
  def tileTopLeft : TilePosition  = _cacheTile.get
  def top         : Int           = base.getTop
  def left        : Int           = base.getLeft
  def right       : Int           = base.getRight
  def bottom      : Int           = base.getBottom
  
  ////////////
  // Orders //
  ////////////
  
  def gatheringMinerals : Boolean = base.isGatheringMinerals
  def gatheringGas      : Boolean = base.isGatheringGas
  
  /*
  def attacking:Boolean
  def attackFrame:Boolean
  def constructing:Boolean
  def following:Boolean
  def holdingPosition:Boolean
  def idle:Boolean
  def interruptible:Boolean
  def morphing:Boolean
  def repairing:Boolean
  def researching:Boolean
  def patrolling:Boolean
  def startingAttack:Boolean
  def training:Boolean
  def upgrading:Boolean
  */
  
  ///////////////////
  // Friendly only //
  ///////////////////
  
  def command:UnitCommand = base.getLastCommand
  def commandFrame:Int = base.getLastCommandFrame
  
  ////////////////
  // Visibility //
  ////////////////
  
  def burrowed  : Boolean = base.isBurrowed
  def cloaked   : Boolean = base.isCloaked
  def detected  : Boolean = base.isDetected
  def visible   : Boolean = base.isVisible
  
  //////////////
  // Movement //
  //////////////
  
  def accelerating  : Boolean = base.isAccelerating
  def angle         : Double  = base.getAngle
  def braking       : Boolean = base.isBraking
  def ensnared      : Boolean = base.isEnsnared
  def flying        : Boolean = base.isFlying
  def lifted        : Boolean = base.isLifted
  def maelstrommed  : Boolean = base.isMaelstrommed
  def sieged        : Boolean = base.isSieged
  def stasised      : Boolean = base.isStasised
  def stimmed       : Boolean = base.isStimmed
  def stuck         : Boolean = base.isStuck
  def velocityX     : Double  = base.getVelocityX
  def velocityY     : Double  = base.getVelocityY
  
  //////////////
  // Statuses //
  //////////////
  
  def beingConstructed    : Boolean = base.isBeingConstructed
  def beingGathered       : Boolean = base.isBeingGathered
  def beingHealed         : Boolean = base.isBeingHealed
  def blind               : Boolean = base.isBlind
  def carryingMinerals    : Boolean = base.isCarryingMinerals
  def carryingGas         : Boolean = base.isCarryingGas
  def powered             : Boolean = base.isPowered
  def selected            : Boolean = base.isSelected
  def targetable          : Boolean = base.isTargetable
  def underAttack         : Boolean = base.isUnderAttack
  def underDarkSwarm      : Boolean = base.isUnderDarkSwarm
  def underDisruptionWeb  : Boolean = base.isUnderDisruptionWeb
  def underStorm          : Boolean = base.isUnderStorm
}
