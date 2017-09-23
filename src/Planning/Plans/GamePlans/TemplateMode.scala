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
  
  def meldArchonsAt         : Int               = 40
  def aggression            : Double            = 1.0
  def removeMineralBlocksAt : Int               = 60
  def scoutExpansionsAt     : Int               = 100
  def superSaturate         : Boolean           = false
  def blueprints            : Seq[Blueprint]    = Seq.empty
  def buildOrder            : Seq[BuildRequest] = Vector.empty
  def emergencyPlans        : Seq[Plan]         = Vector.empty
  def buildPlans            : Seq[Plan]         = Vector.empty
  def defaultPlacementPlan  : Plan              = new ProposePlacement(blueprints: _*)
  def defaultSupplyPlan     : Plan              = new RequireSufficientSupply
  def defaultWorkerPlan     : Plan              = new TrainWorkersContinuously(superSaturate)
  def defaultScoutPlan      : Plan              = new ScoutAt(14)
  def priorityAttackPlan    : Plan              = NoPlan()
  def defaultAttackPlan     : Plan              = new ConsiderAttacking
  
  def defaultMacroPlans: Vector[Plan] = Vector(
    new MeldArchons(meldArchonsAt),
    new ClearBurrowedBlockers,
    new FollowBuildOrder,
    new RemoveMineralBlocksAt(removeMineralBlocksAt))
  
  def defaultTacticsPlans: Vector[Plan] = Vector(
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
  
  private var initialized = false
  override def onUpdate() {
    if ( ! initialized) {
      initialized = true
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
    super.onUpdate()
  }
  
}
