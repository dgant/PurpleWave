package Planning.Plans.GamePlans.Terran.FFA

import Lifecycle.With
import Macro.BuildRequests.{Get, Tech, Upgrade}
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
      Get(1, Terran.Factory),
      Get(2, Terran.Armory)),
    new UpgradeContinuously(Terran.MechDamage),
    new UpgradeContinuously(Terran.MechArmor),
    new Build(
      Get(1, Terran.Starport),
      Get(1, Terran.ScienceFacility)))
  
  override def defaultAttackPlan: Plan = new If(
    new UnitsAtLeast(20, UnitMatchWarriors),
    super.defaultAttackPlan)
  
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
      new TrainContinuously(Terran.Comsat, 2)),
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
      new TrainContinuously(Terran.NuclearSilo))
  )
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(new UnitsAtLeast(1,  UnitMatchSiegeTank),    new Build(Tech(Terran.SiegeMode))),
    new If(new UnitsAtLeast(12, UnitMatchWarriors),     new RequireMiningBasesFFA(2)),
    new If(new UnitsAtLeast(60, UnitMatchWarriors),     new RequireMiningBasesFFA(3)),
    new If(new UnitsAtLeast(90, UnitMatchWarriors),     new RequireMiningBasesFFA(4)),
    new If(new UnitsAtLeast(50, UnitMatchWarriors),     new TrainContinuously(Terran.Dropship, 2)),
    new TrainContinuously(Terran.CovertOps, 1),
    new TrainContinuously(Terran.Comsat, 2),
    new TrainContinuously(Terran.MachineShop),
    new If(new UnitsAtLeast(1, Terran.NuclearMissile),  new Build(Tech(Terran.GhostCloak))),
    new If(new UnitsAtLeast(1, Terran.NuclearMissile),  new Build(Upgrade(Terran.GhostVisionRange))),
    new If(new UnitsAtLeast(3, Terran.Ghost),           new Build(Tech(Terran.Lockdown))),
    new If(new UnitsAtLeast(8, Terran.Ghost),           new Build(Upgrade(Terran.GhostEnergy))),
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
      Get(1, Terran.Refinery),
      Get(1, Terran.Academy),
      Get(1, Terran.EngineeringBay),
      Tech(Terran.Stim)),
    new UpgradeContinuously(Terran.BioDamage),
    new RequireMiningBasesFFA(2),
    new Build(
      Get(2, Terran.Bunker),
      Get(2, Terran.EngineeringBay),
      Upgrade(Terran.MarineRange),
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
      Upgrade(Terran.GhostEnergy),
      Upgrade(Terran.GhostVisionRange),
      Tech(Terran.GhostCloak),
      Tech(Terran.Lockdown),
      Get(8, Terran.Factory))
  )
}