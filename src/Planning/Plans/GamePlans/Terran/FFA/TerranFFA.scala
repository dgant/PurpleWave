package Planning.Plans.GamePlans.Terran.FFA

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.{UnitMatchSiegeTank, UnitMatchWarriors}
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Terran.Situational.BunkersAtNatural
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBasesFFA}
import Planning.Plans.Macro.Milestones.{EnemyHasShownCloakedThreat, IfOnMiningBases, OnGasPumps, UnitsAtLeast}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitClass.UnitClass

class TerranFFA extends GameplanModeTemplate {
  
  override def defaultPlacementPlan : Plan  = new If (new UnitsAtLeast(1, Terran.Bunker), new BunkersAtNatural(2))
  override val defaultScoutPlan     : Plan  = NoPlan()
  override val aggression                   = 0.8
  override val scoutExpansionsAt            = 150
  
  private class UpgradeBio extends Parallel(
    new Build(
      RequestAtLeast(1, Terran.Academy),
      RequestTech(Terran.Stim),
      RequestUpgrade(Terran.MarineRange),
      RequestAtLeast(2, Terran.EngineeringBay)),
    new UpgradeContinuously(Terran.BioDamage),
    new UpgradeContinuously(Terran.BioArmor),
    new Build(
      RequestAtLeast(1, Terran.Factory),
      RequestAtLeast(1, Terran.Starport),
      RequestAtLeast(1, Terran.ScienceFacility)))
  
  private class UpgradeMech extends Parallel(
    new BuildGasPumps,
    new Build(
      RequestAtLeast(1, Terran.Factory),
      RequestAtLeast(2, Terran.Armory)),
    new UpgradeContinuously(Terran.MechDamage),
    new UpgradeContinuously(Terran.MechArmor),
    new Build(
      RequestAtLeast(1, Terran.Starport),
      RequestAtLeast(1, Terran.ScienceFacility)))
  
  private class UpgradeAir extends Parallel(
    new BuildGasPumps,
    new Build(
      RequestAtLeast(1, Terran.Factory),
      RequestAtLeast(1, Terran.Starport),
      RequestAtLeast(2, Terran.Armory)),
    new UpgradeContinuously(Terran.AirDamage),
    new UpgradeContinuously(Terran.AirArmor),
    new Build(RequestAtLeast(1, Terran.ScienceFacility)))
  
  private class BuildScienceFacilityForAddon(addon: UnitClass) extends Plan {
    val build = new Build()
    override def onUpdate() {
      val numberOfCovertOps         = With.units.ours.count(_.is(Terran.CovertOps))
      val numberOfPhysicsLab        = With.units.ours.count(_.is(Terran.PhysicsLab))
      val numberOfThisAddon         = if (addon == Terran.CovertOps) numberOfCovertOps else numberOfPhysicsLab
      val numberOfOtherAddon        = numberOfCovertOps + numberOfPhysicsLab - numberOfThisAddon
      val numberOfScienceFacilities = if (numberOfThisAddon > 0) 0 else 1 + numberOfOtherAddon
      
      if (numberOfScienceFacilities > 0) {
        build.requests.set(Vector(RequestAtLeast(numberOfScienceFacilities, Terran.ScienceFacility)))
      }
      build.update()
    }
  }
  
  override def defaultAttackPlan: Plan = new If(
    new UnitsAtLeast(20, UnitMatchWarriors),
    super.defaultAttackPlan)
  
  override lazy val buildOrder = Vector(
      RequestAtLeast(1,   Terran.CommandCenter),
      RequestAtLeast(9,   Terran.SCV),
      RequestAtLeast(1,   Terran.SupplyDepot),
      RequestAtLeast(11,  Terran.SCV),
      RequestAtLeast(1,   Terran.Barracks),
      RequestAtLeast(13,  Terran.SCV),
      RequestAtLeast(2,   Terran.Barracks),
      RequestAtLeast(14,  Terran.SCV),
      RequestAtLeast(2,   Terran.SupplyDepot))
  
  override def emergencyPlans: Seq[Plan] = Vector(
    new If(
      new UnitsAtLeast(1, Terran.Academy, complete = true),
      new TrainContinuously(Terran.Comsat, 2)),
    new If(
      new EnemyHasShownCloakedThreat,
      new Build(
        RequestAtLeast(1, Terran.ScienceVessel),
        RequestAtLeast(1, Terran.Refinery),
        RequestAtLeast(1, Terran.Academy),
        RequestAtLeast(1, Terran.EngineeringBay),
        RequestAtLeast(4, Terran.MissileTurret))),
    new If(
      new UnitsAtLeast(1, Terran.Ghost),
      new TrainContinuously(Terran.NuclearSilo))
  )
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new And(
        new UnitsAtLeast(20, UnitMatchWarriors),
        new Check(() => With.self.gas > 1000)),
      new Parallel(
        new Build(RequestAtLeast(1, Terran.Factory)),
        new BuildScienceFacilityForAddon(Terran.CovertOps),
        new Build(RequestAtLeast(1, Terran.CovertOps)),
        new TrainContinuously(Terran.Ghost))
    ),
    new If(new UnitsAtLeast(1,  UnitMatchSiegeTank),    new Build(RequestTech(Terran.SiegeMode))),
    new If(new UnitsAtLeast(12,  UnitMatchWarriors),    new RequireMiningBasesFFA(2)),
    new If(new UnitsAtLeast(30, UnitMatchWarriors),     new RequireMiningBasesFFA(3)),
    new If(new UnitsAtLeast(40, UnitMatchWarriors),     new RequireMiningBasesFFA(4)),
    new If(new UnitsAtLeast(50, UnitMatchWarriors),     new RequireMiningBasesFFA(5)),
    new If(new UnitsAtLeast(30, UnitMatchWarriors),     new TrainContinuously(Terran.Dropship, 2)),
    new TrainContinuously(Terran.Comsat, 2),
    new TrainContinuously(Terran.Battlecruiser),
    new If(new UnitsAtLeast(1, Terran.NuclearMissile),  new Build(RequestTech(Terran.GhostCloak))),
    new If(new UnitsAtLeast(1, Terran.NuclearMissile),  new Build(RequestUpgrade(Terran.GhostVisionRange))),
    new If(new UnitsAtLeast(3, Terran.Ghost),           new Build(RequestTech(Terran.Lockdown))),
    new If(new UnitsAtLeast(8, Terran.Ghost),           new Build(RequestUpgrade(Terran.GhostEnergy))),
    new If(new UnitsAtLeast(1, Terran.Ghost),           new TrainContinuously(Terran.NuclearMissile)),
    new If(new UnitsAtLeast(3, Terran.Battlecruiser),   new Build(RequestTech(Terran.Yamato))),
    new If(new UnitsAtLeast(6, Terran.Battlecruiser),   new Build(RequestUpgrade(Terran.BattlecruiserEnergy))),
    new If(new UnitsAtLeast(5, Terran.Battlecruiser),   new UpgradeAir),
    new If(new UnitsAtLeast(12, Terran.Marine),         new UpgradeBio),
    new If(new UnitsAtLeast(3, UnitMatchSiegeTank),     new UpgradeMech),
    new TrainContinuously(Terran.ScienceVessel, 2, 1),
    new TrainContinuously(Terran.SiegeTankUnsieged, 30),
    new TrainContinuously(Terran.Ghost, 5, 2),
    new If(
      new Check(() =>
        With.units.ours.count(_.is(Terran.Marine)).toDouble /
        Math.max(
          1.0,
          With.units.ours.count(_.is(Terran.Medic)))
        >= 4.0),
      new TrainContinuously(Terran.Medic, 20, 2)),
    new TrainContinuously(Terran.Marine),
    new Build(
      RequestAtLeast(1, Terran.Refinery),
      RequestAtLeast(1, Terran.Academy),
      RequestAtLeast(1, Terran.EngineeringBay),
      RequestAtLeast(1, Terran.MissileTurret)),
    new RequireMiningBasesFFA(2),
    new IfOnMiningBases(2,
      new Build(
        RequestAtLeast(2, Terran.Bunker),
        RequestAtLeast(2, Terran.MissileTurret),
        RequestAtLeast(5, Terran.Barracks))),
    new BuildGasPumps,
    new FlipIf(
      new UnitsAtLeast(50, UnitMatchWarriors),
      new Parallel(
        new TrainContinuously(Terran.MachineShop),
        new OnGasPumps(2, new Build(RequestAtLeast(3, Terran.Factory))),
        new OnGasPumps(3, new Build(RequestAtLeast(5, Terran.Factory))),
        new OnGasPumps(4, new Build(RequestAtLeast(8, Terran.Factory))),
        new IfOnMiningBases(2, new Build(RequestAtLeast(6, Terran.Barracks))),
        new IfOnMiningBases(3, new Build(RequestAtLeast(10, Terran.Barracks))),
        new IfOnMiningBases(4, new Build(RequestAtLeast(12, Terran.Barracks)))),
      new OnGasPumps(2,
        new Parallel(
          new RequireMiningBasesFFA(4),
          new Build(RequestAtLeast(1, Terran.Starport)),
          new BuildScienceFacilityForAddon(Terran.PhysicsLab),
          new Build(RequestAtLeast(1, Terran.PhysicsLab)),
          new TrainContinuously(Terran.ControlTower),
          new OnGasPumps(2, new Build(RequestAtLeast(2, Terran.Starport))),
          new OnGasPumps(3, new Build(RequestAtLeast(3, Terran.Starport))),
          new OnGasPumps(4, new Build(RequestAtLeast(4, Terran.Starport)))))),
    new Build(RequestAtLeast(15, Terran.Barracks))
  )
}