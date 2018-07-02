package Planning.Plans.GamePlans.Zerg.ZvP

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.BuildRequests.Get
import Micro.Squads.Goals.GoalDroneBlockRamp
import Planning.Plans.Army.{Attack, EjectScout, SquadPlan}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Zerg.ZergIdeas.{PumpMutalisks, ScoutSafelyWithOverlord, TrainJustEnoughScourge, PumpJustEnoughZerglings}
import Planning.Plans.Macro.Automatic.{CapGasAtRatioToMinerals, Pump, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.BuildOrder
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases}
import Planning.Plans.Macro.Zerg.{BuildSunkensAtExpansions, BuildSunkensAtNatural}
import Planning.Predicates.Compound.{And, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy, OnMap}
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.UnitCountExactly
import Planning.UnitPreferences.UnitPreferClose
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Zerg
import Strategery.Strategies.Zerg.ZvPThirteenPoolMuta
import Strategery.ThirdWorld

class ZvPThirteenPoolMuta extends GameplanModeTemplate {
  
  override val activationCriteria: Predicate = new Employing(ZvPThirteenPoolMuta)
  
  override def defaultScoutPlan: Plan = new ScoutSafelyWithOverlord
  
  class DroneBlockRamp extends SquadPlan[GoalDroneBlockRamp] {
    val goal: GoalDroneBlockRamp = new GoalDroneBlockRamp
    val drone = new LockUnits
    drone.unitMatcher.set(Zerg.Drone)
    drone.unitCounter.set(UnitCountExactly(1))
    drone.unitPreference.set(UnitPreferClose(goal.destination))
    drone.interruptable.set(false)
    var killedScout = false
    override def onUpdate(): Unit = {
      killedScout = killedScout || squad.previousUnits.exists(_.kills > 0)
      if (killedScout) return
      super.onUpdate()
      drone.acquire(this)
      drone.units.foreach(squad.recruit)
    }
  }
  
  override def priorityAttackPlan: Plan =  new If(
    new And(
      new OnMap(ThirdWorld),
      new FrameAtLeast(GameTime(1, 6)()),
      new FrameAtMost(GameTime(3, 10)())),
    new DroneBlockRamp)
  
  override def defaultAttackPlan: Plan = new Attack
  
  override def defaultBuildOrder: Plan = new Parallel (
    new BuildOrder(
      Get(9, Zerg.Drone),
      Get(2, Zerg.Overlord),
      Get(14, Zerg.Drone),
      Get(Zerg.SpawningPool),
      Get(Zerg.Extractor),
      Get(2, Zerg.Hatchery),
      Get(Zerg.Lair),
      Get(4, Zerg.Zergling)))
  
  override def buildPlans: Seq[Plan] = Vector(
    new CapGasAtRatioToMinerals(1.0, 200),
    new EjectScout,
    new Pump(Zerg.SunkenColony),
    new Pump(Zerg.Drone, 9),
    new Pump(Zerg.Spire, 1),
    new TrainJustEnoughScourge,
    new If(
      new UnitsAtLeast(12, Zerg.Mutalisk),
      new Parallel(
        new RequireBases(3),
        new Pump(Zerg.Drone, 22))),
    new If(new UnitsAtLeast(10, Zerg.Mutalisk), new UpgradeContinuously(Zerg.AirArmor)),
    new If(
      new Or(
        new Not(new UnitsAtLeast(6, Zerg.Mutalisk, countEggs = true)),
        new UnitsAtLeast(18, Zerg.Drone, countEggs = true)),
      new PumpMutalisks,
      new PumpMutalisks(1)),
    new Trigger(
      new UnitsAtLeast(4, Zerg.Mutalisk),
      new Parallel(
        new If(
          new EnemyStrategy(
            With.fingerprints.proxyGateway,
            With.fingerprints.twoGate),
          new Parallel(
            new Pump(Zerg.Zergling, 12),
            new BuildSunkensAtNatural(2)),
          new If(
            new EnemyStrategy(
              With.fingerprints.gatewayFirst,
              With.fingerprints.gatewayFe,
              With.fingerprints.oneGateCore),
            new Parallel(
              new PumpJustEnoughZerglings(3, 7),
              new BuildSunkensAtNatural(1)))))),
    new BuildOrder(
      Get(23, Zerg.Drone),
      Get(5, Zerg.Overlord)),
    new If(
      new UnitsAtLeast(18, Zerg.Drone, countEggs = true),
      new Trigger(new UnitsAtLeast(5, Zerg.Overlord, countEggs = true), new BuildGasPumps)),
    new Trigger(
      new UnitsAtLeast(1, Zerg.Spire, complete = true),
      new Parallel(
        new If(
          new UnitsAtLeast(22, Zerg.Drone),
          new BuildGasPumps),
        new Pump(Zerg.Drone, 27),
        new If(
          new UnitsAtLeast(25, Zerg.Drone),
          new Parallel(
            new BuildSunkensAtExpansions(3),
            new BuildSunkensAtNatural(2))),
        new Pump(Zerg.Zergling),
        new RequireBases(5)
      )))
}
