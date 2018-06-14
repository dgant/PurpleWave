package Planning.Plans.GamePlans.Terran.FFA

import Lifecycle.With
import Macro.BuildRequests.{GetAtLeast, GetTech, GetUpgrade}
import Planning.Composition.UnitMatchers.{UnitMatchSiegeTank, UnitMatchWarriors}
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Terran.Situational.BunkersAtNatural
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBasesFFA}
import Planning.Plans.Predicates.Milestones.{EnemyHasShownCloakedThreat, UnitsAtLeast}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import ProxyBwapi.Races.Terran

class TerranFFABio extends GameplanModeTemplate {
  
  override def defaultPlacementPlan : Plan  = new BunkersAtNatural(2)
  override val defaultScoutPlan     : Plan  = NoPlan()
  override val aggression                   = 0.8
  override val scoutExpansionsAt            = 150
  
  private class UpgradeMech extends Parallel(
    new BuildGasPumps,
    new Build(
      GetAtLeast(1, Terran.Factory),
      GetAtLeast(2, Terran.Armory)),
    new UpgradeContinuously(Terran.MechDamage),
    new UpgradeContinuously(Terran.MechArmor),
    new Build(
      GetAtLeast(1, Terran.Starport),
      GetAtLeast(1, Terran.ScienceFacility)))
  
  override def defaultAttackPlan: Plan = new If(
    new UnitsAtLeast(20, UnitMatchWarriors),
    super.defaultAttackPlan)
  
  override lazy val buildOrder = Vector(
      GetAtLeast(1,   Terran.CommandCenter),
      GetAtLeast(9,   Terran.SCV),
      GetAtLeast(1,   Terran.SupplyDepot),
      GetAtLeast(11,  Terran.SCV),
      GetAtLeast(1,   Terran.Barracks),
      GetAtLeast(13,  Terran.SCV),
      GetAtLeast(2,   Terran.Barracks),
      GetAtLeast(14,  Terran.SCV),
      GetAtLeast(2,   Terran.SupplyDepot))
  
  override def emergencyPlans: Seq[Plan] = Vector(
    new If(
      new UnitsAtLeast(1, Terran.Academy, complete = true),
      new TrainContinuously(Terran.Comsat, 2)),
    new If(
      new EnemyHasShownCloakedThreat,
      new Build(
        GetAtLeast(1, Terran.ScienceVessel),
        GetAtLeast(1, Terran.Refinery),
        GetAtLeast(1, Terran.Academy),
        GetAtLeast(1, Terran.EngineeringBay),
        GetAtLeast(4, Terran.MissileTurret))),
    new If(
      new UnitsAtLeast(1, Terran.Ghost),
      new TrainContinuously(Terran.NuclearSilo))
  )
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(new UnitsAtLeast(1,  UnitMatchSiegeTank),    new Build(GetTech(Terran.SiegeMode))),
    new If(new UnitsAtLeast(12, UnitMatchWarriors),     new RequireMiningBasesFFA(2)),
    new If(new UnitsAtLeast(60, UnitMatchWarriors),     new RequireMiningBasesFFA(3)),
    new If(new UnitsAtLeast(90, UnitMatchWarriors),     new RequireMiningBasesFFA(4)),
    new If(new UnitsAtLeast(50, UnitMatchWarriors),     new TrainContinuously(Terran.Dropship, 2)),
    new TrainContinuously(Terran.CovertOps, 1),
    new TrainContinuously(Terran.Comsat, 2),
    new TrainContinuously(Terran.MachineShop),
    new If(new UnitsAtLeast(1, Terran.NuclearMissile),  new Build(GetTech(Terran.GhostCloak))),
    new If(new UnitsAtLeast(1, Terran.NuclearMissile),  new Build(GetUpgrade(Terran.GhostVisionRange))),
    new If(new UnitsAtLeast(3, Terran.Ghost),           new Build(GetTech(Terran.Lockdown))),
    new If(new UnitsAtLeast(8, Terran.Ghost),           new Build(GetUpgrade(Terran.GhostEnergy))),
    new If(new UnitsAtLeast(3, UnitMatchSiegeTank),     new UpgradeMech),
    new TrainContinuously(Terran.NuclearMissile),
    new TrainContinuously(Terran.NuclearSilo),
    new TrainContinuously(Terran.ScienceVessel, 2),
    new TrainContinuously(Terran.SiegeTankUnsieged, 30),
    new TrainContinuously(Terran.Ghost, 5, 2),
    new If(
      new Check(() =>
        With.units.countOurs(Terran.Marine).toDouble /
        (1 + With.units.countOurs(Terran.Medic))
        >= 4.0),
      new TrainContinuously(Terran.Medic, 20, 2)),
    new TrainContinuously(Terran.Marine),
    new Build(
      GetAtLeast(1, Terran.Refinery),
      GetAtLeast(1, Terran.Academy),
      GetAtLeast(1, Terran.EngineeringBay),
      GetTech(Terran.Stim)),
    new UpgradeContinuously(Terran.BioDamage),
    new RequireMiningBasesFFA(2),
    new Build(
      GetAtLeast(2, Terran.Bunker),
      GetAtLeast(2, Terran.EngineeringBay),
      GetUpgrade(Terran.MarineRange),
      GetAtLeast(6, Terran.Barracks)),
    new UpgradeContinuously(Terran.BioArmor),
    new BuildGasPumps,
    new Build(
      GetAtLeast(1, Terran.Factory),
      GetAtLeast(1, Terran.Starport),
      GetAtLeast(1, Terran.ScienceFacility),
      GetAtLeast(4, Terran.Factory),
      GetAtLeast(10, Terran.Barracks),
      GetAtLeast(2, Terran.Armory)),
    new UpgradeContinuously(Terran.MechDamage),
    new UpgradeContinuously(Terran.MechArmor),
    new Build(
      GetAtLeast(1, Terran.CovertOps),
      GetUpgrade(Terran.GhostEnergy),
      GetUpgrade(Terran.GhostVisionRange),
      GetTech(Terran.GhostCloak),
      GetTech(Terran.Lockdown),
      GetAtLeast(8, Terran.Factory))
  )
}