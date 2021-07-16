package Tactics

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import Squads._
import Performance.Tasks.TimedTask
import Planning.Plans.Army._
import Planning.Plans.Compound.If
import Planning.Plans.GamePlans.Protoss.Standard.PvT.PvTIdeas
import Planning.Plans.Scouting.MonitorBases
import Planning.Predicates.Compound.{And, Not, Or}
import Planning.Predicates.Milestones.{EnemiesAtMost, EnemyHasShownWraithCloak, UnitsAtLeast}
import Planning.Predicates.Strategy.EnemyIsTerran
import Planning.UnitMatchers._
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Tactics.Missions.MissionKillExpansion
import Utilities.Minutes

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class Tactics extends TimedTask {
  private lazy val clearBurrowedBlockers      = new SquadClearExpansionBlockers
  private lazy val ejectScout                 = new SquadEjectScout
  private lazy val followBuildOrder           = new FollowBuildOrder
  private lazy val scoutWithOverlord          = new SquadInitialOverlordScout
  private lazy val defendAgainstProxy         = new DefendAgainstProxy
  private lazy val defendFightersAgainstRush  = new DefendFightersAgainstRush
  private lazy val defendAgainstWorkerRush    = new DefendAgainstWorkerRush
  private lazy val defendFFEAgainst4Pool      = new DefendFFEWithProbes
  private lazy val makeDarkArchons            = new SquadMergeDarchons
  private lazy val mindControl                = new SquadMindControl
  private lazy val catchDTRunby               = new SquadCatchDTRunby
  private lazy val scoutWithWorkers           = new SquadWorkerScout
  private lazy val scoutForCannonRush         = new ScoutForCannonRush
  private lazy val scoutExpansions            = new SquadScoutExpansions
  private lazy val gather                     = new Gather
  private lazy val chillOverlords             = new ChillOverlords
  private lazy val doFloatBuildings           = new DoFloatBuildings
  private lazy val scan                       = new Scan
  private lazy val monitorWithObserver        = new If(
    And(
      EnemyIsTerran(),
      EnemiesAtMost(0, MatchMobileDetector),
      EnemiesAtMost(7, Terran.Factory),
      Not(new EnemyHasShownWraithCloak),
      Or(
        UnitsAtLeast(3, Protoss.Observer, complete = true),
        Not(new PvTIdeas.EnemyHasMines))),
    new MonitorBases(Protoss.Observer))
  private lazy val missionKillExpansion       = new MissionKillExpansion

  override protected def onRun(budgetMs: Long): Unit = {
    // TODO: Attack with units that can safely harass:
    // - Dark Templar/Lurkers/Cloaky Ghosts/Cloaky Wraiths
    // - Vultures/Zerglings (except against faster enemy units, or ranged units vs short-range vision of lings)
    // - Air units (except against faster enemy air-to-air)
    // - Speed Zerglings
    // - Carriers at 4+ (except against cloaked wraiths and no observer)
    runMissions()
    runPrioritySquads()
    runCoreTactics()
    runBackgroundSquads()

    // Moved in here temporarily due to issue where Tactics adding units clears a Squad's current list of enemies
    With.squads.run(budgetMs)
  }

  private def runMissions(): Unit = {
    missionKillExpansion.consider()
  }

  private def runPrioritySquads(): Unit = {
    makeDarkArchons.recruit()
    mindControl.recruit()
    clearBurrowedBlockers.recruit()
    followBuildOrder.update()
    ejectScout.recruit()
    scoutWithOverlord.recruit()
    With.blackboard.scoutPlan().update()
    defendAgainstProxy.update()
    defendFightersAgainstRush.update()
    defendAgainstWorkerRush.update()
    defendFFEAgainst4Pool.update()
    scoutWithWorkers.recruit()
    scoutForCannonRush.update()
    scoutExpansions.recruit()
    monitorWithObserver.update()
    // TODO: EscortSettlers is no longer being used but we do need to do it
    // TODO: Hide Carriers until 4x vs. Terran
    // TODO: Plant Overlords around the map, as appropriate
  }

  private def assign(
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

  // AIST4 last minute hack
  // Getting bizarre ClassNotFound exceptions when creating this as a lazy, particularly when invoked from inside the practive drop defense
  // so let's create this manually
  private var _baseSquads: Option[Map[Base, SquadDefendBase]] = None
  private def baseSquads: Map[Base, SquadDefendBase] = {
    _baseSquads = _baseSquads.orElse(Some(With.geography.bases.map(base => (base, new SquadDefendBase(base))).toMap))
    _baseSquads.get
  }
  private lazy val attackSquad = new SquadAttack
  private lazy val cloakSquad = new SquadCloakedHarass

  private def adjustDefenseBase(base: Base): Base = base.natural.filter(b => b.owner.isUs || b.plannedExpoRecently).getOrElse(base)
  private def runCoreTactics(): Unit = {

    // Sort defense divisions by descending importance
    var divisionsDefending = With.battles.divisions.filter(_.bases.exists(b => b.owner.isUs || b.plannedExpoRecently))
    divisionsDefending = divisionsDefending
      .filterNot(d =>
        // TODO: Old checks which we should probably generalize better
        (d.enemies.size < 3 && d.enemies.forall(e => (e.unitClass.isWorker || ! e.canAttack) && ! e.isTransport))
        || d.enemies.forall(e => e.is(Protoss.Observer) && ! e.matchups.enemyDetectors.exists(_.canMove)))

    // Pick a squad for each
    val squadsDefending = divisionsDefending.map(d => (d, baseSquads({
      val base = d.bases
        .toVector
        .sortBy( - _.economicValue())
        .sortBy( ! _.owner.isEnemy)
        .sortBy( ! _.owner.isUs)
        .minBy( ! _.plannedExpoRecently)
       adjustDefenseBase(base) // TODO: Base defense logic needs to handle case where OTHER bases need scouring and not concave in just one
    })))

    // Assign division to each squad
    squadsDefending.foreach(p => p._2.vicinity = Maff.centroid(p._2.enemies.view.map(_.pixel)))
    squadsDefending.foreach(p => p._2.addEnemies(p._1.enemies))

    // Get freelancers
    val freelancers = (new ListBuffer[FriendlyUnitInfo] ++ With.recruiter.available.view.filter(MatchRecruitableForCombat))
      .sortBy(-_.frameDiscovered) // Assign new units first, as they're most likely to be able to help on defense and least likely to have to abandon a push
      .sortBy(_.unitClass.isTransport) // So transports can go to squads which need them
    val freelancerCountInitial = freelancers.size
    def freelancerValue = freelancers.view.map(_.subjectiveValue).sum
    val freelancerValueInitial = freelancerValue

    // First satisfy each defense squad
    assign(freelancers, squadsDefending.view.map(_._2), 1.0)

    // Always attack with Dark Templar
    assign(freelancers, Seq(cloakSquad), filter = (f, s) => Protoss.DarkTemplar(f))

    // Proactive drop/harassment defense
    if ((With.geography.ourBases.map(_.metro).distinct.size > 1 && With.frame > Minutes(10)()) || With.unitsShown.any(Terran.Dropship)) {
      val dropVulnerableBases = With.geography.ourBases.filter(b =>
        b.workerCount > 5
        && ! divisionsDefending.exists(_.bases.contains(b)) // If it was in a defense division, it should have received some defenders already
        && b.metro.bases.view.flatMap(_.units).count(_.isAny(MatchAnd(MatchComplete, MatchOr(Terran.Factory, Terran.Barracks, Protoss.Gateway, MatchHatchlike, Protoss.PhotonCannon, Terran.Bunker, Zerg.SunkenColony)))) < 3)
      assign(
        freelancers,
        dropVulnerableBases.map(baseSquads(_)),
        filter = (f, s) =>
          f.isAny(Terran.Marine, Terran.Firebat, Terran.Vulture, Terran.Goliath, Protoss.Zealot, Protoss.Dragoon, Zerg.Zergling, Zerg.Hydralisk)
          && s.unitsNext.size < Math.min(3, freelancerCountInitial / 12))
    }

    catchDTRunby.recruit()
    freelancers --= catchDTRunby.lock.units

    // If we want to attack and engough freelancers remain, populate the attack squad
    // TODO: If the attack goal is the enemy army, and we have a defense squad handling it, skip this step
    if (With.blackboard.wantToAttack() && (With.blackboard.yoloing() || freelancerValue >= freelancerValueInitial * .7)) {
      assign(freelancers, Seq(attackSquad))
    } else {
      // If there are no active defense squads, activate one to defend our entrance
      val squadsDefendingOrWaiting: Seq[Squad] =
        if (squadsDefending.nonEmpty) squadsDefending.view.map(_._2)
        else Maff.maxBy(With.geography.bases.filter(b => b.owner.isUs || b.plannedExpoRecently))(_.economicValue()).map(adjustDefenseBase).map(baseSquads).toSeq
      assign(freelancers, squadsDefendingOrWaiting)
    }
  }

  private def runBackgroundSquads(): Unit = {
    gather.update()
    chillOverlords.update()
    doFloatBuildings.update()
    scan.update()
  }
}
