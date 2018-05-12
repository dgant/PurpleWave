package Micro.Squads.Goals

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Agency.Intention
import Micro.Squads.RecruitmentLevel
import Micro.Squads.RecruitmentLevel.RecruitmentLevel
import Planning.Composition.UnitCountEverything
import Planning.Composition.UnitCounters.UnitCounter
import Planning.Composition.UnitMatchers.{UnitMatchWarriors, UnitMatcher}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.CountMap

import scala.collection.mutable

trait GoalBasic extends SquadGoal {
  
  ///////////////
  // Qualities //
  ///////////////
  
  protected trait Quality {
    def matches(u: UnitInfo): Boolean
    val counteredBy: Array[Quality]
    def counterScaling(input: Double): Double = input
  }
  
  final protected object Qualities {
    object Cloaked extends Quality {
      def matches(u: UnitInfo): Boolean = u.isAny(Terran.Ghost, Terran.Wraith, Protoss.Arbiter, Protoss.DarkTemplar, Protoss.Observer, Zerg.Lurker, Zerg.LurkerEgg)
      lazy val counteredBy: Array[Quality] = Array(Detector)
    }
    object Combat extends Quality {
      def matches(u: UnitInfo): Boolean = u.canAttack
      lazy val counteredBy: Array[Quality] = Array(Combat)
    }
    object Detector extends Quality {
      def matches(u: UnitInfo): Boolean = u.unitClass.isDetector
      lazy val counteredBy: Array[Quality] = Array.empty
      override def counterScaling(input: Double): Double = 5.0 * input
    }
    val all: Vector[Quality] = Vector(
      Cloaked,
      Combat,
      Detector
    )
  }
  
  final private val enemiesByQuality  = new CountMap[Quality]
  final private val recruitsByQuality = new CountMap[Quality]
  private var lastUpdateFrame: Int = -1
  private def countQualities(counter: CountMap[Quality], units: Iterable[UnitInfo]) {
    units.foreach(unit =>
      Qualities.all.foreach(quality =>
        if (quality.matches(unit)) counter.add(quality, unit.subjectiveValue)))
  }
  private def updateCounts() {
    if (lastUpdateFrame >= With.frame) return
    lastUpdateFrame = With.frame
    enemiesByQuality.clear()
    recruitsByQuality.clear()
    countQualities(enemiesByQuality, squad.enemies)
    countQualities(recruitsByQuality, squad.units)
  }
  
  /////////////////////////////
  // Default implementations //
  /////////////////////////////
  
  override def run() {
    squad.units.foreach(_.agent.intend(squad.client, new Intention {
      toTravel = Some(destination)
    }))
  }
  
  final override def offer(candidates: Iterable[FriendlyUnitInfo], recruitmentNeed: RecruitmentLevel): Iterable[FriendlyUnitInfo] = {
    updateCounts()
    if ( ! acceptsHelp) return Iterable.empty
    recruitmentNeed match {
      case RecruitmentLevel.Critical  => offerCritical(candidates)
      case RecruitmentLevel.Important => offerUseless(candidates)
      case RecruitmentLevel.Useful    => offerUseful(candidates)
      case RecruitmentLevel.Useless   => offerUseless(candidates)
    }
  }
  
  protected def offerCritical(candidates: Iterable[FriendlyUnitInfo]): Iterable[FriendlyUnitInfo] = {
    lazy val sorted = sortAndFilterCandidates(candidates)
    val output = new mutable.ArrayBuffer[FriendlyUnitInfo]
    val newbiesByQuality  = new CountMap[Quality]
    enemiesByQuality.foreach(pair => {
      if (pair._2 > 0 && pair._1.counteredBy.forall(recruitsByQuality(_) == 0)) {
        val recruit = sorted.find(pair._1.matches)
        recruit.foreach(output += _)
      }})
    output
  }
  
  protected def offerImportant(candidates: Iterable[FriendlyUnitInfo]): Iterable[FriendlyUnitInfo] = {
    lazy val sorted = sortAndFilterCandidates(candidates)
    val output = new mutable.ArrayBuffer[FriendlyUnitInfo]
    enemiesByQuality.foreach(pair => {
      if (pair._2 > Math.max(0.0, pair._1.counterScaling(pair._1.counteredBy.map(recruitsByQuality(_)).sum))) {
        val recruit = sorted.find(pair._1.matches)
        recruit.foreach(output += _)
      }})
    output
  }
  
  protected def offerUseful(candidates: Iterable[FriendlyUnitInfo]): Iterable[FriendlyUnitInfo] = {
    candidates.filter(candidate =>
      enemiesByQuality.exists(pair =>
        pair._2 > 0 && pair._1.counteredBy.exists(_.matches(candidate))))
  }
  
  protected def offerUseless(candidates: Iterable[FriendlyUnitInfo]): Iterable[FriendlyUnitInfo] = {
    candidates
  }
  
  //////////////////
  // Subclass API //
  //////////////////
  
  protected var unitMatcher: UnitMatcher = UnitMatchWarriors
  protected var unitCounter: UnitCounter = UnitCountEverything
  protected def acceptsHelp: Boolean = unitCounter.continue(squad.units)
  protected def equippedSufficiently: Boolean = true
  protected def destination: Pixel = With.intelligence.mostBaselikeEnemyTile.pixelCenter
  protected def sortAndFilterCandidates(candidates: Iterable[FriendlyUnitInfo]): Iterable[FriendlyUnitInfo] = {
    candidates.toVector.filter(unitMatcher.accept).sortBy(_.pixelDistanceTravelling(destination))
  }
  
}
