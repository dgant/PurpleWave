package ProxyBwapi.UnitInfo

import ProxyBwapi.UnitTracking.UnorderedBuffer

import scala.collection.mutable

trait Targeted {
  private var _targetersIndex           = 0
  private val _targeters                = Array.fill(2) { new UnorderedBuffer[UnitInfo]() }
  private val _targetedByRecently       = new mutable.HashSet[UnitInfo]
  private val _targetedByRecentlyMelee  = new mutable.HashSet[UnitInfo]

  def targetedByNow           : Seq[UnitInfo]       = _targetedByNow
  def targetedByBefore        : Seq[UnitInfo]       = _targetedByBefore
  def targetedByRecently      : Iterable[UnitInfo]  = _targetedByRecently.view
  def targetedByRecentlyMelee : Iterable[UnitInfo]  = _targetedByRecentlyMelee.view

  def resetTargeting(): Unit = {
    _targetersIndex += 1
    _targetersIndex %= 2
    _targetedByNow.clear()
    _targetedByRecently.clear()
    _targetedByRecentlyMelee ++= _targetedByBefore
    _targetedByRecentlyMelee ++= _targetedByBefore.view.filter(_.unitClass.melee)
  }

  def addTargeter(targeter: UnitInfo): Unit = {
    _targetedByNow.add(targeter)
    _targetedByRecently += targeter
    if (targeter.unitClass.melee) {
      _targetedByRecentlyMelee += targeter
    }
  }

  private def _targetedByNow    : UnorderedBuffer[UnitInfo] = _targeters(    _targetersIndex)
  private def _targetedByBefore : UnorderedBuffer[UnitInfo] = _targeters(1 - _targetersIndex)
}
