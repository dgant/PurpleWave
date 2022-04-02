package Strategery.Strategies

import Debugging.{SimpleString, ToString}
import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.Plan
import Strategery.{StarCraftMap, StrategyEvaluation, StrategyLegality}
import bwapi.Race

abstract class Strategy extends SimpleString {
  
  override val toString: String = ToString(this)
  
  def gameplan: Option[Plan] = { None }
  
  def choices                 : Iterable[Iterable[Strategy]]    = Iterable.empty
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
  def activate(): Boolean = { With.strategy.activate(this) }

  /**
    * Flag a strategy as inactive, in order to avoid crediting it with our success/failure this game
    * This is appropriate in situations where our reactions cause us to follow a different strategy
    * prior to the strategy having had any impact on the game
    */
  def deactivate(): Unit = { With.strategy.deactivate(this) }

  /**
    * Permanently include a branch in our chosen strategy.
    * This doesn't check whether the new strategy is a branch that would be legal on initial selection;
    * it's totally fine if it not.
    */
  def swapIn(): Unit = { With.strategy.swapIn(this) }

  /**
    * Permanently removes a branch from our chosen strategy.
    * This doesn't check whether the new strategy is a branch that would be legal on initial selection;
    * it's totally fine if it not.
    */
  def swapOut(): Unit = { With.strategy.swapOut(this) }

  def apply(): Boolean = { With.strategy.isActive(this) }

  def legality: StrategyLegality      = With.strategy.legalities(this)
  def evaluation: StrategyEvaluation  = With.strategy.evaluations(this)
}
