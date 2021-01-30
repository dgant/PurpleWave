package Planning.Tactics

import Lifecycle.With
import Planning.Plan
import Planning.Plans.Army._
import Planning.Plans.Compound.If
import Planning.Plans.GamePlans.Protoss.Situational.{CatchDTRunby, DefendAgainstProxy}
import Planning.Plans.Macro.Automatic.Gather
import Planning.Plans.Scouting.ConsiderScoutingWithOverlords
import Planning.Predicates.Compound.Check

class Tactics {

  private val yoloPlan                : Plan = new If(new Check(() => With.yolo.active()), new Attack)
  private val scoutOverlordPlan       : Plan = new ConsiderScoutingWithOverlords
  private val priorityDefensePlan     : Plan = null
  private val priorityAttackPlan      : Plan = null
  private val initialScoutPlan        : Plan = null
  private val defendAgainstProxy      : Plan = new DefendAgainstProxy
  private val defendBases             : Plan = new DefendBases
  private val defendAgainstWorkerRush : Plan = new DefendAgainstWorkerRush
  private val catchDTRunby            : Plan = new CatchDTRunby
  private def scoutExposPlan          : Plan = null
  private def attackPlan              : Plan = null
  private val defendEntrance          : Plan = new DefendEntrance
  private val gather                  : Plan = new Gather
  private val chillOverlords          : Plan = new ChillOverlords
  private val recruitFreelancers      : Plan = new RecruitFreelancers
  private val scan                    : Plan = new Scan

  def update(): Unit = {
    Vector(
      yoloPlan,
      scoutOverlordPlan,
      priorityDefensePlan,
      priorityAttackPlan,
      initialScoutPlan,
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
  }
}
