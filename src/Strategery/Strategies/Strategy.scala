package Strategery.Strategies

import Information.Intelligenze.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.Plan
import Strategery.{StarCraftMap, StrategyEvaluation, StrategyLegality}
import bwapi.Race

abstract class Strategy {
  
  override def toString: String = getClass.getSimpleName.replace("$", "")
  
  def gameplan: Option[Plan] = { None }
  
  def choices: Iterable[Iterable[Strategy]]     = Iterable.empty
  
  def islandMaps              : Boolean                         = false
  def groundMaps              : Boolean                         = true
  def entranceRamped          : Boolean                         = true
  def entranceFlat            : Boolean                         = true
  def entranceInverted        : Boolean                         = true
  def rushDistanceMinimum     : Double                          = Double.NegativeInfinity
  def rushDistanceMaximum     : Double                          = Double.PositiveInfinity
  def multipleEntrances       : Boolean                         = true
  def ourRaces                : Iterable[Race]                  = Vector(Race.Terran, Race.Protoss, Race.Zerg)
  def enemyRaces              : Iterable[Race]                  = Vector(Race.Terran, Race.Protoss, Race.Zerg, Race.Unknown)
  def startLocationsMin       : Int                             = 2
  def startLocationsMax       : Int                             = 1000
  def ffa                     : Boolean                         = false
  def opponentsWhitelisted    : Option[Iterable[String]]        = None
  def mapsBlacklisted         : Iterable[StarCraftMap]          = Vector.empty
  def mapsWhitelisted         : Option[Iterable[StarCraftMap]]  = None
  def responsesBlacklisted    : Iterable[Fingerprint]           = Vector.empty
  def responsesWhitelisted    : Iterable[Fingerprint]           = Vector.empty
  def allowedVsHuman          : Boolean                         = false
  def minimumGamesVsOpponent  : Int                             = 0
  
  def active: Boolean = With.strategy.selectedCurrently.contains(this)

  def legality: StrategyLegality      = With.strategy.legalities(this)
  def evaluation: StrategyEvaluation  = With.strategy.evaluations(this)
}
