package ProxyBwapi.UnitInfo

import ProxyBwapi.UnitTracking.UnorderedBuffer

trait Targeted {
  private var _targetingIndex = 0
  private val targeting = Array.fill(2) { new UnorderedBuffer[UnitInfo]() }

  def targetedBy: Seq[UnitInfo] = _targetedBy
  def targetedByBefore: Seq[UnitInfo] = _targetedByBefore

  def resetTargeting(): Unit = {
    _targetingIndex += 1
    _targetingIndex %= 2
    _targetedBy.clear()
  }

  def addTargeter(targeter: UnitInfo): Unit = {
    _targetedBy.add(targeter)
  }

  private def _targetedBy       : UnorderedBuffer[UnitInfo] = targeting(    _targetingIndex)
  private def _targetedByBefore : UnorderedBuffer[UnitInfo] = targeting(1 - _targetingIndex)
}
