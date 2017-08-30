package Planning.Plans.Protoss.GamePlans.Standard

import Macro.BuildRequests.{RequestAtLeast, _}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Army.{Attack, ConsiderAttacking, DefendZones, DropAttack}
import Planning.Plans.Compound._
import Planning.Plans.Information.Reactive.EnemyCarriers
import Planning.Plans.Information.{Employ, Employing}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, FirstEightMinutes}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, MatchMiningBases, RequireMiningBases}
import Planning.Plans.Macro.Milestones._
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Protoss.GamePlans.Standard.PvP.PvPIdeas
import Planning.Plans.Protoss.ProtossBuilds
import Planning.Plans.Protoss.Situational.{ForgeFastExpand, Nexus2GateThenCannons}
import Planning.Plans.Scouting.{Scout, ScoutExpansionsAt}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP._

class ProtossVsProtossOld extends Parallel {
  
  description.set("Protoss vs Protoss")
  
  ///////////////////////////
  // Early game strategies //
  ///////////////////////////
  
  private class WeAreTwoGating extends Or(
    new Employing(PvPEarly2Gate910),
    new Employing(PvPEarly2Gate1012))
  
  private class Implement2Gate extends Parallel(
    new Trigger(
      new UnitsAtLeast(4, Protoss.Zealot),
      initialBefore = new Parallel(
        new Build(ProtossBuilds.OpeningTwoGate910_WithZealots: _*),
        new Build(ProtossBuilds.OpeningTwoGate1012: _*),
        new RequireSufficientSupply,
        new Build(RequestAnother(2, Protoss.Zealot)),
        new TrainWorkersContinuously
      )))
  
  private class ImplementEarly1GateCore extends FirstEightMinutes(
    new Build(ProtossBuilds.Opening_10Gate11Gas13Core: _*))
  
  private class ImplementEarly1GateZZCore extends FirstEightMinutes(
    new Build(ProtossBuilds.Opening_1GateZZCore: _*))
  
  private class DoingOneGateCore extends Or(
    new Employing(PvPEarly1GateCore),
    new Employing(PvPEarly1GateZZCore))
  
  private class ImplementEarlyFE extends FirstEightMinutes(
    new Parallel(
      new Nexus2GateThenCannons,
      new Build(
        RequestAtLeast(1,   Protoss.Nexus),
        RequestAtLeast(8,   Protoss.Probe),
        RequestAtLeast(1,   Protoss.Pylon),
        RequestAtLeast(12,  Protoss.Probe),
        RequestAtLeast(2,   Protoss.Nexus),
        RequestAtLeast(1,   Protoss.Gateway),
        RequestAtLeast(14,  Protoss.Probe),
        RequestAtLeast(2,   Protoss.Gateway),
        RequestAtLeast(15,  Protoss.Probe)),
      new If(
        new UnitsAtMost(0, Protoss.CyberneticsCore, complete = true),
        new TrainContinuously(Protoss.Zealot)),
      new Build(
        RequestAtLeast(2, Protoss.Pylon),
        RequestAtLeast(1, Protoss.Assimilator),
        RequestAtLeast(1, Protoss.CyberneticsCore),
        RequestAtLeast(1, Protoss.Forge),
        RequestAtLeast(3, Protoss.PhotonCannon)
      )))
    
  private class ImplementEarlyFFE extends FirstEightMinutes(
    new Parallel(
      new ForgeFastExpand,
      new Build(ProtossBuilds.FFE_ForgeFirst: _*)))
  
  private class OpeningIsComplete extends Or(
    
  )
  
  ////////////////////////
  // Midgame strategies //
  ////////////////////////
  
  private class ImplementMidgame4GateGoon extends
    Build(
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(4, Protoss.Gateway))
  
  private class ImplementMidgameDarkTemplar extends Parallel(
    new Build(
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(2, Protoss.Gateway),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestAtLeast(1, Protoss.TemplarArchives)))
  
  private class ImplementMidgameFE extends If(
    new UnitsAtLeast(3, UnitMatchWarriors, complete = false),
    new RequireMiningBases(2))
  
  private class ImplementMidgameReaver extends Parallel(
    new Build(
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(2, Protoss.Gateway),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.RoboticsSupportBay)))
  
  private class ImplementMidgameObserverReaver extends Parallel(
    new Build(
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(3, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Observatory),
      RequestAtLeast(1, Protoss.RoboticsSupportBay)))
  
  private class BuildReaversOrTemplar extends If(
      new UnitsAtLeast(1, Protoss.TemplarArchives, complete = true),
      new Parallel(
        new TrainContinuously(Protoss.HighTemplar,  6),
        new TrainContinuously(Protoss.Reaver,       1)),
      new TrainContinuously(Protoss.Reaver, 4))
  
  ///////////////
  // Expanding //
  ///////////////
  
  private class ExpandAgainstCannons extends If(
    new Or(
      new EnemyUnitsAtLeast(1, Protoss.PhotonCannon),
      new EnemyUnitsAtLeast(1, Protoss.Forge)),
    new RequireMiningBases(2)
  ) { description.set("Expand against cannons")}
  
  private class ExpandAgainAgainstMoreCannons extends If(
    new EnemyUnitsAtLeast(5, Protoss.PhotonCannon),
    new RequireMiningBases(3)
  ) { description.set("Expand again against more cannons")}
  
  private class TakeNatural extends If(
    new Or(
      new UnitsAtLeast(10, UnitMatchWarriors),
      new UnitsAtLeast(2, Protoss.PhotonCannon),
      new UnitsAtLeast(2, Protoss.Reaver),
      new UnitsAtLeast(2, Protoss.DarkTemplar)),
    new RequireMiningBases(2)
  ) { description.set("Take our natural when safe")}
  
  private class TakeThirdBase extends If(
    new UnitsAtLeast(15, UnitMatchWarriors),
    new RequireMiningBases(3)
  ) { description.set("Take our third base when safe")}
  
  /////////////////
  // Here we go! //
  /////////////////
  
  children.set(Vector(
  
    new MeldArchons(40),
    
    // Early game
    new RequireMiningBases(1),
    new Implement2Gate,
    new Employ(PvPEarly1GateCore,       new ImplementEarly1GateCore),
    new Employ(PvPEarly1GateZZCore,     new ImplementEarly1GateZZCore),
    new Employ(PvPEarlyFE,              new ImplementEarlyFE),
    new Employ(PvPEarlyFFE,             new ImplementEarlyFFE),
    
    // Expanding
    new PvPIdeas.ReactToDarkTemplarPossible,
    new PvPIdeas.ReactToDarkTemplarExisting,
    new MatchMiningBases,
    new TakeNatural,
    new ExpandAgainstCannons,
    new ExpandAgainAgainstMoreCannons,
    new TakeThirdBase,
  
    // Early game macro
    new RequireSufficientSupply,
    new TrainWorkersContinuously(oversaturate = true),
    
    // Units/Upgrades
    
    
    new If(
      new UnitsAtLeast(2, Protoss.HighTemplar, complete = false),
      new Build(RequestTech(Protoss.PsionicStorm))),
  
    new If(
      new And(
        new UnitsAtLeast(2, Protoss.Dragoon, complete = false),
        new Or(
          new Not(new Employing(PvPOpeningDarkTemplar)),
          new UnitsAtLeast(2, Protoss.DarkTemplar, complete = false))),
      new Build(RequestUpgrade(Protoss.DragoonRange))),
    
    new If(
      new And(
        new EnemyUnitsAtMost(0, Protoss.Observer),
        new EnemyUnitsAtMost(0, Protoss.Forge)),
      new TrainContinuously(Protoss.DarkTemplar, 3),
      new TrainContinuously(Protoss.DarkTemplar, 1)),
    
    new If(
      new UnitsAtLeast(2, Protoss.Reaver),
      new Build(RequestUpgrade(Protoss.ScarabDamage))),
    
    new OnMiningBases(2, new If(
      new UnitsAtLeast(1, Protoss.CitadelOfAdun),
      new UpgradeContinuously(Protoss.ZealotSpeed))),
    
    new TrainMatchingRatio(Protoss.Observer, 1, 2, Seq(MatchingRatio(Protoss.DarkTemplar, 2.0))),
    
    new If (
      new Not(new EnemyCarriers),
      new TrainContinuously(Protoss.Reaver, 4)),
    
    new PvPIdeas.BuildDragoonsOrZealots,
    
    // Midgame
    
    // Don't directly go expand-> tech
    new OnMiningBases(2,
      new If(
        new UnitsAtLeast(1, Protoss.CyberneticsCore),
        new Build(RequestAtLeast(4, Protoss.Gateway)))),
      
    new Employ(PvPMidgame4GateGoon,       new ImplementMidgame4GateGoon),
    new Employ(PvPOpeningDarkTemplar,     new ImplementMidgameDarkTemplar),
    new Employ(PvPMidgameObserverReaver,  new ImplementMidgameObserverReaver),
    new Employ(PvPMidgameReaver,          new ImplementMidgameReaver),
    
    // Default builds
    new Build(
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(1, Protoss.CyberneticsCore)),
    new BuildGasPumps,
    new Build(
      RequestAtLeast(2, Protoss.Gateway),
      RequestUpgrade(Protoss.DragoonRange)),
    new If(
      new EnemyCarriers,
      new Parallel(
        new Build(RequestAtLeast(4, Protoss.Gateway)),
        new OnMiningBases(2, new Build(RequestAtLeast(8, Protoss.Gateway))),
        new OnMiningBases(3, new Build(RequestAtLeast(12, Protoss.Gateway))),
        new OnMiningBases(4, new Build(
          RequestAtLeast(15, Protoss.Gateway),
          RequestAtLeast(1, Protoss.RoboticsFacility),
          RequestAtLeast(1, Protoss.Observatory),
          RequestAtLeast(3, Protoss.Observer)))),
      new Parallel(
        new Build(
          RequestAtLeast(3, Protoss.Gateway),
          RequestAtLeast(1, Protoss.RoboticsFacility),
          RequestAtLeast(1, Protoss.RoboticsSupportBay)),
        new OnMiningBases(2,
          new Build(
            RequestAtLeast(5, Protoss.Gateway),
            RequestAtLeast(1, Protoss.Observatory),
            RequestAtLeast(1, Protoss.CitadelOfAdun),
            RequestAtLeast(8, Protoss.Gateway))),
        new OnMiningBases(3,
          new Parallel(
            new UpgradeContinuously(Protoss.GroundDamage),
            new Build(
              RequestAtLeast(1, Protoss.Forge),
              RequestAtLeast(1, Protoss.TemplarArchives),
              RequestAtLeast(2, Protoss.Forge),
              RequestUpgrade(Protoss.HighTemplarEnergy)),
            new UpgradeContinuously(Protoss.GroundArmor),
            new Build(RequestAtLeast(12, Protoss.Gateway)))))),
    
    new Build(
      RequestAtLeast(2, Protoss.Forge),
      RequestAtLeast(1, Protoss.TemplarArchives)),
    new UpgradeContinuously(Protoss.GroundDamage),
    new UpgradeContinuously(Protoss.GroundArmor),
    
    new ScoutExpansionsAt(90),
    new If(
      new DoingOneGateCore,
      new If(
        new UnitsAtLeast(1, Protoss.CyberneticsCore, complete = false),
        new Scout),
      new If(
        new UnitsAtLeast(1, Protoss.Pylon, complete = false),
        new Scout)),
  
    new DefendZones,
    new DropAttack,
    new Attack { attackers.get.unitMatcher.set(Protoss.DarkTemplar) },
    new If(
      new And(
        new Employing(PvPOpeningDarkTemplar),
        new Not(new Employing(PvPEarly2Gate910)),
        new Not(new Employing(PvPEarly2Gate1012))),
      new Trigger(
        new UnitsAtLeast(1, Protoss.TemplarArchives, complete = true),
        new ConsiderAttacking),
      new ConsiderAttacking)
  ))
}
