package Planning.Plans.Protoss.GamePlans

import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plans.Army.ConsiderAttacking
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.Information.Employ
import Planning.Plans.Macro.Automatic.{RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, RequireBareMinimum}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Protoss.ProtossBuilds
import Planning.Plans.Protoss.Situational.TwoGatewaysAtNatural
import Planning.Plans.Scouting.Scout
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvR.{PvREarly2Gate1012, PvREarly2Gate910, PvREarly2Gate910AtNatural}

class ProtossVsRandom extends Parallel {
  
  description.set("Protoss vs Random")
  
  children.set(Vector(
    new RequireBareMinimum,
    new Employ(PvREarly2Gate910AtNatural,
      new Parallel(
        new TwoGatewaysAtNatural,
        new Build(ProtossBuilds.OpeningTwoGate910_WithZealots: _*)
      )),
    new Employ(PvREarly2Gate910, new Build(ProtossBuilds.OpeningTwoGate910_WithZealots: _*)),
    new Employ(PvREarly2Gate1012, new Build(ProtossBuilds.OpeningTwoGate1012: _*)),
    new RequireSufficientSupply,
    new TrainContinuously(Protoss.Zealot),
    new TrainWorkersContinuously,
    new RequireMiningBases(2),
    new Build(
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(3, Protoss.Gateway)),
    new If(
      new UnitsAtLeast(1, UnitMatchType(Protoss.Pylon), complete = false),
      new Scout),
    new ConsiderAttacking
  ))
}