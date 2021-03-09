package ProxyBwapi.UnitInfo

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import ProxyBwapi.ConvertBWAPI
import ProxyBwapi.Players.{PlayerInfo, Players}
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.{UnitClass, UnitClasses}
import ProxyBwapi.UnitTracking.Visibility
import ProxyBwapi.Upgrades.Upgrade

import scala.collection.JavaConverters._

abstract class BWAPICachedUnitProxy(bwapiUnit: bwapi.Unit, id: Int) extends UnitInfo(bwapiUnit, id) {
  private var _visibility             : Visibility.Value = _
  private var _player                 : PlayerInfo = _
  private var _unitClass              : UnitClass = _
  private var _pixel                  : Pixel = _
  private var _pixelObserved          : Pixel = _
  private var _tile                   : Tile = _
  private var _tileTopLeft            : Tile = _
  private var _alive                  : Boolean = _
  private var _complete               : Boolean = _
  private var _burrowed               : Boolean = _
  private var _cloaked                : Boolean = _
  private var _detected               : Boolean = _
  private var _flying                 : Boolean = _
  private var _plagued                : Boolean = _
  private var _ensnared               : Boolean = _
  private var _invincible             : Boolean = _
  private var _irradiated             : Boolean = _
  private var _lockedDown             : Boolean = _
  private var _maelstrommed           : Boolean = _
  private var _stasised               : Boolean = _
  private var _stimmed                : Boolean = _
  private var _morphing               : Boolean = _
  private var _constructing           : Boolean = _
  private var _repairing              : Boolean = _
  private var _researching            : Boolean = _
  private var _training               : Boolean = _
  private var _upgrading              : Boolean = _
  private var _beingConstructed       : Boolean = _
  private var _beingGathered          : Boolean = _
  private var _beingHealed            : Boolean = _
  private var _blind                  : Boolean = _
  private var _carryingMinerals       : Boolean = _
  private var _carryingGas            : Boolean = _
  private var _powered                : Boolean = _
  private var _selected               : Boolean = _
  private var _underDarkSwarm         : Boolean = _
  private var _underDisruptionWeb     : Boolean = _
  private var _underStorm             : Boolean = _
  private var _hasNuke                : Boolean = _
  private var _loaded                 : Boolean = _
  private var _lastSeen               : Int = _
  private var _resourcesInitial       : Int = _
  private var _resourcesLeft          : Int = _
  private var _hitPoints              : Int = _
  private var _shieldPoints           : Int = _
  private var _matrixPoints           : Int = _
  private var _energy                 : Int = _
  private var _scarabs                : Int = _
  private var _cooldownRaw            : Int = _
  private var _cooldownSpell          : Int = _
  private var _remainingTrainFrames   : Int = _
  private var _remainingUpgradeFrames : Int = _
  private var _remainingTechFrames    : Int = _
  private var _removalFrames          : Int = _
  private var _spiderMines            : Int = _
  private var _angleRadians           : Double = _
  private var _velocityX              : Double = _
  private var _velocityY              : Double = _
  private var _order                  : String              = "Stop"
  private var _orderTarget            : Option[UnitInfo]    = None
  private var _orderTargetPixel       : Option[Pixel]       = None
  private var _target                 : Option[UnitInfo]    = None
  private var _targetPixel            : Option[Pixel]       = None
  private var _techProducing          : Option[Tech]        = None
  private var _upgradeProducing       : Option[Upgrade]     = None
  private var _addon                  : Option[UnitInfo]    = None
  private var _buildType              : UnitClass           = _
  private var _trainingQueue          : Seq[UnitClass]      = Seq.empty
  private var _interceptors           : Seq[UnitInfo]       = Seq.empty
  private var _interceptorCount       : Int                 = _
  private var _transport              : Option[FriendlyUnitInfo] = None
  @inline final def visibility              : Visibility.Value  = _visibility
  @inline final def player                  : PlayerInfo        = _player
  @inline final def unitClass               : UnitClass         = _unitClass
  @inline final def pixel                   : Pixel             = _pixel
  @inline final def pixelObserved           : Pixel             = _pixelObserved
  @inline final def tile                    : Tile              = _tile
  @inline final def tileTopLeft             : Tile              = _tileTopLeft
  @inline final def visible                 : Boolean           = visibility == Visibility.Visible
  @inline final def alive                   : Boolean           = _alive
  @inline final def complete                : Boolean           = _complete
  @inline final def burrowed                : Boolean           = _burrowed
  @inline final def cloaked                 : Boolean           = _cloaked
  @inline final def detected                : Boolean           = _detected
  @inline final def flying                  : Boolean           = _flying
  @inline final def plagued                 : Boolean           = _plagued
  @inline final def ensnared                : Boolean           = _ensnared
  @inline final def invincible              : Boolean           = _invincible
  @inline final def irradiated              : Boolean           = _irradiated
  @inline final def lockedDown              : Boolean           = _lockedDown
  @inline final def maelstrommed            : Boolean           = _maelstrommed
  @inline final def stasised                : Boolean           = _stasised
  @inline final def stimmed                 : Boolean           = _stimmed
  @inline final def morphing                : Boolean           = _morphing
  @inline final def constructing            : Boolean           = _constructing
  @inline final def repairing               : Boolean           = _repairing
  @inline final def teching                 : Boolean           = _researching // TODO: Resolve name
  @inline final def training                : Boolean           = _training
  @inline final def upgrading               : Boolean           = _upgrading
  @inline final def beingConstructed        : Boolean           = _beingConstructed
  @inline final def beingGathered           : Boolean           = _beingGathered
  @inline final def beingHealed             : Boolean           = _beingHealed
  @inline final def blind                   : Boolean           = _blind
  @inline final def carryingMinerals        : Boolean           = _carryingMinerals
  @inline final def carryingGas             : Boolean           = _carryingGas
  @inline final def powered                 : Boolean           = _powered
  @inline final def selected                : Boolean           = _selected
  @inline final def underDarkSwarm          : Boolean           = _underDarkSwarm
  @inline final def underDisruptionWeb      : Boolean           = _underDisruptionWeb
  @inline final def underStorm              : Boolean           = _underStorm
  @inline final def hasNuke                 : Boolean           = _hasNuke
  @inline final def loaded                  : Boolean           = _loaded
  @inline final def lastSeen                : Int               = _lastSeen
  @inline final def initialResources        : Int               = _resourcesInitial
  @inline final def resourcesLeft           : Int               = _resourcesLeft
  @inline final def hitPoints               : Int               = _hitPoints
  @inline final def shieldPoints            : Int               = _shieldPoints
  @inline final def matrixPoints            : Int               = _matrixPoints
  @inline final def energy                  : Int               = _energy
  @inline final def scarabs                 : Int               = _scarabs
  @inline final def cooldownRaw             : Int               = _cooldownRaw
  @inline final def cooldownGround          : Int               = _cooldownRaw // TODO: Remove
  @inline final def cooldownAir             : Int               = _cooldownRaw // TODO: Remove
  @inline final def cooldownSpell           : Int               = _cooldownSpell
  @inline final def remainingTrainFrames    : Int               = _remainingTrainFrames
  @inline final def remainingUpgradeFrames  : Int               = _remainingUpgradeFrames
  @inline final def remainingTechFrames     : Int               = _remainingTechFrames
  @inline final def removalFrames           : Int               = _removalFrames // TODO: Rename
  @inline final def spiderMines             : Int               = _spiderMines
  @inline final def angleRadians            : Double            = _angleRadians
  @inline final def velocityX               : Double            = _velocityX
  @inline final def velocityY               : Double            = _velocityY
  @inline final def order                   : String            = _order
  @inline final def orderTarget             : Option[UnitInfo]  = _orderTarget
  @inline final def orderTargetPixel        : Option[Pixel]     = _orderTargetPixel
  @inline final def target                  : Option[UnitInfo]  = _target
  @inline final def targetPixel             : Option[Pixel]     = _targetPixel
  @inline final def techProducing           : Option[Tech]      = _techProducing
  @inline final def upgradeProducing        : Option[Upgrade]   = _upgradeProducing
  @inline final def addon                   : Option[UnitInfo]  = _addon
  @inline final def buildType               : UnitClass         = _buildType
  @inline final def trainingQueue           : Seq[UnitClass]    = _trainingQueue
  @inline final def interceptors            : Seq[UnitInfo]     = _interceptors
  @inline final def interceptorCount        : Int               = _interceptorCount
  @inline final def transport               : Option[FriendlyUnitInfo] = _transport
  def readProxy(): Unit = {
    if (With.frame == 0 || bwapiUnit.isVisible(With.self.bwapiPlayer)) {
      changeVisibility(Visibility.Visible)
      _player                 = Players.get(bwapiUnit.getPlayer)
      _unitClass              = UnitClasses.get(bwapiUnit.getType)
      changePixel(new Pixel(bwapiUnit.getPosition))
      _pixelObserved          = _pixel
      _tileTopLeft            = new Tile(bwapiUnit.getTilePosition) // Set this via changePixel()
      _complete               = bwapiUnit.isCompleted
      _burrowed               = bwapiUnit.isBurrowed
      _cloaked                = bwapiUnit.isCloaked
      _detected               = bwapiUnit.isDetected
      _flying                 = _unitClass.isFlyer || (_unitClass.isFlyingBuilding && bwapiUnit.isLifted)
      _plagued                = bwapiUnit.isPlagued
      _ensnared               = bwapiUnit.isEnsnared
      _invincible             = bwapiUnit.isInvincible
      _irradiated             = _unitClass.isOrganic    && bwapiUnit.isIrradiated
      _lockedDown             = _unitClass.isMechanical && bwapiUnit.isLockedDown
      _maelstrommed           = _unitClass.isOrganic    && bwapiUnit.isMaelstrommed
      _stasised               = ! _unitClass.isBuilding && bwapiUnit.isStasised
      _stimmed                = _unitClass.isTerran     && _unitClass.isOrganic && _unitClass.canAttack && bwapiUnit.isStimmed
      _morphing               = bwapiUnit.isMorphing
      _constructing           = _unitClass.isWorker && bwapiUnit.isConstructing
      _repairing              = bwapiUnit.isRepairing
      _researching            = bwapiUnit.isResearching
      _training               = bwapiUnit.isTraining
      _upgrading              = bwapiUnit.isUpgrading
      _beingConstructed       = bwapiUnit.isBeingConstructed
      _beingGathered          = bwapiUnit.isBeingGathered
      _beingHealed            = bwapiUnit.isBeingHealed
      _blind                  = bwapiUnit.isBlind
      _carryingMinerals       = bwapiUnit.isCarryingMinerals
      _carryingGas            = bwapiUnit.isCarryingGas
      _powered                = bwapiUnit.isPowered
      _selected               = bwapiUnit.isSelected
      _underDarkSwarm         = bwapiUnit.isUnderDarkSwarm
      _underDisruptionWeb     = bwapiUnit.isUnderDisruptionWeb
      _underStorm             = bwapiUnit.isUnderStorm
      _hasNuke                = bwapiUnit.hasNuke
      _resourcesInitial       = bwapiUnit.getInitialResources // TODO: Only get once? Or maybe faster to just get every time and not check
      _resourcesLeft          = bwapiUnit.getResources
      _cooldownRaw            = Math.max(bwapiUnit.getAirWeaponCooldown, bwapiUnit.getGroundWeaponCooldown) // TODO: Calculate up front for each unit?
      _cooldownSpell          = bwapiUnit.getSpellCooldown
      _remainingTrainFrames   = bwapiUnit.getRemainingTrainTime
      _remainingUpgradeFrames = bwapiUnit.getRemainingUpgradeTime
      _remainingTechFrames    = bwapiUnit.getRemainingResearchTime
      _removalFrames          = bwapiUnit.getRemoveTimer
      _spiderMines            = bwapiUnit.getSpiderMineCount
      _angleRadians           = bwapiUnit.getAngle
      _velocityX              = bwapiUnit.getVelocityX
      _velocityY              = bwapiUnit.getVelocityY
      _order                  = bwapiUnit.getOrder.toString
      _orderTarget            = With.units.get(bwapiUnit.getOrderTarget)
      _orderTargetPixel       = ConvertBWAPI.position(bwapiUnit.getOrderTargetPosition)
      _target                 = With.units.get(bwapiUnit.getTarget)
      _targetPixel            = ConvertBWAPI.position(bwapiUnit.getTargetPosition)
      _addon                  = With.units.get(bwapiUnit.getAddon)
      if (_player.isUs) {
        _loaded               = bwapiUnit.isLoaded
        _hitPoints            = bwapiUnit.getHitPoints
        _shieldPoints         = bwapiUnit.getShields
        _matrixPoints         = bwapiUnit.getDefenseMatrixPoints
        _energy               = bwapiUnit.getEnergy
        _scarabs              = bwapiUnit.getScarabCount
        _techProducing        = if (_researching) ConvertBWAPI.tech(bwapiUnit.getTech) else None
        _upgradeProducing     = if (_upgrading) ConvertBWAPI.upgrade(bwapiUnit.getUpgrade) else None
        _trainingQueue        = if (training) Range(0, bwapiUnit.getTrainingQueueCount).map(i => UnitClasses.get(bwapiUnit.getTrainingQueueAt(i))) else Seq.empty
        _interceptorCount     = if (unitClass == Protoss.Carrier) bwapiUnit.getInterceptorCount else 0
        _interceptors         = if (_interceptorCount > 0) bwapiUnit.getInterceptors.asScala.flatMap(With.units.get) else Seq.empty // TODO: Slow because JBWAPI checks every unit for every carrier
        _buildType            = UnitClasses.get(bwapiUnit.getBuildType)
        _transport            = if (loaded) With.units.get(bwapiUnit.getTransport).flatMap(_.friendly) else None
      } else {
        _scarabs              = if (unitClass == Protoss.Reaver) 5 else 0
        _hitPoints            = if (_detected || ! _cloaked) bwapiUnit.getHitPoints           else if (_hitPoints == 0) _unitClass.maxHitPoints else _hitPoints
        _shieldPoints         = if (_detected || ! _cloaked) bwapiUnit.getShields             else if (_hitPoints == 0) _unitClass.maxShields   else _shieldPoints
        _matrixPoints         = if (_detected || ! _cloaked) bwapiUnit.getDefenseMatrixPoints else _matrixPoints
        // TODO: Model energy
      }
    } else if (_player.isEnemy) {
      if (With.framesSince(_lastSeen) > 24 && is(Terran.SiegeTankUnsieged) && With.unitsShown(_player, Terran.SiegeTankSieged) > 0) {
        _unitClass = Terran.SiegeTankSieged
      }
    }
  }

  @inline final def changePixel(newPixel: Pixel): Unit = {
    _pixel = newPixel
    _tile  = newPixel.tile
    // TODO: Track previous values
    // On position change, update grids
  }

  @inline final def changeVisibility(value: Visibility.Value): Unit = {
    _visibility = value
    _visibility match {
      case Visibility.Visible           => _burrowed = bwapiUnit.isBurrowed ; _alive = true; _lastSeen = With.frame
      case Visibility.InvisibleBurrowed => _burrowed = true                 ; _alive = true; _detected = false
      case Visibility.InvisibleNearby   => _burrowed = false                ; _alive = true; _detected = false
      case Visibility.InvisibleMissing  => _burrowed = false                ; _alive = true; _detected = false
      case Visibility.Dead              => _alive = false
    }
  }
}
