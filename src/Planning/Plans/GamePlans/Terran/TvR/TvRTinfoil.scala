package Planning.Plans.GamePlans.Terran.TvR

import Macro.Requests.Get
import Planning.Plans.Army.AttackAndHarass
import Planning.Plans.Compound.{If, Trigger}
import Planning.Plans.GamePlans.Terran.RepairBunker
import Planning.Plans.Macro.Automatic.{Friendly, Pump, PumpRatio}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.BuildGasPumps
import Planning.Plans.Macro.Terran.PopulateBunkers
import Planning.Predicates.Milestones.{UnitsAtLeast, UpgradeComplete}
import Planning.Predicates.Strategy.Employing
import Utilities.UnitFilters.IsWarrior
import Planning.Plan
import Planning.Plans.GamePlans.All.GameplanTemplateVsRandom
import Planning.Predicates.Predicate
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvR.TvRTinfoil

class TvRTinfoil extends GameplanTemplateVsRandom {
  
  override val activationCriteria: Predicate = new Employing(TvRTinfoil)
  
  override def attackPlan: Plan = new If(
    new UpgradeComplete(Terran.MarineRange),
    new AttackAndHarass)
  
  override val buildOrder = Vector(
    Get(9,  Terran.SCV),
    Get(Terran.SupplyDepot),
    Get(10, Terran.SCV),
    Get(Terran.Barracks),
    Get(13, Terran.SCV),
    Get(Terran.Marine),
    Get(Terran.Bunker),
    Get(14, Terran.SCV))
  
  override def buildPlans: Seq[Plan] = Vector(
    new RepairBunker,
    new Trigger(
      new UnitsAtLeast(10, IsWarrior),
      initialBefore = new PopulateBunkers),
    new PumpRatio(Terran.Medic, 0, 4, Seq(Friendly(Terran.Marine, 0.2))),
    new Pump(Terran.SiegeTankUnsieged),
    new Pump(Terran.Marine, maximumConcurrently = 1),
    new BuildGasPumps,
    new Build(
      Get(Terran.Factory),
      Get(Terran.MachineShop),
      Get(Terran.SiegeMode),
      Get(Terran.EngineeringBay),
      Get(Terran.Academy),
      Get(Terran.BioDamage),
      Get(Terran.MissileTurret),
      Get(Terran.Stim),
      Get(Terran.Comsat),
      Get(Terran.BioArmor),
      Get(Terran.MarineRange),
      Get(4, Terran.Barracks)),
    new Pump(Terran.Marine)
  )
}