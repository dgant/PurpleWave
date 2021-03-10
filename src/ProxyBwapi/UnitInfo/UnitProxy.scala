package ProxyBwapi.UnitInfo

import Mathematics.Points.{Pixel, Tile}
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitTracking.Visibility
import ProxyBwapi.Upgrades.Upgrade

trait UnitProxy {
  val bwapiUnit                 : bwapi.Unit
  val id                        : Int
  def visibility                : Visibility.Value
  def player                    : PlayerInfo
  def unitClass                 : UnitClass
  def pixel                     : Pixel
  def pixelObserved             : Pixel
  def tile                      : Tile
  def tileTopLeft               : Tile
  def visible                   : Boolean
  def alive                     : Boolean
  def complete                  : Boolean
  def burrowed                  : Boolean
  def cloaked                   : Boolean
  def detected                  : Boolean
  def flying                    : Boolean
  def plagued                   : Boolean
  def ensnared                  : Boolean
  def invincible                : Boolean
  def irradiated                : Boolean
  def lockedDown                : Boolean
  def maelstrommed              : Boolean
  def stasised                  : Boolean
  def stimmed                   : Boolean
  def gatheringMinerals         : Boolean
  def gatheringGas              : Boolean
  def morphing                  : Boolean
  def constructing              : Boolean
  def repairing                 : Boolean
  def teching                   : Boolean
  def training                  : Boolean
  def upgrading                 : Boolean
  def beingConstructed          : Boolean
  def beingGathered             : Boolean
  def beingHealed               : Boolean
  def blind                     : Boolean
  def carryingMinerals          : Boolean
  def carryingGas               : Boolean
  def powered                   : Boolean
  def selected                  : Boolean
  def underDarkSwarm            : Boolean
  def underDisruptionWeb        : Boolean
  def underStorm                : Boolean
  def hasNuke                   : Boolean
  def loaded                    : Boolean
  def lastSeen                  : Int
  def initialResources          : Int
  def resourcesLeft             : Int
  def hitPoints                 : Int
  def shieldPoints              : Int
  def matrixPoints              : Int
  def energy                    : Int
  def scarabs                   : Int
  def cooldownRaw               : Int
  def cooldownGround            : Int
  def cooldownAir               : Int
  def cooldownSpell             : Int
  def remainingCompletionFrames : Int
  def remainingTrainFrames      : Int
  def remainingUpgradeFrames    : Int
  def remainingTechFrames       : Int
  def removalFrames             : Int
  def spiderMines               : Int
  def angleRadians              : Double
  def velocityX                 : Double
  def velocityY                 : Double
  def order                     : String
  def orderTarget               : Option[UnitInfo]
  def orderTargetPixel          : Option[Pixel]
  def target                    : Option[UnitInfo]
  def targetPixel               : Option[Pixel]
  def techProducing             : Option[Tech]
  def upgradeProducing          : Option[Upgrade]
  def addon                     : Option[UnitInfo]
  def buildType                 : UnitClass
  def trainingQueue             : Seq[UnitClass]
  def interceptors              : Seq[UnitInfo]
}
