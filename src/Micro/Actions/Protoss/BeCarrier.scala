package Micro.Actions.Protoss

import Mathematics.Maff
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Actions.Protoss.Carrier._
import Micro.Agency.Commander
import Micro.Targeting.FiltersSituational.{TargetFilterCombatants, TargetFilterVisibleInRange}
import Micro.Targeting.Target
import ProxyBwapi.Orders
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.LightYear


object BeCarrier extends Action {
  
  // Carriers are really finicky and demand their own micro.

  // TODO: Implement Stop-Hold (SH) micro:
  // https://tl.net/forum/brood-war/572188-new-carrier-micro-sh-micro
  // https://tl.net/forum/brood-war/572186-the-age-of-pusagi-new-carrier-micro-trick
  // https://www.youtube.com/watch?v=6W0B0jZEQrc
  // In short: Carrier DPS can be *doubled* if you're not picky about who they're targeting
  // Plan for implementation:
  // 1. Carrier micro test map
  // 2. Extensive visualizations on Carriers
  // Issue grouped commands using Game.issueCommand()
  //
  // Unfortunately BWAPI 4.4 does not support grouped commands for client bots.
  // So this may not be achievable without an API change.
  //
  // On the other hand, similar performance against small units could potentially be achieved by
  // having carriers target different enemies, which would reduce the frequency of carriers having to switch targets.
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = Protoss.Carrier(unit) && unit.matchups.enemies.nonEmpty

  protected final def isInterceptorActive(interceptor: UnitInfo): Boolean = Seq(Orders.InterceptorAttack, Orders.InterceptorReturn).contains(interceptor.order)

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    if (unit.agent.shouldFight) {
      // Avoid changing targets (causes interceptors to not attack)
      // Avoid targeting something leaving leash range
      def target = unit.agent.toAttack
      unit.agent.toAttack = unit
        .orderTarget
        .filter(t => t.alive && t.visible && t.isEnemy && t.pixelDistanceEdge(unit) <= 32.0 * 10.0)
        .orElse(Target.choose(unit, TargetFilterCombatants, TargetFilterVisibleInRange))
        .orElse(Target.choose(unit))

      val interceptorsActive      = unit.interceptors.count(isInterceptorActive) >= 1 + unit.interceptors.count(_.complete) / 2
      val interceptorSafetyMargin = Maff.max(unit.matchups.threats.filterNot(Protoss.Carrier).map(threat => threat.pixelsToGetInRange(unit) - 8 * 32)).getOrElse(LightYear().toDouble)
      val carrierSafetyMargin     = unit.matchups.pixelsEntangled

      if (target.isDefined) {
        if (interceptorsActive) {
          val chasePixel = unit.agent.toAttack.get.projectFrames(48)
          val chasingAlready = unit.speedApproaching(chasePixel) > unit.topSpeed / 2
          unit.agent.toTravel = Some(chasePixel)

          // Chase with impunity, or if target is leaving leash range
          if ( ! chasingAlready && (interceptorSafetyMargin > 32 || unit.pixelDistanceEdge(target.get) > 32 * 9)) {
            unit.agent.act("CarrierChase")
            Commander.move(unit)

          // Attack with impunity
          } else if (interceptorSafetyMargin > 0) {
            unit.agent.act("CarrierAttack")
            Commander.attack(unit)

          // Open leash
          } else if (target.get.pixelDistanceEdge(unit) < 32 * 8) {
            Retreat.delegate(unit)
            unit.agent.act("OpenLeash")

          // Hold leash
          } else {
            unit.agent.toTravel = unit.agent.toAttack.map(_.pixel.project(unit.agent.home, 7.5 * 32))
            Commander.move(unit)
          }
        } else {
          unit.agent.act("Launch")
          Commander.attack(unit)
        }
      } else {
        WarmUpInterceptors.apply(unit)
        Commander.attackMove(unit)
      }
    } else {
      Retreat.apply(unit)
    }
  }
}
