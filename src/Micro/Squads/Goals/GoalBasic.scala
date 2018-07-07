package Micro.Squads.Goals

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Agency.Intention
import Micro.Squads.RecruitmentLevel
import Micro.Squads.RecruitmentLevel.RecruitmentLevel
import Planning.Composition.UnitCountEverything
import Planning.UnitCounters.UnitCounter
import Planning.UnitMatchers._
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
  
  final object Qualities {
    object Cloaked extends Quality {
      def matches(u: UnitInfo): Boolean = u.burrowed || u.isAny(
        Terran.Ghost, Terran.Wraith, Terran.SpiderMine,
        Protoss.Arbiter, Protoss.DarkTemplar, Protoss.Observer,
        Zerg.Lurker, Zerg.LurkerEgg) || (u.is(Terran.Vulture) && u.player.hasTech(Terran.SpiderMinePlant))
      lazy val counteredBy: Array[Quality] = Array(Detector)
    }
    object SpiderMine extends Quality {
      override def matches(u: UnitInfo): Boolean = u.is(Terran.SpiderMine)
      override val counteredBy: Array[Quality] = Array(AntiVulture)
    }
    object AntiSpiderMine extends Quality {
      override def matches(u: UnitInfo): Boolean = u.attacksAgainstGround > 0 && (
        u.flying
        || u.unitClass.floats
        || u.damageOnHitGround >= Terran.SpiderMine.maxHitPoints
        || u.pixelRangeGround > 32.0 * 3.0)
      override val counteredBy: Array[Quality] = Array.empty
    }
    object Vulture extends Quality {
      override def matches(u: UnitInfo): Boolean = u.is(Terran.Vulture)
      override val counteredBy: Array[Quality] = Array(AntiVulture)
    }
    object AntiVulture extends Quality {
      override def matches(u: UnitInfo): Boolean = (u.is(UnitMatchWarriors)
        && ! u.isAny(Protoss.Zealot, Protoss.DarkTemplar, Protoss.Scout, Protoss.Arbiter, Protoss.Carrier, Zerg.Zergling))
      override val counteredBy: Array[Quality] = Array.empty
    }
    object Air extends Quality {
      def matches(u: UnitInfo): Boolean = u.flying
      lazy val counteredBy: Array[Quality] = Array(AntiAir)
    }
    object Ground extends Quality {
      def matches(u: UnitInfo): Boolean = ! u.flying
      lazy val counteredBy: Array[Quality] = Array(AntiGround)
    }
    object AntiAir extends Quality {
      def matches(u: UnitInfo): Boolean = u.is(UnitMatchCombatSpellcaster) || u.attacksAgainstAir > 0
      lazy val counteredBy: Array[Quality] = Array.empty
    }
    object AntiGround extends Quality {
      def matches(u: UnitInfo): Boolean = u.is(UnitMatchCombatSpellcaster) || (u.attacksAgainstGround > 0 && ! u.unitClass.isWorker)
      lazy val counteredBy: Array[Quality] = Array.empty
    }
    object Combat extends Quality {
      def matches(u: UnitInfo): Boolean = (u.canAttack && ! u.unitClass.isWorker)
      lazy val counteredBy: Array[Quality] = Array(Combat)
    }
    object Detector extends Quality {
      def matches(u: UnitInfo): Boolean = u.unitClass.isDetector
      lazy val counteredBy: Array[Quality] = Array.empty
      override def counterScaling(input: Double): Double = 5.0 * input
    }
    val all: Vector[Quality] = Vector(
      Cloaked,
      SpiderMine,
      AntiSpiderMine,
      Vulture,
      AntiVulture,
      Air,
      Ground,
      AntiAir,
      AntiGround,
      Combat,
      Detector
    )
  }
  
  final protected val enemiesByQuality  = new CountMap[Quality]
  final protected val recruitsByQuality = new CountMap[Quality]
  private var lastUpdateFrame: Int = -1
  private def countUnit(unit: UnitInfo) {
    val counter = if (unit.isFriendly) recruitsByQuality else enemiesByQuality
    Qualities.all.foreach(quality =>
      if (quality.matches(unit)) counter.add(quality, unit.subjectiveValue.toInt))
  }
  private def updateCounts() {
    if (lastUpdateFrame >= With.frame) return
    lastUpdateFrame = With.frame
    enemiesByQuality.clear()
    recruitsByQuality.clear()
    squad.units.foreach(countUnit)
    squad.enemies.foreach(countUnit)

    // Bit of a hack -- if we have lots of units, demand detection
    if (squad.previousUnits.size > 4) {
      enemiesByQuality(Qualities.Cloaked) = Math.max(1, enemiesByQuality(Qualities.Cloaked))
    }
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
    if ( ! acceptsHelp) return
    recruitmentNeed match {
      case RecruitmentLevel.Critical  => offerCritical(candidates)
      case RecruitmentLevel.Important => offerImportant(candidates)
      case RecruitmentLevel.Useful    => offerUseful(candidates)
      case RecruitmentLevel.Useless   => offerUseless(candidates)
    }
  }
  
  protected def offerConditional(
    candidates: Iterable[FriendlyUnitInfo],
    condition: (FriendlyUnitInfo, Quality) => Boolean) {
    if ( ! acceptsHelp) return
    val qualities = enemiesByQuality
      .keys
      .toVector
      .filter(_.counteredBy.nonEmpty)
      .sortBy(quality => -enemiesByQuality(quality) / Math.max(1, recruitsByQuality(quality)))
    qualities.foreach(quality => {
      if (acceptsHelp) {
        val candidatesSorted = filterAndSortCandidates(candidates, Some(quality))
        candidatesSorted.foreach(candidate => {
          if (acceptsHelp && condition(candidate, quality)) {
            addCandidate(candidate)
          }
        })
      }
    })
  }
  
  protected val counterMin = 1.5
  protected val counterMax = 3.0
  
  protected def offerCritical(candidates: Iterable[FriendlyUnitInfo]) {
    offerConditional(
      candidates,
      (candidate, quality) =>
        // Enemy quality is represented
        enemiesByQuality(quality) > 0
        // Candidate counters it
        && quality.counteredBy.exists(_.matches(candidate))
        // And we have no recruits countering it
        && recruitsByQuality(quality) == 0)
  }
  
  protected def offerImportant(candidates: Iterable[FriendlyUnitInfo]) {
    offerConditional(
      candidates,
      (candidate, quality) =>
        // Enemy quality is represented
        enemiesByQuality(quality) > 0
        // Candidate counters it
        && quality.counteredBy.exists(_.matches(candidate))
        // And we have no recruits countering it
        && quality.counterScaling(recruitsByQuality(quality)) <= enemiesByQuality(quality) * counterMin)
  }
  
  protected def offerUseful(candidates: Iterable[FriendlyUnitInfo]) {
    offerConditional(
      candidates,
      (candidate, quality) =>
        // Enemy quality is represented
        enemiesByQuality(quality) > 0
        // Candidate counters it
        && quality.counteredBy.exists(_.matches(candidate))
        // And we have no recruits countering it
        && quality.counterScaling(recruitsByQuality(quality)) <= enemiesByQuality(quality) * counterMax)
  }
  
  protected def offerUseless(candidates: Iterable[FriendlyUnitInfo]) {
    for (candidate <- candidates) {
      if (unitMatcher.accept(candidate)) {
        addCandidate(candidate)
      }
    }
  }
  
  protected def addCandidate(unit: FriendlyUnitInfo) {
    if (unit.squad.isDefined) return
    squad.recruit(unit)
    countUnit(unit)
  }
  
  //////////////////
  // Subclass API //
  //////////////////
  
  var unitMatcher: UnitMatcher = UnitMatchRecruitableForCombat
  var unitCounter: UnitCounter = UnitCountEverything
  protected def acceptsHelp: Boolean = unitCounter.continue(squad.units)
  protected def equippedSufficiently: Boolean = true
  protected def destination: Pixel = With.intelligence.mostBaselikeEnemyTile.pixelCenter
  protected def filterCandidates(
    candidates: Iterable[FriendlyUnitInfo],
    enemyQuality: Option[Quality] = None): Iterable[FriendlyUnitInfo] = {
    candidates
      .filter(u =>
        unitMatcher.accept(u)
          && u.squad.isEmpty
          && enemyQuality.forall(_.counteredBy.exists(_.matches(u))))
  }
  
  protected def filterAndSortCandidates(
    candidates: Iterable[FriendlyUnitInfo],
    enemyQuality: Option[Quality] = None):
    Iterable[FriendlyUnitInfo] = {
      filterCandidates(candidates, enemyQuality)
        .toVector
        .sortBy(unit =>
          (if (squad.previousUnits.contains(unit)) 1.0 else 1.5) // stickiness
          * unit.pixelDistanceTravelling(destination))
  }
  
}
