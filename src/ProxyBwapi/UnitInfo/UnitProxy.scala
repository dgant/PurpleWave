package ProxyBwapi.UnitInfo

import ProxyBwapi.UnitClass.UnitClass
import bwapi.{Player, Position, TilePosition}

abstract class UnitProxy(var base:bwapi.Unit) {
    
  var id = base.getID
  
  ///////////////////
  // Tracking info //
  ///////////////////
  
  def player:Player
  def lastSeen:Int
  def possiblyStillThere:Boolean
  
  ////////////
  // Health //
  ////////////
  
  def alive:Boolean
  def complete:Boolean
  def defensiveMatrixPoints:Int
  def hitPoints:Int
  def initialResources:Int
  def invincible:Boolean
  def resourcesLeft:Int
  def shieldPoints:Int
  def unitClass:UnitClass
  def plagued:Boolean
  
  ////////////
  // Combat //
  ////////////
  
  def attacking:Boolean
  def attackStarting:Boolean
  def attackAnimationHappening:Boolean
  def airWeaponCooldownLeft:Int
  def groundWeaponCooldownLeft:Int
  
  //////////////
  // Geometry //
  //////////////
  
  def pixelCenter:Position
  def tileTopLeft:TilePosition
  def top:Int
  def left:Int
  def right:Int
  def bottom:Int
  
  ////////////
  // Orders //
  ////////////
  
  def gatheringMinerals:Boolean
  def gatheringGas:Boolean
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
  
  ////////////////
  // Visibility //
  ////////////////
  
  def burrowed:Boolean
  def cloaked:Boolean
  def detected:Boolean
  def visible:Boolean
  
  //////////////
  // Movement //
  //////////////
  
  def accelerating:Boolean
  def angle:Double
  def braking:Boolean
  def ensnared:Boolean
  def flying:Boolean
  def lifted:Boolean
  def maelstrommed:Boolean
  def sieged:Boolean
  def stasised:Boolean
  def stimmed:Boolean
  def stuck:Boolean
  def velocityX:Double
  def velocityY:Double
  
  //////////////
  // Statuses //
  //////////////
  
  def beingConstructed:Boolean
  def beingGathered:Boolean
  def beingHealed:Boolean
  def blind:Boolean
  def carryingMinerals:Boolean
  def carryingGas:Boolean
  def powered:Boolean
  def selected:Boolean
  def targetable:Boolean
  def underAttack:Boolean
  def underDarkSwarm:Boolean
  def underDisruptionWeb:Boolean
  def underStorm:Boolean
}
