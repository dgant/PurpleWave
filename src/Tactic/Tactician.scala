package Tactic

import Information.Counting.MacroCounter
import Information.Geography.Types.Base
import Lifecycle.With
import Macro.Facts.MacroFacts
import Mathematics.Maff
import Performance.Tasks.TimedTask
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
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
  private val ralph                     = addPriorityTactic(new TacticRalph)
          val scoutExpansions           = addPriorityTactic(new SquadScoutExpansions)
          val monitor                   = addPriorityTactic(new TacticMonitor)

  //////////////////
  // Core tactics //
  //////////////////

  lazy val defenseSquads: Map[Base, SquadDefendBase] = With.geography.bases.map(base => (base, new SquadDefendBase(base))).toMap
  private val catchDTs = new SquadCatchDTs
  val attackSquad = new SquadAttack

  ////////////////////////
  // Background tactics //
  ////////////////////////

  private val removeMineralBlocks       = addBackgroundTactic(new TacticRemoveMineralBlocks)
  private val gather                    = addBackgroundTactic(new TacticGather)
  private val chillOverlords            = addBackgroundTactic(new TacticOverlords)
  private val doFloatBuildings          = addBackgroundTactic(new TacticFloatBuildings)
  private val scan                      = addBackgroundTactic(new TacticScan)

  override protected def onRun(budgetMs: Long): Unit = {
    missions.foreach(_.launch())
    priorityTactics.foreach(_.launch())
    runCoreTactics()
    backgroundTactics.foreach(_.launch())
    With.squads.run(budgetMs)
  }

  private def runCoreTactics(): Unit = {

    /////////////
    // DEFENSE //
    /////////////

    val defenseDivisions = With.battles.divisions
      .filter(d => d.bases.exists(b => (b.isOurs || b.plannedExpoRecently) && ! b.isEnemy))
      .map(new DefenseDivision(_))
      .filter(_.needsDefense)
      .sortBy(_.attackKeyDistanceTo((With.geography.home.center)))

    val squadsDefending = defenseDivisions.map(d => (d, defenseSquads(d.base))).distinct

    // Assign division to each squad
    squadsDefending.foreach(p => {
      p._2.setEnemies(p._1.enemiesOuter)
      p._2.vicinity = p._1.attackCentroidKey
    })

    // Proactive DT defense
    catchDTs.launch()

    // Get freelancers
    val freelancers = (new ListBuffer[FriendlyUnitInfo] ++ With.recruiter.available.view.filter(u => IsRecruitableForCombat(u) && ! Zerg.Overlord(u)))
      .sortBy( - _.frameDiscovered)     // Assign new units first, as they're most likely to be able to help on defense and least likely to have to abandon a push
      .sortBy(_.unitClass.isTransport)  // So transports can go to squads which need them
    val freelancerCountInitial  = freelancers.size
    def freelancerValue         = freelancers.view.map(_.subjectiveValue).sum
    val freelancerValueInitial  = freelancerValue

    // First satisfy each defense squad
    // First pass gets essential defenders
    // Second pass gets additional defenders to be sure
    Seq(1.0, 0.75).foreach(ratio => squadsDefending.foreach(squad => Assignment.squadsPick(freelancers, Seq(squad._2), ratio)))

    // Proactive Muta defense with Archon
    if (With.scouting.enemyProximity < 0.5 && MacroFacts.enemyHasShown(Zerg.Mutalisk) && ( ! With.blackboard.wantToAttack() || ! acePilots.hasFleet)) {
      Assignment.unitsPick(
        freelancers,
        With.geography.ourBases.filter(MacroFacts.isMiningBase).map(defenseSquads(_)),
        filter = (f, s) => Protoss.Archon(f) && s.unitsNext.isEmpty)
    }

    // Proactive drop/harassment defense
    if (With.scouting.enemyProximity < 0.5 && (With.geography.ourBases.map(_.metro).distinct.size > 1 && With.frame > Minutes(10)()) || With.unitsShown.any(Terran.Vulture, Terran.Dropship)) {

      val dropVulnerableBases = With.geography.ourBases.filter(b =>
        b.workerCount >= 3
        && ! defenseDivisions.exists(_.division.bases.contains(b)) // If it was in a defense division, it should have received some defenders already
        && b.metro.bases.view.flatMap(_.ourUnits).count(_.isAny(IsAll(IsComplete, IsAny(Terran.Factory, Terran.Barracks, Protoss.Gateway, IsHatchlike, Protoss.PhotonCannon, Terran.Bunker, Zerg.SunkenColony)))) < 3)

      val qualifiedClasses =
        ?(With.enemies.exists(_.isTerran),
        Seq(Terran.Marine, Terran.Vulture, Terran.Goliath, Protoss.Dragoon, Protoss.Archon, Zerg.Hydralisk, Zerg.Lurker),
        Seq(Terran.Marine, Terran.Firebat, Terran.Vulture, Terran.Goliath, Protoss.Zealot, Protoss.Dragoon, Protoss.Archon, Zerg.Zergling, Zerg.Hydralisk, Zerg.Lurker))

      Assignment.unitsPick(
        freelancers,
        dropVulnerableBases.map(defenseSquads(_)),
        filter = (f, s) => qualifiedClasses.exists(_(f)) && s.unitsNext.size < Math.min(3, freelancerCountInitial / 12))
    }

    //////////
    // FILL //
    //////////

    // If we want to attack and enough freelancers remain, populate the attack squad
    // TODO: If the attack goal is the enemy army, and we have a defense squad handling it, skip this step
    if (With.blackboard.wantToAttack() && (With.blackboard.yoloing() || freelancerValue >= freelancerValueInitial * .7)) {
      val attackQualities = attackSquad.qualityCounter.qualitiesEnemy
      // Let's set the attack squad up for success
      if (MacroFacts.enemyHasShown(Terran.MachineShop, Terran.Vulture, Terran.SpiderMine, Terran.SiegeTankUnsieged, Terran.SiegeTankSieged)) {
        attackQualities.increaseTo(Qualities.Vulture,     (Terran.Vulture.subjectiveValue * Math.max(3, With.units.countEnemy(Terran.Vulture))).toInt)
        attackQualities.increaseTo(Qualities.SpiderMine,  3 * Terran.SpiderMine.subjectiveValue.toInt)
        attackQualities.increaseTo(Qualities.Cloaked,     3 * Terran.SpiderMine.subjectiveValue.toInt)
      }
      if (MacroFacts.enemyDarkTemplarLikely) {
        attackQualities.increaseTo(Qualities.Cloaked, Protoss.DarkTemplar.subjectiveValue.toInt)
      }
      if (MacroFacts.enemyLurkersLikely) {
        attackQualities.increaseTo(Qualities.Cloaked, Zerg.Lurker.subjectiveValue.toInt)
      }
      Assignment.unitsPick(freelancers, Seq(attackSquad))
    } else {
      // Assign remaining freelancers to a defense squad.
      // If none are active, activate one to defend our entrance
      Assignment.unitsPick(freelancers, ?(
        squadsDefending.nonEmpty,
        squadsDefending.view.map(_._2),
        Maff.maxBy(Maff.orElse(
            With.blackboard.basesToHold(),
            With.geography.ourBasesAndSettlements))(_.economicValue())
          .map(GetDefenseBase(_))
          .map(defenseSquads)
          .toSeq))
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
