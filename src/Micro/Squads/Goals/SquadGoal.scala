package Micro.Squads.Goals

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Agency.Intention
import Micro.Squads.{QualityCounter, Squad, SquadBatch}
import Planning.UnitCounters.{CountEverything, UnitCounter}
import Planning.UnitMatchers.{MatchAnything, UnitMatcher}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.{ByOption, CountMap}

import scala.collection.mutable.ArrayBuffer

trait SquadGoalWithSquad {
  def squad: Squad = _squad
  def setSquad(squad: Squad): Unit = {
    _squad = squad
  }
  private var _squad: Squad = _
}

object ProximityValue {
  def apply(candidate: FriendlyUnitInfo, destinations: Seq[Pixel]): Double = {
    // We want to reward being close but not let the multiplicative factor blow up as the unit gets very close.
    // After some experimentation with graphing it, I like 1 / sqrt(1 + tiles/8)
    ByOption.max(destinations.map(d => 1.0 / Math.sqrt(1.0 + candidate.pixelDistanceTravelling(d) / 8 / 32))).getOrElse(1.0)
  }
}

trait SquadRecruiter extends SquadGoalWithSquad {
  def inherentValue: Double
  def addCandidate(candidate: FriendlyUnitInfo): Unit
  def candidates: Seq[FriendlyUnitInfo]
  def candidateWelcome(batch: SquadBatch, candidate: FriendlyUnitInfo): Boolean
  def candidateValue(batch: SquadBatch, candidate: FriendlyUnitInfo): Double
}

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

trait SquadGoal extends SquadRecruiter {
  def squad: Squad
  def setSquad(squad: Squad): Unit
  def run(): Unit
  def destination: Pixel = With.scouting.mostBaselikeEnemyTile.pixelCenter
  override def toString: String = getClass.getSimpleName.replaceAllLiterally("$", "")
}

trait SquadGoalBasic extends SquadGoal with SquadRecruiterSimple with SquadGoalWithSquad {
  def run() {
    squad.units.foreach(_.agent.intend(this, new Intention { toTravel = Some(destination) }))
  }
}