package Planning.Plans.GamePlans.Protoss.Standard.PvR

import Macro.BuildRequests.Get
import Planning.{Plan, Predicate}
import Planning.Plans.GamePlans.GameplanTemplateVsRandom
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Predicates.Milestones.UnitsAtLeast
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvROpen2Gate1012

class PvR2Gate1012 extends GameplanTemplateVsRandom {
  
  override val activationCriteria: Predicate = new Employing(PvROpen2Gate1012)
  override val completionCriteria: Predicate = new UnitsAtLeast(2, Protoss.Zealot)
  override val buildOrder         = ProtossBuilds.TwoGate1012
  override def scoutPlan   = new ScoutOn(Protoss.Pylon)
  override def attackPlan  = new Plan
  
  override def buildPlans = Vector(
    new Pump(Protoss.Zealot, 5),
    new Build(
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore)))
}
