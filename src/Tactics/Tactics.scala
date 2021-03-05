package Tactics

import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Squads._
import Performance.Tasks.TimedTask
import Planning.Plans.Army._
import Planning.Plans.Compound.{If, Or}
import Planning.Plans.GamePlans.Protoss.Standard.PvT.PvTIdeas
import Planning.Plans.Scouting.{DoScoutWithWorkers, MonitorBases, ScoutExpansions, ScoutWithOverlord}
import Planning.Predicates.Compound.{And, Not}
import Planning.Predicates.Milestones.{EnemiesAtMost, EnemyHasShownWraithCloak, UnitsAtLeast}
import Planning.UnitMatchers.MatchRecruitableForCombat
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class Tactics extends TimedTask {
  private lazy val clearBurrowedBlockers      = new ClearBurrowedBlockers
  private lazy val followBuildOrder           = new FollowBuildOrder
  private lazy val ejectScout                 = new SquadEjectScout
  private lazy val scoutWithOverlord          = new ScoutWithOverlord
  private lazy val defendAgainstProxy         = new DefendAgainstProxy
  private lazy val defendFightersAgainstRush  = new DefendFightersAgainstRush
  private lazy val defendAgainstWorkerRush    = new DefendAgainstWorkerRush
  private lazy val defendFFEAgainst4Pool      = new DefendFFEWithProbes
  private lazy val catchDTRunby               = new SquadCatchDTRunby
  private lazy val scoutWithWorkers           = new DoScoutWithWorkers
  private lazy val scoutExpansions            = new ScoutExpansions
  private lazy val gather                     = new Gather
  private lazy val chillOverlords             = new ChillOverlords
  private lazy val doFloatBuildings           = new DoFloatBuildings
  private lazy val scan                       = new Scan
  private lazy val monitorWithObserver        = new If(
    new And(
      new EnemiesAtMost(7, Terran.Factory),
      new Or(
        new UnitsAtLeast(2, Protoss.Observer, complete = true),
        new And(
          new Not(new EnemyHasShownWraithCloak),
          new Not(new PvTIdeas.EnemyHasMines)))),
    new MonitorBases(Protoss.Observer))

  override protected def onRun(budgetMs: Long): Unit = {
    // TODO: Attack with units that can safely harass:
    // - Dark Templar/Lurkers/Cloaky Ghosts/Cloaky Wraiths
    // - Vultures/Zerglings (except against faster enemy units, or ranged units vs short-range vision of lings)
    // - Air units (except against faster enemy air-to-air)
    // - Speed Zerglings
    // - Carriers at 4+ (except against cloaked wraiths and no observer)
    launchMissions()
    runMissions()
    runPrioritySquads()
    runCoreTactics()
    runBackgroundSquads()
  }

  private def launchMissions(): Unit = {}
  private def runMissions(): Unit = {}
  private def runPrioritySquads(): Unit = {
    clearBurrowedBlockers.update()
    followBuildOrder.update()
    ejectScout.recruit()
    scoutWithOverlord.update()
    With.blackboard.scoutPlan().update()
    defendAgainstProxy.update()
    defendFightersAgainstRush.update()
    defendAgainstWorkerRush.update()
    defendFFEAgainst4Pool.update()
    scoutWithWorkers.update()
    scoutExpansions.update()
    monitorWithObserver.update()
    // TODO: EscortSettlers is no longer being used but we do need to do it
    // TODO: Hide Carriers until 4x vs. Terran
    // TODO: Plant Overlords around the map, as appropriate
  }

  private def assign(freelancers: mutable.Buffer[FriendlyUnitInfo], squads: Squad*): Unit = assignIf(freelancers, squads)
  private def assignIf(
      freelancers: mutable.Buffer[FriendlyUnitInfo],
      squads: Seq[Squad],
      minimumValue: Double = Double.NegativeInfinity): Unit = {
    var eligibleSquads: Seq[Squad] = Seq.empty
    var i = 0
    while (i < freelancers.length) {
      val freelancer = freelancers(i)
      val squadValues = squads.filter(_.candidateValue(freelancer) > minimumValue)
      val bestSquad = ByOption.minBy(squadValues)(squad => freelancer.pixelDistanceTravelling(squad.vicinity))
      if (bestSquad.isDefined) {
        bestSquad.get.addUnit(freelancers.remove(i))
      } else {
        i += 1
      }
    }
  }
  private lazy val baseSquads = With.geography.bases.map(base => (base, new SquadDefendBase(base))).toMap
  private lazy val attackSquad = new SquadAttack
  private def runCoreTactics(): Unit = {

    // Sort defense divisions by descending importance
    var divisionsDefending = With.battles.divisions.filter(_.bases.exists(b => b.owner.isUs || b.plannedExpo()))
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
        .sortBy( ! _.plannedExpo())
        .minBy(  ! _.owner.isUs)
      base.natural.filter(_.owner.isUs).getOrElse(base) // TODO: BAse defense logic needs to handle case where OTHER bases need scouring and not concave in just one
    })))

    // Assign division to each squad
    squadsDefending.foreach(p => p._2.vicinity = PurpleMath.centroid(p._2.enemies.view.map(_.pixel)))
    squadsDefending.foreach(p => p._2.addEnemies(p._1.enemies))
    squadsDefending.foreach(p => p._2.asInstanceOf[SquadDefendBase].setDivision(p._1))

    // Get freelancers
    val freelancers = (new ListBuffer[FriendlyUnitInfo] ++ With.recruiter.unlocked.view.filter(MatchRecruitableForCombat))
      .sortBy(_.topSpeed) // Assign fast units last, as they're more flexible
      .sortBy(_.flying) // Assign fliers last, as they're more flexible
      .sortBy(_.unitClass.isTransport) // So transports can go to squads which need them
    def freelancerValue = freelancers.view.map(_.subjectiveValue).sum
    val freelancerValueInitial = freelancerValue

    // First satisfy each defense squad
    assignIf(freelancers, squadsDefending.view.map(_._2), 1.0)

    // TODO: Always attack with Dark Templar

    // If we want to attack and engough freelancers remain, populate the attack squad
    // TODO: If the attack goal is the enemy army, and we have a defense squad handling it, skip this step
    if (With.blackboard.wantToAttack() && (With.blackboard.yoloing() || freelancerValue >= freelancerValueInitial * .7)) {
      assignIf(freelancers, Seq(attackSquad))
    } else {
      // If there are no active defense squads, activate one to defend our entrance
      val squadsDefendingOrWaiting: Seq[Squad] =
        if (squadsDefending.nonEmpty) squadsDefending.view.map(_._2)
        else ByOption.maxBy(With.geography.ourBases)(_.economicValue())
          .map(b => b.natural.filter(n => n.owner.isUs || n.townHallTile.altitude > b.townHallTile.altitude).getOrElse(b))
          .map(baseSquads)
          .toSeq
      assignIf(freelancers, squadsDefendingOrWaiting)
    }
  }

  private def runBackgroundSquads(): Unit = {
    gather.update()
    chillOverlords.update()
    doFloatBuildings.update()
    scan.update()
  }
}
