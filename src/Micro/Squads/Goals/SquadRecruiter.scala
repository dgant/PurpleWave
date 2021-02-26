package Micro.Squads.Goals

import Micro.Squads.QualityCounter.QualityCounter
import Micro.Squads.Squad
import Planning.UnitMatchers.UnitMatcher
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.CountMap

import scala.collection.mutable.ArrayBuffer

trait SquadRecruiter {
  def squad: Squad

  protected var qualityCounter: QualityCounter = _
  protected val _candidates = new ArrayBuffer[FriendlyUnitInfo]

  def candidates: Seq[FriendlyUnitInfo] = _candidates

  def candidateValue(candidate: FriendlyUnitInfo): Double = qualityCounter.utility(candidate)

  def addCandidate(candidate: FriendlyUnitInfo): Unit = {
    _candidates += candidate
    qualityCounter.countUnit(candidate)
  }

  def resetCandidates(): Unit = {
    _candidates.clear()
    qualityCounter = new QualityCounter
    qualityCounter.setNeeds(new CountMap[UnitMatcher])
    squad.enemies.foreach(qualityCounter.countUnit)
  }
}
