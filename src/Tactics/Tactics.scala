package Tactics

import Lifecycle.With
import Performance.Tasks.TimedTask
import Planning.Plans.Army._
import Planning.Plans.Scouting.{DoScoutWithWorkers, ScoutExpansions, ScoutWithOverlord}

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
  private lazy val defendEntrance             = new DefendEntrance
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

  private def runCoreTactics(): Unit = {
    val divisions = With.battles.divisions.filter(d =>
      if (With.blackboard.wantToAttack()) true
      else d.bases.exists(_.owner.isUs))
    divisions
      .sortBy( ! _.bases.exists(_.owner.isEnemy))
      // TODO: Sort by "We want to expand here"
      .sortBy( ! _.bases.exists(_.owner.isUs))
    // We're killing freelancer drafting for top-down approach:
    // 1. Sort divisions by descending importance
    // 2. Pick a squad for each
    // 3a. Attempt to fill each squad
    // 3b. Squads that fail to fill go to low-pri queue
    // 3c. NOT SURE IF BEST: If all squads fail to fill, replace queue with low-pri queue
    // 4. Assign remaining units to nearest squad
  }

  private def runBackgroundSquads(): Unit = {
    defendEntrance.update()
    gather.update()
    chillOverlords.update()
    recruitFreelancers.update()
    doFloatBuildings.update()
    scan.update()
  }
}
