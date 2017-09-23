package Planning.Plans.GamePlans

import Macro.Architecture.Blueprint
import Macro.BuildRequests.BuildRequest
import Planning.Plan
import Planning.Plans.Army._
import Planning.Plans.Compound.NoPlan
import Planning.Plans.Macro.Automatic.{Gather, MeldArchons, RequireSufficientSupply, TrainWorkersContinuously}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{BuildOrder, FollowBuildOrder, RequireEssentials}
import Planning.Plans.Macro.Expanding.RemoveMineralBlocksAt
import Planning.Plans.Protoss.Situational.DefendAgainstProxy
import Planning.Plans.Recruitment.RecruitFreelancers
import Planning.Plans.Scouting.{ScoutAt, ScoutExpansionsAt}

abstract class TemplateMode extends Mode {
  
  val meldArchonsAt         : Int               = 40
  val aggression            : Double            = 1.0
  val removeMineralBlocksAt : Int               = 60
  val scoutExpansionsAt     : Int               = 100
  val superSaturate         : Boolean           = false
  val blueprints            : Seq[Blueprint]    = Seq.empty
  val buildOrder            : Seq[BuildRequest] = Vector.empty
  val emergencyPlans        : Seq[Plan]         = Vector.empty
  val buildPlans            : Seq[Plan]         = Vector.empty
  val defaultPlacementPlan  : Plan              = new ProposePlacement(blueprints: _*)
  val defaultSupplyPlan     : Plan              = new RequireSufficientSupply
  val defaultWorkerPlan     : Plan              = new TrainWorkersContinuously(superSaturate)
  val defaultScoutPlan      : Plan              = new ScoutAt(14)
  val priorityAttackPlan    : Plan              = NoPlan
  val defaultAttackPlan     : Plan              = new ConsiderAttacking
  
  val defaultMacroPlans: Vector[Plan] = Vector(
    new MeldArchons(meldArchonsAt),
    new ClearBurrowedBlockers,
    new FollowBuildOrder,
    new RemoveMineralBlocksAt(removeMineralBlocksAt))
  
  val defaultTacticsPlans: Vector[Plan] = Vector(
    new Aggression(aggression),
    priorityAttackPlan,
    defaultScoutPlan,
    new DefendZones,
    new DefendAgainstProxy,
    new EscortSettlers,
    new ScoutExpansionsAt(scoutExpansionsAt),
    defaultAttackPlan,
    new DefendEntrance,
    new Gather,
    new RecruitFreelancers
  )
  
  children.set(
    Vector(defaultPlacementPlan)
    ++ Vector(new RequireEssentials)
    ++ emergencyPlans
    ++ Vector(new BuildOrder(buildOrder: _*))
    ++ Vector(defaultSupplyPlan)
    ++ Vector(defaultWorkerPlan)
    ++ buildPlans
    ++ defaultMacroPlans
    ++ defaultTacticsPlans
  )
}
