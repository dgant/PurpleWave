package Planning.Plans.GamePlans.Terran.Standard.TvP

import Lifecycle.With
import Macro.Requests.{RequestProduction, Get}
import Planning.Plans.Army.{Aggression, AttackAndHarass}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.BuildGasPumps
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.{Latch, Not, Or}
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers.{MatchOr, MatchTank}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Terran.TvP2FacJoyO

class TvP2FacJoyO extends GameplanTemplate {
  
  override val activationCriteria: Predicate = new Employing(TvP2FacJoyO)
  override val completionCriteria: Predicate = new Latch(new BasesAtLeast(2))

  override def scoutPlan: Plan = new ScoutOn(Terran.SupplyDepot, quantity = 2)
  override def aggressionPlan: Plan = new Aggression(1.5)
  
  override def attackPlan: Plan = new Trigger(
    new Or(
      new UnitsAtLeast(3, MatchTank, complete = true),
      new EnemyStrategy(With.fingerprints.nexusFirst)),
    new AttackAndHarass)

  val vulturesVs2Gate = 7
  override def emergencyPlans: Seq[Plan] = Seq(new If(
    new EnemyStrategy(With.fingerprints.twoGate),
    new Pump(Terran.Vulture, 7)
  ))

  override def workerPlan: Plan = new PumpWorkers

  // https://liquipedia.net/starcraft/JoyO_Rush
  override def buildOrder: Seq[RequestProduction] = Vector(
    Get(9, Terran.SCV),
    Get(Terran.SupplyDepot),
    Get(11, Terran.SCV),
    Get(Terran.Barracks),
    Get(12, Terran.SCV),
    Get(Terran.Refinery),
    Get(15, Terran.SCV), // Theoretically 13 for a wall-in -> leave unfinished until 15
    Get(2, Terran.SupplyDepot),
    Get(16, Terran.SCV),
    Get(Terran.Factory),
    Get(19, Terran.SCV),
    Get(2, Terran.Factory),
    Get(Terran.Marine)
  )
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new Or(
        new Not(new EnemyStrategy(With.fingerprints.twoGate)),
        new UnitsAtLeast(vulturesVs2Gate, Terran.Vulture)),
      new Pump(Terran.MachineShop, 2)),
    new BuildGasPumps,
    new If(
      new Or(
        new EnemyHasShown(Protoss.Forge),
        new EnemyHasShown(Protoss.PhotonCannon)),
      new Build(Get(Terran.SiegeMode))),
    new Trigger(
      new UnitsAtLeast(3, MatchTank),
      new Parallel(
        new If(
          new UnitsAtMost(2, Terran.Factory, complete = true),
          new Pump(Terran.Marine)),
        new Build(Get(Terran.VultureSpeed), Get(Terran.SpiderMinePlant)),
        new PumpRatio(Terran.Armory, 0, 1, Seq(Enemy(MatchOr(Protoss.Stargate, Protoss.Carrier, Protoss.Scout, Protoss.Arbiter), 1.0))),
        new If(
          new UnitsAtLeast(1, Terran.Armory),
          new Parallel(
            new UpgradeContinuously(Terran.GoliathAirRange),
            new UpgradeContinuously(Terran.MechArmor, 1),
            new UpgradeContinuously(Terran.MechDamage, 1),
            new PumpRatio(Terran.Goliath, 4, 50, Seq(Enemy(Protoss.Carrier, 6.0), Enemy(Protoss.Scout, 1.0), Enemy(Protoss.Arbiter, 1.0))),
          )),
        new If(
          new UnitsAtLeast(5, Terran.Factory, complete = true),
          new Pump(Terran.SiegeTankUnsieged, maximumConcurrently = 3),
          new If(
            new UnitsAtLeast(3, Terran.Factory, complete = true),
            new Pump(Terran.SiegeTankUnsieged, maximumConcurrently = 1))),
        new Pump(Terran.Vulture),
        new Build(
          Get(Terran.SiegeMode),
          Get(2, Terran.CommandCenter),
          Get(Terran.Academy),
          Get(Terran.EngineeringBay))),
      new Parallel(
        new FlipIf(
          new UnitsAtLeast(2, Terran.MachineShop),
          new Pump(Terran.Marine),
          new Pump(Terran.SiegeTankUnsieged))))
  )
}
