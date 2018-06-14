package Planning.Plans.GamePlans.Terran.Standard.TvR

import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{GetAtLeast, GetTech}
import Planning.Composition.UnitMatchers.UnitMatchSiegeTank
import Planning.Plan
import Planning.Plans.Compound.{If, NoPlan}
import Planning.Plans.GamePlans.GameplanModeTemplateVsRandom
import Planning.Plans.Predicates.Employing
import Planning.Plans.Macro.Automatic.TrainContinuously
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
    GetAtLeast(1,   Terran.CommandCenter),
    GetAtLeast(9,   Terran.SCV),
    GetAtLeast(1,   Terran.Barracks),
    GetAtLeast(1,   Terran.SupplyDepot),
    GetAtLeast(11,  Terran.SCV),
    GetAtLeast(1,   Terran.Marine),
    GetAtLeast(1,   Terran.Bunker))
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(new UnitsAtLeast(1, UnitMatchSiegeTank), new Build(GetTech(Terran.SiegeMode))),
    new TrainContinuously(Terran.SiegeTankUnsieged),
    new If(
      new UnitsAtLeast(6, Terran.Marine),
      new TrainContinuously(Terran.Medic, 3, 1)),
    new TrainContinuously(Terran.Marine),
    new Build(
      GetAtLeast(2, Terran.Barracks),
      GetAtLeast(1, Terran.Refinery),
      GetAtLeast(1, Terran.Factory),
      GetAtLeast(1, Terran.MachineShop),
      GetAtLeast(2, Terran.Bunker),
      GetAtLeast(1, Terran.Academy),
      GetAtLeast(1, Terran.Comsat),
      GetAtLeast(1, Terran.EngineeringBay),
      GetAtLeast(2, Terran.MissileTurret),
      GetAtLeast(2, Terran.Factory),
      GetTech(Terran.Stim),
      GetAtLeast(2, Terran.MachineShop))
  )
}