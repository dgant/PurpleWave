package ProxyBwapi.UnitInfo

import Mathematics.Points.{Pixel, Tile}
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.UnitClasses.UnitClass

abstract class UnitProxy(val bwapiUnit: bwapi.Unit, val id: Int) {
  
  ///////////////////
  // Tracking info //
  ///////////////////

  def player: PlayerInfo
  def lastSeen: Int

  def readProxy(): Unit = {}
  
  ////////////
  // Health //
  ////////////
  
  def alive: Boolean
  def complete: Boolean
  def matrixPoints: Int
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
  def scarabs: Int
  def spiderMines: Int

  def cooldownAir: Int
  def cooldownGround: Int
  def cooldownSpell: Int
  
  //////////////
  // Geometry //
  //////////////
  
  def pixel: Pixel
  def tile: Tile // Center tile; not part of BWAPI but we always want it
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
  def stasised: Boolean
  def stimmed: Boolean
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

  def removalFrames: Int
}
