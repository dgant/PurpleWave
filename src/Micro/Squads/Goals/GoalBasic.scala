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
      def matches(u: UnitInfo): Boolean = u.burrowed || u.isAny(
        Terran.Ghost, Terran.Wraith, Terran.SpiderMine,
        Protoss.Arbiter, Protoss.DarkTemplar, Protoss.Observer,
        Zerg.Lurker, Zerg.LurkerEgg) || (u.is(Terran.Vulture) && u.player.hasTech(Terran.SpiderMinePlant))
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
  private def countUnit(unit: UnitInfo) {
    val counter = if (unit.isFriendly) recruitsByQuality else enemiesByQuality
    Qualities.all.foreach(quality =>
      if (quality.matches(unit)) counter.add(quality, unit.subjectiveValue))
  }
  private def updateCounts() {
    if (lastUpdateFrame >= With.frame) return
    lastUpdateFrame = With.frame
    enemiesByQuality.clear()
    recruitsByQuality.clear()
    squad.units.foreach(countUnit)
    squad.enemies.foreach(countUnit)
  }
  
  /////////////////////////////
  // Default implementations //
  /////////////////////////////
  
  override def run() {
    squad.units.foreach(_.agent.intend(squad.client, new Intention {
      toTravel = Some(destination)
    }))
  }
  
  final override def offer(candidates: Iterable[FriendlyUnitInfo], recruitmentNeed: RecruitmentLevel) {
    updateCounts()
    if ( ! acceptsHelp) return Iterable.empty
    recruitmentNeed match {
      case RecruitmentLevel.Critical  => offerCritical(candidates)
      case RecruitmentLevel.Important => offerImportant(candidates)
      case RecruitmentLevel.Useful    => offerUseful(candidates)
      case RecruitmentLevel.Useless   => offerUseless(candidates)
    }
  }
  
  protected def offerCritical(candidates: Iterable[FriendlyUnitInfo]) {
    lazy val sorted = sortAndFilterCandidates(candidates)
    if ( ! acceptsHelp) return
    for (candidate <- sorted) {
      if ( ! acceptsHelp) return
      if (enemiesByQuality.exists{ case (quality, count) => (
        count > 0
          && quality.counteredBy.exists(_.matches(candidate))
          && recruitsByQuality(quality) == 0)}) {
        addCandidate(candidate)
      }
    }
  }
  
  protected def offerImportant(candidates: Iterable[FriendlyUnitInfo]) {
    lazy val sorted = sortAndFilterCandidates(candidates)
    if ( ! acceptsHelp) return
    for (candidate <- sorted) {
      if ( ! acceptsHelp) return
      if (enemiesByQuality.exists{ case (quality, count) => (
        count > 0
        && quality.counteredBy.exists(counter =>
          counter.matches(candidate)
          && quality.counterScaling(recruitsByQuality(quality)) <= enemiesByQuality(quality)) )}) {
        addCandidate(candidate)
      }
    }
  }
  
  protected def offerUseful(candidates: Iterable[FriendlyUnitInfo]) {
    lazy val sorted = sortAndFilterCandidates(candidates)
    if ( ! acceptsHelp) return
    for (candidate <- sorted) {
      if ( ! acceptsHelp) return
      if (enemiesByQuality.exists{ case (quality, count) => (
        count > 0
        && quality.counteredBy.exists(_.matches(candidate)))}) {
        addCandidate(candidate)
      }
    }
  }
  
  protected def offerUseless(candidates: Iterable[FriendlyUnitInfo]) {
    for (candidate <- candidates) {
      if ( ! acceptsHelp) return
      if (unitMatcher.accept(candidate)) {
        addCandidate(candidate)
      }
    }
  }
  
  protected def addCandidate(unit: FriendlyUnitInfo) {
    squad.recruit(unit)
    countUnit(unit)
  }
  
  //////////////////
  // Subclass API //
  //////////////////
  
  var unitMatcher: UnitMatcher = UnitMatchWarriors
  var unitCounter: UnitCounter = UnitCountEverything
  protected def acceptsHelp: Boolean = unitCounter.continue(squad.units)
  protected def equippedSufficiently: Boolean = true
  protected def destination: Pixel = With.intelligence.mostBaselikeEnemyTile.pixelCenter
  protected def sortAndFilterCandidates(candidates: Iterable[FriendlyUnitInfo]): Iterable[FriendlyUnitInfo] = {
    candidates.toVector.filter(unitMatcher.accept).sortBy(_.pixelDistanceTravelling(destination))
  }
  
}
