package Tactic

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import Performance.Tasks.TimedTask
import Planning.Predicates.MacroFacts
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Tactic.Missions._
import Tactic.Production.Produce
import Tactic.Squads._
import Tactic.Tactics._
import Utilities.?
import Utilities.Time.Minutes
import Utilities.UnitFilters._

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

class Tactician extends TimedTask {
  private val missions = new ArrayBuffer[Mission]()
  private val priorityTactics = new ArrayBuffer[Tactic]()
  private val backgroundTactics = new ArrayBuffer[Tactic]()
  private def addMission[T <: Mission](mission: T): T = { missions += mission; mission }
  private def addPriorityTactic[T <: Tactic](job: T): T = { priorityTactics += job; job }
  private def addBackgroundTactic[T <: Tactic](job: T): T = { backgroundTactics += job; job }

  //////////////
  // Missions //
  //////////////

  private val missionKillExpansion      = addMission(new MissionKillExpansion)
  private val missionDTDrop             = addMission(new MissionDTDrop)
  private val missionStormDrop          = addMission(new MissionStormDrop)
  private val missionSpeedlotDrop       = addMission(new MissionSpeedlotDrop)
  private val missionReaverDrop         = addMission(new MissionReaverDrop)

  /////////////////////
  // Priority squads //
  ////////////////////

          val produce: Produce          = addPriorityTactic(new Produce)
  private val acePilots                 = addPriorityTactic(new SquadAcePilots)
  private val clearBurrowedBlockers     = addPriorityTactic(new SquadClearExpansionBlockers)
  private val ejectScout                = addPriorityTactic(new SquadEjectScout)
  private val scoutWithOverlord         = addPriorityTactic(new SquadInitialOverlordScout)
  private val backstabProxy             = addPriorityTactic(new SquadBackstabProxy)
  private val defendAgainstProxy        = addPriorityTactic(new DefendAgainstProxy)
  private val defendFightersAgainstRush = addPriorityTactic(new DefendFightersAgainstRush)
  private val defendAgainstWorkerRush   = addPriorityTactic(new DefendAgainstWorkerRush)
  private val defendFFEAgainst4Pool     = addPriorityTactic(new DefendFFEWithProbes)
  private val makeDarkArchons           = addPriorityTactic(new SquadMergeDarchons)
  private val mindControl               = addPriorityTactic(new SquadMindControl)
  private val scoutWithWorkers          = addPriorityTactic(new SquadWorkerScout)
  private val scoutForCannonRush        = addPriorityTactic(new ScoutForCannonRush)
  private val scoutExpansions           = addPriorityTactic(new SquadScoutExpansions)
  private val monitorWithObserver       = addPriorityTactic(new MonitorTerranWithObserver)
  private val darkTemplar               = addPriorityTactic(new SquadDarkTemplar)

  //////////////////
  // Basic squads //
  //////////////////

  private lazy val baseSquads: Map[Base, SquadDefendBase] = With.geography.bases.map(base => (base, new SquadDefendBase(base))).toMap
  private val catchDTRunby = new SquadCatchDTRunby
  private val attackSquad = new SquadAttack

  ///////////////////////
  // Background squads //
  ///////////////////////

  private val removeMineralBlocks       = addBackgroundTactic(new RemoveMineralBlocks)
  private val gather                    = addBackgroundTactic(new Gather)
  private val chillOverlords            = addBackgroundTactic(new ChillOverlords)
  private val doFloatBuildings          = addBackgroundTactic(new DoFloatBuildings)
  private val scan                      = addBackgroundTactic(new Scan)

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
      val bestSquad = Maff.minBy(squadsEligible)(squad => freelancer.pixelDistanceTravelling(squad.vicinity) + (if (freelancer.squad.contains(squad)) 0 else 320))
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
        freelancersSorted
          .find(u => freelancersAvailable.contains(u) && squad.candidateValue(u) > minimumValue)
          .foreach(hired => {
            freelancersAvailable -= hired
            freelancers -= hired
            squad.addUnit(hired)
            With.recruiter.lockTo(squad.lock, hired)
          })
      } while (hired.isDefined)
    })
  }

  private def adjustDefenseBase(base: Base): Base = base.natural.filter(b => b.isOurs || b.plannedExpoRecently).getOrElse(base)
  private def runCoreTactics(): Unit = {

    // Sort defense divisions by descending distance from home.
    // The further the base is, the pickier we must be about which defenders we bring along.
    var divisionsToDefend = With.battles.divisions.filter(_.bases.exists(b => b.isOurs || b.plannedExpoRecently))
    divisionsToDefend = divisionsToDefend
      .filterNot(d =>
        // TODO: Old checks which we should probably generalize better
        (d.enemies.size < 3 && d.enemies.forall(e => (e.unitClass.isWorker || ! e.canAttack) && ! e.isTransport))
        || d.enemies.forall(e => Protoss.Observer(e) && ! e.matchups.enemyDetectors.exists(_.canMove)))
      .sortBy(d => - ?(d.attackers.nonEmpty, d.attackCentroidGround, d.centroidGround).walkablePixel.groundPixels(With.geography.home))

    // Pick a squad for each
    val squadsDefending = divisionsToDefend.map(d => (d, baseSquads({
      val base = d.bases
        .toVector
        .sortBy( - _.economicValue())
        .sortBy( ! _.isEnemy)
        .sortBy( ! _.isOurs)
        .minBy( ! _.plannedExpoRecently)
      adjustDefenseBase(base) // TODO: Base defense logic needs to handle case where OTHER bases need scouring and not concave in just one
    }))).distinct

    // Assign division to each squad
    squadsDefending.foreach(p => p._2.vicinity = Maff.centroid(p._2.enemies.view.map(_.pixel)))
    squadsDefending.foreach(p => p._2.addEnemies(p._1.enemies))

    // Get freelancers
    val freelancers = (new ListBuffer[FriendlyUnitInfo] ++ With.recruiter.available.view.filter(IsRecruitableForCombat))
      .sortBy(-_.frameDiscovered) // Assign new units first, as they're most likely to be able to help on defense and least likely to have to abandon a push
      .sortBy(_.unitClass.isTransport) // So transports can go to squads which need them
    val freelancerCountInitial = freelancers.size
    def freelancerValue = freelancers.view.map(_.subjectiveValue).sum
    val freelancerValueInitial = freelancerValue

    // First satisfy each defense squad
    // First pass gets essential defenders
    // Second pass gets additional defenders to be sure
    Seq(1.0, 0.75).foreach(ratio => squadsDefending.foreach(squad => squadsPick(freelancers, Seq(squad._2), ratio)))

    // Proactive Muta defense with Archon
    if (With.scouting.enemyProximity < 0.5 && MacroFacts.enemyHasShown(Zerg.Mutalisk) && ( ! With.blackboard.wantToAttack() || ! acePilots.hasFleet)) {
      freelancersPick(
        freelancers,
        With.geography.ourBases.filter(MacroFacts.isMiningBase).map(baseSquads(_)),
        filter = (f, s) => Protoss.Archon(f) && s.unitsNext.size == 0)
    }
    // Proactive drop/harassment defense
    if (With.scouting.enemyProximity < 0.5 && (With.geography.ourBases.map(_.metro).distinct.size > 1 && With.frame > Minutes(10)()) || With.unitsShown.any(Terran.Vulture, Terran.Dropship)) {
      val dropVulnerableBases = With.geography.ourBases.filter(b =>
        b.workerCount > 5
        && ! divisionsToDefend.exists(_.bases.contains(b)) // If it was in a defense division, it should have received some defenders already
        && b.metro.bases.view.flatMap(_.ourUnits).count(_.isAny(IsAll(IsComplete, IsAny(Terran.Factory, Terran.Barracks, Protoss.Gateway, IsHatchlike, Protoss.PhotonCannon, Terran.Bunker, Zerg.SunkenColony)))) < 3)
      val qualifiedClasses = if (With.enemies.exists(_.isTerran))
        Seq(Terran.Marine, Terran.Vulture, Terran.Goliath, Protoss.Dragoon, Protoss.Archon, Zerg.Hydralisk, Zerg.Lurker)
      else
        Seq(Terran.Marine, Terran.Firebat, Terran.Vulture, Terran.Goliath, Protoss.Zealot, Protoss.Dragoon, Protoss.Archon, Zerg.Zergling, Zerg.Hydralisk, Zerg.Lurker)
      squadsPick(
        freelancers,
        dropVulnerableBases.map(baseSquads(_)),
        filter = (f, s) => f.isAny(qualifiedClasses: _*) && s.unitsNext.size < Math.min(3, freelancerCountInitial / 12))
    }

    catchDTRunby.launch()
    freelancers --= catchDTRunby.lock.units

    // If we want to attack and engough freelancers remain, populate the attack squad
    // TODO: If the attack goal is the enemy army, and we have a defense squad handling it, skip this step
    if (With.blackboard.wantToAttack() && (With.blackboard.yoloing() || freelancerValue >= freelancerValueInitial * .7)) {
      freelancersPick(freelancers, Seq(attackSquad))
    } else {
      // If there are no active defense squads, activate one to defend our entrance
      val squadsDefendingOrWaiting: Seq[Squad] =
        if (squadsDefending.nonEmpty) squadsDefending.view.map(_._2)
        else Maff.maxBy(Maff.orElse(With.blackboard.basesToHold(), With.geography.ourBasesAndSettlements))(_.economicValue()).map(adjustDefenseBase).map(baseSquads).toSeq
      freelancersPick(freelancers, squadsDefendingOrWaiting)
    }
  }
}
