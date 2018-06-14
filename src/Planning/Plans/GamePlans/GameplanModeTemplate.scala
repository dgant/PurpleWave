package Planning.Plans.GamePlans

import Macro.Architecture.Blueprint
import Macro.BuildRequests.BuildRequest
import Planning.Predicates.Compound.Not
import Planning.Plan
import Planning.Plans.Army.{RecruitFreelancers, _}
import Planning.Plans.Compound.{If, NoPlan}
import Planning.Plans.GamePlans.Protoss.Situational.DefendAgainstProxy
import Planning.Predicates.Strategy.WeAreZerg
import Planning.Plans.Macro.Automatic.{Gather, PumpWorkers, RequireSufficientSupply}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{BuildOrder, FollowBuildOrder, RequireEssentials}
import Planning.Plans.Macro.Expanding.RemoveMineralBlocksAt
import Planning.Plans.Macro.Protoss.MeldArchons
import Planning.Plans.Scouting.{ChillOverlords, ScoutAt, ScoutExpansionsAt}

abstract class GameplanModeTemplate extends GameplanMode {
  
  def meldArchonsAt         : Int               = 40
  def aggression            : Double            = 1.0
  def removeMineralBlocksAt : Int               = 40
  def scoutAt               : Int               = 14
  def scoutExpansionsAt     : Int               = 60
  def superSaturate         : Boolean           = false
  def blueprints            : Seq[Blueprint]    = Seq.empty
  def buildOrder            : Seq[BuildRequest] = Vector.empty
  def emergencyPlans        : Seq[Plan]         = Vector.empty
  def buildPlans            : Seq[Plan]         = Vector.empty
  def defaultAggressionPlan : Plan              = new Aggression(aggression)
  def defaultPlacementPlan  : Plan              = new ProposePlacement(blueprints: _*)
  def defaultArchonPlan     : Plan              = new MeldArchons(meldArchonsAt)
  def defaultBuildOrder     : Plan              = new BuildOrder(buildOrder: _*)
  def defaultSupplyPlan     : Plan              = new RequireSufficientSupply
  def defaultWorkerPlan     : Plan              = new If(new Not(new WeAreZerg), new PumpWorkers(superSaturate))
  def defaultScoutPlan      : Plan              = new ScoutAt(scoutAt)
  def defaultScoutExposPlan : Plan              = new ScoutExpansionsAt(scoutExpansionsAt)
  def priorityDefensePlan   : Plan              = NoPlan()
  def priorityAttackPlan    : Plan              = NoPlan()
  def defaultNukePlan       : Plan              = new NukeBase
  def defaultAttackPlan     : Plan              = new ConsiderAttacking
  def defaultDropPlan       : Plan              = new DropAttack
  def defaultOverlordPlan   : Plan              = new ChillOverlords
  def defaultMacroPlans: Vector[Plan] = Vector(
    defaultArchonPlan,
    new ClearBurrowedBlockers,
    new FollowBuildOrder,
    new RemoveMineralBlocksAt(removeMineralBlocksAt))
  
  def defaultTacticsPlans: Vector[Plan] = Vector(
    defaultAggressionPlan,
    priorityDefensePlan,
    priorityAttackPlan,
    defaultNukePlan,
    defaultDropPlan,
    defaultScoutPlan,
    new DefendZones,
    new DefendAgainstProxy,
    new DefendAgainstWorkerRush,
    new EscortSettlers,
    defaultScoutExposPlan,
    defaultAttackPlan,
    new DefendEntrance,
    new Gather,
    defaultOverlordPlan,
    new RecruitFreelancers,
    new Scan
  )
  
  private var initialized = false
  override def onUpdate() {
    if ( ! initialized) {
      initialized = true
      children.set(
        Vector(defaultPlacementPlan)
          ++ Vector(new RequireEssentials)
          ++ emergencyPlans
          ++ Vector(defaultBuildOrder)
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
