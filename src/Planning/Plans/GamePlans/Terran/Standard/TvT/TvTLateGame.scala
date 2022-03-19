package Planning.Plans.GamePlans.Terran.Standard.TvT

import Macro.Buildables.Get
import Planning.Plan
import Planning.Plans.Army.{AttackAndHarass, FloatBuildings}
import Planning.Plans.Compound.{If, Parallel, Trigger}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Predicates.Compound.Or
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.Employing
import Planning.UnitMatchers._
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvT2Base2Port

class TvTLateGame extends GameplanTemplate {

  override def attackPlan: Plan = new Trigger(
    new Or(
      new UnitsAtLeast(5, Terran.Factory, complete = true),
      new TechStarted(Terran.WraithCloak)),
    new AttackAndHarass)

  override def workerPlan: Plan = new Parallel(
    new Pump(Terran.Comsat),
    new PumpWorkers(oversaturate = false))

  override def buildPlans: Seq[Plan] = Seq(
    new RequireMiningBases(2),
    new Build(
      Get(Terran.Barracks),
      Get(Terran.Refinery),
      Get(Terran.Factory),
      Get(Terran.MachineShop)),
    new BuildGasPumps,
    new If(new UnitsAtLeast(8, MatchTank, complete = true), new RequireMiningBases(3)),
    new UpgradeContinuously(Terran.MechDamage),
    new If(
      new UnitsAtLeast(2, Terran.Battlecruiser),
      new Parallel(
        new Build(Get(Terran.Yamato)),
        new UpgradeContinuously(Terran.AirArmor),
        new If(
          new UpgradeComplete(Terran.AirArmor, 3),
          new UpgradeContinuously(Terran.AirDamage)))),
    new If(
      new Or(
        new UnitsAtLeast(2, Terran.Armory),
        new UpgradeComplete(Terran.MechDamage, 3)),
      new UpgradeContinuously(Terran.MechArmor)),
    new If(new MiningBasesAtLeast(3), new Build(Get(7, Terran.Factory))),
    new If(new MiningBasesAtLeast(4), new Build(Get(10, Terran.Factory))),
    new If(new MiningBasesAtLeast(5), new Build(Get(14, Terran.Factory))),
    new If(new GasPumpsAtLeast(3),
      new Build(
        Get(Terran.Starport),
        Get(Terran.ScienceFacility),
        Get(Terran.ControlTower),
        Get(2, Terran.Armory),
        Get(Terran.WraithCloak),
        Get(3, Terran.MachineShop))),
    new If(new GasPumpsAtLeast(4), new Build(Get(6, Terran.MachineShop))),
    new PumpRatio(Terran.Armory, 0, 1, Seq(Enemy(Terran.Wraith, 1.0))),
    new If(
      new Or(
        new UnitsAtMost(0, Terran.PhysicsLab),
        new EnemyHasShownWraithCloak),
      new Pump(Terran.ScienceVessel, 1)),
    new If(
      new Or(
        new EnemyHasShown(Terran.Wraith),
        new EnemiesAtLeast(1, MatchAnd(MatchBuilding, Match(u => u.flying)))),
      new UpgradeContinuously(Terran.GoliathAirRange)),
    new Pump(Terran.Battlecruiser),
    new PumpRatio(Terran.Goliath, 0, 50, Seq(Enemy(Terran.Wraith, 3.0), Enemy(Terran.Dropship, 2.0), Enemy(Terran.Battlecruiser, 6.0))),
    new If(new EnemyHasShownWraithCloak, new Build(Get(Terran.Academy), Get(Terran.Starport), Get(Terran.ScienceFacility), Get(Terran.ControlTower))),
    new If(new UnitsAtMost(0, Terran.Armory, complete = true), new PumpRatio(Terran.Marine, 0, 8, Seq(Enemy(Terran.Wraith, 4.0)))),
    new If(
      new Employing(TvT2Base2Port),
      new Pump(Terran.Wraith)),
    new Pump(Terran.Wraith, 3),
    new Pump(Terran.SiegeTankUnsieged),
    new Build(
      Get(2, Terran.Factory),
      Get(Terran.SiegeMode),
      Get(Terran.Armory),
      Get(5, Terran.Factory),
      Get(Terran.Academy),
      Get(Terran.VultureSpeed),
      Get(Terran.SpiderMinePlant),
      Get(Terran.GoliathAirRange),
      Get(2, Terran.MachineShop)),
    new Pump(Terran.Vulture, 12),
    new Pump(Terran.Goliath),
    new Pump(Terran.Vulture),
    new RequireMiningBases(3),
    new PumpWorkers,
    new RequireMiningBases(5),
    new FloatBuildings(Terran.Barracks, Terran.EngineeringBay)
  )
}
