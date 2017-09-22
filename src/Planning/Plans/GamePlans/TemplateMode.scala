package Planning.Plans.GamePlans

import Macro.BuildRequests.BuildRequest
import Planning.Plan
import Planning.Plans.Army._
import Planning.Plans.Macro.Automatic.{Gather, MeldArchons, RequireSufficientSupply, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.{BuildOrder, FollowBuildOrder, RequireEssentials}
import Planning.Plans.Macro.Expanding.RemoveMineralBlocksAt
import Planning.Plans.Protoss.Situational.DefendAgainstProxy
import Planning.Plans.Recruitment.RecruitFreelancers
import Planning.Plans.Scouting.{ScoutAt, ScoutExpansionsAt}

abstract class TemplateMode extends Mode {
  
  val meldArchonsAt         : Int     = 40
  val aggression            : Double  = 1.0
  val removeMineralBlocksAt : Int     = 60
  val scoutExpansionsAt     : Int     = 100
  
  val buildOrder    : Seq[BuildRequest] = Vector.empty
  val supplyPlan    : Plan              = new RequireSufficientSupply
  val workerPlan    : Plan              = new TrainWorkersContinuously
  val scoutPlan     : Plan              = new ScoutAt(14)
  val attackPlan    : Plan              = new ConsiderAttacking
  
  val placementPlans: Vector[Plan] = Vector.empty
  
  val emergencyPlans: Vector[Plan] = Vector.empty
  
  val buildPlans: Vector[Plan] = Vector.empty
  
  val macroPlans: Vector[Plan] = Vector(
    new MeldArchons(meldArchonsAt),
    new ClearBurrowedBlockers,
    new FollowBuildOrder,
    new RemoveMineralBlocksAt(removeMineralBlocksAt)
  )
  
  val tacticsPlans: Vector[Plan] = Vector(
    new Aggression(aggression),
    scoutPlan,
    new DefendZones,
    new DefendAgainstProxy,
    new EscortSettlers,
    new ScoutExpansionsAt(scoutExpansionsAt),
    attackPlan,
    new DefendEntrance,
    new Gather,
    new RecruitFreelancers
  )
  
  children.set(
    placementPlans
    ++ Vector(new RequireEssentials)
    ++ emergencyPlans
    ++ Vector(new BuildOrder(buildOrder: _*))
    ++ Vector(supplyPlan)
    ++ Vector(workerPlan)
    ++ buildPlans
    ++ macroPlans
    ++ tacticsPlans
  )
}
