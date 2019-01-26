package Planning.Plans.GamePlans

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.BuildRequests.BuildRequest
import Planning.Plan
import Planning.Plans.Army.{RecruitFreelancers, _}
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound.If
import Planning.Plans.GamePlans.Protoss.Situational.{CatchDTRunby, DefendAgainstProxy}
import Planning.Plans.Macro.Automatic.{Gather, PumpWorkers, RequireSufficientSupply}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{BuildOrder, FollowBuildOrder, RequireEssentials}
import Planning.Plans.Macro.Expanding.RemoveMineralBlocksAt
import Planning.Plans.Macro.Protoss.MeldArchons
import Planning.Plans.Scouting.{ChillOverlords, ScoutAt, ScoutExpansionsAt}
import Planning.Predicates.Compound.{Check, Not}
import Planning.Predicates.Milestones.BasesAtLeast
import Planning.Predicates.Strategy.WeAreZerg

abstract class GameplanTemplate extends GameplanMode {
  
  val meldArchonsAt         : Int               = 40
  val aggression            : Double            = 1.0
  val removeMineralBlocksAt : Int               = 40
  def blueprints            : Seq[Blueprint]    = Seq.empty
  val superSaturate         : Boolean           = false
  def buildOrder            : Seq[BuildRequest] = Vector.empty
  def emergencyPlans        : Seq[Plan]         = Vector.empty
  def buildPlans            : Seq[Plan]         = Vector.empty
  def aggressionPlan        : Plan              = new Aggression(aggression)
  def placementPlan         : Plan              = new ProposePlacement(blueprints: _*)
  def archonPlan            : Plan              = new MeldArchons(meldArchonsAt)
  def buildOrderPlan        : Plan              = new BuildOrder(buildOrder: _*)
  def supplyPlan            : Plan              = new RequireSufficientSupply
  def workerPlan            : Plan              = new If(new Not(new WeAreZerg), new PumpWorkers(superSaturate))
  def scoutPlan             : Plan              = new ScoutAt(14)
  def scoutExposPlan        : Plan              = new If(new BasesAtLeast(2), new ScoutExpansionsAt(60))
  def yoloPlan              : Plan              = new If(new Check(() => With.yolo.active()), new Attack)
  def priorityDefensePlan   : Plan              = NoPlan()
  def priorityAttackPlan    : Plan              = NoPlan()
  def nukePlan              : Plan              = NoPlan() // new NukeBase
  def attackPlan            : Plan              = new ConsiderAttacking
  def dropPlan              : Plan              = NoPlan() //new DropAttack
  def overlordPlan          : Plan              = new ChillOverlords

  def tacticsPlans: Vector[Plan] = Vector(
    aggressionPlan,
    yoloPlan,
    priorityDefensePlan,
    priorityAttackPlan,
    nukePlan,
    dropPlan,
    scoutPlan,
    new DefendZones,
    new DefendAgainstProxy,
    new DefendAgainstWorkerRush,
    new CatchDTRunby,
    new EscortSettlers,
    scoutExposPlan,
    attackPlan,
    new DefendEntrance,
    new Gather,
    overlordPlan,
    new RecruitFreelancers,
    new Scan
  )

  private var initialized = false
  override def onUpdate() {
    if ( ! initialized) {
      initialized = true
      children.set(
        Vector(placementPlan)
          ++ Vector(new RequireEssentials)
          ++ emergencyPlans
          ++ Vector(buildOrderPlan)
          ++ Vector(supplyPlan)
          ++ Vector(workerPlan)
          ++ buildPlans
          ++ Vector(
            archonPlan,
            new ClearBurrowedBlockers,
            new FollowBuildOrder,
            new RemoveMineralBlocksAt(removeMineralBlocksAt))
          ++ tacticsPlans
      )
    }
    super.onUpdate()
  }
  
}
