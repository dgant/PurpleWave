package ProxyBwapi.UnitInfo

import Mathematics.Points.{Pixel, Tile}
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.UnitClasses.UnitClass

abstract class UnitProxy(var bwapiUnit: bwapi.Unit, val id: Int) {
  
  ///////////////////
  // Tracking info //
  ///////////////////
  
  def player: PlayerInfo
  def lastSeen: Int
  
  ////////////
  // Health //
  ////////////
  
  def alive: Boolean
  def complete: Boolean
  def defensiveMatrixPoints: Int
  def hitPoints: Int
  def initialResources: Int
  def invincible: Boolean
  def resourcesLeft: Int
  def shieldPoints: Int
  def energy: Int
  def unitClass: UnitClass
  def plagued: Boolean
  
  ////////////
  // Combat //
  ////////////
  
  def interceptors: Iterable[UnitInfo]
  def scarabCount: Int
  def spiderMines: Int

  def airCooldownLeft: Int
  def groundCooldownLeft: Int
  def spellCooldownLeft: Int
  
  //////////////
  // Geometry //
  //////////////
  
  def pixel: Pixel
  def tileTopLeft: Tile
  def top: Int
  def left: Int
  def right: Int
  def bottom: Int
  
  ////////////
  // Orders //
  ////////////
  
  def gatheringMinerals: Boolean
  def gatheringGas: Boolean
  
  def target: Option[UnitInfo]
  def targetPixel: Option[Pixel]
  def orderTarget: Option[UnitInfo]
  def orderTargetPixel: Option[Pixel]
  def order: String
  
  def attacking: Boolean
  def constructing: Boolean
  def morphing: Boolean
  def repairing: Boolean
  def teching: Boolean
  def training: Boolean
  def upgrading: Boolean
  
  ////////////////
  // Visibility //
  ////////////////
  
  def burrowed: Boolean
  def cloaked: Boolean
  def detected: Boolean
  def visible: Boolean
  
  //////////////
  // Movement //
  //////////////

  def angleRadians: Double
  def ensnared: Boolean
  def flying: Boolean
  def irradiated: Boolean
  def lockedDown: Boolean
  def maelstrommed: Boolean
  def sieged: Boolean
  def stasised: Boolean
  def stimmed: Boolean
  def stuck: Boolean
  def velocityX: Double
  def velocityY: Double
  
  //////////////
  // Statuses //
  //////////////

  def remainingCompletionFrames: Int
  def remainingUpgradeFrames: Int
  def remainingTechFrames: Int
  def remainingTrainFrames: Int

  def beingHealed: Boolean
  def blind: Boolean
  def carryingMinerals: Boolean
  def carryingGas: Boolean
  def powered: Boolean
  def selected: Boolean
  def underDarkSwarm: Boolean
  def underDisruptionWeb: Boolean
  def underStorm: Boolean
  
  def addon: Option[UnitInfo]
  def hasNuke: Boolean

  def framesUntilRemoval: Int
}
