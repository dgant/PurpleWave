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
    val counteredBy: Array[Quality] = Array.empty
    def counterScaling(input: Double): Double = input
  }
  
  final object Qualities {
    object Cloaked extends Quality {
      def matches(u: UnitInfo): Boolean = u.burrowed || u.isAny(
        Terran.Ghost, Terran.Wraith, Terran.SpiderMine,
        Protoss.Arbiter, Protoss.DarkTemplar, Protoss.Observer,
        Zerg.Lurker, Zerg.LurkerEgg) || (u.is(Terran.Vulture) && u.player.hasTech(Terran.SpiderMinePlant))
      override val counteredBy: Array[Quality] = Array(Detector)
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
      override def matches(u: UnitInfo): Boolean = (AntiGround.matches(u)
        && ! u.isAny(Protoss.Zealot, Protoss.DarkTemplar, Protoss.Scout, Protoss.Arbiter, Protoss.Carrier, Zerg.Zergling))
      override val counteredBy: Array[Quality] = Array.empty
    }
    object Air extends Quality {
      def matches(u: UnitInfo): Boolean = u.flying
      override val counteredBy: Array[Quality] = Array(AntiAir)
    }
    object Ground extends Quality {
      def matches(u: UnitInfo): Boolean = ! u.flying
      override val counteredBy: Array[Quality] = Array(AntiGround)
    }
    object AntiAir extends Quality {
      def matches(u: UnitInfo): Boolean = u.is(UnitMatchCombatSpellcaster) || u.attacksAgainstAir > 0
    }
    object AntiGround extends Quality {
      def matches(u: UnitInfo): Boolean = u.is(UnitMatchCombatSpellcaster) || (u.attacksAgainstGround > 0 && ! u.unitClass.isWorker)
    }
    object Combat extends Quality {
      def matches(u: UnitInfo): Boolean = (u.canAttack && ! u.unitClass.isWorker)
      override val counteredBy: Array[Quality] = Array(Combat)
    }
    object Detector extends Quality {
      def matches(u: UnitInfo): Boolean = u.unitClass.isDetector
      override def counterScaling(input: Double): Double = 5.0 * input
    }
    object Transport extends Quality {
      def matches(u: UnitInfo): Boolean = u.isAny(Terran.Dropship, Protoss.Shuttle, Zerg.Overlord)
    }
    object Transportable extends Quality {
      def matches(u: UnitInfo): Boolean = u.isAny(Protoss.HighTemplar, Protoss.Reaver, Zerg.Defiler)
    }
    val threats: Array[Quality] = Array(
      Cloaked,
      SpiderMine,
      Vulture,
      Air,
      Ground,
    )
    val answers: Array[Quality] = Array(
      Detector,
      AntiSpiderMine,
      AntiVulture,
      AntiAir,
      AntiGround,
    )
    val roles: Array[Quality] = Array(
      Transport,
      Transportable
    )
  }
  
  final protected val enemiesByQuality  = new CountMap[Quality]
  final protected val recruitsByQuality = new CountMap[Quality]
  private var lastUpdateFrame: Int = -1
  private def countUnit(unit: UnitInfo, qualities: Seq[Quality]) {
    val counter = if (unit.isFriendly) recruitsByQuality else enemiesByQuality
    qualities.foreach(quality =>
      if (quality.matches(unit)) counter.add(quality, unit.subjectiveValue.toInt))
  }
  private def updateCounts() {
    if (lastUpdateFrame >= With.frame) return
    lastUpdateFrame = With.frame
    enemiesByQuality.clear()
    recruitsByQuality.clear()
    squad.units.foreach(countUnit(_, Qualities.roles))
    squad.units.foreach(countUnit(_, Qualities.answers))
    squad.enemies.foreach(countUnit(_, Qualities.threats))

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
  
  protected val counterMin = 1.2
  protected val counterMax = 2.0
  
  protected def offerCritical(candidates: Iterable[FriendlyUnitInfo]) {
    offerConditional(
      candidates,
      (candidate, quality) =>
        // Enemy quality is represented
        enemiesByQuality(quality) > 0
        && quality.counteredBy.exists(counter =>
          // Candidate counters it
          counter.matches(candidate)
          // And we have no recruits countering it
          && recruitsByQuality(counter) == 0))
  }
  
  protected def offerImportant(candidates: Iterable[FriendlyUnitInfo]) {
    offerConditional(
      candidates,
      (candidate, quality) =>
        // Enemy quality is represented
        enemiesByQuality(quality) > 0
        && quality.counteredBy.exists(counter =>
          // Candidate counters it
          counter.matches(candidate)
          // And we have insufficiently many recruits countering it
          && quality.counterScaling(recruitsByQuality(counter)) <= enemiesByQuality(quality) * counterMin))
  }
  
  protected def offerUseful(candidates: Iterable[FriendlyUnitInfo]) {
    offerConditional(
      candidates,
      (candidate, quality) =>
        if (Qualities.Transport.matches(candidate) && recruitsByQuality(Qualities.Transportable) < recruitsByQuality(Qualities.Transport)) {
          true
        } else {
          (
            // Enemy quality is represented
            enemiesByQuality(quality) > 0
              && quality.counteredBy.exists(counter =>
              // Candidate counters it
              counter.matches(candidate)
                // And we coulduse more recruits to counter it
                && quality.counterScaling(recruitsByQuality(counter)) <= enemiesByQuality(quality) * counterMax)
          )
      })
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
    countUnit(unit, Qualities.answers)
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
          (if (squad.previousUnits.contains(unit)) 1.0 else 2.0) // stickiness
          * unit.pixelDistanceTravelling(destination))
  }
  
}
