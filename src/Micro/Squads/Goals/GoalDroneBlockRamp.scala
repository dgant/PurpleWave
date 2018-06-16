package Micro.Squads.Goals

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Agency.{Intention, Leash}
import Planning.UnitCounters.UnitCountOne
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class GoalDroneBlockRamp extends GoalBasic {
  
  override def toString: String = "Block ramp of our main"
  
  override def destination: Pixel = With.geography.ourMain.zone.exit.map(_.pixelCenter).getOrElse(With.intelligence.mostBaselikeEnemyTile.pixelCenter)
  
  override def acceptsHelp: Boolean = super.acceptsHelp && With.geography.ourMain.zone.exit.isDefined
  override def run() {
    val pixel = With.geography.ourMain.zone.exit.map(_.pixelCenter)
    if (pixel.isEmpty) return
    
    squad.units.foreach(unit => {
      val blockingBuilder = With.units.inPixelRadius(pixel.get, 64).exists(ally =>
        ally.friendly.exists(_.agent.toBuild.contains(Zerg.Hatchery))
        && unit.pixelDistanceCenter(pixel.get) < ally.pixelDistanceCenter(pixel.get))
      val intent = new Intention
      intent.canFlee = false
      if (blockingBuilder) {
        intent.toAttack = unit.matchups.targets.headOption
        intent.toTravel = Some(With.intelligence.mostBaselikeEnemyTile.pixelCenter)
      }
      else {
        intent.toTravel = pixel
        intent.toLeash = Some(Leash(pixel.get, 15))
      }
      unit.agent.intend(squad.client, intent)
    })
  }
  
  override def offerCritical(candidates: Iterable[FriendlyUnitInfo]) {
    candidates.foreach(unit => {
      if (acceptsHelp && unitMatcher.accept(unit)) {
        addCandidate(unit)
      }
    })
    
  }
  unitMatcher = Zerg.Drone
  unitCounter = UnitCountOne
}
