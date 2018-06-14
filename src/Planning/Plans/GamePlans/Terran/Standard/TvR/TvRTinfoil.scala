package Planning.Plans.GamePlans.Terran.Standard.TvR

import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.Get
import Planning.Composition.UnitMatchers.UnitMatchSiegeTank
import Planning.Plan
import Planning.Plans.Compound.{If, NoPlan}
import Planning.Plans.GamePlans.GameplanModeTemplateVsRandom
import Planning.Plans.Predicates.Employing
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Predicates.Milestones.UnitsAtLeast
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvR.TvRTinfoil

class TvRTinfoil extends GameplanModeTemplateVsRandom {
  
  override val activationCriteria: Plan = new Employing(TvRTinfoil)
  override val completionCriteria: Plan = new UnitsAtLeast(8, UnitMatchSiegeTank, complete = true)
  
  override lazy val blueprints = Vector(
    new Blueprint(this, building = Some(Terran.Bunker),       placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this, building = Some(Terran.Barracks),     placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this, building = Some(Terran.SupplyDepot),  placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this, building = Some(Terran.Barracks),     placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this, building = Some(Terran.SupplyDepot),  placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this, building = Some(Terran.Factory),      placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this, building = Some(Terran.SupplyDepot),  placement = Some(PlacementProfiles.hugTownHall)))
  
  override def defaultAttackPlan: Plan = NoPlan()
  
  override val buildOrder = Vector(
    Get(1,   Terran.CommandCenter),
    Get(9,   Terran.SCV),
    Get(1,   Terran.Barracks),
    Get(1,   Terran.SupplyDepot),
    Get(11,  Terran.SCV),
    Get(1,   Terran.Marine),
    Get(1,   Terran.Bunker))
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(new UnitsAtLeast(1, UnitMatchSiegeTank), new Build(Get(Terran.SiegeMode))),
    new Pump(Terran.SiegeTankUnsieged),
    new If(
      new UnitsAtLeast(6, Terran.Marine),
      new Pump(Terran.Medic, 3, 1)),
    new Pump(Terran.Marine),
    new Build(
      Get(2, Terran.Barracks),
      Get(1, Terran.Refinery),
      Get(1, Terran.Factory),
      Get(1, Terran.MachineShop),
      Get(2, Terran.Bunker),
      Get(1, Terran.Academy),
      Get(1, Terran.Comsat),
      Get(1, Terran.EngineeringBay),
      Get(2, Terran.MissileTurret),
      Get(2, Terran.Factory),
      Get(Terran.Stim),
      Get(2, Terran.MachineShop))
  )
}