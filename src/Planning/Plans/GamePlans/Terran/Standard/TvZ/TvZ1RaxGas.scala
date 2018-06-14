package Planning.Plans.GamePlans.Terran.Standard.TvZ

import Macro.BuildRequests.GetAtLeast
import Planning.Composition.Latch
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Compound.{And, FlipIf}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Terran.Situational.TvZPlacement
import Planning.Plans.Predicates.{Employing, SafeAtHome}
import Planning.Plans.Macro.Automatic.TrainContinuously
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
    GetAtLeast(1,   Terran.CommandCenter),
    GetAtLeast(9,   Terran.SCV),
    GetAtLeast(1,   Terran.SupplyDepot),
    GetAtLeast(11,  Terran.SCV),
    GetAtLeast(1,   Terran.Barracks),
    GetAtLeast(12,  Terran.SCV),
    GetAtLeast(1,   Terran.Refinery),
    GetAtLeast(13,  Terran.SCV),
    GetAtLeast(1,   Terran.Marine),
    GetAtLeast(14,  Terran.SCV),
    GetAtLeast(2,   Terran.SupplyDepot),
    GetAtLeast(2,   Terran.Marine),
    GetAtLeast(1,   Terran.Factory))
  
  override def buildPlans: Seq[Plan] = Vector(
    new TrainContinuously(Terran.Vulture),
    new TrainContinuously(Terran.Marine),
    new FlipIf(
      new And(
        new SafeAtHome,
        new UnitsAtLeast(4, UnitMatchWarriors)),
      new Build(GetAtLeast(4, Terran.Barracks)),
      new RequireMiningBases(2))
  )
}
