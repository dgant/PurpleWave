package Planning.Tactics

import Lifecycle.With
import Planning.Plan
import Planning.Plans.Army._
import Planning.Plans.Compound.If
import Planning.Plans.GamePlans.Protoss.Situational.{CatchDTRunby, DefendAgainstProxy}
import Planning.Plans.Macro.Automatic.Gather
import Planning.Plans.Scouting.{ConsiderScoutingWithOverlords, ScoutExpansions, ShouldScoutExpansions}
import Planning.Predicates.Compound.Check

class Tactics {
  private lazy val yoloPlan                 : Plan = new If(new Check(() => With.yolo.active()), new Attack)
  private lazy val scoutOverlordPlan        : Plan = new ConsiderScoutingWithOverlords
  private lazy val defendAgainstProxy       : Plan = new DefendAgainstProxy
  private lazy val defendBases              : Plan = new DefendBases
  private lazy val defendAgainstWorkerRush  : Plan = new DefendAgainstWorkerRush
  private lazy val catchDTRunby             : Plan = new CatchDTRunby
  private lazy val scoutExposPlan           : Plan = new If(new ShouldScoutExpansions, new ScoutExpansions)
  private lazy val attackPlan               : Plan = new DoAttack
  private lazy val defendEntrance           : Plan = new DefendEntrance
  private lazy val gather                   : Plan = new Gather
  private lazy val chillOverlords           : Plan = new ChillOverlords
  private lazy val recruitFreelancers       : Plan = new RecruitFreelancers
  private lazy val scan                     : Plan = new Scan

  def update(): Unit = {
    // TODO: Attack with units that can safely harass:
    // - Dark Templar/Lurkers/Cloaky Ghosts/Cloaky Wraiths
    // - Vultures/Zerglings (except against faster enemy units, or ranged units vs short-range vision of lings)
    // - Air units (except against faster enemy air-to-air)
    // - Speed Zerglings
    // - Carriers at 4+ (except against cloaked wraiths and no observer)
    Vector(
      yoloPlan,
      scoutOverlordPlan,
      With.blackboard.scoutPlan(),
      defendAgainstProxy,
      defendBases,
      defendAgainstWorkerRush,
      catchDTRunby,
      scoutExposPlan,
      attackPlan,
      defendEntrance,
      gather,
      chillOverlords,
      recruitFreelancers,
      scan).foreach(_.update())

    // TODO: EjectScout
    // TODO: EscortSettlers is no longer being used but we do need to do it
    // TODO: Hide Carriers until 4x vs. Terran
  }
}
