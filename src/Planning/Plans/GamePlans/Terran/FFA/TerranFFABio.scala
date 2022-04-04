package Planning.Plans.GamePlans.Terran.FFA

import Lifecycle.With
import Macro.Requests.Get
import Planning.Predicates.Compound.Check
import Utilities.UnitFilters.{IsTank, IsWarrior}
import Planning.Plan
import Planning.Plans.Army.Aggression
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Terran.Situational.PlaceBunkersAtNatural
import Planning.Plans.Macro.Automatic.{Pump, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBasesFFA}
import Planning.Predicates.Milestones.{EnemyHasShownCloakedThreat, UnitsAtLeast}
import ProxyBwapi.Races.Terran

class TerranFFABio extends GameplanTemplate {
  
  override def placementPlan : Plan  = new PlaceBunkersAtNatural(2)
  override val scoutPlan     : Plan  = NoPlan()
  override def aggressionPlan: Plan = new Aggression(0.8)
  
  private class UpgradeMech extends Parallel(
    new BuildGasPumps,
    new Build(
      Get(1, Terran.Factory),
      Get(2, Terran.Armory)),
    new UpgradeContinuously(Terran.MechDamage),
    new UpgradeContinuously(Terran.MechArmor),
    new Build(
      Get(1, Terran.Starport),
      Get(1, Terran.ScienceFacility)))
  
  override def attackPlan: Plan = new If(
    new UnitsAtLeast(20, IsWarrior),
    super.attackPlan)
  
  override lazy val buildOrder = Vector(
      Get(1,   Terran.CommandCenter),
      Get(9,   Terran.SCV),
      Get(1,   Terran.SupplyDepot),
      Get(11,  Terran.SCV),
      Get(1,   Terran.Barracks),
      Get(13,  Terran.SCV),
      Get(2,   Terran.Barracks),
      Get(14,  Terran.SCV),
      Get(2,   Terran.SupplyDepot))
  
  override def emergencyPlans: Seq[Plan] = Vector(
    new If(
      new UnitsAtLeast(1, Terran.Academy, complete = true),
      new Pump(Terran.Comsat, 2)),
    new If(
      new EnemyHasShownCloakedThreat,
      new Build(
        Get(1, Terran.ScienceVessel),
        Get(1, Terran.Refinery),
        Get(1, Terran.Academy),
        Get(1, Terran.EngineeringBay),
        Get(4, Terran.MissileTurret))),
    new If(
      new UnitsAtLeast(1, Terran.Ghost),
      new Pump(Terran.NuclearSilo))
  )
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(new UnitsAtLeast(1,  IsTank),    new Build(Get(Terran.SiegeMode))),
    new If(new UnitsAtLeast(12, IsWarrior),     new RequireMiningBasesFFA(2)),
    new If(new UnitsAtLeast(60, IsWarrior),     new RequireMiningBasesFFA(3)),
    new If(new UnitsAtLeast(90, IsWarrior),     new RequireMiningBasesFFA(4)),
    new If(new UnitsAtLeast(50, IsWarrior),     new Pump(Terran.Dropship, 2)),
    new Pump(Terran.CovertOps, 1),
    new Pump(Terran.Comsat, 2),
    new Pump(Terran.MachineShop),
    new If(new UnitsAtLeast(1, Terran.NuclearMissile),  new Build(Get(Terran.GhostCloak))),
    new If(new UnitsAtLeast(1, Terran.NuclearMissile),  new Build(Get(Terran.GhostVisionRange))),
    new If(new UnitsAtLeast(3, Terran.Ghost),           new Build(Get(Terran.Lockdown))),
    new If(new UnitsAtLeast(8, Terran.Ghost),           new Build(Get(Terran.GhostEnergy))),
    new If(new UnitsAtLeast(3, IsTank),     new UpgradeMech),
    new Pump(Terran.NuclearMissile),
    new Pump(Terran.NuclearSilo),
    new Pump(Terran.ScienceVessel, 2),
    new Pump(Terran.SiegeTankUnsieged, 30),
    new Pump(Terran.Ghost, 5, 2),
    new If(
      new Check(() =>
        With.units.countOurs(Terran.Marine).toDouble /
        (1 + With.units.countOurs(Terran.Medic))
        >= 4.0),
      new Pump(Terran.Medic, 20, 2)),
    new Pump(Terran.Marine),
    new Build(
      Get(1, Terran.Refinery),
      Get(1, Terran.Academy),
      Get(1, Terran.EngineeringBay),
      Get(Terran.Stim)),
    new UpgradeContinuously(Terran.BioDamage),
    new RequireMiningBasesFFA(2),
    new Build(
      Get(2, Terran.Bunker),
      Get(2, Terran.EngineeringBay),
      Get(Terran.MarineRange),
      Get(6, Terran.Barracks)),
    new UpgradeContinuously(Terran.BioArmor),
    new BuildGasPumps,
    new Build(
      Get(1, Terran.Factory),
      Get(1, Terran.Starport),
      Get(1, Terran.ScienceFacility),
      Get(4, Terran.Factory),
      Get(10, Terran.Barracks),
      Get(2, Terran.Armory)),
    new UpgradeContinuously(Terran.MechDamage),
    new UpgradeContinuously(Terran.MechArmor),
    new Build(
      Get(1, Terran.CovertOps),
      Get(Terran.GhostEnergy),
      Get(Terran.GhostVisionRange),
      Get(Terran.GhostCloak),
      Get(Terran.Lockdown),
      Get(8, Terran.Factory))
  )
}