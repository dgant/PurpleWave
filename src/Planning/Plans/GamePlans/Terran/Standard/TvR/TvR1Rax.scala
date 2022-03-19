package Planning.Plans.GamePlans.Terran.Standard.TvR

import Macro.Buildables.Get
import Planning.Predicates.Compound.{Not, Or}
import Planning.UnitMatchers.MatchWarriors
import Planning.{Plan, Predicate}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplateVsRandom
import Planning.Plans.GamePlans.Terran.Situational.PlaceBunkersAtNatural
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Predicates.Milestones.UnitsAtLeast
import Planning.Predicates.Reactive.SafeAtHome
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvR.TvR1Rax

class TvR1Rax extends GameplanTemplateVsRandom {
  
  override val activationCriteria: Predicate = new Employing(TvR1Rax)
  override val completionCriteria: Predicate = new UnitsAtLeast(2, MatchWarriors)
  
  override val buildOrder = Vector(
    Get(9,Terran.SCV),
    Get(Terran.SupplyDepot),
    Get(11, Terran.SCV),
    Get(Terran.Barracks))
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new Or(
        new SafeAtHome,
        new UnitsAtLeast(1, Terran.Bunker),
        new UnitsAtLeast(8, Terran.Marine)),
      new PlaceBunkersAtNatural(2)),
  
    new Pump(Terran.Marine),
    new If(
      new Not(new SafeAtHome),
      new Build(
        Get(Terran.Bunker),
        Get(4, Terran.Barracks))),
    new RequireMiningBases(2))
}