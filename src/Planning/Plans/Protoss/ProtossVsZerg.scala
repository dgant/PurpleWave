package Planning.Plans.Protoss

import Macro.BuildRequests.Request
import Planning.Composition.UnitMatchers.{UnitMatchType, UnitMatchWarriors}
import Planning.Plans.Army._
import Planning.Plans.Compound.{If, _}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildAssimilators, RequireMiningBases}
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Protoss.Situational.ForgeFastExpand
import Planning.Plans.Scouting.ScoutAt
import ProxyBwapi.Races.Protoss

class ProtossVsZerg extends Parallel {
  
  description.set("Protoss vs Zerg")
  
  class FiveGateGoon_Start extends Parallel(
    // http://wiki.teamliquid.net/starcraft/5_Gate_Ranged_Goons_(vs._Zerg)
    new Build(
      Request.unit(Protoss.Nexus),
      Request.unit(Protoss.Probe,         8),
      Request.unit(Protoss.Pylon),
      Request.unit(Protoss.Probe,         10),
      Request.unit(Protoss.Forge),
      Request.unit(Protoss.Probe,         12),
      Request.unit(Protoss.PhotonCannon,  2),
      Request.unit(Protoss.Probe,         15),
      Request.unit(Protoss.PhotonCannon,  3),
      Request.unit(Protoss.Probe,         16),
      Request.unit(Protoss.Pylon,         2)
    )
  )
  
  class BuildDragoonsAndAssimilators_AfterCyberneticsCore extends If(
    new UnitsAtLeast(1, UnitMatchType(Protoss.CyberneticsCore), complete = true),
    new Parallel(
      new Build(Request.upgr(Protoss.DragoonRange)),
      new BuildAssimilators,
      new TrainContinuously(Protoss.Dragoon)),
    new TrainContinuously(Protoss.Zealot)
  )
  private class AttackWithFour extends If(
    new UnitsAtLeast(4, UnitMatchWarriors),
    new ConsiderAttacking,
    new DefendChokes)
  
  private val earlyZealotCount = 8
  
  children.set(Vector(
    new ForgeFastExpand,
    new RequireMiningBases(1),
    new FiveGateGoon_Start,
    new RequireMiningBases(2),
    new RequireSufficientPylons,
    new TrainProbesContinuously,
    new Build(Request.unit(Protoss.Gateway)),
    new Build(Request.unit(Protoss.Assimilator)),
    new Build(Request.unit(Protoss.CyberneticsCore, 1)),
    new Build(Request.unit(Protoss.PhotonCannon,  4)),
    new BuildDragoonsAndAssimilators_AfterCyberneticsCore,
    new Build(Request.unit(Protoss.Gateway, 5)),
    //This part is freelancing. Replace with an actual late game.
    new Build(
      Request.unit(Protoss.Gateway, 8),
      Request.upgr(Protoss.GroundArmor),
      Request.upgr(Protoss.GroundDamage)),
    new ScoutAt(9),
    new ControlMap,
    new AttackWithFour
  ))
}
