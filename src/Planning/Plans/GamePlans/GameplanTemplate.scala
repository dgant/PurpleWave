package Planning.Plans.GamePlans

import Macro.Architecture.Blueprint
import Macro.Requests.RequestBuildable
import Planning.Plans.Army._
import Planning.Plans.Basic.{NoPlan, WriteStatus}
import Planning.Plans.Compound.If
import Planning.Plans.Macro.Automatic.{PumpWorkers, RequireSufficientSupply}
import Planning.Plans.Macro.BuildOrders.{BuildOrder, RequireEssentials}
import Planning.Plans.Macro.Expanding.RemoveMineralBlocksAt
import Planning.Plans.Macro.Protoss.MeldArchons
import Planning.Plans.Placement.ProposePlacement
import Planning.Plans.Scouting._
import Planning.Predicates.Compound.Not
import Planning.Predicates.Strategy.WeAreZerg
import Planning.Predicates.{Always, Never}
import Planning.{Plan, Predicate}

abstract class GameplanTemplate extends Plan with Modal {
  val activationCriteria    : Predicate       = new Always
  val completionCriteria    : Predicate       = new Never
  val meldArchonsAt         : Int             = 40
  val removeMineralBlocksAt : Int             = 80
  def status                : String          = this.toString
  def blueprints            : Seq[Blueprint]  = Seq.empty
  def buildOrder            : Seq[RequestBuildable]  = Vector.empty
  def emergencyPlans        : Seq[Plan]       = Vector.empty
  def buildPlans            : Seq[Plan]       = Vector.empty
  def aggressionPlan        : Plan            = NoPlan()
  def statusPlan            : Plan            = new WriteStatus(() => status)
  def placementPlan         : Plan            = new ProposePlacement(blueprints: _*)
  def archonPlan            : Plan            = new MeldArchons(meldArchonsAt)
  def buildOrderPlan        : Plan            = new BuildOrder(buildOrder: _*)
  def supplyPlan            : Plan            = new RequireSufficientSupply
  def workerPlan            : Plan            = new If(new Not(new WeAreZerg), new PumpWorkers)
  def scoutPlan             : Plan            = new ConsiderScoutingWithWorker
  def attackPlan            : Plan            = new ConsiderAttacking

  private lazy val children = (Vector(statusPlan)
    ++ Vector(placementPlan)
    ++ Vector(new RequireEssentials)
    ++ emergencyPlans
    ++ Vector(buildOrderPlan)
    ++ Vector(supplyPlan)
    ++ Vector(workerPlan)
    ++ buildPlans
    ++ Vector(
      archonPlan,
      new RemoveMineralBlocksAt(removeMineralBlocksAt),
      scoutPlan,
      attackPlan))

  override def isComplete: Boolean = completionCriteria.apply || ! activationCriteria.apply

  override def onUpdate() {
    children.foreach(_.update())
  }
  
}
