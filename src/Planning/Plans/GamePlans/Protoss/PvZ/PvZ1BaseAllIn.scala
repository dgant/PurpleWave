package Planning.Plans.GamePlans.Protoss.PvZ

import Lifecycle.With
import ProxyBwapi.Races.{Protoss, Zerg}
import Utilities.UnitFilters.IsWarrior

abstract class PvZ1BaseAllIn extends PvZ1BaseOpenings {

  protected var timingAttack  : Boolean = false
  protected var needToAllIn   : Boolean = false

  override def executeBuild(): Unit = {
    open(allowExpanding = false)
  }

  protected def allInLogic(): Unit = {
    timingAttack ||= unitsComplete(IsWarrior) >= 30 // Safety valve in case build gets disrupted somehow
    timingAttack ||= needToAllIn
    status(timingAttack, "Timing")
    if (enemies(Zerg.Lurker) > 0 && ! have(Protoss.Observer)) {
      status("Lurkers")
      get(Protoss.RoboticsFacility, Protoss.Observatory)
      buildCannonsAtOpenings(1)
    } else {
      if (timingAttack || needToAllIn) {
        attack()
        aggression(1.5)
      } else {
        var canAttackEarly    = opening == Open910
        canAttackEarly      ||= opening == Open1012
        canAttackEarly      ||= opening == OpenZZCoreZ    && With.fingerprints.twelveHatch()
        canAttackEarly      ||= haveComplete(Protoss.Scout,  Protoss.DarkTemplar)
        canAttackEarly      &&= unitsEver(IsWarrior) >= 5 || With.fingerprints.twelveHatch()
        canAttackEarly      &&= safePushing
        canAttackEarly      &&= ! With.fingerprints.twoHatchMain()
        if (canAttackEarly) {
          attack()
        }
      }
      if (needToAllIn) {
        status("NeedToAllIn")
        allIn()
      }
    }
  }

  protected def mutalisksImminent: Boolean = (
    enemiesShown(Zerg.Mutalisk) == 0
    && (enemiesComplete(Zerg.Spire) > 0
      || With.units.enemy.exists(e => Zerg.Lair(e) && With.framesSince(e.lastClassChange) >= Zerg.Lair.buildFrames + Zerg.Spire.buildFrames)))

  protected def mutalisksInBase: Boolean = With.geography.ourBases.exists(_.enemies.exists(e => Zerg.Mutalisk(e) && e.visible))
}