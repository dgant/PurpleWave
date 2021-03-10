package Micro.Actions.Combat.Targeting.Filters
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

case class TargetFilterLeash(radiusPixels: Double) extends TargetFilter {
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
     actor.inRangeToAttack(target) || actor.pixelToFireAt(target).pixelDistance(actor.agent.origin) < radiusPixels
  }
}
