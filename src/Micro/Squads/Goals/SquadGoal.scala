package Micro.Squads.Goals

import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Micro.Agency.Intention
import Micro.Squads.{QualityCounter, Squad, SquadBatch}
import Planning.UnitCounters.{UnitCountEverything, UnitCounter}
import Planning.UnitMatchers.{UnitMatchRecruitableForCombat, UnitMatcher}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

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
    // After some experimentation with graphing it, I like 1 / sqrt(tiles/8 + 1)
    ByOption.max(destinations.map(d => 1.0 / PurpleMath.fastInverseSqrt(candidate.pixelDistanceTravelling(d) / 8 / 32 + 1))).getOrElse(1.0)
  }
}

trait SquadRecruiter extends SquadGoalWithSquad {
  def inherentValue: Double
  def addCandidate(candidate: FriendlyUnitInfo): Unit
  def candidates: Seq[FriendlyUnitInfo]
  def candidateWelcome(batch: SquadBatch, candidate: FriendlyUnitInfo): Boolean
  def candidateDistance(candidate: FriendlyUnitInfo): Double
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
    unitMatcher.accept(candidate) && unitCounter.accept(candidates)
  }
  def candidateDistance(candidate: FriendlyUnitInfo): Double = ByOption.max(destinations.map(candidate.pixelDistanceTravelling)).getOrElse(10 * With.mapPixelWidth)
  def candidateValue(batch: SquadBatch, candidate: FriendlyUnitInfo): Double = {
    invalidateBatch(batch)
    ProximityValue(candidate, destinations) * qualityCounter.utility(candidate)
  }
  protected def invalidateBatch(batch: SquadBatch): Unit = {
    if (batch != currentBatch) {
      currentBatch = batch
      qualityCounter = new QualityCounter
      _candidates.clear()
      squad.enemies.foreach(qualityCounter.countUnit)
    }
  }

  protected var currentBatch: SquadBatch = _
  protected var qualityCounter: QualityCounter = _
  protected val _candidates = new ArrayBuffer[FriendlyUnitInfo]
  var unitMatcher: UnitMatcher = UnitMatchRecruitableForCombat
  var unitCounter: UnitCounter = UnitCountEverything
}

trait SquadGoal extends SquadRecruiter {
  def squad: Squad
  def setSquad(squad: Squad): Unit
  def run(): Unit
  def destination: Pixel = With.intelligence.mostBaselikeEnemyTile.pixelCenter
  override def toString: String = getClass.getSimpleName.replaceAllLiterally("$", "")
}

trait SquadGoalBasic extends SquadGoal with SquadRecruiterSimple with SquadGoalWithSquad {
  def run() {
    squad.units.foreach(_.agent.intend(squad.client, new Intention { toTravel = Some(destination) }))
  }
}