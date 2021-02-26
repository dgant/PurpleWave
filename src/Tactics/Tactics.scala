package Tactics

import Lifecycle.With
import Micro.Squads.Goals.GoalControlBase
import Micro.Squads.Squad
import Performance.Tasks.TimedTask
import Planning.Plans.Army._
import Planning.Plans.Scouting.{DoScoutWithWorkers, ScoutExpansions, ScoutWithOverlord}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

import scala.collection.mutable

class Tactics extends TimedTask {
  private lazy val clearBurrowedBlockers      = new ClearBurrowedBlockers
  private lazy val followBuildOrder           = new FollowBuildOrder
  private lazy val ejectScout                 = new EjectScout
  private lazy val scoutWithOverlord          = new ScoutWithOverlord
  private lazy val defendAgainstProxy         = new DefendAgainstProxy
  private lazy val defendBases                = new DefendBases
  private lazy val defendFightersAgainstRush  = new DefendFightersAgainstRush
  private lazy val defendAgainstWorkerRush    = new DefendAgainstWorkerRush
  private lazy val defendFFEAgainst4Pool      = new DefendFFEWithProbes
  private lazy val catchDTRunby               = new CatchDTRunby
  private lazy val scoutWithWorkers           = new DoScoutWithWorkers
  private lazy val scoutExpansions            = new ScoutExpansions
  private lazy val attack                     = new DoAttack
  private lazy val gather                     = new Gather
  private lazy val chillOverlords             = new ChillOverlords
  private lazy val recruitFreelancers         = new RecruitFreelancers
  private lazy val doFloatBuildings           = new DoFloatBuildings
  private lazy val scan                       = new Scan

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
    ejectScout.update()
    scoutWithOverlord.update()
    With.blackboard.scoutPlan().update()
    defendAgainstProxy.update()
    defendBases.update()
    defendFightersAgainstRush.update()
    defendAgainstWorkerRush.update()
    defendFFEAgainst4Pool.update()
    catchDTRunby.update()
    scoutWithWorkers.update()
    scoutExpansions.update()
    attack.update()
    // TODO: EscortSettlers is no longer being used but we do need to do it
    // TODO: Hide Carriers until 4x vs. Terran
    // TODO: Plant Overlords around the map, as appropriate
  }

  def assignIf(freelancers: mutable.Buffer[FriendlyUnitInfo], squads: Seq[Squad], terminationCondition: (Squad, FriendlyUnitInfo) => Boolean): Unit = {
    var eligibleSquads: Seq[Squad] = Seq.empty
    var i = 0
    while (i < freelancers.length) {
      eligibleSquads = squads.view.filterNot(terminationCondition(_, freelancers.head))
      if (eligibleSquads.nonEmpty) {
        val freelancer = freelancers.remove(i)
        eligibleSquads.head.addFreelancers(Seq(freelancer))
      } else {
        i += 1
      }
    }
  }

  private lazy val baseSquads = With.geography.bases.map(base => (base, new Squad(new GoalControlBase(base)))).toMap
  private def runCoreTactics(): Unit = {

    // Sort defense divisions by descending importance
    var divisionsDefending = With.battles.divisions.filter(_.bases.exists(b => b.owner.isUs || b.plannedExpo()))
    divisionsDefending = divisionsDefending
      .sortBy( - _.bases.view.map(_.economicValue()).sum)
      .sortBy( ! _.bases.exists(_.owner.isEnemy))
      .sortBy( ! _.bases.exists(_.plannedExpo()))
      .sortBy( ! _.bases.exists(_.owner.isUs))

    // Pick a squad for each
    val squadsDefending = divisionsDefending.map(d => (d, baseSquads(d.bases
      .toVector
      .sortBy( - _.economicValue())
      .sortBy( ! _.owner.isEnemy)
      .sortBy( ! _.plannedExpo())
      .minBy(  ! _.owner.isUs))))

    // Assign division to each squad goal
    squadsDefending.foreach(p => p._2.setEnemies(p._1.enemies))
    squadsDefending.foreach(p => p._2.goal.asInstanceOf[GoalControlBase].setDivision(p._1))
    squadsDefending.foreach(_._2.goal.resetCandidates())

    // Attempt to fill each squad
    val freelancers = With.squads.freelancersMutable
      .sortBy(_.topSpeed)
      .sortBy( ! _.flying) // Assign fliers last, as they're more flexible
      .sortBy( ! _.unitClass.isTransport) // So transports go to squads which need them

    assignIf(freelancers, squadsDefending.view.map(_._2), (s, u) => s.goal.candidateValue(u) >= 1.0)


    // Consider sending flier packs to one of these squads

    // 3b. Squads that fail to fill go to low-pri queue
    // 3c. NOT SURE IF BEST: If all squads fail to fill, replace queue with low-pri queue
    // 4. Assign remaining units to nearest squad

    // TODO: Apply anti-trolling rules from DefendBase
    // TODO: Assign leftovers to attack
    // TODO: If no attack or defense squads, DefendEntrance
    // TODO: If attack squad small relative to defense squads and enemy army, assign them to defense
    // TODO: Ensure that the switch from "roamy defense of enemies outside our base" to "attack" seamless

    lazy val defaultDefenseBase = ByOption.minBy(With.geography.ourBasesAndSettlements)(_.heart.groundPixels(With.scouting.threatOrigin))
  }

  private def runBackgroundSquads(): Unit = {
    gather.update()
    chillOverlords.update()
    recruitFreelancers.update()
    doFloatBuildings.update()
    scan.update()
  }
}
