package Planning.Plans.GamePlans.Protoss.Standard.PvR

import Macro.Requests.{RequestBuildable, Get}
import Planning.Plans.Army.AttackAndHarass
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.{CapGasAt, Pump}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Predicates.Milestones.UpgradeComplete
import Planning.Predicates.Strategy.Employing
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvR2Gate4Gate

class PvR2Gate4Gate extends GameplanTemplate {

  override val activationCriteria: Predicate = Employing(PvR2Gate4Gate)

  override def attackPlan: Plan = new Trigger(
    UpgradeComplete(Protoss.DragoonRange),
    new AttackAndHarass)

  override val buildOrder: Vector[RequestBuildable] = ProtossBuilds.TwoGate910

  override def buildPlans = Vector(
    new CapGasAt(250),
    new Pump(Protoss.Dragoon),
    new Pump(Protoss.Zealot),
    new Build(
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore),
      Get(Protoss.DragoonRange),
      Get(4, Protoss.Gateway)))
}
