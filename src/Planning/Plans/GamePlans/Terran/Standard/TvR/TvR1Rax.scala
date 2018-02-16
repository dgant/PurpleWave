package Planning.Plans.GamePlans.Terran.Standard.TvR

import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplateVsRandom
import Planning.Plans.GamePlans.Terran.Situational.BunkersAtNatural
import Planning.Plans.Predicates.{Employing, SafeAtHome}
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Predicates.Milestones.UnitsAtLeast
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvR.TvR1Rax

class TvR1Rax extends GameplanModeTemplateVsRandom {
  
  override val activationCriteria: Plan = new Employing(TvR1Rax)
  override val completionCriteria: Plan = new UnitsAtLeast(2, UnitMatchWarriors)
  
  override val buildOrder = Vector(
    RequestAtLeast(1,   Terran.CommandCenter),
    RequestAtLeast(9,   Terran.SCV),
    RequestAtLeast(1,   Terran.SupplyDepot),
    RequestAtLeast(11,  Terran.SCV),
    RequestAtLeast(1,   Terran.Barracks))
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new Or(
        new SafeAtHome,
        new UnitsAtLeast(1, Terran.Bunker),
        new UnitsAtLeast(8, Terran.Marine)),
      new BunkersAtNatural(2)),
  
    new TrainContinuously(Terran.Marine),
    new If(
      new Not(new SafeAtHome),
      new Build(
        RequestAtLeast(1, Terran.Bunker),
        RequestAtLeast(4, Terran.Barracks))),
    new RequireMiningBases(2))
}