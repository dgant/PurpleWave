package Geometry.Influence

import Geometry.Circle
import Startup.With

class MapEnemyVision extends InfluenceMap {
  override def update() {
    reset()
    With.memory.knownEnemyUnits.filter(_.possiblyStillThere).foreach(u => {
      val range = u.getType.sightRange/32
      Circle.points(range)
        .foreach(tileDelta =>
          setTilePosition(
            u.getTilePosition.getX + tileDelta._1 * 32,
            u.getTilePosition.getY + tileDelta._2 * 32,
            1))
    })
  }
}
