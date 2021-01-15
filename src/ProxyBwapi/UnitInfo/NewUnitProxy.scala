package ProxyBwapi.UnitInfo

import Mathematics.Points.{Pixel, Tile}
import ProxyBwapi.Players.{PlayerInfo, Players}
import ProxyBwapi.UnitClasses.{UnitClass, UnitClasses}

class BWAPIUnitProxy(val id: Int) {
  private var _bwapiUnit    : bwapi.Unit = _
  private var _player       : PlayerInfo = _
  private var _alive        : Boolean = _
  private var _complete     : Boolean = _
  private var _hitPoints    : Int  = _
  private var _shieldPoints : Int = _
  private var _matrixPoints : Int = _
  private var _unitClass    : UnitClass = _
  private var _cooldownLeft : Int = _
  private var _pixel        : Pixel = _
  private var _tile         : Tile = _
  private var _tileTopLeft  : Tile = _
  private var _burrowed     : Boolean = _
  private var _cloaked      : Boolean = _
  private var _detected     : Boolean = _
  private var _flying       : Boolean = _
  @inline final def bwapiUnit     : bwapi.Unit  = _bwapiUnit
  @inline final def player        : PlayerInfo  = _player
  @inline final def alive         : Boolean     = _alive
  @inline final def complete      : Boolean     = _complete
  @inline final def hitPoints     : Int         = _hitPoints
  @inline final def shieldPoints  : Int         = _shieldPoints
  @inline final def matrixPoints  : Int         = _matrixPoints
  @inline final def unitClass     : UnitClass   = _unitClass
  @inline final def cooldownLeft  : Int         = _cooldownLeft
  @inline final def pixel         : Pixel       = _pixel
  @inline final def tile          : Tile        = _tile
  @inline final def tileTopLeft   : Tile        = _tileTopLeft
  @inline final def burrowed      : Boolean     = _burrowed
  @inline final def cloaked       : Boolean     = _cloaked
  @inline final def detected      : Boolean     = _detected
  @inline final def flying        : Boolean     = _flying
  def updateProxy(newBwapiUnit: bwapi.Unit): Unit = {
    _bwapiUnit    = newBwapiUnit
    _player       = Players.get(_bwapiUnit.getPlayer)
    _alive        = true
    _complete     = _bwapiUnit.isCompleted
    _hitPoints    = _bwapiUnit.getHitPoints
    _shieldPoints = _bwapiUnit.getShields
    _matrixPoints = _bwapiUnit.getDefenseMatrixPoints
    _unitClass    = UnitClasses.get(_bwapiUnit.getType)
    _cooldownLeft = Math.max(_bwapiUnit.getAirWeaponCooldown, _bwapiUnit.getGroundWeaponCooldown) // TODO: Correct?
    _tileTopLeft  = new Tile(_bwapiUnit.getTilePosition)
    _burrowed     = _bwapiUnit.isBurrowed
    _cloaked      = _bwapiUnit.isCloaked
    _detected     = _bwapiUnit.isDetected
    _flying       = _bwapiUnit.isFlying
    changePixel(new Pixel(_bwapiUnit.getPosition))
  }
  def changePixel(pixel: Pixel): Unit = {
    _pixel = pixel
    _tile  = pixel.tile
    // TODO: Track previous values
    // On position change, update grids
  }
  def this(originalBwapi: bwapi.Unit) = {
    this(originalBwapi.getID)
    updateProxy(originalBwapi)
  }
}
