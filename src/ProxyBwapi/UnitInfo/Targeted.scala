package ProxyBwapi.UnitInfo

import Mathematics.Maff
import ProxyBwapi.UnitTracking.UnorderedBuffer

import scala.collection.mutable

trait Targeted {
  private var _caught                   = false
  private var _targetersIndex           = 0
  private val _targeters                = Array.fill(2) { new UnorderedBuffer[UnitInfo]() }
  private val _targetedByRecently       = new mutable.HashSet[UnitInfo]
  private val _targetedByRecentlyMelee  = new mutable.HashSet[UnitInfo]

  def caught                  : Boolean             = _caught
  def targetedByNow           : Seq[UnitInfo]       = _targetedByNow
  def targetedByBefore        : Seq[UnitInfo]       = _targetedByBefore
  def targetedByRecently      : Iterable[UnitInfo]  = _targetedByRecently.view
  def targetedByRecentlyMelee : Iterable[UnitInfo]  = _targetedByRecentlyMelee.view

  def resetTargeting(): Unit = {
    _targetersIndex = Maff.mod2(_targetersIndex + 1)
    _targetedByNow.clear()
    _targetedByRecently.clear()
    _targetedByRecentlyMelee ++= _targetedByBefore
    _targetedByRecentlyMelee ++= _targetedByBefore.view.filter(_.unitClass.melee)
    _caught = _targetedByRecently.exists(caughtBy)
  }

  def addTargeter(targeter: UnitInfo): Unit = {
    _caught ||= caughtBy(targeter)
    _targetedByNow.add(targeter)
    _targetedByRecently += targeter
    if (targeter.unitClass.melee) {
      _targetedByRecentlyMelee += targeter
    }
  }

  def caughtBy(targeter: UnitInfo): Boolean = {
    Option(asInstanceOf[UnitInfo]).exists(unit => targeter.topSpeed >= unit.topSpeed || ! unit.canAttack(targeter) || targeter.pixelRangeAgainst(unit) >= unit.pixelRangeAgainst(targeter))
  }

  private def _targetedByNow    : UnorderedBuffer[UnitInfo] = _targeters(    _targetersIndex)
  private def _targetedByBefore : UnorderedBuffer[UnitInfo] = _targeters(1 - _targetersIndex)
}
