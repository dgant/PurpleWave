package Strategery.Strategies

import Debugging.ToString
import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Macro.Facts.MacroCounting
import Performance.GameCache
import Planning.Plans.Plan
import Strategery.{StarCraftMap, StrategyEvaluation, StrategyLegality}
import Utilities.{?, LightYear}
import bwapi.Race

abstract class Strategy extends MacroCounting {
  override val toString: String = ToString(this)

  private var _islandMaps             : Boolean             = false
  private var _groundMaps             : Boolean             = true
  private var _entranceRamped         : Boolean             = true
  private var _entranceFlat           : Boolean             = true
  private var _entranceInverted       : Boolean             = true
  private var _ffa                    : Boolean             = false
  private var _moneyMap               : Boolean             = false
  private var _allowedVsHuman         : Boolean             = true
  private var _rushTilesMinimum       : Int                 = - LightYear()
  private var _rushTilesMaximum       : Int                 =   LightYear()
  private var _startLocationsMin      : Int                 = 2
  private var _startLocationsMax      : Int                 = 16
  private var _minimumGamesVsOpponent : Int                 = 0
  private var _workerDelta            : Int                 = 0
  private var _ourRaces               : Seq[Race]           = Seq(Race.Terran, Race.Protoss, Race.Zerg)
  private var _enemyRaces             : Seq[Race]           = Seq(Race.Terran, Race.Protoss, Race.Zerg, Race.Unknown)
  private var _mapsBlacklisted        : Seq[StarCraftMap]   = Seq.empty
  private var _mapsWhitelisted        : Seq[StarCraftMap]   = Seq.empty
  private var _responsesBlacklisted   : Seq[Fingerprint]    = Seq.empty
  private var _responsesWhitelisted   : Seq[Fingerprint]    = Seq.empty
  private var _choices                : Seq[Seq[Strategy]]  = Seq.empty
  private var _selectionRequirements  : Seq[() => Boolean]  = Seq.empty
  private var _activationRequirements : Seq[() => Boolean]  = Seq.empty

  def gameplan                : Option[Plan]        = None
  def islandMaps              : Boolean             = _islandMaps
  def groundMaps              : Boolean             = _groundMaps
  def entranceRamped          : Boolean             = _entranceRamped
  def entranceFlat            : Boolean             = _entranceFlat
  def entranceInverted        : Boolean             = _entranceInverted
  def ffa                     : Boolean             = _ffa
  def moneyMap                : Boolean             = _moneyMap
  def allowedVsHuman          : Boolean             = _allowedVsHuman
  def rushTilesMinimum        : Int                 = _rushTilesMinimum
  def rushTilesMaximum        : Int                 = _rushTilesMaximum
  def startLocationsMin       : Int                 = _startLocationsMin
  def startLocationsMax       : Int                 = _startLocationsMax
  def minimumGamesVsOpponent  : Int                 = _minimumGamesVsOpponent
  def workerDelta             : Int                 = _workerDelta
  def ourRaces                : Seq[Race]           = _ourRaces
  def enemyRaces              : Seq[Race]           = _enemyRaces
  def mapsBlacklisted         : Seq[StarCraftMap]   = _mapsBlacklisted
  def mapsWhitelisted         : Seq[StarCraftMap]   = _mapsWhitelisted
  def responsesBlacklisted    : Seq[Fingerprint]    = _responsesBlacklisted
  def responsesWhitelisted    : Seq[Fingerprint]    = _responsesWhitelisted
  def selectionRequirements   : Seq[() => Boolean]  = _selectionRequirements
  def activationRequirements  : Seq[() => Boolean]  = _activationRequirements
  def choices                 : Seq[Seq[Strategy]]  = _choices

  /////////////////////////////////////////////////////
  // 2022 style of implementing strategies: Mutators //
  /////////////////////////////////////////////////////

  def setIslandMaps             (value: Boolean)              : Unit = _islandMaps              = value
  def setGroundMaps             (value: Boolean)              : Unit = _groundMaps              = value
  def setEntranceRamped         (value: Boolean)              : Unit = _entranceRamped          = value
  def setEntranceFlat           (value: Boolean)              : Unit = _entranceFlat            = value
  def setEntranceInverted       (value: Boolean)              : Unit = _entranceInverted        = value
  def setFFA                    (value: Boolean)              : Unit = _ffa                     = value
  def setMoneyMap               (value: Boolean)              : Unit = _moneyMap                = value
  def setAllowedVsHuman         (value: Boolean)              : Unit = _allowedVsHuman          = value
  def setRushTilesMinimum       (value: Int)                  : Unit = _rushTilesMinimum        = value
  def setRushTilesMaximum       (value: Int)                  : Unit = _rushTilesMaximum        = value
  def setStartLocationsMin      (value: Int)                  : Unit = _startLocationsMin       = value
  def setStartLocationsMax      (value: Int)                  : Unit = _startLocationsMax       = value
  def setMinimumGamesVsOpponent (value: Int)                  : Unit = _minimumGamesVsOpponent  = value
  def setWorkerDelta            (value: Int)                  : Unit = _workerDelta             = value
  def setOurRace                (values: Race*)               : Unit = _ourRaces                = values
  def setEnemyRace              (values: Race*)               : Unit = _enemyRaces              = values
  def whitelistOn               (maps: StarCraftMap*)         : Unit = _mapsWhitelisted       ++= maps
  def blacklistOn               (maps: StarCraftMap*)         : Unit = _mapsBlacklisted       ++= maps
  def whitelistVs               (fingerprints: Fingerprint*)  : Unit = _responsesWhitelisted  ++= fingerprints
  def blacklistVs               (fingerprints: Fingerprint*)  : Unit = _responsesBlacklisted  ++= fingerprints
  def addChoice                 (strategies: Strategy*)       : Unit = _choices                 = _choices :+ strategies
  def addSelectionRequirement   (predicate: () => Boolean)    : Unit = _selectionRequirements   = _selectionRequirements  :+ predicate
  def addActivationRequirement  (predicate: () => Boolean)    : Unit = _activationRequirements  = _activationRequirements :+ predicate

  /**
    * Flag a strategy as being salient to the gameplay.
    * This allows us to distinguish between strategy choices that had an effect on the game (the opening, perhaps)
    * versus those that didn't impact it at all (a late game strategy for a game lasting three minutes, for example)
    */
  def activate(): Boolean = With.strategy.activate(this)
  def apply(shouldActivate: Boolean = true): Boolean = ?(shouldActivate, activate(), selected)

  /**
    * Flag a strategy as inactive, in order to avoid crediting it with our success/failure this game
    * This is appropriate in situations where our reactions cause us to follow a different strategy
    * prior to the strategy having had any impact on the game
    */
  def deactivate(): Unit = With.strategy.deactivate(this)

  /**
    * Permanently include a branch in our chosen strategy.
    * This doesn't check whether the new strategy is a branch that would be legal on initial selection;
    * it's totally fine if it not.
    */
  def swapIn(): Unit = With.strategy.swapIn(this)

  /**
    * Permanently removes a branch from our chosen strategy.
    * This doesn't check whether the new strategy is a branch that would be legal on initial selection;
    * it's totally fine if it not.
    */
  def swapOut(): Unit = With.strategy.swapOut(this)

  def active      : Boolean             = With.strategy.isActive(this)
  def selected    : Boolean             = With.strategy.isSelected(this)
  def legal       : Boolean             = legality.isLegal
  def legality    : StrategyLegality    = _legality()
  def evaluation  : StrategyEvaluation  = _evaluation()

  private val _legality   = new GameCache(() => new StrategyLegality(this))
  private val _evaluation = new GameCache(() => new StrategyEvaluation(this))
}
