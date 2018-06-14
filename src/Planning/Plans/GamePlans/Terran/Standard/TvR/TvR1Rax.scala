package Planning.Plans.GamePlans.Terran.Standard.TvR

import Macro.BuildRequests.Get
import Planning.Predicates.Compound.Not
import Planning.UnitMatchers.UnitMatchWarriors
import Planning.{Plan, Predicate}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplateVsRandom
import Planning.Plans.GamePlans.Terran.Situational.BunkersAtNatural
import Planning.Predicates.{Employing, SafeAtHome}
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Predicates.Milestones.UnitsAtLeast
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvR.TvR1Rax

class TvR1Rax extends GameplanModeTemplateVsRandom {
  
  override val activationCriteria: Predicate = new Employing(TvR1Rax)
  override val completionCriteria: Predicate = new UnitsAtLeast(2, UnitMatchWarriors)
  
  override val buildOrder = Vector(
    Get(1,   Terran.CommandCenter),
    Get(9,   Terran.SCV),
    Get(1,   Terran.SupplyDepot),
    Get(11,  Terran.SCV),
    Get(1,   Terran.Barracks))
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new Or(
        new SafeAtHome,
        new UnitsAtLeast(1, Terran.Bunker),
        new UnitsAtLeast(8, Terran.Marine)),
      new BunkersAtNatural(2)),
  
    new Pump(Terran.Marine),
    new If(
      new Not(new SafeAtHome),
      new Build(
        Get(1, Terran.Bunker),
        Get(4, Terran.Barracks))),
    new RequireMiningBases(2))
}