package Planning.Plans.GamePlans

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.BuildRequests.BuildRequest
import Planning.Plan
import Planning.Plans.Army.{RecruitFreelancers, _}
import Planning.Plans.Basic.{NoPlan, WriteStatus}
import Planning.Plans.Compound.If
import Planning.Plans.GamePlans.Protoss.Situational.{CatchDTRunby, DefendAgainstProxy}
import Planning.Plans.Macro.Automatic.{Gather, PumpWorkers, RequireSufficientSupply}
import Planning.Plans.Macro.BuildOrders.{BuildOrder, FollowBuildOrder, RequireEssentials}
import Planning.Plans.Macro.Expanding.RemoveMineralBlocksAt
import Planning.Plans.Macro.Protoss.MeldArchons
import Planning.Plans.Placement.ProposePlacement
import Planning.Plans.Scouting._
import Planning.Predicates.Compound.{And, Check, Not}
import Planning.Predicates.Milestones.BasesAtLeast
import Planning.Predicates.Strategy.WeAreZerg

abstract class GameplanTemplate extends GameplanMode {
  val meldArchonsAt         : Int               = 40
  val removeMineralBlocksAt : Int               = 80
  def status                : String            = this.toString
  def blueprints            : Seq[Blueprint]    = Seq.empty
  def buildOrder            : Seq[BuildRequest] = Vector.empty
  def emergencyPlans        : Seq[Plan]         = Vector.empty
  def buildPlans            : Seq[Plan]         = Vector.empty
  def aggressionPlan        : Plan              = NoPlan()
  def statusPlan            : Plan              = new WriteStatus(() => status)
  def placementPlan         : Plan              = new ProposePlacement(blueprints: _*)
  def archonPlan            : Plan              = new MeldArchons(meldArchonsAt)
  def buildOrderPlan        : Plan              = new BuildOrder(buildOrder: _*)
  def supplyPlan            : Plan              = new RequireSufficientSupply
  def workerPlan            : Plan              = new If(new Not(new WeAreZerg), new PumpWorkers)
  def initialScoutPlan      : Plan              = new ConsiderScoutingWithWorker
  def scoutExposPlan        : Plan              = new If(new And(new BasesAtLeast(2), new ShouldScoutExpansions), new ScoutExpansions)
  def priorityDefensePlan   : Plan              = NoPlan()
  def priorityAttackPlan    : Plan              = NoPlan()
  def attackPlan            : Plan              = new ConsiderAttacking

  def tacticsPlans: Vector[Plan] = Vector(
    aggressionPlan,
    new If(new Check(() => With.yolo.active()), new Attack),
    new ConsiderScoutingWithOverlords,
    priorityDefensePlan,
    priorityAttackPlan,
    initialScoutPlan,
    new DefendAgainstProxy,
    new DefendBases,
    new DefendAgainstWorkerRush,
    new CatchDTRunby,
    scoutExposPlan,
    attackPlan,
    new DefendEntrance(),
    new Gather,
    new ChillOverlords,
    new RecruitFreelancers,
    new Scan
  )

  private var initialized = false
  override def onUpdate() {
    if ( ! initialized) {
      initialized = true
      children.set(
        Vector(statusPlan)
          ++ Vector(placementPlan)
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
