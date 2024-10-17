package Planning.Plans.Gameplans.All

import Macro.Requests.RequestBuildable
import Planning.Plan
import Planning.Plans.Army._
import Planning.Plans.Compound.If
import Planning.Plans.Macro.Automatic.{PumpWorkers, SupplierPlan}
import Planning.Plans.Macro.BuildOrders.{BuildOrder, RequireEssentials}
import Planning.Plans.Macro.Protoss.MeldArchons
import Planning.Plans.Scouting._
import Planning.Predicates.Compound.Not
import Planning.Predicates.Strategy.WeAreZerg
import Planning.Predicates.{Always, Never, Predicate}

abstract class GameplanTemplate extends Plan with Modal {
  val activationCriteria    : Predicate       = new Always
  val completionCriteria    : Predicate       = new Never
  def buildOrder            : Seq[RequestBuildable]  = Vector.empty
  def emergencyPlans        : Seq[Plan]       = Vector.empty
  def buildPlans            : Seq[Plan]       = Vector.empty
  def buildOrderPlan        : Plan            = new BuildOrder(buildOrder: _*)
  def supplyPlan            : Plan            = new SupplierPlan
  def workerPlan            : Plan            = new If(Not(new WeAreZerg), new PumpWorkers)
  def scoutPlan             : Plan            = new ConsiderScoutingWithWorker
  def attackPlan            : Plan            = new ConsiderAttacking
  def archonPlan            : Plan            = new MeldArchons(40)

  private lazy val children = (
    Vector(new RequireEssentials)
    ++ emergencyPlans
    ++ Vector(buildOrderPlan)
    ++ Vector(supplyPlan)
    ++ Vector(workerPlan)
    ++ buildPlans
    ++ Vector(
      archonPlan,
      scoutPlan,
      attackPlan))

  override def isComplete: Boolean = completionCriteria.apply || ! activationCriteria.apply

  override def onUpdate(): Unit = {
    children.foreach(_.update())
  }
  
}
