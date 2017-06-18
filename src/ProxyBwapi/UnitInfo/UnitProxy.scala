package ProxyBwapi.UnitInfo

import Mathematics.Points.{Pixel, Tile}
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.UnitClass.UnitClass
import bwapi.UnitCommand

abstract class UnitProxy(var base:bwapi.Unit) {
    
  var id = base.getID
  
  ///////////////////
  // Tracking info //
  ///////////////////
  
  def player:PlayerInfo
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
  def energy:Int
  def unitClass:UnitClass
  def plagued:Boolean
  
  ////////////
  // Combat //
  ////////////
  
  def interceptors:Int
  def scarabs:Int
  
  def attackStarting:Boolean
  def attackAnimationHappening:Boolean
  def airCooldownLeft:Int
  def groundCooldownLeft:Int
  def spellCooldownLeft:Int
  
  //////////////
  // Geometry //
  //////////////
  
  def pixelCenter:Pixel
  def tileTopLeft:Tile
  def top:Int
  def left:Int
  def right:Int
  def bottom:Int
  
  ////////////
  // Orders //
  ////////////
  
  def gatheringMinerals:Boolean
  def gatheringGas:Boolean
  
  def target:Option[UnitInfo]
  def targetPixel:Option[Pixel]
  def orderTarget:Option[UnitInfo]
  def orderTargetPixel:Option[Pixel]
  
  def command:Option[UnitCommand]
  def order:String
  
  def attacking:Boolean
  def constructing:Boolean
  def following:Boolean
  def holdingPosition:Boolean
  def idle:Boolean
  def interruptible:Boolean
  def morphing:Boolean
  def repairing:Boolean
  def researching:Boolean
  def patrolling:Boolean
  def training:Boolean
  def upgrading:Boolean
  
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
  def angleRadians:Double
  def braking:Boolean
  def ensnared:Boolean
  def flying:Boolean
  def lifted:Boolean
  def lockedDown:Boolean
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
