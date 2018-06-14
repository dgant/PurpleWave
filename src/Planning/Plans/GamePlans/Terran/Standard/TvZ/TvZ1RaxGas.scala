package Planning.Plans.GamePlans.Terran.Standard.TvZ

import Macro.BuildRequests.Get
import Planning.Composition.Latch
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Compound.{And, FlipIf}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Terran.Situational.TvZPlacement
import Planning.Plans.Predicates.{Employing, SafeAtHome}
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Predicates.Milestones.{MiningBasesAtLeast, UnitsAtLeast}
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvZ.TvZEarly1RaxGas

class TvZ1RaxGas extends GameplanModeTemplate {
  
  override val activationCriteria: Plan = new Employing(TvZEarly1RaxGas)
  override val completionCriteria: Plan = new Latch(new MiningBasesAtLeast(2))
  
  override def defaultPlacementPlan: Plan = new TvZPlacement
  
  override val buildOrder = Vector(
    Get(1,   Terran.CommandCenter),
    Get(9,   Terran.SCV),
    Get(1,   Terran.SupplyDepot),
    Get(11,  Terran.SCV),
    Get(1,   Terran.Barracks),
    Get(12,  Terran.SCV),
    Get(1,   Terran.Refinery),
    Get(13,  Terran.SCV),
    Get(1,   Terran.Marine),
    Get(14,  Terran.SCV),
    Get(2,   Terran.SupplyDepot),
    Get(2,   Terran.Marine),
    Get(1,   Terran.Factory))
  
  override def buildPlans: Seq[Plan] = Vector(
    new Pump(Terran.Vulture),
    new Pump(Terran.Marine),
    new FlipIf(
      new And(
        new SafeAtHome,
        new UnitsAtLeast(4, UnitMatchWarriors)),
      new Build(Get(4, Terran.Barracks)),
      new RequireMiningBases(2))
  )
}
