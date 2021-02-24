package Micro.Squads.Recruitment

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Squads.{QualityCounter, SquadBatch}
import Planning.UnitCounters.{CountEverything, UnitCounter}
import Planning.UnitMatchers.{MatchAnything, UnitMatcher}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.{ByOption, CountMap}

import scala.collection.mutable.ArrayBuffer

trait SquadRecruiterSimple extends SquadRecruiter {
  def destination: Pixel
  def destinations: Seq[Pixel] = Seq(destination)
  def inherentValue: Double = 1.0

  def addCandidate(candidate: FriendlyUnitInfo): Unit = {
    _candidates += candidate
    qualityCounter.countUnit(candidate)
  }
  def candidates: Seq[FriendlyUnitInfo] = _candidates
  def candidateWelcome(batch: SquadBatch, candidate: FriendlyUnitInfo): Boolean = {
    invalidateBatch(batch)
    unitMatcher.apply(candidate) && unitCounter.continue(batch.assignments.find(_.squad == squad).map(_.units).getOrElse(Seq.empty))
  }
  def candidateDistance(candidate: FriendlyUnitInfo): Double = ByOption.max(destinations.map(candidate.pixelDistanceTravelling)).getOrElse(10 * With.mapPixelWidth)
  def candidateValue(batch: SquadBatch, candidate: FriendlyUnitInfo): Double = {
    invalidateBatch(batch)
    ProximityValue(candidate, destinations) * qualityCounter.utility(candidate)
  }
  def qualityNeeds: CountMap[UnitMatcher] = new CountMap[UnitMatcher]
  protected def invalidateBatch(batch: SquadBatch): Unit = {
    if (batch != currentBatch) {
      currentBatch = batch
      qualityCounter = new QualityCounter
      qualityCounter.setNeeds(qualityNeeds)
      _candidates.clear()
      squad.enemies.foreach(qualityCounter.countUnit)
    }
  }

  protected var currentBatch: SquadBatch = _
  protected var qualityCounter: QualityCounter = _
  protected val _candidates = new ArrayBuffer[FriendlyUnitInfo]
  var unitMatcher: UnitMatcher = MatchAnything
  var unitCounter: UnitCounter = CountEverything
}
