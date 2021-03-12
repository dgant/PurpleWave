package Strategery.Strategies

import Debugging.{SimpleString, ToString}
import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.Plan
import Strategery.{StarCraftMap, StrategyEvaluation, StrategyLegality}
import bwapi.Race

import scala.collection.mutable

abstract class Strategy extends SimpleString {
  
  override val toString: String = ToString(this)
  
  def gameplan: Option[Plan] = { None }
  
  def choices                 : Iterable[Iterable[Strategy]]    = Iterable.empty
  def alternatives            : mutable.Set[Strategy]           = mutable.HashSet.empty
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

  /**
    * Flag a strategy as being salient to the gameplay.
    * This allows us to distinguish between strategy choices that had an effect on the game (the opening, perhaps)
    * versus those that didn't impact it at all (a late game strategy for a game lasting three minutes, for example)
    */
  def registerActive(): Boolean = {
    val output = With.strategy.selectedCurrently.contains(this)
    if (output) {
      With.strategy.registerActive(this)
      // Register any *alternatives* to this strategy as active,
      // as
      With.strategy.selectedCurrently.view
        .map(_.choices)
        .foreach(_.filter(_.exists(_ == this)).foreach(_.foreach(With.strategy.registerActive)))
    }
    output
  }

  def legality: StrategyLegality      = With.strategy.legalities(this)
  def evaluation: StrategyEvaluation  = With.strategy.evaluations(this)
}
