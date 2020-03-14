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
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{BuildOrder, FollowBuildOrder, RequireEssentials}
import Planning.Plans.Macro.Expanding.RemoveMineralBlocksAt
import Planning.Plans.Macro.Protoss.MeldArchons
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
  def scoutOverlordPlan     : Plan              = new ConsiderScoutingWithOverlords
  def scoutWorkerPlan       : Plan              = new ScoutDefault
  def scoutExposPlan        : Plan              = new If(new And(new BasesAtLeast(2), new IsTimeToScoutExpansions), new ScoutExpansions)
  def yoloPlan              : Plan              = new If(new Check(() => With.yolo.active()), new Attack)
  def priorityDefensePlan   : Plan              = NoPlan()
  def priorityAttackPlan    : Plan              = NoPlan()
  def nukePlan              : Plan              = NoPlan() // new NukeBase
  def attackPlan            : Plan              = new ConsiderAttacking
  def dropPlan              : Plan              = NoPlan() //new DropAttack
  def defendEntrance        : Plan              = new DefendEntrance()

  def tacticsPlans: Vector[Plan] = Vector(
    aggressionPlan,
    yoloPlan,
    priorityDefensePlan,
    priorityAttackPlan,
    nukePlan,
    dropPlan,
    scoutWorkerPlan,
    new DefendAgainstProxy,
    new DefendZones,
    new DefendAgainstWorkerRush,
    new CatchDTRunby,
    new EscortSettlers,
    scoutExposPlan,
    attackPlan,
    defendEntrance,
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
