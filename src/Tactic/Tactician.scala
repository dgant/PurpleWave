package Tactic

import Information.Battles.Types.{Division, DivisionRadius}
import Information.Counting.MacroCounter
import Information.Geography.Types.Base
import Lifecycle.With
import Macro.Facts.MacroFacts
import Mathematics.Maff
import Performance.Tasks.TimedTask
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Tactic.Missions._
import Tactic.Production.Produce
import Tactic.Squads._
import Tactic.Tactics._
import Utilities.?
import Utilities.Time.Minutes
import Utilities.UnitCounters.CountUpTo
import Utilities.UnitFilters._
import Utilities.UnitPreferences.PreferClose
import _root_.Tactic.Squads.Qualities.Qualities

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

final class Tactician extends TimedTask {
  private val missions          = new ArrayBuffer[Mission]()
  private val priorityTactics   = new ArrayBuffer[Tactic]()
  private val backgroundTactics = new ArrayBuffer[Tactic]()
  private def addMission          [T <: Mission](mission: T): T = { missions          += mission; mission }
  private def addPriorityTactic   [T <: Tactic] (job: T)    : T = { priorityTactics   += job; job }
  private def addBackgroundTactic [T <: Tactic] (job: T)    : T = { backgroundTactics += job; job }

  //////////////
  // Missions //
  //////////////

  private val missionKillExpansion      = addMission(new MissionKillExpansion)
  private val missionDTDrop             = addMission(new MissionDTDrop)
  private val missionStormDrop          = addMission(new MissionStormDrop)
  private val missionSpeedlotDrop       = addMission(new MissionSpeedlotDrop)
  private val missionReaverDrop         = addMission(new MissionReaverDrop)

  //////////////////////
  // Priority tactics //
  /////////////////////

          val pylonBlock                = addPriorityTactic(new TacticPylonBlock)
          val produce: Produce          = addPriorityTactic(new Produce)
          val acePilots                 = addPriorityTactic(new SquadAcePilots)
  private val clearBurrowedBlockers     = addPriorityTactic(new SquadClearExpansionBlockers)
  private val ejectScout                = addPriorityTactic(new TacticEjectScout)
          val scoutWithOverlord         = addPriorityTactic(new TacticInitialOverlordScout)
  private val backstabProxy             = addPriorityTactic(new SquadBackstabProxy)
  private val defendAgainstProxy        = addPriorityTactic(new DefendAgainstProxy)
  private val defendFightersAgainstRush = addPriorityTactic(new DefendFightersAgainstRush)
  private val defendAgainstWorkerRush   = addPriorityTactic(new DefendAgainstWorkerRush)
  private val defendFFEAgainst4Pool     = addPriorityTactic(new DefendFFEWithProbes)
  private val makeDarkArchons           = addPriorityTactic(new TacticMeldDarchons)
  private val makeHighArchons           = addPriorityTactic(new TacticMeldArchons)
  private val mindControl               = addPriorityTactic(new SquadMindControl)
          val workerScout               = addPriorityTactic(new TacticWorkerScout)
  private val scoutForCannonRush        = addPriorityTactic(new TacticScoutForCannonRush)
          val darkTemplar               = addPriorityTactic(new SquadDarkTemplar)
          val scoutExpansions           = addPriorityTactic(new SquadScoutExpansions)
          val monitor                   = addPriorityTactic(new TacticMonitor)

  ///////////////////
  // Basic tactics //
  ///////////////////

  lazy val defenseSquads: Map[Base, SquadDefendBase] = With.geography.bases.map(base => (base, new SquadDefendBase(base))).toMap
  private val catchDTs = new SquadCatchDTs
  val attackSquad = new SquadAttack

  ////////////////////////
  // Background tactics //
  ////////////////////////

  private val removeMineralBlocks       = addBackgroundTactic(new TacticRemoveMineralBlocks)
  private val gather                    = addBackgroundTactic(new TacticGather)
  private val chillOverlords            = addBackgroundTactic(new TacticChillOverlords)
  private val doFloatBuildings          = addBackgroundTactic(new TacticFloatBuildings)
  private val scan                      = addBackgroundTactic(new TacticScan)
  private val ralph                     = addBackgroundTactic(new TacticRalph)

  override protected def onRun(budgetMs: Long): Unit = {
    missions.foreach(_.launch())
    priorityTactics.foreach(_.launch())
    runCoreTactics()
    backgroundTactics.foreach(_.launch())

    // Moved in here temporarily due to issue where Tactics adding units clears a Squad's current list of enemies
    With.squads.run(budgetMs)
  }

  // Let freelancers pick the squad they can best serve
  private def freelancersPick(
      freelancers: mutable.Buffer[FriendlyUnitInfo],
      squads: Seq[Squad],
      minimumValue: Double = Double.NegativeInfinity,
      filter: (FriendlyUnitInfo, Squad) => Boolean = (f, s) => true): Unit = {
    var i = 0
    while (i < freelancers.length) {
      val freelancer = freelancers(i)
      val squadsEligible = squads.filter(squad => filter(freelancer, squad) && squad.candidateValue(freelancer) > minimumValue)
      val bestSquad = Maff.minBy(squadsEligible)(squad => freelancer.pixelDistanceTravelling(squad.vicinity) + ?(freelancer.squad.contains(squad), 0, 320))
      if (bestSquad.isDefined) {
        bestSquad.get.addUnit(freelancers.remove(i))
        With.recruiter.lockTo(bestSquad.get.lock, freelancer)
      } else {
        i += 1
      }
    }
  }

  // Let squads pick the freelancers they need
  private def squadsPick(
      freelancers: mutable.Buffer[FriendlyUnitInfo],
      squads: Seq[Squad],
      minimumValue: Double = Double.NegativeInfinity,
      filter: (FriendlyUnitInfo, Squad) => Boolean = (f, s) => true): Unit = {
    val freelancersSorted     = new ArrayBuffer[FriendlyUnitInfo]
    val freelancersAvailable  = new mutable.HashSet[FriendlyUnitInfo]
    freelancersSorted     ++= freelancers
    freelancersAvailable  ++= freelancers
    squads.foreach(squad => {
      Maff.sortStablyInPlaceBy(freelancersSorted)(_.framesToTravelTo(squad.vicinity))
      var hired: Option[FriendlyUnitInfo] = None
      do {
        hired = freelancersSorted.find(u => freelancersAvailable.contains(u) && squad.candidateValue(u) > minimumValue && filter(u, squad))
        hired.foreach(h => {
            freelancersAvailable -= h
            freelancers -= h
            squad.addUnit(h)
            With.recruiter.lockTo(squad.lock, h)
          })
      } while (hired.isDefined)
    })
  }

  @inline private def defenseDanger(e: UnitInfo): Double = {
    if (IsWorker(e))
      0.35
    else if (Protoss.Observer(e) && e.matchups.groupVs.mobileDetectors.isEmpty)
      0.0
    else if (e.flying && (Zerg.Overlord(e) || e.unitClass.isFlyingBuilding) && ! e.matchups.groupVs.attacksAir)
      0.0
    else if (e.unitClass.attacksOrCastsOrDetectsOrTransports)
      1.0
    else
      0.0
  }

  @inline private def defenseDistance(e: UnitInfo): Double = {
    Maff.orElse(With.geography.ourBases.map(_.heart), Seq(With.geography.home)).map(e.pixelDistanceTravelling).min
  }

  private class DefenseDivision(val division: Division) extends UnitGroup {

    val enemyDistances: Vector[(UnitInfo, Double)] = division
      .enemies
      .filter(defenseDanger(_) > 0)
      .map(e => (e, defenseDistance(e)))
      .toVector

    // Our approach to choosing relevant enemies is flawed because it can cut off portions of a cluster
    val       enemiesInner : Vector[UnitInfo] = enemyDistances.view.filter(_._2 < DivisionRadius.inner).map(_._1).toVector
    lazy val  enemiesOuter : Vector[UnitInfo] = enemyDistances.view.filter(_._2 < DivisionRadius.outer).map(_._1).toVector

    val needsDefense: Boolean = enemiesInner.view.map(defenseDanger).sum >= 1.0

    lazy val base: Base =
      adjustDefenseBase(division.bases
        .toVector
        .sortBy( - _.economicValue())
        .sortBy( ! _.isEnemy)
        .sortBy( ! _.isOurs)
        .minBy( ! _.plannedExpoRecently)) // TODO: Base defense logic needs to handle case where OTHER bases need scouring and not concave in just one

    override def groupUnits: Seq[UnitInfo] = enemiesInner
  }

  private def adjustDefenseBase(base: Base): Base = {
    base.natural.filter(b => b.isOurs || b.plannedExpoRecently).getOrElse(base)
  }

  private def runCoreTactics(): Unit = {

    val defenseDivisions = With.battles.divisions
      .filter(d => d.bases.exists(b => (b.isOurs || b.plannedExpoRecently) && ! b.isEnemy))
      .map(new DefenseDivision(_))

    val defenseDivisionsActive = defenseDivisions
      .filter(_.needsDefense)
      .sortBy(_.attackKeyDistanceTo((With.geography.home.center)))

    val squadsDefending = defenseDivisionsActive.map(d => (d, defenseSquads(d.base))).distinct

    // Assign division to each squad
    squadsDefending.foreach(p => {
      p._2.setEnemies(p._1.enemiesOuter)
      p._2.vicinity = p._1.attackCentroidKey
    })

    // Proactive DT defense
    catchDTs.launch()

    // Get freelancers
    val freelancers = (new ListBuffer[FriendlyUnitInfo] ++ With.recruiter.available.view.filter(IsRecruitableForCombat))
      .sortBy( - _.frameDiscovered) // Assign new units first, as they're most likely to be able to help on defense and least likely to have to abandon a push
      .sortBy(_.unitClass.isTransport) // So transports can go to squads which need them
    val freelancerCountInitial  = freelancers.size
    def freelancerValue         = freelancers.view.map(_.subjectiveValue).sum
    val freelancerValueInitial  = freelancerValue

    // First satisfy each defense squad
    // First pass gets essential defenders
    // Second pass gets additional defenders to be sure
    Seq(1.0, 0.75).foreach(ratio => squadsDefending.foreach(squad => squadsPick(freelancers, Seq(squad._2), ratio)))

    // Proactive Muta defense with Archon
    if (With.scouting.enemyProximity < 0.5 && MacroFacts.enemyHasShown(Zerg.Mutalisk) && ( ! With.blackboard.wantToAttack() || ! acePilots.hasFleet)) {
      freelancersPick(
        freelancers,
        With.geography.ourBases.filter(MacroFacts.isMiningBase).map(defenseSquads(_)),
        filter = (f, s) => Protoss.Archon(f) && s.unitsNext.isEmpty)
    }

    // Proactive drop/harassment defense
    if (With.scouting.enemyProximity < 0.5 && (With.geography.ourBases.map(_.metro).distinct.size > 1 && With.frame > Minutes(10)()) || With.unitsShown.any(Terran.Vulture, Terran.Dropship)) {

      val dropVulnerableBases = With.geography.ourBases.filter(b =>
        b.workerCount > 5
        && ! defenseDivisionsActive.exists(_.division.bases.contains(b)) // If it was in a defense division, it should have received some defenders already
        && b.metro.bases.view.flatMap(_.ourUnits).count(_.isAny(IsAll(IsComplete, IsAny(Terran.Factory, Terran.Barracks, Protoss.Gateway, IsHatchlike, Protoss.PhotonCannon, Terran.Bunker, Zerg.SunkenColony)))) < 3)

      val qualifiedClasses = if (With.enemies.exists(_.isTerran))
        Seq(Terran.Marine, Terran.Vulture, Terran.Goliath, Protoss.Dragoon, Protoss.Archon, Zerg.Hydralisk, Zerg.Lurker)
      else
        Seq(Terran.Marine, Terran.Firebat, Terran.Vulture, Terran.Goliath, Protoss.Zealot, Protoss.Dragoon, Protoss.Archon, Zerg.Zergling, Zerg.Hydralisk, Zerg.Lurker)

      freelancersPick(
        freelancers,
        dropVulnerableBases.map(defenseSquads(_)),
        filter = (f, s) => f.isAny(qualifiedClasses: _*) && s.unitsNext.size < Math.min(3, freelancerCountInitial / 12))
    }

    // If we want to attack and enough freelancers remain, populate the attack squad
    // TODO: If the attack goal is the enemy army, and we have a defense squad handling it, skip this step
    if (With.blackboard.wantToAttack() && (With.blackboard.yoloing() || freelancerValue >= freelancerValueInitial * .7)) {
      // Let's set the attack squad up for success
      if (MacroFacts.enemyHasShown(Terran.MachineShop, Terran.Vulture, Terran.SpiderMine, Terran.SiegeTankUnsieged, Terran.SiegeTankSieged)) {
        attackSquad.qualityCounter.qualitiesEnemy.increaseTo(Qualities.Vulture,     (Terran.Vulture.subjectiveValue * Math.max(3, With.units.countEnemy(Terran.Vulture))).toInt)
        attackSquad.qualityCounter.qualitiesEnemy.increaseTo(Qualities.SpiderMine,  3 * Terran.SpiderMine.subjectiveValue.toInt)
        attackSquad.qualityCounter.qualitiesEnemy.increaseTo(Qualities.Cloaked,     3 * Terran.SpiderMine.subjectiveValue.toInt)
      }
      if (MacroFacts.enemyDarkTemplarLikely) {
        attackSquad.qualityCounter.qualitiesEnemy.increaseTo(Qualities.Cloaked, Protoss.DarkTemplar.subjectiveValue.toInt)
      }
      if (MacroFacts.enemyLurkersLikely) {
        attackSquad.qualityCounter.qualitiesEnemy.increaseTo(Qualities.Cloaked, Zerg.Lurker.subjectiveValue.toInt)
      }
      freelancersPick(freelancers, Seq(attackSquad))
    } else {
      // If there are no active defense squads, activate one to defend our entrance
      val squadsDefendingOrWaiting: Seq[Squad] =
        if (squadsDefending.nonEmpty) squadsDefending.view.map(_._2)
        else Maff.maxBy(Maff.orElse(
          With.blackboard.basesToHold(),
          With.geography.ourBasesAndSettlements))(_.economicValue())
            .map(adjustDefenseBase)
            .map(defenseSquads).toSeq
      freelancersPick(freelancers, squadsDefendingOrWaiting)
    }

    // SCVs for Terran attacks
    if (With.self.isTerran) {
      val workers = MacroCounter.countOursComplete(Terran.SCV)
      val jobs    = With.geography.ourBases.view.map(b => 2 * b.minerals.length + 3 * b.gas.length).sum + 3
      var excess  = workers - jobs

      With.squads.next.foreach(squad =>
        if ( ! squad.vicinity.metro.exists(_.isOurs) || squad.engagedUpon || squad.engagingOn) {
          val bcs     = squad.lock.units.count(Terran.Battlecruiser)
          val tanks   = squad.lock.units.count(IsTank)
          val valks   = squad.lock.units.count(Terran.Valkyrie)
          val others  = squad.lock.units.count(_.isAny(Terran.Vulture, Terran.Goliath, Terran.Wraith))

          val scvsMin = 3 * bcs + tanks + valks
          val scvsMax = scvsMin + tanks + valks + (4 + others) / 5
          val scvs    = Maff.clamp(scvsMax, scvsMin, excess)

          squad
            .scvLock
            .setPreference(PreferClose(squad.vicinity))
            .setCounter(CountUpTo(scvs))
            .acquire()

          excess -= squad.scvLock.units.size
        })
    }
  }
}
