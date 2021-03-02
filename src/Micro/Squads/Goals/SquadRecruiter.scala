package Micro.Squads.Goals

import Mathematics.Points.{Pixel, SpecificPoints}
import Micro.Squads.QualityCounter.QualityCounter
import Micro.Squads.Squad
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable.ArrayBuffer

trait SquadRecruiter {
  def squad: Squad

  protected var _qualityCounter: QualityCounter = _
  protected val _candidates = new ArrayBuffer[FriendlyUnitInfo]

  var vicinity: Pixel = SpecificPoints.middle

  def candidates: Seq[FriendlyUnitInfo] = _candidates

  def candidateValue(candidate: FriendlyUnitInfo): Double = _qualityCounter.utility(candidate)

  def addUnit(candidate: FriendlyUnitInfo): Unit = {
    _candidates += candidate
    _qualityCounter.countUnit(candidate)
  }

  def onSquadCommission(): Unit = {
    _candidates.clear()
    _qualityCounter = new QualityCounter
    squad.enemies.foreach(_qualityCounter.countUnit)
  }
}
