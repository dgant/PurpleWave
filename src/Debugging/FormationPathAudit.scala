package Debugging

import Information.Geography.Pathfinding.Types.ZonePath
import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Formation.FormationFlanking
import Tactic.Squads.FriendlyUnitGroup

import scala.collection.mutable

object FormationPathAudit {
  private val lastFrame = new mutable.WeakHashMap[FriendlyUnitGroup, Int]

  def log(group: FriendlyUnitGroup, origin: Pixel, goal: Pixel, threat: Pixel, direct: Option[ZonePath], route: FormationFlanking.Route): Unit = {
    if (With.frame - lastFrame.getOrElse(group, -240) < 240) return
    lastFrame(group) = With.frame
    def describe(path: Option[ZonePath]): String = path.map(p =>
      f"${p.length.toInt}:" + p.steps.map(s => f"${s.edge.pixelCenter.x},${s.edge.pixelCenter.y},${s.edge.diameterPixels.toInt}").mkString(";")).getOrElse("none")
    With.logger.debug(s"FormationPath group=$group origin=$origin goal=$goal threat=$threat flanking=${route.flanking} direct=${describe(direct)} chosen=${describe(route.path)} units=${group.groupFriendlyUnits.map(_.id).mkString(",")}")
  }
}
